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
artifact_path: chapter07/chapter-7.6.1.html
source_url: https://csapp.cs.cmu.edu/3e/errata.html
---

# 2026-07-26 CSAPP Ch7.6.1 Duplicate Symbol Names

CSAPP 3판 §7.6.1 **How Linkers Resolve Duplicate Symbol Names** 강의노트.

1차 HTML:
[chapter07/chapter-7.6.1.html](../../../chapter07/chapter-7.6.1.html)

## 다루는 내용

- strong symbol과 weak symbol의 교재 규칙
- 교재의 weak 모델과 ELF `STB_WEAK`/`SHN_COMMON`의 차이
- multiple definition과 symbol resolution
- C tentative definition
- `.data`, `.bss`, `COMMON`의 관계
- GCC 10의 `-fno-common` 기본값 변경
- GNU ld와 lld의 실제 진단
- `gcc`, `clang`, `nm`, `readelf`, `objdump` 재현 실습

## 관련

- 토픽: [[compile-linking]]
- 선행 노트: [[0001-compile-linking]]
- 실습과 참고문헌: [chapter07](../../../chapter07/)
