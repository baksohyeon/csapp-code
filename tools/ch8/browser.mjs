import {chromium} from 'playwright';
import {mkdir,writeFile} from 'node:fs/promises';
import path from 'node:path';
const base=process.argv[2]||'http://127.0.0.1:8765/';
const phase=process.argv[3]||'local';
const out=path.resolve('validation/ch8/2026-10-25',phase);
await mkdir(out,{recursive:true});
const browser=await chromium.launch({channel:'chrome',headless:true});
const sizes=[[320,568],[375,812],[812,375],[390,844],[430,932],[768,1024],[1024,768],[1440,900]];
const results=[];
for(const theme of ['light','dark']){
 for(const [width,height] of (theme==='light'?sizes:[[375,812],[1440,900]])){
  const context=await browser.newContext({viewport:{width,height},colorScheme:theme,reducedMotion:'reduce'});
  const page=await context.newPage();const errors=[];
  page.on('pageerror',e=>errors.push(e.message));
  page.on('console',m=>{if(m.type()==='error')errors.push(m.text());});
  page.on('requestfailed',r=>errors.push(r.url()+': '+r.failure()?.errorText));
  page.on('response',r=>{if(r.status()>=400)errors.push(r.status()+' '+r.url());});
  const url=new URL('wiki/lectures/2026/2026-10-04-csapp-ch8-exceptional-control-flow.html',base).href;
  const response=await page.goto(url,{waitUntil:'networkidle'});
  await page.evaluate(async()=>{
   for(const image of document.images){image.loading='eager';await image.decode();}
   await document.fonts.ready;
  });
  const metrics=await page.evaluate(async()=>{
   const root=document.documentElement;
   const ellipsis=[...document.querySelectorAll('*')].filter(e=>getComputedStyle(e).textOverflow==='ellipsis');
   const clipped=ellipsis.filter(e=>e.scrollWidth>e.clientWidth+1);
   const figures=await Promise.all([...document.querySelectorAll('figure img')].map(async img=>{
    const xml=await (await fetch(img.src)).text();
    const svg=new DOMParser().parseFromString(xml,'image/svg+xml');
    const error=svg.querySelector('parsererror')?.textContent;
    const element=svg.documentElement;
    return {src:img.getAttribute('src'),alt:img.alt,ariaLabel:element.getAttribute('aria-label'),width:img.getBoundingClientRect().width,loaded:img.complete&&img.naturalWidth>0,xmlError:error||null};
   }));
   window.scrollTo(0,700);const moved=window.scrollY;
   const tables=[...document.querySelectorAll('.table-wrap')].map(e=>{
    e.scrollLeft=100;return {overflow:e.scrollWidth>e.clientWidth+1,scrolled:e.scrollLeft>0};
   });
   const brokenAnchors=[...document.querySelectorAll('a[href^="#"]')].filter(e=>!document.getElementById(decodeURI(e.getAttribute('href').slice(1)))).map(e=>e.getAttribute('href'));
   return {pageOverflow:Math.max(0,root.scrollWidth-root.clientWidth),ellipsisDeclarations:ellipsis.length,truncateCount:clipped.length,scrollY:moved,documentHeight:root.scrollHeight,figures,tables,brokenAnchors,iframes:document.querySelectorAll('iframe').length,sectionCount:document.querySelectorAll('.docs-article>section').length};
  });
  if(width<1024) await page.locator('.toc').evaluate(e=>e.open=true);
  await page.locator(width<1024?'.toc a[href="#processes"]':'.docs-outline a[href="#processes"]').click();
  await page.waitForTimeout(100);
  metrics.anchorNavigation=await page.evaluate(()=>location.hash==='#processes');
  await page.evaluate(()=>{document.querySelectorAll('.table-wrap').forEach(e=>e.scrollLeft=0);window.scrollTo(0,0);});
  const screenshot=`${width}x${height}-${theme}.jpg`;
  await page.screenshot({path:path.join(out,screenshot),type:'jpeg',quality:75});
  if(width===375){
   for(const id of ['exceptions','address-space','labs','lab-debugging']){
    await page.locator(`[id="${id}"]`).evaluate(e=>e.scrollIntoView({block:'start'}));
    await page.screenshot({path:path.join(out,`${id}-${theme}.jpg`),type:'jpeg',quality:75});
   }
   for(const [i,figure] of (await page.locator('figure').all()).entries()){
    await figure.screenshot({path:path.join(out,`figure-${i+1}-${theme}.jpg`),type:'jpeg',quality:80});
   }
  }
  const result={url,width,height,theme,status:response.status(),...metrics,errors,screenshot};
  results.push(result);
  console.log(`${phase} ${width}x${height} ${theme}: overflow=${metrics.pageOverflow} truncate=${metrics.truncateCount} scroll=${metrics.scrollY} figures=${metrics.figures.length} errors=${errors.length}`);
  await context.close();
 }
}
await browser.close();
await writeFile(path.join(out,'metrics.json'),JSON.stringify({checkedAt:new Date().toISOString(),phase,results},null,2)+'\n');
if(results.some(r=>r.status!==200||r.pageOverflow>1||r.truncateCount||r.scrollY===0||r.errors.length||r.brokenAnchors.length||!r.anchorNavigation||r.figures.some(f=>!f.loaded||f.xmlError||!f.ariaLabel)))process.exit(1);
