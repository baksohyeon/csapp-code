import path from 'node:path';
export const stem='2026-10-04-csapp-ch8-exceptional-control-flow';
export const lectures=[
 {title:'Ch6 · Memory Hierarchy',date:'2026-07-05',file:'wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.html',scope:'Chapter 6',description:'메모리 계층·캐시·지역성'},
 {title:'Ch7 · Symbol Resolution',date:'2026-07-26',file:'chapter07/chapter-7.6.1.html',scope:'§7.6.1–§7.6.3',description:'심볼 해석·정적 라이브러리·링커 실습'},
 {title:'Ch8 · Exceptional Control Flow',date:'2026-10-04',file:`wiki/lectures/2026/${stem}.html`,scope:'§8.1–§8.2.4',description:'예외·프로세스·주소 공간·Linux 실습'},
 {title:'Ch8 · 시그널과 비지역 점프',date:'2026-11-08',file:'wiki/lectures/2026/2026-11-08-csapp-ch8-signals-and-nonlocal-jumps.html',scope:'§8.2.5~§8.8 · Ch9 예습',description:'문맥 교환·프로세스 제어·시그널·비지역 점프·가상 메모리 예습'},
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
export function portalHead(title){
  return `<title>${escape(title)} | Dorito’s Homelab</title><link rel="icon" type="image/x-icon" href="/favicon.ico"><link rel="icon" type="image/svg+xml" sizes="any" href="/favicon.svg">`;
}
export function resources(from){return `${themeInit}<link rel="stylesheet" href="${relative(from,'wiki/assets/site/tokens.css')}"><link rel="stylesheet" href="${relative(from,'wiki/assets/site/docs.css')}"><link rel="stylesheet" href="${relative(from,'wiki/assets/site/github-dark.css')}"><script defer src="${relative(from,'wiki/assets/site/docs.js')}"></script>`;}
export function header(from){return `<header class="docs-header"><div class="docs-header-inner"><div class="docs-identity"><a class="homelab-link" href="/" aria-label="홈으로 · Dorito’s Homelab"><img src="/favicon.svg" alt="" width="21" height="21">홈으로</a><a class="docs-brand" href="${relative(from,'wiki/index.html')}">CSAPP <span>Wiki</span></a></div><nav aria-label="사이트"><a href="${relative(from,'wiki/index.html')}">학습 목차</a><a class="source-link" href="https://github.com/baksohyeon/csapp-code">GitHub</a><button class="docs-theme" data-theme-toggle type="button">테마</button></nav></div></header>`;}
export function collection(from){return `<nav aria-label="강의 목록"><p class="docs-nav-title">강의노트 · 2026</p><ul>${lectures.map(l=>`<li><a href="${relative(from,l.file)}"${l.file===from?' aria-current="page"':''}>${escape(l.title)}</a></li>`).join('')}</ul></nav>`;}
