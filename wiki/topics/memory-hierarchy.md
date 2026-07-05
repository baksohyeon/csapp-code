---
type: Topic
status: living
related_to:
  - cache-locality
---

# 메모리 계층 구조 (Memory Hierarchy)

레지스터 → 캐시(L1/L2/L3) → 메인 메모리 → 디스크로 이어지는 저장 장치 계층.
위로 갈수록 빠르고 작고 비싸며, 계층 구조가 성립하는 이유는 프로그램의
지역성([[cache-locality]]) 때문이다. CSAPP Chapter 6의 중심 주제.

## 계층별 역할

레지스터는 CPU 내부의 임시 값(루프 변수, 누산값)을 담고, L1은 코어에 가장 가까운 작은
캐시, L2/L3는 점점 크고 느려지는 중간 캐시다. 메인 메모리(DRAM)는 캐시보다 훨씬 느리고,
SSD/디스크는 그보다 더 느리다. 캐시는 메인 메모리의 모든 값을 들고 있는 게 아니라, 곧 쓸
가능성이 높은 block 일부를 가까운 곳에 복사해 둔다. "무엇을 가까이 둘 것인가"는 프로그램의
지역성이 결정한다. working set이 L1에 들어가면 매우 빠르고, L2/L3로 밀리면 느려지고,
메인 메모리까지 내려가면 더 느려진다 — 이것이 memory mountain의 성능 계단이다.

## 캐시 조직: S, E, B, tag

캐시는 set, line, block으로 조직되고, 주소는 `[ tag | set index | block offset ]`으로
해석한다. S = 2^s는 set 개수, E는 set당 line 개수(associativity), B = 2^b는 block(cache
line) 크기, 데이터 용량은 C = S × E × B다. E = 1이면 direct-mapped, S = 1이면 fully
associative. hit 판정은 tag 일치만으로 끝나지 않고 valid bit가 켜져 있어야 한다.
쓰기에는 write-through/write-back, write-allocate/no-write-allocate policy가 붙는다.

## Hit, Miss, AMAT

miss는 세 종류로 분류한다. compulsory(cold) miss는 처음 보는 block이라 나는 miss,
capacity miss는 working set이 캐시보다 커서 밀려나는 miss(blocking으로 완화),
conflict miss는 공간이 남아도 같은 set에 몰려서 나는 miss(padding, associativity 증가로
완화)다. 평균 접근 비용은 AMAT = hit time + miss rate × miss penalty. hit time 1 cycle,
miss penalty 100 cycles라면 97% hit는 평균 4 cycles, 99% hit는 2 cycles — miss rate가
조금만 줄어도 체감 성능이 크게 바뀌는 이유다.

## 자주 혼동하는 포인트

- 캐시는 byte 단위가 아니라 block(보통 64B) 단위로 데이터를 가져온다. miss 한 번에
  연속된 B byte가 통째로 올라온다.
- capacity miss와 conflict miss의 구분: 전자는 캐시 전체가 부족한 것, 후자는 특정 set만
  몰려서 나는 것이다. 특정 stride가 같은 set만 반복해서 때리면 conflict miss다.
- C = S × E × B는 데이터 용량이며 valid bit, tag 같은 metadata는 제외한 값이다.

## 관련 문서

- 강의: [[2026-07-05-csapp-ch6-memory-hierarchy]]
- 하위 토픽: [[cache-locality]]
