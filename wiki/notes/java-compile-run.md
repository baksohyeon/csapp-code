---
type: Note
status: done
date: 2026-07-05
topics:
  - compile-linking
  - tree-index
artifact_path: java-datastructures
---

# Java 코드 컴파일하고 실행하기

Java 프로그램을 돌리려면 크게 두 단계가 필요하다.

1.  **컴파일(Compile)**: `javac` 컴파일러로 사람이 쓴 Java 소스 코드(`.java`)를 바이트코드(`.class`)로 바꾼다.
2.  **실행(Execute)**: `java` 명령어로 JVM(자바 가상 머신)을 띄워서 컴파일된 바이트코드를 실행시킨다.

## Java 파일 컴파일

`javac`는 **Java 컴파일러**다. 이건 우리가 쓴 `.java` 소스 코드를 JVM이 알아먹는 `.class` 바이트코드로 번역하는 역할을 한다.

`javac`가 하는 일:
*   **소스 코드 변환**: `.java` 파일을 `.class` 파일로 만든다.
*   **문법 & 타입 체크**: 코드에 문법 오류나 타입이 안 맞는 부분이 없는지 확인하고, 문제 있으면 에러를 띄운다.
*   **플랫폼 독립성**: 컴파일된 `.class` 파일은 OS나 하드웨어에 상관없다. 이게 바로 Java가 "한 번 짜면 어디서든 돌아간다"고 하는 이유다.

이 프로젝트처럼 패키지 구조(`java-datastructures` 폴더 안에 `BST` 패키지)가 잡혀있으면, `-d` 옵션을 붙여주는 게 좋다. 이 옵션은 컴파일된 `.class` 파일을 어디에 둘지 정해주는 건데, 원래 패키지 구조를 그대로 복사해서 만들어준다.

프로젝트 루트 폴더에서 아래 명령어를 치면 `java-datastructures/BST` 폴더의 모든 자바 파일을 컴파일할 수 있다.

```bash
javac -d java-datastructures java-datastructures/BST/*.java
```

이 명령어는 `java-datastructures/BST` 안의 모든 `.java` 파일을 컴파일하고, 결과물인 `.class` 파일들을 `java-datastructures` 폴더 안에 `BST` 패키지 구조를 그대로 만들어서 넣어준다. (예: `java-datastructures/BST/BinarySearchTree.class`)

컴파일할 때 "unchecked or unsafe operations" 같은 경고가 뜰 수도 있는데, 보통 제네릭 쓸 때 타입 정보가 좀 모자라서 뜨는 거라 지금은 무시해도 된다.

## 데모 프로그램 실행

컴파일이 끝났으면 `java` 명령어로 데모 프로그램을 돌려볼 수 있다. 이때 `-cp`(classpath) 옵션이 중요한데, JVM한테 실행할 `.class` 파일이 어디 있는지 알려주는 역할을 한다.

**`BinarySearchTreeDemo` 실행:**

```bash
java -cp java-datastructures BST.BinarySearchTreeDemo
```

**`AVLTreeDemo` 실행:**

```bash
java -cp java-datastructures BST.AVLTreeDemo
```

이렇게 하면 각 데모 클래스 파일 안에 있는 `main` 메소드가 실행되면서, 이진 탐색 트리랑 AVL 트리가 어떻게 도는지 볼 수 있다.

## C 컴파일과의 비교

C는 컴파일 결과가 CPU 아키텍처에 묶인 기계어([[0001-compile-linking]] 참고)지만,
Java는 플랫폼 독립적인 바이트코드를 만들고 JVM이 실행 시점에 해석/컴파일한다.
`.class`는 어느 환경에서든 재생성 가능한 빌드 산출물이라 git으로 추적하지 않는다
(`.gitignore`의 `*.class`).

## 관련

- 토픽: [[compile-linking]], [[tree-index]] (BST/AVL 구현체가 실습 대상)
- 실습: [java-datastructures/BST](../../java-datastructures/BST/)
