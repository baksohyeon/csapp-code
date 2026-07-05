---
type: Topic
status: living
related_to:
  - memory-hierarchy
---

# 컴파일 · 링킹 (Compile & Linking)

소스 코드가 오브젝트 파일로 컴파일되고, 링커가 심볼을 해석해 실행 파일로 결합되는 과정.
컴파일 타임 에러와 링크 타임 에러의 구분, forward declaration의 역할이 핵심. CSAPP 기준
Ch1(hello 프로그램의 일생)과 Ch7(Linking)에 해당한다.

## 빌드 4단계

`gcc hello.c -o hello` 한 줄은 실제로는 네 단계를 거친다.

| 단계 | 도구 | 입력 → 출력 | gcc 옵션 |
|------|------|-------------|----------|
| 전처리 (preprocessing) | cpp | `hello.c` → `hello.i` | `-E` |
| 컴파일 (compilation) | cc1 | `hello.i` → `hello.s` (어셈블리) | `-S` |
| 어셈블 (assembly) | as | `hello.s` → `hello.o` (오브젝트 파일) | `-c` |
| 링킹 (linking) | ld | `hello.o` + 다른 `.o`/라이브러리 → 실행 파일 | (기본) |

`-c`로 멈추면 오브젝트 파일까지만 만들어지고, 링킹은 `gcc hello.o sum.o -o hello`처럼
오브젝트 파일들을 모아 따로 수행할 수 있다. 소스가 여러 개면
`gcc hello.c sum.c -o hello`로 한 번에 컴파일·링킹해도 결과는 같다.

## 컴파일 타임 에러 vs 링크 타임 에러

컴파일 타임 에러는 소스를 오브젝트 파일로 변환하는 단계에서 나며, 문법 오류·타입
불일치·선언되지 않은 식별자가 원인이다(`error: undeclared identifier`). 링크 타임 에러는
컴파일은 성공했지만 오브젝트 파일들을 결합하는 단계에서 나며, 선언된 심볼의 정의를
어디서도 찾지 못할 때 발생한다(`undefined reference to` / `undefined symbols`).
함수 정의가 담긴 소스를 빼놓고 컴파일하거나, 외부 라이브러리를 링크하지 않거나,
심볼 이름이 어긋난 경우가 대표적이다.

## Forward Declaration의 역할

`int sum(int num);` 같은 선언은 컴파일러에게 "이 심볼이 존재한다"는 사실만 알려 컴파일을
통과시키고, 실제 정의를 찾아 연결하는 일은 링커의 몫이다. 그래서 선언만 있고 정의가 없는
코드는 컴파일은 되지만 링크에서 실패한다 — 이 시차가 두 에러를 구분하는 기준이다.

## 자주 혼동하는 포인트

- 링커가 해석하는 것은 함수/전역 변수 같은 심볼이다. 지역 변수는 심볼 해석 대상이 아니다.
- 오브젝트 파일은 기계어를 담고 있지만 아직 주소가 확정되지 않은 재배치 가능(relocatable)
  파일이라 단독으로 실행할 수 없다.
- 오브젝트 파일은 아키텍처 의존 산출물이다. 다른 플랫폼에서는 `.o`를 재생성해야 한다.

## 관련 문서

- 노트: [[0001-compile-linking]] — 링크 타임 에러 재현 실습
- 실습: [examples/0001-compile-linking](../../examples/0001-compile-linking/)
