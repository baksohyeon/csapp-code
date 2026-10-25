# Ch8 문서 도구

```sh
npm ci --prefix tools/ch8
node tools/ch8/render.mjs
node tools/ch8/render-site.mjs
node tools/ch8/verify.mjs
python3 -m http.server 8765 --bind 127.0.0.1
# 별도 터미널, 레포 root에서 실행. 설치된 Google Chrome 사용.
node tools/ch8/browser.mjs http://127.0.0.1:8765/ local
```

기존 Ch8 완성형 Markdown을 보존하면서 HTML의 내용 동기화를 재현한다.
frontmatter·문서 본문은 Markdown에, 디자인 토큰은 `wiki/assets/site/tokens.css`, 문서 레이아웃은 `wiki/assets/site/docs.css`, 탐색·테마 동작은 `wiki/assets/site/docs.js`에 있다.
`render.mjs --check`는 파일을 변경하지 않고 동기화 여부를 확인한다.
브라우저 검사는 8개 viewport와 대표 크기의 dark theme를 렌더링하고,
`validation/ch8/2026-10-25/<phase>/`에 JSON·스크린샷을 저장한다.
이 경로는 이번 검증 실행용이다. 이후 실행 날짜에 맞춰 출력 경로도 갱신한다.

배포본 검증:

```sh
node tools/ch8/browser.mjs https://raspberrypi.bobcat-fort.ts.net/csapp/ live
```

HTML·Markdown·SVG 및 기존 Ch6/Ch7 파일의 라이브 hash 비교: `node tools/ch8/live.mjs`.


`/`는 학습 문서 진입점, `/wiki/`는 wiki 목차다. 정본 강의 파일은 AGENTS.md의 경로를 유지한다.
Ch8의 실습 디버깅 사례는 LAB 아래 `#lab-debugging`에 있으며 이전 한글 fragment는 호환 앵커로 보존한다.
CSS·JS와 문서 링크는 상대 경로여서 로컬 root와 배포 `/csapp/` 아래에서 동일하게 동작한다.
