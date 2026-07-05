---
type: Lecture
status: done
date: 2026-07-05
topics:
  - memory-hierarchy
  - cache-locality
artifact_path: wiki/lectures/2026/2026-07-05-csapp-ch6-memory-hierarchy.html
source_url: https://i.hyeon.me/csapp
---

# 2026-07-05 CSAPP Ch6 Memory Hierarchy 통합 강의노트

CSAPP Chapter 6 (Memory Hierarchy) 통합 강의노트.
1차 자료는 같은 이름의 HTML 원본이다:
[2026-07-05-csapp-ch6-memory-hierarchy.html](2026-07-05-csapp-ch6-memory-hierarchy.html)

## 다루는 내용

- 메모리 계층 구조 (registers → L1/L2/L3 → main memory → SSD/디스크) — [[memory-hierarchy]]
- 캐시 구조 (S/E/B, tag)와 hit/miss 판정, AMAT
- 지역성(temporal/spatial), cache line·stride·working set — [[cache-locality]]
- Memory mountain (size/stride 축 읽는 법)
- 행렬곱 6가지 loop order 비교와 blocking/tiling (CSAPP source-backed matrix visuals 포함)
- 언어별 메모리 레이아웃: JS(V8/Bun)·Python/Ruby·Java class 배열, AoS/SoA
- 실전 체크리스트, 연습 문제, 암기 카드

## 관련

- 토픽: [[memory-hierarchy]], [[cache-locality]]
