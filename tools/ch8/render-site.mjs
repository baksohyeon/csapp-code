import {readFile,writeFile,readdir,access} from 'node:fs/promises';
import path from 'node:path';
import MarkdownIt from 'markdown-it';
import YAML from 'yaml';
import {lectures,relative,resources,header,escape} from './site.mjs';
const md=new MarkdownIt({html:false});
const wiki=await readFile('wiki/index.md','utf8');
const files=[];
async function walk(dir){for(const e of await readdir(dir,{withFileTypes:true})){const p=path.posix.join(dir,e.name);if(e.isDirectory())await walk(p);else if(e.name.endsWith('.md'))files.push(p);}}
await walk('wiki');
const targets=new Map();
for(const file of files){
 let target=file;const text=await readFile(file,'utf8');const fm=text.match(/^---\n([\s\S]*?)\n---/);
 if(fm){const meta=YAML.parse(fm[1]);if(meta.artifact_path){try{await access(meta.artifact_path);target=meta.artifact_path;}catch{}}}
 targets.set(path.posix.basename(file,'.md'),target);
}
function shell(from,title,content){return `<!doctype html><html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="color-scheme" content="light dark"><link rel="icon" href="data:,"><title>${escape(title)} | CSAPP</title>${resources(from)}</head><body class="docs-page"><a class="skip" href="#content">본문으로 건너뛰기</a>${header(from)}<main id="content" class="docs-home">${content}</main></body></html>\n`;}
const from='index.html';
const content=`<h1>컴퓨터 시스템 학습 문서</h1><p>CSAPP 스터디의 강의노트와 재현 실습을 모았습니다.</p><h2>강의노트</h2><ul class="lecture-list">${[...lectures].reverse().map(l=>`<li><a href="${relative(from,l.file)}">${escape(l.title)}</a><p>${l.date} · ${escape(l.description)}</p></li>`).join('')}</ul><h2>탐색</h2><p><a href="wiki/">전체 Wiki 목차</a> · <a href="wiki/index.md">목차 Markdown</a> · <a href="README.md">레포 안내</a></p>`;
await writeFile(from,shell(from,'학습 문서',content));
const index='wiki/index.html';
const body=wiki.replace(/^---\n[\s\S]*?\n---\n/,'').replace(/\[\[([^\]]+)\]\]/g,(_,name)=>targets.has(name)?`[${name}](${relative(index,targets.get(name))})`:name);
await writeFile(index,shell(index,'전체 목차',`<article class="docs-article">${md.render(body)}</article>`));
console.log('Rendered /index.html and /wiki/index.html from wiki metadata');
