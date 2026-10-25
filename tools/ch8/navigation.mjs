import {chromium} from 'playwright';
import AxeBuilder from '@axe-core/playwright';
import {mkdir,writeFile} from 'node:fs/promises';
import assert from 'node:assert/strict';
const base=process.argv[2]||'http://127.0.0.1:8765/';
const out=process.argv[3]||'validation/ch8/2026-10-25/navigation';
await mkdir(out,{recursive:true});
const file='wiki/lectures/2026/2026-10-04-csapp-ch8-exceptional-control-flow.html';
const url=new URL(file,base).href;
const browser=await chromium.launch({channel:'chrome',headless:true});
const context=await browser.newContext({viewport:{width:1440,height:900},reducedMotion:'reduce'});
const page=await context.newPage();const results=[];
try{
 let response=await page.goto(base);assert.equal(response.status(),200);
 assert.equal(await page.locator('h1').innerText(),'컴퓨터 시스템 학습 문서');
 await page.getByRole('link',{name:'Ch8 · Exceptional Control Flow',exact:true}).click();
 assert.equal(page.url(),url);
 await page.locator('.docs-collection a').filter({hasText:'Ch7'}).click();
 assert(page.url().endsWith('/chapter07/chapter-7.6.1.html'));
 await page.getByRole('navigation',{name:'강의 이동'}).getByRole('link',{name:'Ch8 →'}).click();
 assert.equal(page.url(),url);
 await page.goto(url+'#labs');
 await page.locator('.docs-outline a[href="#lab-debugging"]').click();assert.equal(new URL(page.url()).hash,'#lab-debugging');
 assert.equal(await page.locator('#lab-debugging').evaluate(e=>e.closest('section').dataset.section),'labs');
 await page.goBack();assert.equal(new URL(page.url()).hash,'#labs');
 await page.goForward();assert.equal(new URL(page.url()).hash,'#lab-debugging');
 await page.reload();assert.equal(new URL(page.url()).hash,'#lab-debugging');
 const targetTop=await page.locator('#lab-debugging').evaluate(e=>e.getBoundingClientRect().top);
 const headerHeight=await page.locator('.docs-header').evaluate(e=>e.getBoundingClientRect().height);assert(targetTop>=headerHeight-1);
 await page.goto(url+'#심층-사례-strace-관측-실패-디버깅');
 assert.equal(await page.locator('[id="심층-사례-strace-관측-실패-디버깅"]').count(),1);
 await page.goto(new URL('wiki/notes/2026-10-25-x86-strace-debugging.html',base).href);
 await page.waitForURL(url+'#lab-debugging');
 await page.locator('.docs-header').getByRole('link',{name:'전체 목차',exact:true}).click();
 assert.equal(page.url(),new URL('wiki/',base).href);
 assert.equal(await page.locator('h1').innerText(),'CSAPP Wiki');
 response=await page.request.get(new URL('this-page-does-not-exist',base).href);assert.equal(response.status(),404);
 for(const [asset,type] of [['tokens.css','text/css'],['docs.css','text/css'],['docs.js','javascript']]){
  response=await page.request.get(new URL('wiki/assets/site/'+asset,base).href);assert.equal(response.status(),200);assert(response.headers()['content-type'].includes(type));
 }
 results.push({test:'home, collection, Chapter 7/8, index, deep links, back/forward, reload, legacy redirect, assets, 404',pass:true});
 await context.close();
 for(const [width,height,scheme] of [[375,812,'light'],[812,375,'light'],[1440,900,'light'],[375,812,'dark'],[1440,900,'dark']]){
  const c=await browser.newContext({viewport:{width,height},colorScheme:scheme,reducedMotion:'reduce'});const p=await c.newPage();
  await p.goto(url,{waitUntil:'networkidle'});
  const audit=await new AxeBuilder({page:p}).withTags(['wcag2a','wcag2aa','wcag21aa']).analyze();
  const violations=audit.violations.map(v=>({id:v.id,impact:v.impact,nodes:v.nodes.map(n=>n.target)}));
  const articleWidth=await p.locator('.docs-article').evaluate(e=>e.getBoundingClientRect().width);assert(articleWidth<=736.1);
  const before=await p.locator('html').getAttribute('data-theme');await p.locator('[data-theme-toggle]').click();
  const after=await p.locator('html').getAttribute('data-theme');assert(['dark','light'].includes(after));assert(after!==before);
  await p.reload();assert.equal(await p.locator('html').getAttribute('data-theme'),after);
  await p.locator('[data-theme-toggle]').click();
  if(width===375) await p.locator('.docs-mobile-nav summary').click();
  await p.screenshot({path:`${out}/${width}x${height}-${scheme}.png`});
  results.push({width,height,scheme,articleWidth,themePersistence:true,violations});console.log(width,scheme,'axe violations:',violations.length);
  await c.close();
 }
}finally{await browser.close();await writeFile(out+'/results.json',JSON.stringify({base,checkedAt:new Date().toISOString(),results},null,2)+'\n');}
if(results.some(r=>r.violations?.length))process.exit(1);
console.log('PASS: navigation, native history, stable anchors, theme persistence, WCAG A/AA automated checks');
