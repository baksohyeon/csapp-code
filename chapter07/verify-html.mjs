import { readFile, access } from "node:fs/promises";
import { dirname, resolve } from "node:path";
import { fileURLToPath } from "node:url";

const here = dirname(fileURLToPath(import.meta.url));
const htmlPath = resolve(here, "chapter-7.6.1.html");
const html = await readFile(htmlPath, "utf8");
const failures = [];

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

if (failures.length) {
  console.error(failures.join("\n"));
  process.exit(1);
}

console.log(`HTML checks passed: ${ids.size} ids, local links resolved.`);
