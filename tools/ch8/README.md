# Ch8 문서 도구

```sh
npm ci --prefix tools/ch8
node tools/ch8/render.mjs
node tools/ch8/verify.mjs
python3 -m http.server 8765 --bind 127.0.0.1
# 별도 터미널, 레포 root에서 실행. 설치된 Google Chrome 사용.
node tools/ch8/browser.mjs http://127.0.0.1:8765/ local
```

기존 Ch8 완성형 Markdown을 보존하면서 HTML의 내용 동기화를 재현한다.
frontmatter·문서 본문은 Markdown에, HTML 디자인은 `style.css`와 renderer에 있다.
`render.mjs --check`는 파일을 변경하지 않고 동기화 여부를 확인한다.
브라우저 검사는 8개 viewport와 대표 크기의 dark theme를 렌더링하고,
`validation/ch8/2026-10-25/<phase>/`에 JSON·스크린샷을 저장한다.
이 경로는 이번 검증 실행용이다. 이후 실행 날짜에 맞춰 출력 경로도 갱신한다.

배포본 검증:

```sh
node tools/ch8/browser.mjs https://raspberrypi.bobcat-fort.ts.net/csapp/ live
```

HTML·Markdown·SVG 및 기존 Ch6/Ch7 파일의 라이브 hash 비교: `node tools/ch8/live.mjs`.
