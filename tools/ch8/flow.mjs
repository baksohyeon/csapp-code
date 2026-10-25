import {chromium} from 'playwright';
import AxeBuilder from '@axe-core/playwright';
import {mkdir,writeFile,readFile} from 'node:fs/promises';
import assert from 'node:assert/strict';
const base=process.argv[2]||'http://127.0.0.1:8765/';const out=process.argv[3]||'/tmp/csapp-flow';
await mkdir(out,{recursive:true});
const b=await chromium.launch({channel:'chrome',headless:true});const records=[];
try{
 const context=await b.newContext({viewport:{width:1440,height:1000},permissions:['clipboard-read','clipboard-write']});const page=await context.newPage();
 if(process.argv.includes('--homepage')){
  await page.goto(new URL('/',base).href);await page.getByRole('link',{name:'csapp/',exact:true}).click();
 }else await page.goto(base);
 await page.waitForURL(new URL('wiki/',base).href);await page.locator('h1').waitFor();
 assert.equal(await page.locator('h1').innerText(),'CSAPP Wiki');
 const cards=await page.locator('.lecture-list>li>a').allTextContents();assert.deepEqual(cards.map(t=>t.match(/Ch\d/)[0]),['Ch6','Ch7','Ch8']);
 assert((await page.locator('.lecture-list').innerText()).includes('§8.1–§8.2.4'));
 const destinations=await page.locator('main a').evaluateAll(xs=>[...new Set(xs.map(x=>x.href))].filter(h=>h.startsWith(location.origin)&&!h.includes('#')));
 assert(destinations.every(h=>!h.endsWith('.md')),'Wiki learning links must not open raw Markdown');
 for(const href of destinations){const r=await page.request.get(href);assert.equal(r.status(),200,href);assert(r.headers()['content-type'].includes('text/html'),href);}
 await page.screenshot({path:out+'/wiki-desktop.png'});
 await page.getByRole('link',{name:'Ch8 · Exceptional Control Flow',exact:true}).click();
 const expected=[['exceptions',2,null],['exception-handling',3,'exceptions'],['exception-classes',3,'exceptions'],['linux-exceptions',3,'exceptions'],['processes',2,null],['logical-flow',3,'processes'],['concurrent-flows',3,'processes'],['address-space',3,'processes'],['modes',3,'processes']];
 const audit=JSON.parse(await readFile('validation/ch8/2026-10-25/textbook-audit.json','utf8'));
 const bodyText=await page.locator('.docs-article').innerText();
 for(let i=0;i<audit.sections.length;i++){
  const section=audit.sections[i];const heading=page.locator('#'+expected[i][0]);
  assert.equal((await heading.innerText()).replace(/#$/,'').trim(),section.number+' '+section.title);
  const row=page.locator('table tr').filter({has:page.locator('td:first-child').filter({hasText:new RegExp('^'+section.number.replaceAll('.',String.fromCharCode(92)+'.')+'$')})}).first();
  assert((await row.innerText()).includes('p.'+section.printedPage));assert((await row.innerText()).includes('p.'+section.globalPrintedPage));
 }
 let last=-1;
 const headings=await page.locator('.docs-article h2,.docs-article h3').evaluateAll(xs=>xs.map(x=>x.id));
 for(const [id,level,parent] of expected){const e=page.locator('#'+id);assert.equal(await e.evaluate(x=>x.tagName),'H'+level);const index=headings.indexOf(id);assert(index>last);last=index;if(parent)assert.equal(await e.evaluate(x=>x.closest('section').dataset.section),parent);}
 for(const id of ['exception-handling','exception-classes','linux-exceptions','logical-flow','concurrent-flows','address-space','modes'])assert.equal(await page.locator('.docs-outline a[href="#'+id+'"]').count(),1);
 const assembly=page.locator('code.language-x86asm');assert.equal(await assembly.count(),1);assert(await assembly.locator('.hljs-built_in,.hljs-keyword,.hljs-meta').count()>0);
 const c=page.locator('code.language-c').first();assert(await c.locator('.hljs-string').count()>0);
 const colors=await c.evaluate(e=>({plain:getComputedStyle(e).color,string:getComputedStyle(e.querySelector('.hljs-string')).color}));assert.notEqual(colors.plain,colors.string);
 const copy=page.locator('.code-block').filter({has:c}).getByRole('button');await copy.click();assert.equal(await page.evaluate(()=>navigator.clipboard.readText()),await c.textContent());
 await c.scrollIntoViewIfNeeded();await page.screenshot({path:out+'/code-desktop.png'});
 await page.getByRole('link',{name:'학습 목차',exact:true}).click();assert.equal(page.url(),new URL('wiki/',base).href);
 await page.getByRole('link',{name:'컴파일과 링커',exact:true}).click();assert(page.url().endsWith('/wiki/notes/0001-compile-linking.html'));assert.equal(await page.locator('h1').count(),1);
 await page.getByRole('link',{name:'← CSAPP Wiki',exact:true}).click();
 await page.getByRole('link',{name:'Ch8 Linux LAB',exact:true}).click();assert(page.url().endsWith('/examples/0002-exceptional-control-flow/'));assert.equal(await page.locator('h1').count(),1);
 const ids=await page.locator('[id]').evaluateAll(xs=>xs.map(x=>x.id));assert.equal(ids.length,new Set(ids).size);
 records.push({test:'homepage/CSAPP → Wiki → chapter, readable notes, source reader, back to Wiki; textbook hierarchy; code highlight and clipboard',pass:true,linkedPages:destinations.length,colors});
 await context.close();
 const samples=['wiki/','wiki/notes/0001-compile-linking.html','wiki/topics/compile-linking.html','examples/0002-exceptional-control-flow/','chapter07/examples/'];
 for(const route of samples)for(const [width,scheme] of [[375,'light'],[1440,'dark']]){
  const context=await b.newContext({viewport:{width,height:812},colorScheme:scheme});const p=await context.newPage();await p.goto(new URL(route,base).href,{waitUntil:'networkidle'});
  const audit=await new AxeBuilder({page:p}).withTags(['wcag2a','wcag2aa','wcag21aa']).analyze();
  const metrics=await p.evaluate(()=>({overflow:Math.max(0,document.documentElement.scrollWidth-innerWidth),truncate:[...document.querySelectorAll('*')].filter(e=>getComputedStyle(e).textOverflow==='ellipsis'&&e.scrollWidth>e.clientWidth).length,h1:document.querySelectorAll('h1').length}));
  const result={route,width,scheme,...metrics,violations:audit.violations.map(v=>({id:v.id,nodes:v.nodes.map(n=>n.target)}))};records.push(result);console.log(JSON.stringify(result));
  if(route==='wiki/')await p.screenshot({path:out+`/wiki-${width}-${scheme}.png`});
  await context.close();
 }
}finally{await b.close();await writeFile(out+'/results.json',JSON.stringify({base,checkedAt:new Date().toISOString(),records},null,2)+'\n');}
assert(records.every(r=>r.pass||r.overflow===0&&r.truncate===0&&r.h1===1&&r.violations.length===0));
console.log('PASS: learning flow, textbook hierarchy, code clipboard, readable routes and accessible catalog/notes/source readers');
