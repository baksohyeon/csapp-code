---
type: Type
status: living
---

# Example

노트가 참조하는 실습 코드. 링커/컴파일 실습처럼 직접 돌려보는 자료.

## 규칙

- 위치: `examples/<id>-<slug>/`
- 각 실습은 최소 하나의 노트([[note]])에서 참조되어야 한다.
- `.o` 같은 빌드 산출물은 노트가 직접 참조하는 경우에만 추적한다. 해당 노트에
  CPU/플랫폼 의존 산출물이라는 점과 재생성 명령을 적는다.

## 실습

- `examples/0001-compile-linking/` — [[0001-compile-linking]]에서 사용
