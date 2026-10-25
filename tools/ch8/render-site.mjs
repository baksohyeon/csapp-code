import {readFile,writeFile,readdir,access,stat} from 'node:fs/promises';
import path from 'node:path';
import YAML from 'yaml';
import {createMarkdown} from './markdown.mjs';
import {lectures,relative,resources,header,escape} from './site.mjs';
const files=[];
async function walk(dir){for(const e of await readdir(dir,{withFileTypes:true})){const p=path.posix.join(dir,e.name);if(e.isDirectory())await walk(p);else if(e.name.endsWith('.md'))files.push(p);}}
await walk('wiki');
const docs=new Map();
for(const file of files){
 const text=await readFile(file,'utf8');const fm=text.match(/^---\n([\s\S]*?)\n---\n/);const meta=fm?YAML.parse(fm[1]):{};
 const title=text.match(/^# (.+)$/m)?.[1].replace(/^\d{4}-\d{2}-\d{2}\s+/,'')||path.basename(file,'.md');
 let target=file.replace(/\.md$/,'.html');
 if(meta.type==='Lecture'&&meta.artifact_path?.endsWith('.html')){await access(meta.artifact_path);target=meta.artifact_path;}
 docs.set(file,{file,text,meta,title,target});
}
const byName=new Map([...docs.values()].map(d=>[path.basename(d.file,'.md'),d]));
const sourceDirs=['examples/0001-compile-linking','examples/0002-exceptional-control-flow','chapter07/examples'];
function shell(from,title,content,mainClass='docs-home'){
 return `<!doctype html><html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="color-scheme" content="light dark"><link rel="icon" href="data:,"><title>${escape(title)} | CSAPP Wiki</title>${resources(from)}</head><body class="docs-page"><a class="skip" href="#content">본문으로 건너뛰기</a>${header(from)}<nav class="docs-breadcrumbs" aria-label="현재 위치"><ol><li><a href="/">홈페이지</a></li><li>${from==='wiki/index.html'?'<span aria-current="page">CSAPP Wiki</span>':`<a href="${relative(from,'wiki/index.html')}">CSAPP Wiki</a>`}</li>${from==='wiki/index.html'?'':`<li aria-current="page">${escape(title)}</li>`}</ol></nav><main id="content" class="${mainClass}">${content}</main></body></html>\n`;
}
async function targetFor(from,href){
 if(/^(https?:|mailto:|data:|#)/.test(href))return href;
 const [raw,fragment]=href.split('#');const abs=path.posix.normalize(path.posix.join(path.posix.dirname(from),raw));
 let target=docs.get(abs)?.target;
 if(!target&&sourceDirs.includes(abs.replace(/\/$/,'')))target=abs.replace(/\/$/,'')+'/index.html';
 if(!target&&abs.endsWith('.md')){const html=abs.replace(/\.md$/,'.html');try{await access(html);target=html;}catch{}}
 if(!target){try{if((await stat(abs)).isDirectory())return 'https://github.com/baksohyeon/csapp-code/tree/main/'+abs;}catch{}}
 return target?relative(from,target)+(fragment?'#'+fragment:''):href;
}
async function renderBody(from,text){
 let body=text.replace(/^---\n[\s\S]*?\n---\n/,'').replace(/\[\[([^\]]+)\]\]/g,(_,key)=>{
  const [name,label]=key.split('|');const d=byName.get(name);return d?`[${label||d.title}](${relative(from,d.file)})`:label||name;
 });
 // Resolve destinations before Markdown parsing so wrappers always open their readable HTML artifact.
 const destinations=[...body.matchAll(/\]\(([^)]+)\)/g)].map(m=>m[1]);
 for(const href of new Set(destinations))body=body.replaceAll(']('+href+')',']('+await targetFor(from,href)+')');
 const md=createMarkdown({idPrefix:"document-code"});let count=0;
 md.renderer.rules.heading_open=(tokens,i)=>{
  const title=tokens[i+1].content;const id=title.toLowerCase().replace(/[^\p{L}\p{N}_ -]/gu,'').replace(/ /g,'-')||'section-'+(++count);
  return `<${tokens[i].tag} id="${escape(id)}">`;
 };
 return md.render(body);
}
const index='wiki/index.html';
let body=await renderBody(index,docs.get('wiki/index.md').text);
const cards=`<ol class="lecture-list">${lectures.map(l=>`<li><p class="docs-kicker">${escape(l.scope)} · ${l.date}</p><a href="${relative(index,l.file)}">${escape(l.title)}</a><p>${escape(l.description)}</p></li>`).join('')}</ol>`;
body=body.replace(/(<h2[^>]*>강의노트<\/h2>)\s*<ul>[\s\S]*?<\/ul>/,`$1${cards}`);
body=body.replace(/(<h2[^>]*>문서 작성과 검증 기록<\/h2>)([\s\S]*)$/, '<details class="wiki-maintenance"><summary>문서 작성과 검증 기록</summary>$2</details>');
const menu=`<nav class="wiki-jump" aria-label="Wiki 탐색"><a href="#강의노트">강의노트</a><a href="#개념-노트">개념 노트</a><a href="#실습과-학습-기록">실습</a><a href="#교재와-스터디-자료">참고 자료</a></nav>`;
body=body.replace(/(<\/h1>\s*<p>[\s\S]*?<\/p>)/,'$1'+menu);
await writeFile(index,shell(index,'CSAPP Wiki',`<article class="docs-article wiki-catalog">${body}</article>`));
// One learning entrance. Old /csapp/ links keep working and land on the Wiki.
await writeFile('index.html','<!doctype html><html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta http-equiv="refresh" content="0;url=wiki/"><title>CSAPP Wiki</title></head><body><p><a href="wiki/">CSAPP Wiki 열기</a></p></body></html>\n');
for(const d of docs.values()){
 if(d.file==='wiki/index.md'||d.meta.type==='Lecture'||d.file.includes('x86-strace-debugging'))continue;
 const category=({Topic:'개념 노트',Note:'학습 기록'})[d.meta.type]||'문서 안내';
 let html=await renderBody(d.file,d.text);
 html=html.replace(/<h1[^>]*>[\s\S]*?<\/h1>/,`<p class="docs-kicker">${category}${d.meta.date?' · '+d.meta.date:''}</p><h1>${escape(d.title)}</h1>`);
 const footer=`<footer class="docs-footer"><a href="${relative(d.target,index)}">← CSAPP Wiki</a> · <a href="${path.basename(d.file)}">Markdown 원문</a></footer>`;
 await writeFile(d.target,shell(d.target,d.title,`<article class="docs-article reference-document">${html}${footer}</article>`));
}
// Source readers replace raw directory listings for the learning exercises.
for(const dir of sourceDirs){
 const from=dir+'/index.html';const title=dir.includes('0002')?'Ch8 Linux LAB':dir.includes('0001')?'C 컴파일·링킹 실습':'Ch7 ELF 실습';
 let content=`<h1>${title}</h1><p>실습 설명을 읽고 소스를 확인한다. 원본 파일 링크는 소스 텍스트를 연다.</p>`;
 try{content+=(await renderBody(dir+'/README.md',await readFile(dir+'/README.md','utf8'))).replace(/<h1[^>]*>[\s\S]*?<\/h1>/,'');}catch(e){if(e.code!=='ENOENT')throw e;}
 const codeFiles=[];
 async function sources(folder){for(const e of await readdir(folder,{withFileTypes:true})){const p=folder+'/'+e.name;if(e.isDirectory())await sources(p);else if(/\.(c|h|sh)$/.test(p))codeFiles.push(p);}}
 await sources(dir);
 const md=createMarkdown();
 for(const file of codeFiles.sort()){
  const lang=file.endsWith('.sh')?'sh':'c';const text=await readFile(file,'utf8');const name=path.posix.relative(dir,file);
  content+=`<details class="source-file"><summary>${escape(name)}</summary><p><a href="${escape(name)}">원본 파일</a></p>${md.render('```'+lang+'\n'+text+'\n```')}</details>`;
 }
 content+=`<footer class="docs-footer"><a href="${relative(from,index)}">← CSAPP Wiki</a></footer>`;
 await writeFile(from,shell(from,title,`<article class="docs-article reference-document">${content}</article>`));
}
console.log(`Rendered one Wiki entrance, ${docs.size} document mappings, readable notes/topics and 3 source readers`);
