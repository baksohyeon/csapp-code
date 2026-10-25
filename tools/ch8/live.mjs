import {readFile,writeFile} from 'node:fs/promises';
import {createHash} from 'node:crypto';
import {execFileSync} from 'node:child_process';
const base='https://raspberrypi.bobcat-fort.ts.net/csapp/';
const stem='2026-10-04-csapp-ch8-exceptional-control-flow';
const files=[
 `wiki/lectures/2026/${stem}.html`,`wiki/lectures/2026/${stem}.md`,
 ...['abstraction-layers','exception-flow','concurrency-timeline','virtual-address-space','user-kernel-mode','blocking-io','go-gmp'].map(n=>`wiki/assets/${stem}/${n}.svg`),
 'wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.html',
 'chapter07/chapter-7.6.1.html','README.md',
];
const sha=b=>createHash('sha256').update(b).digest('hex');
const results=await Promise.all(files.map(async file=>{
 const r=await fetch(new URL(file,base),{signal:AbortSignal.timeout(30000)});
 const remote=Buffer.from(await r.arrayBuffer());const local=await readFile(file);
 return {file,status:r.status,contentType:r.headers.get('content-type'),localSha256:sha(local),liveSha256:sha(remote),match:sha(local)===sha(remote)};
}));
const data={checkedAt:new Date().toISOString(),commit:execFileSync('git',['rev-parse','HEAD'],{encoding:'utf8'}).trim(),base,results};
await writeFile(process.argv[2]||'validation/ch8/2026-10-25/deployment-content.json',JSON.stringify(data,null,2)+'\n');
for(const r of results)console.log(`${r.status} match=${r.match} ${r.file}`);
if(results.some(r=>r.status!==200||!r.match))process.exit(1);
