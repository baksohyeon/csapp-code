import {readFile,access,readdir} from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import {spawnSync} from 'node:child_process';
import YAML from 'yaml';
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../..');
const stem='2026-10-04-csapp-ch8-exceptional-control-flow';
const base=path.join(root,'wiki/lectures/2026');
const html=await readFile(path.join(base,stem+'.html'),'utf8');
const md=await readFile(path.join(base,stem+'.md'),'utf8');
const errors=[];
if((html.match(/<h1\b/g)||[]).length!==1)errors.push('expected one page title');
const meta=YAML.parse(md.match(/^---\n([\s\S]*?)\n---/)[1]);
if(meta.type!=='Lecture'||meta.date!=='2026-10-04')errors.push('lecture metadata');
const ids=[...html.matchAll(/\bid="([^"]+)"/g)].map(x=>x[1]);
if(ids.length!==new Set(ids).size)errors.push('duplicate ids');
for(const [,attribute,value] of html.matchAll(/\b(href|src)="([^"]+)"/g)){
 if(/^(https?|data):/.test(value))continue;
 if(value.startsWith('#')){if(!ids.includes(value.slice(1)))errors.push('anchor '+value);continue;}
 try{await access(path.resolve(base,decodeURI(value.split('#')[0])));}catch{errors.push(attribute+' '+value);}
}
for(const section of ['8.1.1','8.1.2','8.1.3','8.2.1','8.2.2','8.2.3','8.2.4']){
 if(!md.includes(section)||!html.includes(section))errors.push('missing section '+section);
}
const figures=path.join(root,'wiki/assets',stem);
const files=(await readdir(figures)).filter(x=>x.endsWith('.svg'));
for(const name of files){
 const svg=await readFile(path.join(figures,name),'utf8');
 if(!svg.includes('width="100%"')||!svg.includes('viewBox=')||!svg.includes('aria-label=')||!svg.includes('role="img"'))errors.push('SVG accessibility '+name);
 if(/#[a-fA-F0-9]{3,8}\b/.test(svg))errors.push('SVG hex '+name);
 if(!html.includes(name)||!md.includes(name))errors.push('figure not shared '+name);
}
for(const [name,text] of [['HTML',html],['Markdown',md]]){
 if(text.includes('\u2014'))errors.push(name+' em dash');
 if(/아니라|라기보다|흥미로운 점은/.test(text))errors.push(name+' prohibited wording');
}
for(const name of ['2026-10-25-csapp-ch8-validation','2026-10-25-csapp-ch8-learning-prompt']){
 const text=await readFile(path.join(root,'wiki/notes',name+'.md'),'utf8');
 const fm=YAML.parse(text.match(/^---\n([\s\S]*?)\n---/)[1]);
 if(fm.date!==name.slice(0,10))errors.push('note date '+name);
 const index=await readFile(path.join(root,'wiki/index.md'),'utf8');
 if(!index.includes('[['+name+']]'))errors.push('index '+name);
}
const render=spawnSync(process.execPath,[path.join(root,'tools/ch8/render.mjs'),'--check'],{encoding:'utf8'});
if(render.status!==0)errors.push(render.stderr);
if(errors.length){console.error(errors.join('\n'));process.exit(1);}
console.log(`PASS: frontmatter, ${ids.length} unique ids, relative links, 7 textbook subsections, ${files.length} shared SVGs, prose, HTML/Markdown parity`);
