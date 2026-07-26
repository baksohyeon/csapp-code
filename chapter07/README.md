# CSAPP Chapter 7.6.1 — Duplicate Symbol Names

2026-07-26 스터디용 강의노트와 재현 실습이다. CSAPP 3판의 정확한 절 제목은
**7.6.1 How Linkers Resolve Duplicate Symbol Names**이며, 교재의 설명을 현대 GCC와
ELF 관점에서 다시 검증한다.

## 읽는 순서

1. [chapter-7.6.1.html](chapter-7.6.1.html) — 본 강의노트
2. [references.md](references.md) — 정본과 공식 문서, 조사 범위
3. [examples/](examples/) — 사례별 C 소스
4. [results/verified-linux-aarch64.txt](results/verified-linux-aarch64.txt) — 실제 검증 출력

`figures/`에는 강의노트용 SVG 원본이 있다. 외부 CDN이나 네트워크 요청 없이 HTML과
로컬 자산만으로 열 수 있다.

## 핵심 결론

- 교재의 strong/weak 규칙은 중복 이름을 이해하는 유용한 **링커 모델**이다.
- 하지만 교재가 “weak”라고 부르는 `int x;`는 현대 ELF에서 보통 실제
  `STB_WEAK`가 아니다. `-fcommon`에서는 `STB_GLOBAL + SHN_COMMON`으로 나타난다.
- GCC 10부터 C의 기본값이 `-fno-common`이 되어, 여러 번역 단위의 `int x;`는
  기본적으로 링크 오류가 된다.
- `COMMON`은 `.bss`와 같은 입력 섹션이 아니다. 아직 저장 공간이 할당되지 않은 특별한
  심볼 상태이고, 최종 링크 때 보통 출력 `.bss`에 공간을 배정받는다.
- 올바른 전역 변수 패턴은 헤더에 `extern` 선언을 두고 정확히 한 `.c` 파일에 정의를 두는
  것이다.

## 실습 실행

현재 호스트가 macOS이므로 ELF 결과는 Docker 안의 Linux에서 검증한다.

```bash
cd chapter07
./verify-in-docker.sh
```

스크립트는 Ubuntu 24.04 이미지를 만들고 아래 항목을 검사한다.

- strong + strong
- strong + COMMON (`-fcommon`)
- COMMON + COMMON (`-fcommon`)
- 중복 함수 정의
- tentative definition의 `COMMON`/`.bss` 배치
- GCC 10 이전/이후 의미를 재현하는 `-fcommon`/`-fno-common`
- 서로 다른 타입의 중복 이름
- `static` 내부 연결
- 실제 ELF `STB_WEAK`
- GNU ld와 lld의 진단 및 입력 순서 관찰

빌드 산출물은 `examples/build/`에 생성되며 Git에서 제외한다. 검증 환경과 텍스트 출력은
`results/verified-linux-aarch64.txt`에 기록한다. `.o` 파일은 CPU/플랫폼 의존 산출물이므로
추적하지 않고 위 명령으로 재생성한다.

## HTML 구조 검사

```bash
node chapter07/verify-html.mjs
```

필수 학습 블록, 내부 fragment link, 로컬 자산, 이미지 대체 텍스트, 외부 CSS/JS 의존성
여부를 검사한다.

## 문서 상태

이 문서는 Chapter 7 전체 시리즈의 첫 초안이다. 7.1–7.5의 선행 개념은 `REMIND`로 필요한
만큼만 복원하고, 다음 절인 7.6.2 정적 라이브러리 탐색은 범위 밖으로 남겨 두었다.
