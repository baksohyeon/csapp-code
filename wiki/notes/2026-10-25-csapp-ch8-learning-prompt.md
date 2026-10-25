---
type: Note
status: draft
date: 2026-10-25
related_to:
  - "[[2026-07-05-csapp-ch6-memory-hierarchy]]"
  - "[[2026-07-26-csapp-ch7-symbol-resolution]]"
source_url: https://developers.openai.com/api/docs/guides/latest-model?model=gpt-5.5
---

# CSAPP Ch8 학습 문서 개선·검증·배포 프롬프트

아래 실행 프롬프트를 작업할 Codex에 전달한다. 이 파일은 작업 지시서다.
이 파일의 작성은 Ch8 수정, GitHub push, Dokku 배포의 실행 결과를 의미하지 않는다.

## 검토 기준과 적용

확인 기준일: 2026-10-25 KST.

[OpenAI 공식 GPT-5.5 안내의 Prompting best practices](https://developers.openai.com/api/docs/guides/latest-model?model=gpt-5.5#prompting-best-practices)를 확인했다.
해당 URL은 상단에 최신 모델 안내도 포함하므로 본문의 `Using GPT-5.5` 아래 절을 기준으로 읽는다.
목표, 제약, 근거, 완료 조건을 중심으로 재구성하고 세부 작업 순서는 실행자가 선택하도록 했다.

사용자가 제공한 학습 페이지 지침은 이 레포의 HTML·Markdown·SVG 산출물에 맞춰 적용한다.
`WriteCanvas`, `grok/canvas`, `useGrokTheme`, `pages.grok.bot`는 해당 Canvas 환경에 종속된 기능이다.
이 레포에서는 HTML, CSS design tokens, 공유 SVG와 실제 Dokku 서비스 주소를 사용한다.
새 외부 Page를 필수 산출물로 추가하지 않는다.

원본 지침의 충돌은 다음과 같이 정리했다.

| 항목 | 적용 기준 |
|---|---|
| Markdown 전체 복제와 wrapper | AGENTS.md의 HTML 원본 + Markdown wrapper 유지. 기존 완성형 Markdown이 있으면 보존·동기화 |
| ELI10와 비유 최소화 | 어려운 개념 진입부에 짧은 비유, 바로 뒤 실제 용어·비유의 한계 |
| 정정 절과 편집 이력 금지 | 학습에 필요한 조건·미확인만 본문에, 수정 사유·작업 경과는 별도 보고 |
| SVG 2~3장과 기존 6장 | 기존 그림의 교육 효과를 평가하고 유지. 장수보다 개념 정확성·실제 가독성 우선 |
| ellipsis 개수와 실제 잘림 | CSS 선언 수와 실제 잘린 요소 수를 분리해 측정 |
| 343px 렌더 폭 | 컨테이너 여백에 따른 실제 폭 기록. 특정 폭을 모든 화면의 합격 기준으로 고정하지 않음 |
| 과거 완료 보고 | 커밋·원격 파일·라이브 응답을 현재 환경에서 재확인 |

## 실행 프롬프트 시작

당신은 CSAPP 스터디 레포의 기술 문서 편집자이자 검증 담당자다.
주니어 백엔드 개발자가 CSAPP 3판 §8.1~§8.2.4를 이해하고 Linux 실습으로 확인할 수 있도록,
기존 Ch8 학습 문서를 개선하고 GitHub 반영과 Raspberry Pi Dokku 배포까지 수행하라.
설명으로 끝내지 말고 가능한 작업을 실제로 수행하라.

### 목표와 완료 조건

완료된 결과는 다음 조건을 만족한다.

- 교재 절과 문서 절의 대응이 명확하고, 교재·강의·공식 구현·작성자 해설의 출처가 구분된다.
- 핵심 개념은 쉬운 진입 설명, 정확한 정의, 표 또는 그림, 직접 확인할 LAB으로 연결된다.
- 주요 기술 주장에는 이를 직접 뒷받침하는 1차 출처와 적용 조건이 있다.
- 기존 Ch6/Ch7과 일관된 HTML, Markdown wrapper, 공용 SVG, 목차 링크를 제공한다.
- 로컬과 배포 URL에서 구조·링크·모바일 렌더링을 검증하고 증거를 남긴다.
- 이번 변경의 커밋 SHA, GitHub 반영, Dokku release와 실제 서비스 내용을 확인한다.

필수 조건이 남으면 해당 단계는 미완료로 보고한다. 로컬 수정, push, 배포, 라이브 검증의 상태를 각각 기록한다.

### 시작 맥락과 파일 탐색

대상 레포는 `baksohyeon/csapp-code`이며 예상 기본 브랜치는 `main`이다.
AGENTS.md, README.md, 현재 브랜치·작업 트리·remote·최근 이력으로 실제 상태를 확인한다.
기존 변경을 보존하고 이번 작업 파일만 수정·stage한다.

이전 GPT는 다음과 같이 보고했다. 아래 내용은 현재 검증할 단서다.

- Ch8 HTML·Markdown, 6개 SVG, README를 갱신했고 GitHub main에 반영했다고 보고했다.
- 마지막 HTML 동기화 커밋으로 `9dc088ecb019ad79dd246b06d159c26d1d80f290`을 제시했다.
- 당시 실행 환경에서 라이브 호스트 DNS 해석이 실패했고 Dokku 배포는 미완료라고 보고했다.

위 SHA의 존재·변경 파일·원격 브랜치 포함 여부를 확인한다. 필요하면 remote를 fetch한다.
작업 트리가 깨끗하지 않으면 checkout/reset으로 덮어쓰지 말고 파일 조회 또는 격리된 작업 경로를 사용한다.
Ch8 파일이 로컬에 없으면 원격 이력과 해당 커밋에서 찾는다.
원본을 확보하지 못하면 강의 내용을 추정해 복원하지 않는다. 원본 의존 작업을 미완료로 남기고,
확보된 자료로 할 수 있는 조사와 검증은 진행한다.

주요 예상 경로:

```text
wiki/lectures/2026/2026-10-04-csapp-ch8-exceptional-control-flow.{html,md}
wiki/assets/2026-10-04-csapp-ch8-exceptional-control-flow/
  exception-flow.svg
  concurrency-timeline.svg
  virtual-address-space.svg
  user-kernel-mode.svg
  abstraction-layers.svg
  go-gmp.svg
wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.{html,md}
chapter07/chapter-7.6.1.{html,md}
chapter07/references.md
chapter07/verify-html.mjs
wiki/index.md
Dockerfile
deploy/nginx.conf
```

경로와 파일 수는 탐색의 단서다. 실제 목록과 기존 링크를 기준으로 편집한다.
기존 노트 생성일은 보존한다. 새 강의노트의 파일명·frontmatter date는 실제 생성일로 맞추고,
세션 날짜가 다르면 본문에 별도로 적는다. 과거 생성일을 새 문서에 임의로 부여하지 않는다.

### 근거와 기술 검증

문서 상단에 실제 확인 기준일을 KST 절대 날짜로 쓴다.
교재 3판 본문·공식 목차·정오표, Intel SDM, Linux man-pages, GNU glibc,
Go 공식 문서·runtime source, Erlang/OTP 문서, Rust 공식 문서,
Rob Pike 원발표, OSTEP 공식 교재를 우선한다.
필요한 페이지를 실제로 열고 관련 절을 읽는다. 검색 요약만으로 판정하지 않는다.

출처 성격과 근거 상태는 두 축으로 구분한다.

- 출처: `[CSAPP]`, `[LECTURE]`, `[OFFICIAL]`, `[COMMENTARY]`.
- 상태: `[사실]`, `[미확인]`, `[추론]`, `[제안]`.

문단·표·보충 박스 단위로 필요한 곳에 표시해 반복을 줄인다.
강의 메모는 맥락 자료로 선의 해석한다. 명시하지 않은 주장을 강의자에게 귀속하지 않는다.
외부 주장 또는 메모와 검증 자료를 비교할 필요가 있으면
`주장 | 판정(일치/조건부/불일치/미확인) | 조건 | 근거` 표를 만든다.
원문에 없는 반박 대상을 만들지 않는다. 자료 간 불일치는 적용 범위와 함께 기록한다.

구현 세부에는 OS·ISA·ABI·런타임 버전 또는 소스 commit/tag를 붙인다.
교재 설명과 현대 구현, 역사적 구현을 구분한다. 수치에는 단위·환경·측정 조건을 붙인다.
핵심 주장에 충분한 근거가 확보되면 해당 조사를 끝낸다.
자료가 부족하면 다른 1차 출처를 찾고, 계속 확인되지 않는 부분은 미확인으로 남긴다.

Ch8에서 다음 검증 항목을 우선한다. 항목 자체를 검증 결과로 취급하지 않는다.

| 묶음 | 확인할 질문 |
|---|---|
| Exception | interrupt/trap/fault/abort의 원인·동기성·복귀 지점은 무엇인가? 교재와 Intel 용어는 어떻게 대응하는가? |
| Divide error | ISA의 예외 발생 조건과 Linux signal 전달은 어느 계층에서 정의되는가? C 예제의 undefined behavior와 컴파일러 최적화는 재현에 어떤 영향을 주는가? |
| Syscall ABI | Linux x86-64의 번호·인자·반환값·clobber 규칙은 무엇인가? C 함수 호출 ABI, libc wrapper, 다른 ISA와 어떻게 대응하는가? |
| 표준 입출력 | fd 0/1/2, libc stream, 버퍼링, 실제 syscall은 어떻게 연결되는가? strace로 어디까지 관측하는가? |
| Process | logical control flow, concurrency, parallelism의 정의와 단일·다중 코어 타임라인은 어떻게 연결되는가? |
| 런타임 보충 | C10K, thread-per-request, blocking I/O, function coloring, cooperative/preemptive scheduling, green thread는 어떤 문제를 설명하는가? |
| Go/Erlang/Rust | G/M/P, netpoll, syscall handoff, NIF와 dirty CPU/I/O scheduler, async task와 executor는 공식 자료의 어떤 범위에서 설명되는가? |
| 주소 공간 | private virtual address space, MMU·page table, 공유 mapping은 어떻게 연결되는가? maps의 r/w/x/s/p와 pagemap은 무엇을 보여주고 어떤 접근 제한이 있는가? |
| 권한 전환 | user/kernel mode, privileged instruction, syscall, context switch의 관계는 무엇인가? exit_group과 power-off 요청의 반환·권한 조건은 무엇인가? |

### 학습 구조와 설명

Ch6의 시각적 설명과 Ch7의 교재 대응표·출처 분리·실험 구조를 참고한다.
다음 학습 순서를 유지하되, 교재 본문의 §8.1~§8.2.4 순서는 바꾸지 않는다.

1. 한 줄 요약, 학습 목표, 읽기 범위와 확인 기준일, 바로 아래 목차.
2. 기초 개념과 ELI10 진입부. 처음 나오는 전문용어를 정의한다.
3. 교재 대응표와 배경·대안 비교. 본문과 보충으로 이동하는 링크를 제공한다.
4. 교재 순서의 심층 설명, 방법, 코드, 조건을 붙인 관측 표와 한계.
5. 적용 로드맵과 LAB, 개념 확인 질문과 해설.
6. 확인된 적용 조건과 남은 미확인 사항. 학습상 필요한 정정만 포함한다.
7. 출처. 짧은 자료명, 정확한 URL, 문서 버전·절, 확인 날짜를 제공한다.

교재 대응표에는 §8.1, §8.1.1~§8.1.3, §8.2, §8.2.1~§8.2.4를 포함한다.
현대 런타임·언어 보충은 관련 절에서 링크되는 별도 보충으로 배치한다.
CSAPP, Rob Pike, OSTEP의 concurrency 설명은 각각 원자료 맥락을 밝혀 연결한다.

핵심 개념은 문제 상황 → 정확한 동작 → 관측 도구 → 해석 순서로 설명한다.
어려운 개념에는 짧은 ELI10 비유 하나를 사용하고 실제 용어와 비유가 설명하지 못하는 범위를 잇는다.
모든 소절에 같은 템플릿을 강제하지 않는다. 표는 비교에, 단계는 절차에 사용한다.
핵심 경고 Callout은 1~2개로 제한하고, 나머지 조건은 관련 설명 가까이에 둔다.
짧은 한국어 긍정 명제로 쓰고 AGENTS.md의 강의 메모 해석·대조 문형 규칙을 따른다.
em dash와 편집 경과·AI 작업 지시를 학습 본문에 넣지 않는다.

### LAB와 재현성

기존 실습은 재사용하고, 부족한 부분에만 `examples/<id>-<slug>/` 실습을 추가한다.
각 LAB에 목적, 실행 환경, 명령, EXPECTED, 실제 관측, 해석, 한계를 제공한다.
OBSERVED/PASS는 실제 실행한 경우에만 기록한다. 실행하지 못했으면 미실행과 원인을 쓴다.

우선 LAB은 syscall/strace, 프로세스와 동시 실행, `/proc/PID/maps` 관측이다.
교재 ISA 실습은 Linux/x86-64에서 수행한다. macOS·ARM64 실행 결과에는 별도 환경 라벨을 붙인다.
타임라인은 반복 실행마다 달라질 수 있는 조건을 설명한다.
pagemap 권한 제한과 syscall wrapper에 따른 관측 차이를 기록한다.
실제 호스트를 종료하는 shutdown/reboot 호출은 실행하지 않는다. 문서·소스 분석으로 설명한다.
추적하는 빌드 산출물은 노트가 직접 참조하는 것으로 제한하고 플랫폼과 재생성 명령을 적는다.

### HTML·Markdown·SVG

HTML은 기존 design tokens, typography, navigation, code highlighting, 표, print 스타일을 재사용한다.
CSS 변수로 밝은·어두운 테마의 배경·글자·선·강조색을 관리한다.
SVG에 새 hex 색상을 하드코딩하지 않는다. 독립 SVG는 자체 CSS 변수와 테마 fallback을 정의한다.
`img`로 불러온 SVG의 실제 테마 표시도 확인한다.

기존 6개 SVG를 검토한다. 예외 복귀, 실행 타임라인, 가상 주소 공간,
권한 전환, 추상화 계층, Go scheduler의 개념·화살표·범례를 확인한다.
기존 그림과 중복되지 않으며 이해를 개선하는 경우 blocking I/O/event loop 또는 mapping 그림을 추가한다.
그림은 관련 표 바로 아래나 절차 바로 위에 배치하고 긴 설명을 그림 밖으로 뺀다.

SVG는 `width="100%"`와 viewBox, 짧은 라벨, `role="img"`, 설명적인 aria-label을 갖춘다.
외부 SVG를 참조하는 HTML `img`에는 의미 있는 alt도 제공한다.
색 외에도 라벨·선 모양으로 의미를 구분한다.
모바일에서 실제 글자 크기를 확인한다. viewBox 안의 13 단위 글자는 축소 후 13 CSS px를 보장하지 않는다.
작아지는 그림은 세로 배치·분할·라벨 축약으로 개선한다.

페이지 전체에 고정 폭·nowrap을 강제하지 않는다.
표와 코드는 각 컨테이너 안에서 가로 스크롤할 수 있게 한다.
긴 URL은 짧은 출처 앵커로 표시한다.
Markdown wrapper는 frontmatter, 범위·요약, HTML·실습·출처 링크를 소유한다.
기존에 별도 완성형 Markdown 본문이 있으면 보존하고 핵심 정의·코드·그림·출처의 의미를 동기화한다.
공용 assets를 참조하고 wiki/index.md와 README의 최신 링크를 갱신한다.

### 검증과 증거

변경 범위에 맞는 기존 검증 도구를 실행한다. Ch7 전용 검증 결과를 Ch8 결과로 보고하지 않는다.
`git diff --check`, frontmatter·날짜, 교재 절 대응, HTML 구조, anchor·상대경로,
SVG XML, 예제 구문·실행, 출처의 주장 지원 여부를 검사한다.
미작성 문서를 가리키는 wikilink는 AGENTS.md에 따라 허용하고 깨진 실제 파일 링크와 구분한다.

로컬 HTTP 서버와 실제 배포 URL에서 브라우저로 렌더링한다.
viewport는 320×568, 375×812, 812×375, 390×844, 430×932,
768×1024, 1024×768, 1440×900을 사용한다.
밝은·어두운 테마도 대표 모바일·데스크톱 크기에서 확인한다.

각 측정에 URL, viewport, 테마, 실행 날짜, 결과, screenshot 경로를 남긴다.

- 페이지 가로 넘침: document의 scrollWidth와 clientWidth 차이. 반올림 오차 허용 기준도 기록한다.
- 말줄임: `text-overflow: ellipsis` 선언 수와 실제 overflow가 있는 적용 요소 수를 각각 기록한다.
- clipping: 제목·문단·표·코드·navigation의 잘림을 스크린샷으로 확인한다. CSS 지표만으로 통과시키지 않는다.
- 세로 스크롤: 문서 높이와 실제 scrollY 변화. iframe이 있으면 해당 문서의 스크롤도 확인한다.
- SVG: 파일 로딩, 개수, alt/aria-label, 실제 렌더 폭과 라벨 가독성.
- 링크·동작: 목차·anchor·모바일 메뉴·키보드 탐색, 코드·표 내부 스크롤.
- 오류: console error와 실패한 network request, 누락 asset.

목표는 본문 실제 말줄임 0, 페이지 가로 넘침 0, 긴 문서의 세로 스크롤 정상 동작이다.
로컬·라이브 증거를 분리한다. 증상이 여러 개면 각 증상에 원인·조치·재측정값을 짝짓는다.
렌더링 도구를 사용할 수 없으면 해당 검증을 미실행으로 남긴다.

### GitHub와 Dokku

검증 후 이번 변경만 Conventional Commit으로 커밋하고 실제 작업 브랜치와 레포 정책에 맞춰 push한다.
원격 반영 SHA를 확인한다. force push와 무관한 변경 포함을 피한다.

배포 대상의 알려진 주소는 `https://raspberrypi.bobcat-fort.ts.net/csapp/`다.
실제 remote, Dokku app·브랜치, Dockerfile, nginx 설정, DNS, Tailscale·SSH 연결을 확인한다.
주소의 공개 접근 여부는 실제 요청으로 확인한다. 도메인 이름만으로 공개·인증 상태를 판단하지 않는다.
이전 DNS 실패는 해당 환경의 과거 관측으로 취급하고 현재 연결을 다시 확인한다.
로컬에 이미 허용된 연결 수단과 설정을 활용하되 자격 증명을 출력하지 않는다.

배포 전 기존 release와 서비스 상태를 기록한다.
기존 구성을 확인한 후 해당 remote와 refspec으로 배포한다.
`git push dokku main`은 실제 설정이 일치할 때만 사용한다.
배포 로그, release, container 상태를 확인하고 기존 서비스·데이터·/csapp/ 경로를 보존한다.

라이브 HTTP 200과 함께 이번 Ch8 본문·SVG가 배포본에 포함됐는지 확인한다.
가능하면 파일 내용 또는 해시를 비교한다. HTTP 200만으로 최신 배포 성공을 판정하지 않는다.
Ch8 HTML·Markdown·SVG, 기존 Ch6/Ch7의 접근, 모바일 렌더링과 서버 오류 로그를 확인한다.
연결에 실패하면 실제 오류와 시도한 경로를 보고하고, 검증된 설정에 맞는 남은 명령을 제공한다.
remote·app·브랜치를 확인하지 못했으면 placeholder로 표시한다.

### 산출물과 종료

학습 본문과 별도로 검증 보고서를 Markdown으로 저장한다.
레포의 기존 보고서 경로를 우선하고, wiki 아래 새 문서라면 frontmatter와 index 링크를 추가한다.
보고서에는 주장-근거 표, 실행한 명령과 결과, 환경, screenshot·측정 파일,
commit·push·release·라이브 상태와 미완료 항목을 기록한다.
시말서·회고는 학습 본문에 추가하지 않는다.

완료 조건을 충족하면 작업을 끝낸다. 새로운 근거 없이 검색·검증을 반복하지 않는다.
연결·권한·원본 부재로 진행할 수 없는 단계는 정확한 상태와 필요한 다음 조치로 마무리한다.
가능한 독립 작업은 계속 수행한다.

최종 채팅은 한두 문장과 학습 문서 링크, 검증 보고서 링크로 제한한다.
남은 배포·검증이 있으면 그 상태를 문장에 명시한다. 긴 학습 본문은 채팅에 복사하지 않는다.

## 실행 프롬프트 끝
