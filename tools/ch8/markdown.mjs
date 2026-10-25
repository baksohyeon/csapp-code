import MarkdownIt from 'markdown-it';
import hljs from 'highlight.js';

export function createMarkdown({idPrefix="code"}={}) {
 const md=new MarkdownIt({html:false});let number=0;
 md.renderer.rules.fence=(tokens,i)=>{
  const code=tokens[i].content;
  const requested=tokens[i].info.trim().split(/\s+/)[0]||'text';
  const language=({asm:'x86asm',sh:'bash',shell:'bash',text:'plaintext'})[requested]||requested;
  if(!hljs.getLanguage(language))throw new Error('Unsupported code language: '+requested);
  const highlighted=hljs.highlight(code,{language,ignoreIllegals:true}).value;
  const label=({x86asm:'x86-64 Assembly',bash:'Shell',plaintext:'실행 출력',c:'C',java:'Java',python:'Python'})[language]||language;
  const id=idPrefix+'-'+(++number);
  return `<div class="code-block"><div class="code-toolbar"><span>${md.utils.escapeHtml(label)}</span><button type="button" class="code-copy" data-copy-code="${id}" aria-label="${label} 코드 복사">복사</button></div><pre tabindex="0" aria-label="${label} 코드"><code id="${id}" class="hljs language-${language}">${highlighted}</code></pre></div>\n`;
 };
 md.renderer.rules.table_open=()=>'<div class="table-wrap" tabindex="0" role="region" aria-label="가로 스크롤 가능한 표"><table>\n';
 md.renderer.rules.table_close=()=>'</table></div>\n';
 return md;
}
