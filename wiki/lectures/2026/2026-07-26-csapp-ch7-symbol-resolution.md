---
type: Lecture
status: draft
date: 2026-07-26
topics:
  - compile-linking
related_to:
  - 0001-compile-linking
has:
  - chapter-7.6.1
  - chapter-7.6.2
  - chapter-7.6.3
artifact_path: chapter07/chapter-7.6.1.html
source_url: https://csapp.cs.cmu.edu/3e/errata.html
---

# 2026-07-26 CSAPP Ch7.6 Symbol Resolution

CSAPP 3판 §7.6 **Symbol Resolution** 강의노트.

- Markdown:
  [chapter07/chapter-7.6.1.md](../../../chapter07/chapter-7.6.1.md)
- 반응형 HTML:
  [chapter07/chapter-7.6.1.html](../../../chapter07/chapter-7.6.1.html)

## 다루는 내용

- strong symbol과 weak symbol의 교재 규칙
- 교재의 weak 모델과 ELF `STB_WEAK`/`SHN_COMMON`의 차이
- multiple definition과 symbol resolution
- C tentative definition
- `.data`, `.bss`, `COMMON`의 관계
- GCC 10의 `-fno-common` 기본값 변경
- compiler driver, `cc1`, `ld`, `lld`, `ldd`, libc의 역할
- `PT_INTERP`와 `DT_NEEDED`, 동적 로더의 실행 순서
- ASLR, PIC, PIE의 차이와 PIE, 비 PIE 주소 비교
- 7.6.2 정적 라이브러리와 필요한 archive member의 선택
- 7.6.3 `E`, `U`, `D` 집합과 라이브러리 입력 순서
- GNU ld와 LLD의 archive 탐색 차이
- 7.7 재배치로 이어지는 섹션 주소 배정과 참조 수정
- `gcc`, `clang`, `nm`, `readelf`, `objdump` 재현 실습

## 관련

- 토픽: [[compile-linking]]
- 선행 노트: [[0001-compile-linking]]
- 실습과 참고문헌: [chapter07](../../../chapter07/)
