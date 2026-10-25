import path from 'node:path';
export const stem='2026-10-04-csapp-ch8-exceptional-control-flow';
export const lectures=[
 {title:'Ch6 · Memory Hierarchy',date:'2026-07-05',file:'wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.html',description:'메모리 계층·캐시·지역성'},
 {title:'Ch7 · Symbol Resolution',date:'2026-07-26',file:'chapter07/chapter-7.6.1.html',description:'심볼 해석·정적 라이브러리·링커 실습'},
 {title:'Ch8 · Exceptional Control Flow',date:'2026-10-04',file:`wiki/lectures/2026/${stem}.html`,description:'예외·프로세스·주소 공간·Linux 실습'},
];
export const relative=(from,to)=>{
  if(path.posix.basename(to)==='index.html'){
    const directory=path.posix.relative(path.posix.dirname(from),path.posix.dirname(to));
    return directory?directory+'/':'./';
  }
  return path.posix.relative(path.posix.dirname(from),to)||'./';
};
export const escape=s=>String(s).replaceAll('&','&amp;').replaceAll('"','&quot;').replaceAll('<','&lt;').replaceAll('>','&gt;');
export const themeInit=`<script>try{const t=localStorage.getItem('csapp-theme');if(t==='light'||t==='dark')document.documentElement.dataset.theme=t}catch{}</script>`;
export function resources(from){return `${themeInit}<link rel="stylesheet" href="${relative(from,'wiki/assets/site/tokens.css')}"><link rel="stylesheet" href="${relative(from,'wiki/assets/site/docs.css')}"><script defer src="${relative(from,'wiki/assets/site/docs.js')}"></script>`;}
export function header(from){return `<header class="docs-header"><div class="docs-header-inner"><a class="docs-brand" href="${relative(from,'index.html')}">CSAPP <span>학습 문서</span></a><nav aria-label="사이트"><a href="${relative(from,'wiki/index.html')}">전체 목차</a><a class="source-link" href="https://github.com/baksohyeon/csapp-code">GitHub</a><button class="docs-theme" data-theme-toggle type="button">테마</button></nav></div></header>`;}
export function collection(from){return `<nav aria-label="강의 목록"><p class="docs-nav-title">강의노트 · 2026</p><ul>${lectures.map(l=>`<li><a href="${relative(from,l.file)}"${l.file===from?' aria-current="page"':''}>${escape(l.title)}</a></li>`).join('')}</ul></nav>`;}
