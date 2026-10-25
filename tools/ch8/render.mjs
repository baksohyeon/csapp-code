import {readFile,writeFile} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';
import YAML from 'yaml';
const here=path.dirname(fileURLToPath(import.meta.url));
const root=path.resolve(here,'../..');
const stem='2026-10-04-csapp-ch8-exceptional-control-flow';
const file=path.join(root,'wiki/lectures/2026',stem+'.md');
const source=await readFile(file,'utf8');
const match=source.match(/^---\n([\s\S]*?)\n---\n/);
if(!match)throw new Error('frontmatter required');
const meta=YAML.parse(match[1]);
if(meta.date!=='2026-10-04')throw new Error('date mismatch');
const md=new MarkdownIt({html:false,highlight(code,language){
 return language&&hljs.getLanguage(language)?hljs.highlight(code,{language}).value:'';
}});
const slug=s=>s.toLowerCase().replace(/[^\p{L}\p{N}_ -]/gu,'').replace(/ /g,'-');
const headings=[];
md.renderer.rules.heading_open=(tokens,i)=>{
 const level=tokens[i].tag;
 const title=tokens[i+1].content;
 const id=slug(title);
 if(level==='h2')headings.push({id,title});
 return `<${level} id="${id}">`;
};
md.renderer.rules.table_open=()=>'<div class="table-wrap" tabindex="0" role="region" aria-label="가로 스크롤 가능한 비교 표"><table>\n';
md.renderer.rules.table_close=()=>'</table></div>\n';
let html=md.render(source.slice(match[0].length).trimStart().replace(/^# .*\n+/,''));
// Replace the authored Markdown TOC with the same headings used in desktop navigation.
html=html.replace(/<h3 id="목차">목차<\/h3>\n<ul>[\s\S]*?<\/ul>\n/,'');
html=html.replace(/<li>(\s*(?:<p>)?)<strong>\[S(\d+)\]/g,'<li id="source-s$2">$1<strong>[S$2]');
html=html.replace(/\[S(\d+)\]/g,'<a class="source-ref" href="#source-s$1">[S$1]</a>');
html=html.replace(/<p><img src="([^"]+)" alt="([^"]*)"><\/p>/g,(_,src,alt)=>`<figure><img src="${src}" alt="${alt}" loading="lazy"><figcaption>${alt}</figcaption></figure>`);
html=html.replace(/<blockquote>\n<p><strong>WARNING/g,'<blockquote class="warning">\n<p><strong>WARNING');
const toc=`<details class="toc" open><summary>학습 목차</summary><ol>${headings.map(h=>`<li><a href="#${h.id}">${h.title}</a></li>`).join('')}</ol></details>`;
html=html.replace(/(<h2[^>]*>요약과 학습 지도<\/h2>\n<p>[\s\S]*?<\/p>)/,`$1${toc}`);
html=html.replace(/<h2 id="([^"]+)">/g,'</section><section id="$1"><h2>');
html=html.replace(/^<\/section>/,'')+'</section>';
const css=await readFile(path.join(here,'style.css'),'utf8');
const output=`<!doctype html>
<html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="description" content="CSAPP 3판 8.1~8.2.4: 예외, 프로세스, 가상 주소 공간, 권한 전환과 Linux LAB"><link rel="icon" href="data:,"><title>CSAPP Ch8 · Exceptional Control Flow</title><style>${css}</style></head>
<body><a class="skip" href="#content">본문으로 건너뛰기</a>
<aside class="side-nav" aria-label="고정 목차"><p>CSAPP CHAPTER 8</p>${headings.map(h=>`<a href="#${h.id}">${h.title}</a>`).join('')}</aside>
<div class="layout"><header class="hero"><p class="eyebrow">CSAPP 3e · 2026-10-04 강의노트</p><h1>Exceptional<br>Control Flow</h1><p>사건 처리부터 실행 흐름, 주소 공간과 CPU 권한까지.<br>교재 모델과 공식 구현을 연결하고 Linux LAB으로 확인한다.</p><div class="hero-points"><div class="hero-point"><b>Exception</b>사건과 제어 이전</div><div class="hero-point"><b>Process</b>이어지는 실행 흐름</div><div class="hero-point"><b>Memory</b>주소 변환과 보호</div><div class="hero-point"><b>Privilege</b>커널 진입과 복귀</div></div></header>
<main id="content">${html}</main><footer>CSAPP 스터디 · 확인 기준일 2026-10-25 KST · <a href="${stem}.md">Markdown</a></footer></div></body></html>\n`;
if(process.argv.includes('--check')){
 if(await readFile(file.replace(/\.md$/,'.html'),'utf8')!==output)throw new Error('HTML differs from rendered Markdown');
}else await writeFile(file.replace(/\.md$/,'.html'),output);
console.log(`Rendered ${headings.length} sections; ${[...html.matchAll(/<figure>/g)].length} shared SVGs`);
