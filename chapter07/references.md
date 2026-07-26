# Chapter 7.6.1 리서치 출처

조사일: 2026-07-26
원칙: 영어 1차 자료를 먼저 읽고, 한국어 강의노트에서는 용어의 영문 원어를 병기한다.

## 출처 표기 체계

강의노트 본문은 다음 세 층을 구분한다.

- **CSAPP** — 교재 3판 7.6.1의 논점, 규칙, 예제, 연습문제를 재서술
- **OFFICIAL** — 언어 표준 초안, ABI, 컴파일러·링커 공식 문서와 공식 정오표
- **COMMENTARY** — 위 자료와 재현 실험을 연결한 작성자 해설

교재 문장은 장문 복제하지 않았다. 절의 구조와 모든 사례를 보존하되 한국어로 다시 설명하고,
코드는 동일 논점을 재현하는 새 실습으로 작성했다.

## 1. 정본: CSAPP

1. Randal E. Bryant, David R. O’Hallaron,
   *Computer Systems: A Programmer’s Perspective*, 3rd ed.,
   §7.6.1 “How Linkers Resolve Duplicate Symbol Names”.
   - 이 레포 README가 가리키는 **3판 Global Edition PDF**의 인쇄면 716–720을
     로컬에서 확인했다. 공식 북미판 정오표에서는 대응 위치를 p.680부터로 표기한다.
   - 3판의 절 제목은 “Multiply Defined Global Symbols”가 아니라
     **“Duplicate Symbol Names”**이다. “Multiply Defined Global Symbols”는 2판 제목이다.
   - 7.6.1에는 번호가 붙은 Figure가 없다. 두 모듈씩 이루어진 다섯 코드 사례와
     Practice Problem 7.2가 핵심 구성이다.
2. [CS:APP3e 공식 정오표](https://csapp.cs.cmu.edu/3e/errata.html)
   - p.680: GCC 10부터 `-fno-common`이 기본이므로 책의 multiply-defined weak 사례가
     이제 기본 설정에서 링크 오류가 됨.
   - p.682: `foo5`의 정확한 손상 값은 시스템 의존적임.
3. [CS:APP3e 공식 Figure 원본](https://csapp.cs.cmu.edu/3e/figures.html)
   - Chapter 7 전체 그림 목록을 대조했다.
   - 7.6.1 자체에는 공식 번호 Figure가 없음을 확인했다. 본 문서의 SVG는 설명을 위해
     새로 제작한 도식이며 교재 Figure 복제가 아니다.

## 2. 공식 강의자료

1. [CMU 15-213 Linking 강의 슬라이드](https://www.cs.cmu.edu/afs/cs/academic/class/15213-m14/www/lectures/15-linking.pdf)
   - strong/weak 세 규칙, 심볼 해석, relocation의 강의 순서를 대조했다.
2. [서울대학교 Systems Programming: Code Optimization and Linking](https://compsec.snu.ac.kr/class/systems-programming/slides/07-optimization-linking.pdf)
   - CSAPP 저자 자료를 기반으로 만든 공개 강의자료다.
   - compiler driver → relocatable object → linker 흐름, 전역/지역/외부 심볼의 분류,
     symbol resolution과 relocation의 경계를 교차 확인했다.

강의 슬라이드는 교재를 대체하는 정본으로 사용하지 않고, 설명 순서와 강조점을 검증하는
보조 1차 자료로 사용했다.

## 3. C 언어

1. [ISO C11 위원회 초안 N1570](https://www.open-std.org/jtc1/sc22/wg14/www/docs/n1570.pdf),
   §6.9.2 External object definitions.
   - 파일 범위에서 initializer 없이 선언한 객체가 언제 tentative definition인지 확인했다.
   - 한 번역 단위 안에 실제 외부 정의가 없다면, 번역 단위 끝에서 0으로 초기화된 정의처럼
     동작한다는 언어 의미를 확인했다.

중요한 경계: C 표준은 `COMMON`, `.bss`, `STB_WEAK`를 규정하지 않는다. 그것들은
컴파일러·오브젝트 포맷·링커 층의 구현 전략이다.

## 4. ELF ABI

1. [System V ABI: Symbol Table](https://refspecs.linuxfoundation.org/elf/gabi4%2B/ch4.symtab.html)
   - `STB_LOCAL`, `STB_GLOBAL`, `STB_WEAK`의 의미
   - `STT_OBJECT`, `STT_FUNC`, `STT_COMMON`
   - `SHN_UNDEF`, `SHN_COMMON`
   - strong global과 weak 정의가 함께 있을 때 global을 선택하는 규칙
   - `SHN_COMMON`의 값은 정렬 조건, 크기는 필요한 바이트 수라는 정의

교재와 ELF를 연결할 때 가장 중요한 교정은 다음과 같다.

> `-fcommon`의 tentative definition은 `readelf`에서 보통
> `OBJECT GLOBAL DEFAULT COM`으로 보인다. 이것은 `WEAK` binding이 아니다.

## 5. GCC

1. [GCC 10 Porting Guide: Default to -fno-common](https://gcc.gnu.org/gcc-10/porting_to.html)
   - 헤더에서 `extern`을 빠뜨린 전역 변수 패턴이 여러 정의를 만든다는 설명
   - GCC 10의 기본값 변경과 `-fcommon` 호환 옵션
2. [GCC 10 Changes](https://gcc.gnu.org/gcc-10/changes.html)
   - `-fno-common` 기본값과 여러 tentative definition의 링크 오류
   - 전역 접근의 효율 및 코드 크기 이점
3. [GCC Code Generation Options: -fcommon](https://gcc.gnu.org/onlinedocs/gcc/Code-Gen-Options.html)
   - `-fno-common`: 초기화 없는 전역을 오브젝트의 BSS에 배치
   - `-fcommon`: common block에 배치해 링커 병합을 허용
4. [GCC Variable Attributes](https://gcc.gnu.org/onlinedocs/gcc/Common-Variable-Attributes.html)
   - 개별 변수의 `common`, `nocommon` attribute

“GCC 10 전후 실험”은 GCC 13.3에서 `-fcommon`과 `-fno-common`을 명시해 두 정책을
같은 소스에 재현했다. GCC 9/10 바이너리 자체의 버전 비교가 아니라, 공식 변경 문서에 적힌
두 기본 정책의 의미 비교다.

## 6. GNU binutils: nm, readelf, ld

1. [GNU nm](https://sourceware.org/binutils/docs/binutils/nm.html)
   - `B`: BSS, `C`: common, `D`: initialized data, `T`: text, `U`: undefined,
     `V`/`W`: weak object/function의 출력 의미
2. [GNU ld Options](https://sourceware.org/binutils/docs/ld/Options.html)
   - `--warn-common`: common 병합·override의 진단
   - `--allow-multiple-definition`: 여러 정의를 허용하면 첫 정의를 사용
3. [GNU ld: Input Section for Common Symbols](https://sourceware.org/binutils/docs/ld/Input-Section-Common.html)
   - common은 실제 입력 섹션이 아니므로 링커 스크립트에서 `COMMON`이라는 특별 표기로 다룸
   - 일반적으로 출력 `.bss`가 `*(COMMON)`을 받아 저장 공간을 할당

## 7. Clang과 lld

1. [Clang Command Guide: -fcommon, -fno-common](https://clang.llvm.org/docs/CommandGuide/clang.html)
   - initializer 없는 변수의 common linkage 제어
2. [LLVM lld 공식 man page 원본](https://github.com/llvm/llvm-project/blob/main/lld/docs/ld.lld.1)
   - 기본적으로 중복 정의는 오류
   - `--allow-multiple-definition`을 사용하면 첫 정의를 사용

GNU ld와 lld는 본 실습의 기본 중복 정의를 모두 오류로 처리했다. 진단 형식은 달랐다.
GNU ld는 “multiple definition / first defined here” 형태였고, lld는 두 정의의 파일·행을
계층적으로 나열했다. 명시적 weak + weak는 두 링커 모두 실험에서 먼저 전달된 오브젝트를
선택했지만, 교재와 ABI가 이 선택을 이식 가능한 계약으로 보장하지 않으므로 그 순서에
의존하면 안 된다.

Clang 18.1.3도 기본 컴파일과 명시적 `-fno-common`에서 tentative definition을 `.bss`
GLOBAL 정의로 내보내 중복 링크를 거부했고, 명시적 `-fcommon`에서는 `GLOBAL COM`으로
내보내 병합했다.

## 8. 로컬 재현 환경

검증 출력: [results/verified-linux-aarch64.txt](results/verified-linux-aarch64.txt)

- Host: macOS arm64, Docker/OrbStack
- Guest: Ubuntu 24.04 aarch64
- GCC 13.3.0
- Clang 18.1.3
- GNU ld/readelf/nm/objdump 2.42
- lld 18.1.3

명령은 [verify-elf.sh](verify-elf.sh), 컨테이너 실행은
[verify-in-docker.sh](verify-in-docker.sh)에 기록했다.

## 확인하지 못했거나 범위에서 제외한 것

- 특정 GCC 9 바이너리와 GCC 10 바이너리를 나란히 설치한 버전별 실행은 하지 않았다.
  대신 GCC 공식 변경 문서를 근거로 현재 GCC에서 두 플래그를 명시해 의미를 재현했다.
- 서로 다른 아키텍처, 오브젝트 포맷(Mach-O/COFF), 상용 Unix 링커의 선택 규칙은 검증하지
  않았다.
- 동적 링커의 interposition과 shared object 심볼 lookup은 7.10 이후 범위이므로 설명을
  확장하지 않았다.
- `--allow-multiple-definition`은 관찰용 escape hatch일 뿐, 일반 프로그램의 중복 정의를
  고치는 방법으로 권장하지 않는다.
