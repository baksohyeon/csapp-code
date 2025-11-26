
# csapp-code
- PDF: https://i.hyeon.me/csapp.pdf
- Past whiteboard (this page): https://i.hyeon.me/csapp-archive
- Current whiteboard: https://i.hyeon.me/csapp


= 2025 09 21 =
오늘 배운 것:
- bomb lab 시크릿 페이즈 → 이진 트리
- 바이너리 트리가 왜 빠른지 특징이 뭔지 
- B+ tree 와 Hash Index
- OLTP, OLAP DB 특징을 배움
- ACID 개념이 뭔지

소감문: 안다고 생각햇는데 ACID 키워드만 알앗지 설명하라고 했으면 탈탈 털렷을 것 같다. 그리고 인덱스의 기본 원리에 대해서 배웠는데 너무 궁금했던 부분이었어서 속이 싹 내려갔다. 
그리고 관련해서 검색해보니 Inno DB 라는 키워드도 보였는데 그것도 궁금해졋다. // MySQL 의 구현체 (Inno DB 그 외의 것은 안쓰면 된다 생각하면 편하다 `MYSML 안쓰면 된다` )

예전에 레디스를 직접 구현해보는 걸 시도해본 적 있었는데, 단순 key value 로 이루어진 그 무언가라고 생각했던 것이 디게 복잡해서 놀랐었다. 오늘 스터디를 듣고보니  아주 고능한 알고리즘의 집합체엿구나 너무 기특하다.

---
## Java 코드 컴파일하고 실행하기

Java 프로그램을 돌리려면 크게 두 단계가 필요하다.

1.  **컴파일(Compile)**: `javac` 컴파일러로 사람이 쓴 Java 소스 코드(`.java`)를 바이트코드(`.class`)로 바꾼다.
2.  **실행(Execute)**: `java` 명령어로 JVM(자바 가상 머신)을 띄워서 컴파일된 바이트코드를 실행시킨다.

### Java 파일 컴파일

`javac`는 **Java 컴파일러**다. 이건 우리가 쓴 `.java` 소스 코드를 JVM이 알아먹는 `.class` 바이트코드로 번역하는 역할을 한다.

`javac`가 하는 일:
*   **소스 코드 변환**: `.java` 파일을 `.class` 파일로 만든다.
*   **문법 & 타입 체크**: 코드에 문법 오류나 타입이 안 맞는 부분이 없는지 확인하고, 문제 있으면 에러를 띄운다.
*   **플랫폼 독립성**: 컴파일된 `.class` 파일은 OS나 하드웨어에 상관없다. 이게 바로 Java가 "한 번 짜면 어디서든 돌아간다"고 하는 이유다.

이 프로젝트처럼 패키지 구조(`java-datastructures` 폴더 안에 `BST` 패키지)가 잡혀있으면, `-d` 옵션을 붙여주는 게 좋다. 이 옵션은 컴파일된 `.class` 파일을 어디에 둘지 정해주는 건데, 원래 패키지 구조를 그대로 복사해서 만들어준다.

프로젝트 루트 폴더(`/Users/dorito/csapp-code`)에서 아래 명령어를 치면 `java-datastructures/BST` 폴더의 모든 자바 파일을 컴파일할 수 있다.

```bash
javac -d java-datastructures java-datastructures/BST/*.java
```

이 명령어는 `java-datastructures/BST` 안의 모든 `.java` 파일을 컴파일하고, 결과물인 `.class` 파일들을 `java-datastructures` 폴더 안에 `BST` 패키지 구조를 그대로 만들어서 넣어준다. (예: `java-datastructures/BST/BinarySearchTree.class`)

컴파일할 때 "unchecked or unsafe operations" 같은 경고가 뜰 수도 있는데, 보통 제네릭 쓸 때 타입 정보가 좀 모자라서 뜨는 거라 지금은 무시해도 된다.

### 데모 프로그램 실행

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
