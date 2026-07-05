---
type: Topic
status: living
related_to:
  - memory-hierarchy
---

# 캐시와 지역성 (Cache & Locality)

시간 지역성(temporal locality)과 공간 지역성(spatial locality), 그리고 이를 활용하는
캐시 구조. 행렬 순회 순서(row-major vs column-major)에 따라 성능이 크게 달라지는
이유가 대표적인 예다.

## 두 가지 지역성

temporal locality는 최근에 쓴 데이터를 곧 다시 쓰는 성질, spatial locality는 방금 쓴
주소 근처를 곧 쓰는 성질이다. 캐시는 이 습관을 믿고 설계된다. 캐시는 miss가 나면 cache
line(보통 64B, double 8개) 단위로 데이터를 가져오므로, stride-1 접근은 가져온 line을
알뜰하게 쓰고 큰 stride는 line을 가져와도 일부만 쓰고 버린다. memory mountain(read
throughput을 size × stride 함수로 그린 지형)에서 size 축은 주로 temporal locality를,
stride 축은 주로 spatial locality를 흔든다. 한 줄 요약: 작은 working set은 temporal
locality를 살리고, stride-1은 spatial locality를 살린다.

## 행렬 곱셈과 loop order

C의 2차원 배열은 row-major라서 두 번째 인덱스가 변하면 연속 접근, 첫 번째 인덱스가
변하면 행 크기만큼 점프다. `C[i][j] += A[i][k] * B[k][j]`의 여섯 가지 루프 순서는 가장
안쪽 루프 변수(세 글자 중 마지막 글자)로 성능이 갈린다.

| 안쪽 루프 | 순서 | inner-loop miss 근사 | 판정 |
|-----------|------|---------------------|------|
| j | ikj, kij | 약 0.5 misses/iter | 빠름 — B, C가 행 방향 |
| k | ijk, jik | 약 1.25 misses/iter | 중간 — A는 행 방향, B는 열 방향 |
| i | jki, kji | 약 2.0 misses/iter | 느림 — A, C가 모두 열 방향 |

판정법: 안쪽 루프 변수가 각 배열의 두 번째 인덱스면 행 방향(좋음), 첫 번째 인덱스면
열 방향 점프(나쁨)다. 바깥 두 루프만 바꾼 쌍(ijk/jik 등)은 안쪽 접근 패턴이 같아서
성능 분류도 같다. 이 miss 수치는 32B block, 8B double, 충분히 큰 n 가정의 근사이며,
실제 값은 prefetching, TLB, 컴파일러 최적화에 따라 달라진다.

working set이 캐시보다 큰 행렬은 blocking(tiling)으로 L × L 타일 단위로 쪼개 캐시 안에서
재사용을 극대화한다. 타일은 클수록 재사용이 늘지만 A/B/C 세 타일이 캐시에 함께 들어가야
하므로 대략 `3 * L^2 * sizeof(double) < usable cache bytes`를 본다. capacity miss를
줄이는 대표 기법이다.

## 자주 혼동하는 포인트

- ijk 표기에서 마지막 글자가 가장 안쪽 루프다. 성능은 거의 이 글자가 결정한다.
- ikj/kij는 안쪽에서 C[i][j]를 반복 갱신해 store가 더 많은데도 빠른 쪽이다. B와 C를
  행 방향으로 훑는 cache line 활용이 store 횟수보다 크게 지배하기 때문이다.
- CSAPP의 mountain.c에서 stride는 byte가 아니라 원소 개수 단위다. double 배열의
  stride 8은 64B 간격이다.
- 값이 연속으로 놓이는 건 C 배열, Java primitive 배열, typed array 같은 값 배열이다.
  Java의 객체 배열(`Player[]`)이나 Python list는 reference 배열이라 한 번 더 따라가야
  하고(reference chasing), 지역성이 나빠진다. hot field만 primitive 배열로 분리하는
  SoA가 해결 방향이다.

## 관련 문서

- 강의: [[2026-07-05-csapp-ch6-memory-hierarchy]] — 행렬 접근 패턴 시각화 포함
- 상위 토픽: [[memory-hierarchy]]
