import { readFile, access, readdir } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const htmlPath = resolve(here, "chapter-7.6.1.html");
const markdownPath = resolve(here, "chapter-7.6.1.md");
const html = await readFile(htmlPath, "utf8");
const markdown = await readFile(markdownPath, "utf8");
const readme = await readFile(resolve(here, "README.md"), "utf8");
const references = await readFile(resolve(here, "references.md"), "utf8");
const figureNames = (await readdir(resolve(here, "figures")))
  .filter((name) => name.endsWith(".svg"))
  .sort();
const figures = await Promise.all(
  figureNames.map(async (name) => [
    `SVG ${name}`,
    await readFile(resolve(here, "figures", name), "utf8"),
  ]),
);
const failures = [];

for (const [name, document] of [
  ["HTML", html],
  ["Markdown", markdown],
  ["README", readme],
  ["References", references],
  ...figures,
]) {
  if (document.includes("\u2014")) {
    failures.push(`${name} contains a forbidden em dash`);
  }
  for (const phrase of [
    ["면접", "질문"].join(" "),
    ["이", "문서에서", "새로", "제작한", "설명용", "SVG"].join(" "),
    "설명은 틀리다",
    "라고 말할 수는 없다",
    "LTO 전체를 하나의 시간 복잡도로 단정하지 않는다",
    "LTO는 캐시할 수 없다",
    "GCC에 링커가 내장된 것은 아니다",
    "ld가 부족한 링커라서 실패한 것이 아니다",
    "GCC는 완전한 C 표준 라이브러리 구현을 제공하지 않는다",
    "실패가 아니라 성공이다",
    "STB_WEAK가 아니다",
    "COMMON은 .bss와 같은 입력 섹션이 아니다",
    "아카이브 전체를 실행 파일에 복사하지 않고",
    "이해하면 안 된다",
    "관측을 계약으로 승격하지 말 것",
    "PIE 자체가 무작위화를 수행하지는 않는다",
    "한 단어 “weak”로 뭉개지 않는다",
    "가장 중요한 구분",
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
  "ELI10",
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
  "lto-thinlto",
  "loader-aslr",
  "static-libraries",
  "archive-search",
  "relocation-preview",
  "modern-linkers",
]) {
  if (!html.includes(`id="${requiredId}"`)) {
    failures.push(`missing HTML section: ${requiredId}`);
  }
  if (!markdown.includes(`<a id="${requiredId}"></a>`)) {
    failures.push(`missing Markdown section: ${requiredId}`);
  }
}

const expectedSectionOrder = [
  "reading",
  "mapping",
  "eli10",
  "context",
  "rules",
  "book-cases",
  "elf-truth",
  "storage",
  "gcc10",
  "lab",
  "linkers",
  "beyond-book",
  "static-libraries",
  "driver-libc",
  "archive-search",
  "practice",
  "glossary",
  "relocation-preview",
  "modern-linkers",
  "lto-thinlto",
  "loader-aslr",
  "beyond",
];
const htmlSectionOrder = [...html.matchAll(/<section id="([^"]+)">/g)].map((match) => match[1]);
const markdownSectionOrder = [...markdown.matchAll(/<a id="([^"]+)"><\/a>/g)].map((match) => match[1]);
if (htmlSectionOrder.join("\n") !== expectedSectionOrder.join("\n")) {
  failures.push(`unexpected HTML section order: ${htmlSectionOrder.join(", ")}`);
}
if (markdownSectionOrder.join("\n") !== expectedSectionOrder.join("\n")) {
  failures.push(`unexpected Markdown section order: ${markdownSectionOrder.join(", ")}`);
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
  "ThinLTO",
  "CSAPP 링크 모델과 현대 LTO",
  "CPU는 기계 명령을 실행한다",
  "weak + weak의 계약 범위",
  "archive 입력과 오브젝트 입력",
  "PIE는 주 실행 파일의 재배치를 가능하게",
  "--gc-sections",
  "thin local LTO",
  'lto = "thin"',
  "2020-05-07",
  "Linux 2.6.12",
  "STB_GLOBAL + SHN_COMMON",
  "optionalDependencies",
  "ClassNotFoundException",
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
if (markdownFigures.length !== 12) {
  failures.push(`expected 12 Markdown figures, found ${markdownFigures.length}`);
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
