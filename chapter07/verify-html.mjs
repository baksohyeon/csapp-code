import { readFile, access } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const htmlPath = resolve(here, "chapter-7.6.1.html");
const markdownPath = resolve(here, "chapter-7.6.1.md");
const html = await readFile(htmlPath, "utf8");
const markdown = await readFile(markdownPath, "utf8");
const failures = [];

for (const [name, document] of [
  ["HTML", html],
  ["Markdown", markdown],
]) {
  if (document.includes("\u2014")) {
    failures.push(`${name} contains a forbidden em dash`);
  }
  for (const phrase of [
    ["면접", "질문"].join(" "),
    ["이", "문서에서", "새로", "제작한", "설명용", "SVG"].join(" "),
  ]) {
    if (document.includes(phrase)) {
      failures.push(`${name} contains removed wording: ${phrase}`);
    }
  }
}

if (/https?:\/\/[^"' )]+(?:\.css|\.js)/i.test(html)) {
  failures.push("external CSS/JS dependency found");
}

for (const required of [
  "ELI5",
  "REMIND",
  "WARNING",
  "GOTCHA",
  "COMMON MISTAKE",
  "QUIZ",
  "EXERCISE",
]) {
  if (!html.includes(required)) failures.push(`missing label: ${required}`);
  if (!markdown.includes(required)) {
    failures.push(`missing Markdown label: ${required}`);
  }
}

for (const requiredId of [
  "driver-libc",
  "loader-aslr",
  "static-libraries",
  "archive-search",
]) {
  if (!html.includes(`id="${requiredId}"`)) {
    failures.push(`missing HTML section: ${requiredId}`);
  }
  if (!markdown.includes(`<a id="${requiredId}"></a>`)) {
    failures.push(`missing Markdown section: ${requiredId}`);
  }
}

for (const requiredText of [
  "relocation entry",
  "libvector.a",
  "--warn-backrefs",
  "not a dynamic executable",
  "PT_INTERP",
  "DT_NEEDED",
  "randomize_va_space",
  "Position Independent Executable",
  "-fuse-ld=mold",
  "GNU gold",
  "증분 컴파일",
]) {
  if (!html.includes(requiredText)) failures.push(`missing HTML content: ${requiredText}`);
  if (!markdown.includes(requiredText)) failures.push(`missing Markdown content: ${requiredText}`);
}

const ids = new Set([...html.matchAll(/\sid="([^"]+)"/g)].map((match) => match[1]));
for (const match of html.matchAll(/href="#([^"]+)"/g)) {
  if (!ids.has(match[1])) failures.push(`broken fragment: #${match[1]}`);
}

for (const match of html.matchAll(/(?:src|href)="([^"#][^"]*)"/g)) {
  const target = match[1];
  if (/^(?:https?:|mailto:)/.test(target)) continue;
  try {
    await access(resolve(here, target));
  } catch {
    failures.push(`missing local target: ${target}`);
  }
}

for (const match of html.matchAll(/<img\b([^>]*)>/g)) {
  if (!/\balt="[^"]*"/.test(match[1])) failures.push("image without alt text");
}

const markdownIds = new Set(
  [...markdown.matchAll(/<a id="([^"]+)"><\/a>/g)].map((match) => match[1]),
);
for (const match of markdown.matchAll(/\[[^\]]+\]\(([^)]+)\)/g)) {
  const target = match[1];
  if (/^(?:https?:|mailto:)/.test(target)) continue;
  if (target.startsWith("#")) {
    if (!markdownIds.has(target.slice(1))) {
      failures.push(`broken Markdown fragment: ${target}`);
    }
    continue;
  }

  const [localTarget] = target.split("#");
  try {
    await access(resolve(here, localTarget));
  } catch {
    failures.push(`missing Markdown local target: ${localTarget}`);
  }
}

const markdownFigures = [...markdown.matchAll(/!\[[^\]]+\]\(([^)]+)\)/g)];
if (markdownFigures.length !== 11) {
  failures.push(`expected 11 Markdown figures, found ${markdownFigures.length}`);
}

const codeFences = [...markdown.matchAll(/^```/gm)].length;
if (codeFences % 2 !== 0) failures.push("unbalanced Markdown code fences");

if (failures.length) {
  console.error(failures.join("\n"));
  process.exit(1);
}

console.log(
  `Document checks passed: HTML ${ids.size} ids, Markdown ${markdownIds.size} ids, local links resolved.`,
);
