---
type: Note
status: draft
date: 2026-10-25
related_to:
  - "[[2026-10-04-csapp-ch8-exceptional-control-flow]]"
  - "[[2026-10-25-csapp-ch8-learning-prompt]]"
---

# Ch8 검증 보고서

확인 기준일: 2026-10-25 KST. 검증 진행 중이며 최종 결과는 실행 후 기록한다.

## 원본 확인

GitHub origin/main에서 이전 보고의 `9dc088ecb019ad79dd246b06d159c26d1d80f290`과
Ch8 HTML·Markdown·6개 SVG를 확인했다. 로컬을 fast-forward하고 기존 프롬프트 변경을 보존했다.
라이브 `/csapp/`는 HTTP 200으로 응답했다. 초기 배포본의 파일 날짜는 2026-07-26이었다.
Pi는 Tailscale SSH 추가 인증 후 연결됐다. Dokku 0.38.20, app `csapp`, deploy branch `main`을 확인했다.

## 개선한 내용

- 교재 절 대응표에 §8.1·§8.2 상위 절과 북미판 인쇄 쪽수를 포함했다.
- ELI10 진입 설명, 사건 처리·주소 변환·runtime 계층, LAB와 적용 로드맵을 추가했다.
- 교재 모델과 Intel taxonomy, raw syscall과 C ABI, libc stream과 fd를 연결했다.
- shutdown 요청·사용자 공간 정리·kernel power-off의 역할과 조건을 구체화했다.
- Go go1.26.0 소스, OTP 29.1.2 NIF 문서, Rust Book의 근거를 연결했다.
- 기존 완성형 Markdown을 보존하고 동일 본문에서 HTML을 재생성하는 도구를 추가했다.
- SVG 6장을 모바일·dark theme에 맞춰 다시 작성하고 blocking I/O 비교 1장을 추가했다.
- 기존 Ch6/Ch7의 CSS 토큰·hero·sidebar·표·코드·Callout 계열을 재사용했다.

## 주요 주장과 근거

| 주장 | 판정 | 근거와 조건 |
|---|---|---|
| Ch8가 GitHub main에 존재 | 확인 | origin/main 9dc088e와 실제 파일 확인 |
| 당시 DNS 오류로 현재도 배포 불가 | 현재 상태 갱신 | 이번 요청에서 공개 HTTPS 200. Tailscale 추가 인증 후 SSH 연결 |
| 3판 교재 범위·쪽수 | 확인 | 공식 preface3e.pdf p.xiv. §8.1 p.723~§8.2.4 p.734 |
| Interrupt/trap/fault/abort | 조건부 일치 | CSAPP 그림·CMU 자료, Intel 093 Vol.3 §7.5~7.6. 용어 범위 구분 |
| #DE 조건 | 확인 | Intel 093 Event 0, divisor 0 또는 quotient overflow |
| x86-64 syscall arg4=r10 | 확인 | syscall(2) Architecture calling conventions |
| C ABI arg4=rcx | 확인 | AMD64 psABI e1ce098의 low-level-sys-info.tex, INTEGER 인자 배치 |
| exit_group 비복귀 | 확인 | exit_group(2) DESCRIPTION·RETURN VALUE |
| maps의 r/w/x/s/p | 확인 | proc_pid_maps(5), man-pages 6.19 |
| PFN 0의 해석 | 조건부 | kernel pagemap, Linux 4.2+ CAP_SYS_ADMIN 제한. 물리 주소 확정 불가 |
| Go G/M/P·P 회수 | 확인 | go1.26.0 HACKING.md, proc.go의 entersyscallblock·retake·netpoll |
| 긴 NIF·dirty 분류 | 확인 | OTP 29.1.2 erl_nif의 Long-running NIFs·Dirty NIFs |
| power-off의 정리·권한 | 보완 | reboot(2) CAP_SYS_BOOT·sync·플랫폼 조건. 실제 전원 제어 미실행 |

[출처 HTTP 검사](../../../validation/ch8/2026-10-25/sources.json).
Intel 안내 페이지는 web 도구로 열람됐으나 직접 HTTP 검사에서는 403이었다.
공식 PDF는 직접 HTTP 200으로 내려받아 revision 093, September 2026과 관련 절을 확인했다.
레포 README의 교재 PDF 주소는 이번 환경에서 PDF 본문을 반환하지 않았다.
교재 범위는 공식 목차·정오표·공식 그림과 CMU 교재 동반 자료로 대조했다.
강의 녹취 원본은 대조하지 않았으며 추정 발언을 추가하지 않았다.

## LAB 실행

실행일: 2026-10-25 KST. macOS ARM64 host의 OrbStack Linux,
Debian bookworm-slim, GCC 12.2.0, strace 6.1.
Kernel: `7.2.12-orbstack-00394-gbecdd6130068`.

| 검사 | ARM64 native | x86-64 에뮬레이션 |
|---|---|---|
| -Wall -Wextra -Werror 컴파일 | PASS | PASS |
| hello·flows·maps 실행 | PASS | PASS |
| write 13바이트·exit_group trace | PASS | syscall trace 불완전 |
| 부모·자식 각 3줄 | PASS | PASS |
| rw-p mapping·pagemap 읽기 | PASS, present=1 PFN=0 | mapping introspection 미실행 |

- [ARM64 전체 출력](../../../validation/ch8/2026-10-25/lab-arm64.txt)
- [x86-64 컴파일·실행](../../../validation/ch8/2026-10-25/lab-amd64.txt)
- [x86-64 trace 불완전 기록](../../../validation/ch8/2026-10-25/lab-amd64-strace-incomplete.txt)
- [재현 명령](../../../examples/0002-exceptional-control-flow/README.md)

x86-64 에뮬레이션에서 strace가 x32/64 mode 전환 메시지를 반복하며 필터된 syscall을 표시하지 않았다.
전체 LAB의 trace 검사는 실패로 취급했다. ARM64의 관측을 x86-64 syscall register 관측으로 보고하지 않았다.
전원 종료·host 재부팅은 실행하지 않았다.

## 구조·문체·회귀 검사

- `node tools/ch8/verify.mjs`: frontmatter·날짜, 고유 ID 55개, 교재 소절 7개, 상대 링크, 공용 SVG 7장, 금지 문형, HTML/Markdown 동기화 PASS.
- `node chapter07/verify-html.mjs`: 기존 Ch7 HTML·Markdown·링크 검사 PASS.
- HTML5 parser 오류 0, SVG XML parse 7개 성공: [구조 결과](../../../validation/ch8/2026-10-25/structure.json).
- `git diff --check`: PASS.

## 로컬 브라우저 검증

설치된 Google Chrome을 Playwright로 headless 실행했다.
320×568, 375×812, 812×375, 390×844, 430×932, 768×1024, 1024×768, 1440×900의 light theme,
375×812·1440×900의 dark theme를 검증했다.

| 측정 | 결과 |
|---|---|
| 페이지 scrollWidth-clientWidth | 모든 조건 0px. 허용 오차 1px |
| ellipsis CSS 적용 요소 | 모든 조건 0 |
| 실제 truncateCount | 모든 조건 0 |
| 실제 scrollY 이동 | 모든 조건 700px |
| anchor 이동·대상 | 정상, 깨진 anchor 0 |
| SVG | 모두 7개 로딩·XML·aria-label 확인 |
| 375px의 SVG 렌더 폭 | 317px, viewBox 360 대비 축소. 최소 19단위 라벨은 약 16.7 CSS px |
| iframe | 0개. 최상위 문서에서 측정 |
| 브라우저 오류·실패 요청 | 최종 검사 0 |

초기 favicon 404를 inline favicon으로 수정했다. 화면 검토에서 중복 제목을 발견해 renderer의
선행 공백 처리를 수정하고 다시 렌더링했다. 본문 표의 내부 가로 스크롤은 허용하고 페이지 넘침과 구분했다.
전체 결과와 도판 스크린샷은 [local metrics](../../../validation/ch8/2026-10-25/local/metrics.json)에 있다.
[모바일 상단](../../../validation/ch8/2026-10-25/local/375x812-light.jpg),
[모바일 가로](../../../validation/ch8/2026-10-25/local/812x375-light.jpg),
[실행 타임라인](../../../validation/ch8/2026-10-25/local/figure-3-light.jpg),
[dark Go 도판](../../../validation/ch8/2026-10-25/local/figure-7-dark.jpg)을 직접 확인했다.

## Git과 배포

문서·로컬 검증은 완료했다. GitHub push, Dokku 배포, 라이브 내용 일치와 브라우저 검증은
이 보고서의 다음 갱신에서 실제 결과를 기록한다.

배포 전 app은 `csapp`, Dokku 0.38.20, branch `main`, container `e1970ef60d3`였다.
서버 bare repo의 main은 `93a22e8b0187b0b0a632412685744d7a1da7e8c0`였다.
기존 `pre-receive` hook이 `dokku git-hook csapp`을 호출하는 것을 확인했다.

## 남은 범위

- native Linux/x86-64의 register·strace 관측은 미실행이다. ABI는 공식 자료로 확인했다.
- Go·Erlang·Rust runtime 동작·성능 비교 실험은 미실행이다.
- 스크린리더 실기기·실제 휴대폰·프린터 출력 검증은 미실행이다.
