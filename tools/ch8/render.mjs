import {readFile,writeFile} from 'node:fs/promises';
import {fileURLToPath} from 'node:url';
import path from 'node:path';
import {createMarkdown} from './markdown.mjs';
import YAML from 'yaml';
import {stem,relative,resources,header,collection} from './site.mjs';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const from=`wiki/lectures/2026/${stem}.html`;
const file=path.join(root,from.replace(/\.html$/,'.md'));
const source=await readFile(file,'utf8');const match=source.match(/^---\n([\s\S]*?)\n---\n/);
if(!match||YAML.parse(match[1]).date!=='2026-10-04')throw new Error('lecture metadata');
const ids=new Map([
 ['요약과 학습 지도','summary'],['기초 개념과 교재 대응','foundations'],['8.1 Exceptions','exceptions'],['8.2 Processes','processes'],['8.2.3 Private Address Space','address-space'],['8.2.4 User and Kernel Modes','modes'],['보충: 서버 동시성과 런타임','runtimes'],['LAB와 적용 로드맵','labs'],['적용 조건과 미확인','conditions'],['출처','references'],
 ['심층 사례: strace 관측 실패 디버깅','lab-debugging'],['환경과 재현 명령','lab-environment'],['LAB 1: syscall·종료 경계','lab-syscall'],['LAB 2: 두 프로세스 흐름','lab-processes'],['LAB 3: 주소와 mapping','lab-mapping'],['관측과 다음 적용','lab-results'],['확인 질문','quiz'],
 ['8.1.1 Exception Handling','exception-handling'],['8.1.2 Classes of Exceptions','exception-classes'],['8.1.3 Exceptions in Linux/x86-64 Systems','linux-exceptions'],['공식 보충: System call과 함수 호출 ABI','syscall-abi'],['실습 연결: stdin·stdout·stderr와 strace','stdio-tracing'],['8.2.1 Logical Control Flow','logical-flow'],['8.2.2 Concurrent Flows','concurrent-flows'],
 ...['goals','tracing','symptom','orb','architecture','hypotheses','decision','setup','results','limits','method'].map((id,i)=>[String(i+1)+'.', 'debug-'+id])
]);
const slug=s=>s.toLowerCase().replace(/[^\p{L}\p{N}_ -]/gu,'').replace(/ /g,'-');
const headings=[];let count=0;const seen=new Set();
const md=createMarkdown();
md.renderer.rules.heading_open=(tokens,i)=>{
 const title=tokens[i+1].content;let id=ids.get(title)||ids.get(title.match(/^\d+\./)?.[0])||`detail-${++count}`;
 while(seen.has(id))id+='-more';seen.add(id);const legacy=slug(title);const level=tokens[i].tag;
 const entry={id,title,level};headings.push(entry);
 tokens[i].meta={id};
 return `${legacy!==id?`<span class="anchor-alias" id="${legacy}" aria-hidden="true"></span>`:''}<${level} id="${id}">`;
};
md.renderer.rules.heading_close=(tokens,i)=>{
 const title=tokens[i-1].content;const item=headings.at(-1);
 return `<a class="heading-link" href="#${item.id}" aria-label="${md.utils.escapeHtml(title)} 고정 링크">#</a></${tokens[i].tag}>\n`;
};
md.renderer.rules.table_open=()=>'<div class="table-wrap" tabindex="0" role="region" aria-label="가로 스크롤 가능한 비교 표"><table>\n';
md.renderer.rules.table_close=()=>'</table></div>\n';
let body=source.slice(match[0].length).trimStart().replace(/^# .*\n+/, '');
// The renderer owns the HTML outline; Markdown keeps its native outline.
body=body.replace(/### 목차\n[\s\S]*?(?=\n\*\*읽는 기준)/,'');
let html=md.render(body);
html=html.replace(/href="(\.\.\/\.\.\/notes\/[^"#]+)\.md(#[^"]*)?"/g, 'href="$1.html$2"');
for(const [id,alias] of [['linux-exceptions','813-linuxx86-64-divide-error와-signal'],['processes','82-processes-실행-흐름'],['syscall-abi','system-call과-함수-호출-abi'],['stdio-tracing','stdinstdoutstderr와-strace']]) html=html.replace(new RegExp(`(<h[234] id="${id}">)`),`<span class="anchor-alias" id="${alias}" aria-hidden="true"></span>$1`);
html=html.replace(/<pre>/g,'<pre tabindex="0" aria-label="코드 예제">');
html=html.replace(/<li>(\s*(?:<p>)?)<strong>\[S(\d+)\]/g,'<li id="source-s$2">$1<strong>[S$2]');
html=html.replace(/\[S(\d+)\]/g,'<a class="source-ref" href="#source-s$1">[S$1]</a>');
html=html.replace(/<p><img src="([^"]+)" alt="([^"]*)"><\/p>/g,(_,src,alt)=>`<figure><img src="${src}" alt="${alt}" loading="lazy"><figcaption>${alt}</figcaption></figure>`);
html=html.replace(/<blockquote>\n<p><strong>WARNING/g,'<blockquote class="warning">\n<p><strong>WARNING');
const top=headings.filter(h=>h.level==='h2'||/^8\.[12]\.\d/.test(h.title)||h.id==='lab-debugging');
const outline=top.map(h=>`<li${h.level==='h3'?' class="nested"':''}><a href="#${h.id}" data-section="${h.id}">${h.id==='lab-debugging'?'실습 디버깅 사례':md.utils.escapeHtml(h.title)}</a></li>`).join('');
const toc=`<details class="toc"><summary>이 문서의 목차</summary><nav aria-label="본문 목차"><ul>${outline}</ul></nav></details>`;
html=html.replace(/(<h2 id="summary">[\s\S]*?<\/h2>\n<p>[\s\S]*?<\/p>)/,`$1${toc}`);
// Sections contain their nested LAB case; aliases remain attached to headings.
html=html.replace(/((?:<span class="anchor-alias"[^>]*><\/span>)?)<h2 id="([^"]+)">/g,'</section><section data-section="$2">$1<h2 id="$2">').replace(/^<\/section>/,'')+'</section>';
const output=`<!doctype html><html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><meta name="color-scheme" content="light dark"><meta name="description" content="CSAPP §8.1~§8.2.4 예외·프로세스·주소 공간과 Linux 실습"><link rel="icon" href="data:,"><title>Ch8 · Exceptional Control Flow | CSAPP</title>${resources(from)}</head><body class="docs-page"><a class="skip" href="#content">본문으로 건너뛰기</a>${header(from)}<nav class="docs-breadcrumbs" aria-label="현재 위치"><ol><li><a href="/">홈페이지</a></li><li><a href="${relative(from,'wiki/index.html')}">CSAPP Wiki</a></li><li aria-current="page">Ch8 · §8.1–§8.2.4</li></ol></nav><header class="docs-hero"><p class="docs-kicker">2026-10-04 · CSAPP STUDY · CHAPTER 8</p><h1><span class="chapter-number">8.1–8.2.4</span>예외와 프로세스</h1><p class="docs-lead"><strong>Exceptional Control Flow.</strong> 예외, 프로세스, 가상 주소 공간과 권한 전환을 Linux 실습으로 연결한다.</p><div class="docs-meta"><span>CSAPP 3e §8.1–§8.2.4</span><span>ARM64 · x86-64 LAB</span><span>확인 기준 2026-10-25 KST</span></div><nav class="docs-hero-nav" aria-label="강의 이동"><a href="${relative(from,'chapter07/chapter-7.6.1.html')}">← Ch7 · 심볼 해석</a><a href="#labs">Linux 실습 →</a></nav></header><div class="docs-grid"><aside class="docs-collection">${collection(from)}</aside><main class="docs-main" id="content"><details class="docs-mobile-nav"><summary>강의 목록</summary>${collection(from)}</details><article class="docs-article">${html}<footer class="docs-footer"><nav class="docs-pagination" aria-label="이전 강의와 원문"><a href="${relative(from,'chapter07/chapter-7.6.1.html')}">← Ch7 · Symbol Resolution</a><a href="${stem}.md">Markdown 원문</a></nav><p>확인 기준일 2026-10-25 KST</p></footer></article></main><aside class="docs-outline"><nav aria-label="이 문서에서"><p class="docs-nav-title">이 문서에서</p><ul>${outline}</ul></nav></aside></div></body></html>\n`;
if(process.argv.includes('--check')){if(await readFile(file.replace(/\.md$/,'.html'),'utf8')!==output)throw new Error('HTML/Markdown mismatch');}
else await writeFile(file.replace(/\.md$/,'.html'),output);
console.log(`Rendered ${headings.filter(h=>h.level==='h2').length} top-level sections, LAB case nested; stable anchors and legacy aliases`);
