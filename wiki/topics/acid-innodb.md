---
type: Topic
status: living
related_to:
  - tree-index
---

# ACID와 InnoDB

트랜잭션의 ACID(Atomicity, Consistency, Isolation, Durability) 속성과
MySQL의 기본 스토리지 엔진인 InnoDB. 인덱스 구조([[tree-index]])와 함께
DB 내부 동작을 이해하는 축.

## ACID 네 가지 속성

Atomicity(원자성)는 트랜잭션이 전부 반영되거나 전부 취소되는 성질이다. 계좌 이체에서
출금만 되고 입금이 안 되는 중간 상태를 남기지 않는다. Consistency(일관성)는 트랜잭션
전후로 제약 조건(외래 키, unique, 잔액 ≥ 0 같은 규칙)이 깨지지 않는 성질이다.
Isolation(격리성)은 동시에 실행되는 트랜잭션들이 서로의 중간 상태를 보지 못하게 하는
성질이고, Durability(지속성)는 커밋된 결과가 장애(전원 차단 등) 후에도 살아남는 성질이다.

## InnoDB가 ACID를 구현하는 방식

InnoDB는 MySQL의 기본 스토리지 엔진으로, 트랜잭션과 ACID를 지원한다는 점이 핵심
정체성이다(구형 엔진 MyISAM은 트랜잭션을 지원하지 않는다).

- 원자성: undo log. 변경 전 값을 기록해 두었다가 롤백 시 되돌린다.
- 지속성: redo log(WAL, Write-Ahead Logging). 데이터 페이지를 디스크에 쓰기 전에
  변경 내역을 로그에 먼저 기록해, 장애 후 재시작 시 커밋된 변경을 재적용한다.
- 격리성: 잠금(lock)과 MVCC. MVCC는 undo log의 이전 버전을 이용해 읽기 트랜잭션이
  잠금 없이 일관된 스냅샷을 읽게 한다. 격리 수준은 READ UNCOMMITTED → READ COMMITTED →
  REPEATABLE READ(InnoDB 기본값) → SERIALIZABLE 순으로 강해진다.
- 일관성: 별도 장치가 있다기보다 위 셋과 제약 조건 검사가 지켜질 때 따라오는 결과 속성이다.

## 클러스터드 인덱스 — tree-index와의 연결

InnoDB 테이블 자체가 PK 기준 B+ tree다(clustered index). 리프 노드에 행 데이터 전체가
들어 있어, PK 조회는 트리 탐색 한 번으로 끝난다. 세컨더리 인덱스의 리프에는 행 위치가
아니라 PK 값이 들어 있어, 세컨더리 인덱스로 찾으면 그 PK로 클러스터드 인덱스를 한 번 더
타야 한다. 인덱스 구조가 곧 저장 구조인 셈이라 [[tree-index]]의 B+ tree 이해가 그대로
InnoDB 이해로 이어진다.

## 자주 혼동하는 포인트

- Consistency는 "데이터가 항상 맞다"는 막연한 말이 아니라 "정의된 제약을 트랜잭션
  단위로 보존한다"는 뜻이며, 나머지 세 속성이 지켜져야 성립하는 결과에 가깝다.
- redo와 undo의 방향: redo는 장애 후 커밋된 것을 다시 적용(roll forward), undo는 롤백과
  MVCC용 이전 버전 보관(roll back)이다.
- InnoDB는 DB가 아니라 MySQL의 스토리지 엔진이다. 같은 MySQL이라도 엔진에 따라
  트랜잭션 지원 여부가 달라진다.

## 관련 문서

- 노트: [[2025-09-21-study-log]]
- 관련 토픽: [[tree-index]]
