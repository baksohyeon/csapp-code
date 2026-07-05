# csapp-code

CSAPP(Computer Systems: A Programmer's Perspective) 스터디 기록.
매주 일요일 오전 10시 스터디에서 배운 것을 wiki 형태로 쌓는 레포다.

## 빠른 시작

| 무엇을 보고 싶나 | 어디로 |
|------------------|--------|
| 렌더링된 노트 (웹/모바일에서 바로 보기) | https://raspberrypi.bobcat-fort.ts.net/csapp/ |
| 최신 강의노트 (Ch6 Memory Hierarchy) | [라이브](https://raspberrypi.bobcat-fort.ts.net/csapp/wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.html) · [레포 소스](wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.html) · [요약 wrapper](wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.md) |
| 전체 목차 | [wiki/index.md](wiki/index.md) |
| 교재 PDF | https://i.hyeon.me/csapp.pdf |
| 스터디 whiteboard | [현재](https://i.hyeon.me/csapp) · [과거 아카이브](https://i.hyeon.me/csapp-archive) |

## 레포 구조

```
wiki/                  ← knowledge base root (여기서부터 읽으면 된다)
  index.md             ← 전체 목차. 항상 여기가 진입점
  lectures/<year>/     ← 강의노트. HTML 원본 + 같은 이름의 .md 요약 wrapper 쌍
  notes/               ← 개별 학습 노트 (컴파일/링킹, Java 실행법, 학습 로그 …)
  topics/              ← 토픽 노드. 여러 노트가 공유하는 개념 허브
  type/                ← 문서 타입 설명 (lecture / note / topic / example)
  assets/              ← 노트에 첨부된 이미지
examples/              ← C 실습 코드 (노트에서 참조)
java-datastructures/   ← BST/AVL 트리 Java 구현
exercises/             ← 자바 연습문제+해답 (hwp)
AGENTS.md              ← 이 레포의 작성 규칙 (에이전트/사람 공용)
```

## 읽는 법

1. [wiki/index.md](wiki/index.md)를 연다. 강의노트·노트·토픽이 전부 링크되어 있다.
2. 문서 안의 `[[이름]]` 표기는 wiki 내부 링크다. `wiki/` 아래에서 같은 이름의
   파일(`이름.md`)을 찾으면 된다 (Obsidian 등 wikilink 지원 도구에서는 바로 클릭 가능).
3. 강의노트는 **HTML이 1차 자료**다. GitHub 웹에서는 HTML이 코드로 보이므로,
   렌더링된 화면을 보려면 라즈베리파이에 셀프호스팅된 공개 URL로 열거나 로컬 클론 후
   브라우저로 연다 (아래 [HTML 렌더링 보기](#html-렌더링-보기) 참고). 같은 이름의 `.md`
   wrapper에 요약과 메타데이터가 있으니 GitHub에서는 wrapper부터 봐도 된다.

## HTML 렌더링 보기

- **웹/모바일 (권장)**: 라즈베리파이 홈서버에 셀프호스팅되어 있어, 아래 주소로 어느 기기에서든
  렌더링된 노트를 바로 볼 수 있다 (Dokku 배포 + Tailscale Funnel 공개).
  - 최신 강의노트: `https://raspberrypi.bobcat-fort.ts.net/csapp/wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.html`
  - csapp 전체 (파일 목록): `https://raspberrypi.bobcat-fort.ts.net/csapp/`
- **로컬**: 클론 후 HTML 파일을 브라우저로 연다.
  ```bash
  git clone git@github.com:baksohyeon/csapp-code.git
  open csapp-code/wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.html
  ```

## 실습 실행하기

### C — 컴파일과 링킹 (자세한 설명: [노트](wiki/notes/0001-compile-linking.md))

```bash
cd examples/0001-compile-linking
gcc hello.c sum.c -o hello   # 컴파일 + 링킹 한 번에
./hello                      # Hello, World! 4950
```

### Java — BST/AVL 데모 (자세한 설명: [노트](wiki/notes/java-compile-run.md))

```bash
javac -d java-datastructures java-datastructures/BST/*.java
java -cp java-datastructures BST.BinarySearchTreeDemo
java -cp java-datastructures BST.AVLTreeDemo
```

## 새 문서 추가하는 법

- **강의노트**: `wiki/lectures/<year>/YYYY-MM-DD-<slug>.html`(원본) +
  같은 이름의 `.md`(frontmatter 소유 wrapper). 처음부터 Markdown으로 썼다면 `.md` 하나만.
- **노트/토픽**: `wiki/notes/`, `wiki/topics/`에 Markdown + YAML frontmatter.
- 자세한 규칙(frontmatter 필드, wikilink, `.o` 추적 정책)은 [AGENTS.md](AGENTS.md) 참고.
