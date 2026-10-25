---
type: Note
status: done
date: 2026-10-25
related_to:
  - "[[2026-10-04-csapp-ch8-exceptional-control-flow]]"
  - "[[2026-10-25-csapp-ch8-learning-prompt]]"
---

# Ch8 검증 보고서

확인 기준일: 2026-10-25 KST. 아래는 각 수정 시점에 실제 실행한 검증 기록이다.

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

[출처 HTTP 검사](../../validation/ch8/2026-10-25/sources.json).
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

- [ARM64 전체 출력](../../validation/ch8/2026-10-25/lab-arm64.txt)
- [x86-64 컴파일·실행](../../validation/ch8/2026-10-25/lab-amd64.txt)
- [x86-64 trace 불완전 기록](../../validation/ch8/2026-10-25/lab-amd64-strace-incomplete.txt)
- [재현 명령](../../examples/0002-exceptional-control-flow/README.md)

x86-64 에뮬레이션에서 strace가 x32/64 mode 전환 메시지를 반복하며 필터된 syscall을 표시하지 않았다.
전체 LAB의 trace 검사는 실패로 취급했다. ARM64의 관측을 x86-64 syscall register 관측으로 보고하지 않았다.
전원 종료·host 재부팅은 실행하지 않았다.

## 구조·문체·회귀 검사

- `node tools/ch8/verify.mjs`: frontmatter·날짜, 고유 ID 55개, 교재 소절 7개, 상대 링크, 공용 SVG 7장, 금지 문형, HTML/Markdown 동기화 PASS.
- `node chapter07/verify-html.mjs`: 기존 Ch7 HTML·Markdown·링크 검사 PASS.
- HTML5 parser 오류 0, SVG XML parse 7개 성공: [구조 결과](../../validation/ch8/2026-10-25/structure.json).
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
전체 결과와 도판 스크린샷은 [local metrics](../../validation/ch8/2026-10-25/local/metrics.json)에 있다.
[모바일 상단](../../validation/ch8/2026-10-25/local/375x812-light.jpg),
[모바일 가로](../../validation/ch8/2026-10-25/local/812x375-light.jpg),
[실행 타임라인](../../validation/ch8/2026-10-25/local/figure-3-light.jpg),
[dark Go 도판](../../validation/ch8/2026-10-25/local/figure-7-dark.jpg)을 직접 확인했다.

## Git과 배포

문서 payload 커밋 `a6e1812`를 GitHub `main`에 push하고 기존 Dokku remote로 배포했다.
Dokku build `mvnl9imxumykhi`는 `succeeded`, exit code 0으로 종료했다.
새 container `fb798948f14b`의 실행과 기본 uptime·port healthcheck 통과를 확인했다.
정적 사이트의 release는 이 build ID·image·container로 식별한다. 별도 `releases:list` 명령은 이 Dokku 설치에 없다.

- 라이브: [Ch8 학습 문서](https://raspberrypi.bobcat-fort.ts.net/csapp/wiki/lectures/2026/2026-10-04-csapp-ch8-exceptional-control-flow.html)
- [배포 상태](../../validation/ch8/2026-10-25/deployment.json)
- [라이브 파일 해시](../../validation/ch8/2026-10-25/deployment-content.json): Ch8 HTML·Markdown·SVG 7장, Ch6·Ch7 HTML, README 등 12개 모두 HTTP 200과 SHA-256 일치.
- [라이브 브라우저 측정](../../validation/ch8/2026-10-25/live/metrics.json): 로컬과 같은 10개 viewport·theme 조건에서 가로 넘침 0·실제 말줄임 0·브라우저 오류 0·세로 스크롤 700px·SVG 7장·anchor 이동 정상.
- [라이브 모바일 화면](../../validation/ch8/2026-10-25/live/375x812-light.jpg)을 직접 확인했다.

`/csapp/` 경로를 유지했고 기존 Ch6·Ch7도 원본 해시와 일치했다.
배포 진행 중 이전 container가 응답한 짧은 구간에는 Ch8 404를 관측했다.
proxy 전환 후 재검사에서 12개 대상이 모두 정상 응답했다.
검사 후 서버 로그 30줄에서 error·crit·emerg는 없었다.
시작 로그의 기존 `charset_types` 중복 `text/html` 경고는 설정에서 중복 항목을 제거했고,
동일 nginx image의 임시 container에서 `nginx -t` 성공을 확인했다.
이 설정과 라이브 검증 증거는 후속 커밋·동기화 배포에 포함한다.

이 보고서의 배포 기록은 위 payload를 직접 검증한 결과다.
후속 보고서·증거 커밋의 SHA와 최종 배포 결과는 Git 이력 및 Dokku build 이력에서 확인한다.

배포 전 app은 `csapp`, Dokku 0.38.20, branch `main`, container `e1970ef60d3`였다.
서버 bare repo의 main은 `93a22e8b0187b0b0a632412685744d7a1da7e8c0`였다.
기존 `pre-receive` hook이 `dokku git-hook csapp`을 호출하는 것을 확인했다.

## 남은 범위

- x86-64 syscall·mapping LAB은 아래 full-system QEMU 검증으로 완료했다. syscall 인자 레지스터 직접 추적과 실제 하드웨어 성능 비교는 별도 범위다.
- Go·Erlang·Rust runtime 동작·성능 비교 실험은 미실행이다.
- 스크린리더 실기기·실제 휴대폰·프린터 출력 검증은 미실행이다.

## x86-64 추가 검증

판단 과정과 재현 방법은 [Ch8 본문의 실습 디버깅 사례](../lectures/2026/2026-10-04-csapp-ch8-exceptional-control-flow.html#lab-debugging)에 정리했다.
통합 전 별도 렌더링의 측정 기록은 [초기 측정·스크린샷](../../validation/ch8/2026-10-25/debug-note/metrics.json)에 보존했다. 최신 본문 통합 검증은 아래 기록을 따른다.

사용자 지적 후 `orb`의 기존 `ubuntu` amd64 환경을 확인했다.
Ubuntu 25.04에 GCC 14.2.0·strace 6.14를 설치하고
`orb -m ubuntu sh examples/0002-exceptional-control-flow/verify.sh`를 실행했다.
`hello, world` 출력은 성공했지만 syscall trace가 비어 전체 LAB은 exit 1이었다.
별도 `strace /bin/echo hello`에서도 x32/64 mode 전환이 반복되고 write·exit_group이 표시되지 않았다.
[Orb 실행 기록](../../validation/ch8/2026-10-25/lab-orb-amd64.txt).

이어 설치된 QEMU로 **x86-64 커널과 userspace를 함께 실행**했다.
Orb의 전역 Rosetta 설정은 변경하지 않았다.

| 항목 | 실제 관측 |
|---|---|
| QEMU | 11.1.2, TCG full-system, q35·CPU max |
| Kernel | Linux 6.18.58-0-lts x86_64 |
| Userspace | Debian bookworm, GCC 12.2.0, strace 6.1 |
| 자원 | virtual CPU 2개·RAM 1536 MiB·network 없음 |
| write | fd=1, 요청 13바이트, 반환 13 |
| exit_group | status=0, 비복귀 trace |
| 흐름 | 부모·자식 각 3줄·순서 0→1→2 |
| mapping | private anonymous rw-p |
| pagemap | present=1, PFN=255844. 해당 guest root 권한에서 관측한 값 |
| 전체 결과 | `CSAPP_QEMU_EXIT=0`, PASS |

- [전체 x86-64 로그](../../validation/ch8/2026-10-25/lab-x86-system.txt)
- [환경·kernel hash](../../validation/ch8/2026-10-25/x86-system-environment.json)
- 재현: 레포 root에서 `python3 tools/ch8/qemu-lab.py`. 저장한 재현 도구를 다시 실행해 PASS를 확인했다. [재실행 로그](../../validation/ch8/2026-10-25/lab-x86-system-reproduced.txt).
- 재현 도구: Docker, QEMU, Python 3, curl, tar, cpio, gzip.

LAB이 끝나면 QEMU guest process와 임시 rootfs를 정리한다. host 재부팅·전원 종료는 수행하지 않는다.
이 결과로 기존 x86-64 syscall LAB의 미검증 상태를 해소했다.

## Ch8 본문 통합 검증

디버깅 사례의 관측·가설·실험·선택 근거·한계·재현 절차를 Ch8의 LAB 내부 하위 절에 통합했다.
기존 별도 노트 URL은 Ch8의 같은 절로 이동한다. wiki 목차도 Ch8를 학습 진입점으로 유지한다.

- HTML/Markdown 동기화, 고유 ID 88개·상대 링크·교재 소절·공유 SVG 9장 검사 PASS.
- HTML5 parser 오류 0, SVG XML 9개 parse 성공.
- 8개 viewport light와 대표 모바일·데스크톱 dark의 총 10개 조건에서 가로 넘침 0·말줄임 0·브라우저 오류 0·scrollY 700px·SVG 9장 로딩.
- [통합 본문 측정·스크린샷](../../validation/ch8/2026-10-25/integrated-local/metrics.json).
- 본문에는 학습 설명과 증거를 두고, 배포·편집 결과는 이 보고서에 기록했다.

## 문서 위치·탐색·디자인 검증

- canonical 문서는 기존 `wiki/lectures/2026/`에 유지했다. 디버깅 사례는 LAB 내부 `#lab-debugging`이며 이전 한글 앵커와 별도 노트 주소도 연결된다.
- `/`는 강의 진입 목차, `/wiki/`는 filesystem 기반 Wiki 목차를 제공한다. nginx는 실제 파일 경로를 사용하며 없는 경로는 404로 응답한다.
- Ch6·Ch7·Ch8 간 이동, breadcrumb, 강의 목록, 현재 문서 목차를 분리했다. 고정 앵커의 직접 열기·새로고침·뒤로/앞으로 이동과 테마 저장을 검증했다.
- 공통 색상·폰트·간격·읽기 폭 토큰을 `wiki/assets/site/tokens.css`에 정의했다. 본문은 최대 46rem, 모바일은 화면에 맞춰 축소되며 표·코드는 내부에서 스크롤한다.
- 모바일의 코드 스크롤 영역에 키보드 포커스를 추가했다. 375·812·1440 light 및 375·1440 dark에서 axe WCAG A/AA 자동 검사 위반 0. 자동 검사는 수동 접근성 평가 전체를 대체하지 않는다.
- [라우팅·접근성 결과와 스크린샷](../../validation/ch8/2026-10-25/navigation/results.json).
- [10개 화면 조건 측정과 스크린샷](../../validation/ch8/2026-10-25/docs-layout/metrics.json). 페이지 가로 넘침·말줄임·브라우저 오류 0, SVG 9장 로딩, 세로 스크롤 확인.

## 기존 강의 디자인에 맞춘 재조정

Ch6·Ch7을 1440px 실제 화면으로 비교하고 Ch7의 hero·범위 배지·파란 강조색·옅은 격자 배경·둥근 섹션 패널·왼쪽 study rail을 Ch8에 적용했다. 본문을 세 열에서 목차와 본문의 두 열로 조정했다. 코드 블록은 어두운 배경과 별도 구문 색상 토큰을 사용한다. 경로·고정 앵커·LAB 내부 디버깅 사례 위치는 유지한다.

- [10개 화면 조건과 스크린샷](../../validation/ch8/2026-10-25/lecture-style/metrics.json): 가로 넘침·말줄임·브라우저 오류 0, SVG 9장, 스크롤 확인.
- [탐색·테마·접근성 검사](../../validation/ch8/2026-10-25/lecture-style-navigation/results.json): 5개 화면·테마 조건의 axe WCAG A/AA 자동 검사 위반 0.
- HTML parser 오류 0, Ch8 본문 동기화·Ch7 기존 검증 통과.

## 교재 목차 재대조와 학습 흐름 정리

### 공식 자료와 대조한 결과

공식 [3판 목차·서문 PDF](https://csapp.cs.cmu.edu/3e/pieces/preface3e.pdf)의 인쇄 xiv쪽(PDF 14쪽)을 확인했다. 절 번호와 p.723~734 표기는 일치했다. HTML/Markdown에서 §8.2.3과 §8.2.4의 heading level이 잘못되어 있어 §8.2 아래로 복원했다. §8.1.3의 제목도 공식 명칭을 사용한다. [구조·쪽수 대조 기록](../../validation/ch8/2026-10-25/textbook-audit.json).

교재 본문 범위는 §8.1~§8.2.4다. 이후 §8.2.5~§8.8은 미작성 범위로 명시했다. syscall ABI·Linux mapping은 공식 보충, `fork` LAB은 §8.4 선행 실습, Go·BEAM·Rust는 본문 뒤의 확장 자료로 구분했다. CMU [동반 강의](https://www.cs.cmu.edu/afs/cs/academic/class/15213-m22/www/lectures/14-ecf-procs.pdf)의 슬라이드 15~17에 근거해 page fault·잘못된 메모리 참조 사례를 보강했고, [공식 정오표](https://csapp.cs.cmu.edu/3e/errata.html)의 p.735 stack 주소·`%rsp` 정정을 반영했다.

### 학습 동선과 코드

- 홈페이지의 `/csapp/`는 `/csapp/wiki/`로 이동한다. 중복 진입 목차를 없애고 Ch6→Ch7→Ch8 순서와 실제 교재 범위를 표시한다.
- Wiki의 개념·학습 노트는 같은 디자인의 HTML로 열린다. 파일명 대신 문서 제목을 표시한다. 작성·검증 기록은 접을 수 있다.
- C·Linux·ELF 실습에는 설명과 소스 뷰어를 제공한다. 학습 링크가 서버 디렉터리 목록으로 빠지지 않도록 했다.
- 코드 블록은 highlight.js 11.12.0의 GitHub Dark 전체 구문 테마를 함께 배포한다. 언어 배지·복사 버튼을 제공하고 `asm`을 `x86asm`으로 매핑한다. 실행 출력은 plaintext로 유지한다. [공식 사용 문서](https://highlightjs.readthedocs.io/en/latest/readme.html), [GitHub 테마 소스](https://github.com/highlightjs/highlight.js/blob/11.12.0/src/styles/github-dark.css).
- [학습 동선·절 계층·클립보드·페이지 검사](../../validation/ch8/2026-10-25/learning-flow/results.json): 실제 클릭·heading parent·색상 차이·복사 내용 일치를 검사했다. Wiki·노트·개념·소스 뷰어 5종의 모바일 light/데스크톱 dark 총 10개 조건에서 가로 넘침·말줄임·접근성 자동 검사 위반 0.
- [Ch8 화면 검사](../../validation/ch8/2026-10-25/textbook-layout/metrics.json): 화면·테마 10개 조건을 렌더링하고 SVG 9장·스크롤·앵커를 검사했다.
