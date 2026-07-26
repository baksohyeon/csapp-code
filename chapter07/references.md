# Chapter 7.6 리서치 출처

조사일: 2026-07-26
원칙: 영어 1차 자료를 먼저 읽고, 한국어 강의노트에서는 용어의 영문 원어를 병기한다.

## 출처 표기 체계

강의노트 본문은 다음 세 층을 구분한다.

- **CSAPP**: 교재 3판 7.6의 논점, 규칙, 예제, 연습문제
- **OFFICIAL**: 언어 표준 초안, ABI, 컴파일러·링커 공식 문서와 공식 정오표
- **COMMENTARY**: 위 자료와 재현 실험을 연결한 작성자 해설

## 1. 정본: CSAPP

1. Randal E. Bryant, David R. O’Hallaron,
   *Computer Systems: A Programmer’s Perspective*, 3rd ed.,
   §7.6 “Symbol Resolution”.
   - 이 레포 README가 가리키는 **3판 Global Edition PDF**에서 §7.6.1, §7.6.2,
     §7.6.3을 확인했다. 공식 북미판 정오표에서는 §7.6.1의 대응 위치를 p.680부터로
     표기한다.
   - 3판의 절 제목은 **“Duplicate Symbol Names”**이다. 2판의 대응 제목은
     “Multiply Defined Global Symbols”이다.
   - 7.6.1에는 번호가 붙은 Figure가 없다. 두 모듈씩 이루어진 다섯 코드 사례와
     Practice Problem 7.2가 핵심 구성이다.
   - 7.6.2의 Figure 7.6, Figure 7.7, Figure 7.8은 `libvector.a`, `main2.c`,
     필요한 archive member의 선택을 설명한다.
   - 7.6.3은 `E`, `U`, `D` 집합을 사용한 왼쪽부터의 archive 탐색, 라이브러리 순서,
     순환 의존성 처리, Practice Problem 7.3을 다룬다.
2. [CS:APP3e 공식 목차](https://csapp.cs.cmu.edu/3e/pieces/preface3e.pdf)
   - 7.6.1, 7.6.2, 7.6.3 다음에 7.7 Relocation이 이어지는 절 순서를 확인
3. [CS:APP3e 공식 정오표](https://csapp.cs.cmu.edu/3e/errata.html)
   - p.680: GCC 10부터 `-fno-common`이 기본이므로 책의 multiply-defined weak 사례가
     이제 기본 설정에서 링크 오류가 됨.
   - p.682: `foo5`의 정확한 손상 값은 시스템 의존적임.
4. [CS:APP3e 공식 Figure 원본](https://csapp.cs.cmu.edu/3e/figures.html)
   - Chapter 7 전체 그림 목록을 대조했다.
   - 7.6.1 자체에는 공식 번호 Figure가 없다.

## 2. 공식 강의자료

1. [CS:APP 공식 Chapter 1 컴파일 과정 그림](https://csapp.cs.cmu.edu/3e/ics3/intro/compilation.pdf)
   - C 소스가 전처리기, 컴파일러, 어셈블러, 링커를 거쳐 실행 파일이 되는 과정
   - 컴파일 결과인 재배치 가능 오브젝트와 링크 결과인 실행 파일의 구분
2. [CMU 15-213 Linking 강의 슬라이드](https://www.cs.cmu.edu/afs/cs/academic/class/15213-m14/www/lectures/15-linking.pdf)
   - strong/weak 세 규칙, 심볼 해석, relocation의 강의 순서를 대조했다.
3. [서울대학교 Systems Programming: Code Optimization and Linking](https://compsec.snu.ac.kr/class/systems-programming/slides/07-optimization-linking.pdf)
   - CSAPP 저자 자료를 기반으로 만든 공개 강의자료다.
   - compiler driver → relocatable object → linker 흐름, 전역/지역/외부 심볼의 분류,
     symbol resolution과 relocation의 경계를 교차 확인했다.

교재를 정본으로 두고, 강의 슬라이드로 설명 순서와 강조점을 교차 확인했다.

## 3. C 언어

1. [ISO C11 위원회 초안 N1570](https://www.open-std.org/jtc1/sc22/wg14/www/docs/n1570.pdf),
   §6.9.2 External object definitions.
   - 파일 범위에서 initializer 없이 선언한 객체가 언제 tentative definition인지 확인했다.
   - 한 번역 단위 안에 실제 외부 정의가 없다면, 번역 단위 끝에서 0으로 초기화된 정의처럼
     동작한다는 언어 의미를 확인했다.
   - 같은 번역 단위 안의 여러 tentative definition은 호환되는 선언이면 같은 객체를 가리킨다.
   - initializer가 붙은 `extern int x = 3;`은 외부 정의다.
   - 끝까지 불완전한 `int a[];` tentative definition은 원소 하나가 0인 배열이 된다.

C 표준은 tentative definition의 언어 의미를 규정한다. `COMMON`, `.bss`,
`STB_WEAK`는 컴파일러, 오브젝트 포맷, 링커 층에서 정한다.

## 4. ELF ABI

1. [System V ABI: Symbol Table](https://refspecs.linuxfoundation.org/elf/gabi4%2B/ch4.symtab.html)
   - `STB_LOCAL`, `STB_GLOBAL`, `STB_WEAK`의 의미
   - `STT_OBJECT`, `STT_FUNC`, `STT_COMMON`
   - `SHN_UNDEF`, `SHN_COMMON`
   - strong global과 weak 정의가 함께 있을 때 global을 선택하는 규칙
   - `SHN_COMMON`의 값은 정렬 조건, 크기는 필요한 바이트 수라는 정의
   - unresolved weak symbol은 0 값을 가지며, undefined weak만 해결하기 위해 archive member를
     추출하지 않는 규칙

교재의 분류와 ELF 출력은 다음과 같이 연결된다.

> `-fcommon`의 tentative definition은 `readelf`에서 보통
> `OBJECT GLOBAL DEFAULT COM`으로 보인다. binding은 `GLOBAL`, section index는
> `SHN_COMMON`이다.

## 5. GCC

1. [GCC 10 Porting Guide: Default to -fno-common](https://gcc.gnu.org/gcc-10/porting_to.html)
   - 헤더에서 `extern`을 빠뜨린 전역 변수 패턴이 여러 정의를 만든다는 설명
   - GCC 10의 기본값 변경과 `-fcommon` 호환 옵션
2. [GCC 10.1 Release Announcement](https://gcc.gnu.org/pipermail/gcc-announce/2020/000163.html)
   - GCC 10.1 공개일 2020-05-07
3. [GCC 10 Changes](https://gcc.gnu.org/gcc-10/changes.html)
   - `-fno-common` 기본값과 여러 tentative definition의 링크 오류
   - 전역 접근의 효율 및 코드 크기 이점
4. [GCC Code Generation Options: -fcommon](https://gcc.gnu.org/onlinedocs/gcc/Code-Gen-Options.html)
   - `-fno-common`: 초기화 없는 전역을 오브젝트의 BSS에 배치
   - `-fcommon`: common block에 배치해 링커 병합을 허용
5. [GCC Variable Attributes](https://gcc.gnu.org/onlinedocs/gcc/Common-Variable-Attributes.html)
   - 개별 변수의 `common`, `nocommon` attribute
6. [GCC Function Attributes: `weak`, `weakref`](https://gcc.gnu.org/onlinedocs/gcc/Common-Function-Attributes.html)
   - 외부 심볼을 실제 weak symbol로 내보내는 `weak` attribute
   - 정의가 없어도 되는 weak reference를 만드는 `weakref`
7. [GCC Warning Options: `-Wlto-type-mismatch`](https://gcc.gnu.org/onlinedocs/gcc/Warning-Options.html)
   - `-flto`로 여러 번역 단위의 중간 표현을 함께 볼 때 전역 선언의 타입 불일치를 경고
   - 일반 정적 링커의 이름 중심 해석과 LTO 진단을 구분
8. [Linux 2.6.12 Makefile](https://raw.githubusercontent.com/torvalds/linux/v2.6.12/Makefile)
   - 2005년 Linux kernel 빌드의 전역 CFLAGS에 이미 `-fno-common`이 명시됨
   - GCC 10 이전에도 프로젝트가 컴파일러 기본값을 명시적으로 덮어쓴 사례

“GCC 10 전후 실험”은 GCC 13.3에서 `-fcommon`과 `-fno-common`을 명시해 두 정책을
같은 소스에 재현했다. GCC 9와 GCC 10의 기본 정책은 공식 변경 문서로 확인했다.

## 6. GNU binutils: nm, readelf, ld

1. [GNU nm](https://sourceware.org/binutils/docs/binutils/nm.html)
   - `B`: BSS, `C`: common, `D`: initialized data, `T`: text, `U`: undefined,
     `V`/`W`: weak object/function의 출력 의미
2. [GNU ld Options](https://sourceware.org/binutils/docs/ld/Options.html)
   - `--warn-common`: common 병합·override의 진단
   - 크기가 다른 common을 병합할 때 더 큰 크기를 선택
   - `--allow-multiple-definition`: 여러 정의를 허용하면 첫 정의를 사용
3. [GNU ld: Input Section for Common Symbols](https://sourceware.org/binutils/docs/ld/Input-Section-Common.html)
   - common은 실제 입력 섹션이 아니므로 링커 스크립트에서 `COMMON`이라는 특별 표기로 다룸
   - 일반적으로 출력 `.bss`가 `*(COMMON)`을 받아 저장 공간을 할당
4. [GNU assembler: `.comm`](https://sourceware.org/binutils/docs/as/Comm.html)
   - `.comm symbol, length, alignment`이 common의 크기와 정렬을 전달
   - 여러 common의 크기가 다르면 GNU ld가 가장 큰 크기를 할당

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

## 8. 드라이버, libc, 정적 라이브러리

1. [GCC Overall Options](https://gcc.gnu.org/onlinedocs/gcc/Overall-Options.html)
   - GCC 호출이 전처리, 컴파일, 어셈블, 링크 단계를 수행한다는 설명
   - `-E`, `-S`, `-c`, `-v`, `-###`의 단계 제어
2. [GCC Link Options](https://gcc.gnu.org/onlinedocs/gcc/Link-Options.html)
   - GCC 드라이버가 시작 파일과 기본 라이브러리를 링크 명령에 추가
   - `-l`, `-L`, `-static`, `-nostartfiles`, `-nodefaultlibs`, `-nostdlib`
   - `-fuse-ld=bfd`, `-fuse-ld=gold`, `-fuse-ld=lld`, `-fuse-ld=mold`
3. [GCC Standard Libraries](https://gcc.gnu.org/onlinedocs/gcc/Standard-Libraries.html)
   - C 표준 라이브러리 구현은 운영체제나 공급자가 제공하며 GCC 드라이버가 이를
     링크한다는 구성
4. [Clang Command Guide](https://clang.llvm.org/docs/CommandGuide/clang.html)
   - Clang driver와 전처리, 파싱, 코드 생성, 어셈블, 링크 단계
5. [GNU ar](https://sourceware.org/binutils/docs/binutils/ar.html)
   - archive member 관리와 `s` 심볼 인덱스
6. [GNU ld Options](https://sourceware.org/binutils/docs/ld/Options.html)
   - 아카이브를 명령줄 위치에서 탐색하는 규칙
   - `--whole-archive`, `--start-group`, `--end-group`
7. [LLD Backward References](https://lld.llvm.org/ELF/warn_backrefs.html)
   - LLD가 앞에서 읽은 archive의 심볼 표를 기억하는 동작
   - GNU ld와 호환되는 입력 순서를 점검하는 `--warn-backrefs`
8. [Linux ldd(1)](https://man7.org/linux/man-pages/man1/ldd.1.html)
   - 동적 의존성 표시와 신뢰할 수 없는 실행 파일에 대한 보안 주의
9. [glibc Manual](https://sourceware.org/glibc/manual/latest/html_mono/libc.html)
   - `libc.a`를 사용하는 정적 링크와 정적 구성의 제약
10. [musl About](https://musl.libc.org/about.html)
    - 정적 링크를 포함한 musl의 배포 특성
11. [Apple Developer Forums: Linker](https://developer.apple.com/forums/tags/linker)
    - Apple Developer Technical Support의 library primer
    - 정적 library archive 지원과 제3자 실행 파일의 표준 동적 링커 사용이라는 플랫폼 경계
12. [MSVC `/MD`, `/MT`](https://learn.microsoft.com/en-us/cpp/build/reference/md-mt-ld-use-run-time-library)
    - DLL CRT와 정적 CRT 선택
13. [Visual C++ Redistributable](https://learn.microsoft.com/en-us/cpp/windows/latest-supported-vc-redist)
    - 동적 Visual C++ 런타임 배포 패키지의 적용 범위
14. [glibc compatibility guidance](https://sourceware.org/pipermail/libc-alpha/2023-July/150165.html)
    - 지원할 가장 오래된 운영체제나 buildroot에서 빌드하는 배포 방식
15. [LLVM LLD](https://lld.llvm.org/)
    - GNU 링커와 호환되는 명령행을 제공하는 LLVM 링커
    - 대규모 프로그램에서 GNU gold보다 빠를 수 있다는 프로젝트 설명과 적용 범위
16. [mold](https://github.com/rui314/mold)
    - MySQL 8.3, Clang 19, Chromium 124 링크 시간과 벤치마크 조건
    - GCC, Clang, Rust에서 mold를 선택하는 설정
    - 병렬 처리와 빠른 자료 구조를 사용하는 설계 목표
17. [Cargo: Optimizing Build Performance](https://doc.rust-lang.org/nightly/cargo/guide/build-performance.html)
    - 증분 빌드에서도 최종 링크가 빌드 시간의 대부분을 차지할 수 있다는 설명
    - LLD, mold, wild 같은 대체 링커 설정
18. [GNU Binutils 2.44 release](https://sourceware.org/pipermail/binutils/2025-February/139195.html)
    - GNU gold의 사용 중단 예정 상태와 향후 제거 계획

## 9. LTO, ThinLTO, section GC

1. [GCC Optimize Options](https://gcc.gnu.org/onlinedocs/gcc/Optimize-Options.html)
   - `-flto`를 컴파일과 최종 링크에 함께 적용하는 방식
   - 번역 단위 사이 인라이닝, 상수 전파 등 interprocedural optimization
   - 현재 GCC의 `-flto-incremental=경로` 캐시
2. [GNU ld Options: `--gc-sections`](https://sourceware.org/binutils/docs/ld/Options.html)
   - 시작점과 보존 심볼에서 도달 가능한 입력 섹션을 재배치 관계로 표시하는 방식
   - 제거된 섹션을 확인하는 `--print-gc-sections`
3. [Clang ThinLTO](https://clang.llvm.org/docs/ThinLTO.html)
   - Full LTO의 단일 모듈 병합과 시간·메모리 확장성 한계
   - 모듈 요약, 통합 인덱스, 병렬 백엔드로 이루어진 ThinLTO 구조
   - LLD의 `--thinlto-cache-dir` 캐시
4. [Clang Command Guide](https://clang.llvm.org/docs/CommandGuide/clang.html)
   - `-flto=full`, `-flto=thin` 선택
5. [ThinLTO: Scalable and Incremental LTO](https://research.google/pubs/thinlto-scalable-and-incremental-lto/)
   - 전체 IR을 읽고 쓰지 않는 요약 기반 분석
   - 병렬 백엔드와 분산·증분 빌드 통합 목표
   - Full LTO의 번역 단위 간 최적화 대부분을 유지하면서 비 LTO에 가까운 확장성을
     목표로 한 설계와 실험 결과
6. [rustc Codegen Options: LTO](https://doc.rust-lang.org/rustc/codegen-options/index.html)
   - `-C lto=thin`의 cross-crate ThinLTO
   - 최적화된 다중 codegen unit 빌드에서 기본으로 시도하는 thin local LTO
7. [Cargo Profiles](https://doc.rust-lang.org/cargo/reference/profiles.html)
   - `lto = false`, `lto = "off"`, `lto = "thin"`의 차이
   - 기본 개발·릴리스 프로필의 `opt-level`, `codegen-units`

`dead code elimination`은 실행 결과에 영향을 주지 않는 코드를 제거하는 일반 최적화다.
링커의 section GC는 심볼, 재배치, 입력 섹션의 도달 가능성을 사용한다. LTO는 compiler
IR을 이용해 번역 단위 사이의 인라이닝과 상수 전파까지 수행한다.

CSAPP의 전통적 링크 모델은 선택된 입력 전체의 심볼과 재배치 정보를 최종 링크에서
처리한다. Full LTO는 단일 모듈 병합과 전역 분석 때문에 큰 입력에서 시간과 메모리
부담이 커진다. ThinLTO는 모듈 요약, 병렬 백엔드, 캐시를 사용한다. 개별 분석 패스의
시간 복잡도는 알고리즘과 구현에 따라 정해진다.

## 10. 동적 로더, ASLR, PIC, PIE

1. [System V ELF ABI: Dynamic Linking](https://refspecs.linuxfoundation.org/elf/gabi4%2B/ch5.dynamic.html)
   - 동적 링크 실행 파일의 `PT_INTERP` 프로그램 헤더
   - 프로그램 인터프리터가 공유 오브젝트를 적재하고 재배치를 처리한 뒤 프로그램에
     제어를 넘기는 순서
2. [System V ELF ABI: Program Header](https://refspecs.linuxfoundation.org/elf/gabi4%2B/ch5.pheader.html)
   - `PT_INTERP`, `PT_LOAD`, `PT_DYNAMIC`의 역할
3. [glibc ld.so(8)](https://man7.org/linux/man-pages/man8/ld.so.8.html)
   - ELF `.interp`에 기록된 동적 로더가 공유 오브젝트를 찾아 적재하는 동작
4. [Linux ldd(1)](https://man7.org/linux/man-pages/man1/ldd.1.html)
   - `ldd`가 일반적으로 동적 로더의 trace 기능으로 의존성을 표시한다는 설명
   - `ldd` 출력만으로 `PT_INTERP`와 `DT_NEEDED`를 구분할 수 없다는 점
5. [Linux kernel: `randomize_va_space`](https://www.kernel.org/doc/html/v6.9/admin-guide/sysctl/kernel.html#randomize-va-space)
   - 값 `0`, `1`, `2`의 ASLR 범위
   - PIE 코드 시작 주소, `mmap`, 공유 라이브러리, 스택, VDSO, 힙의 무작위화
6. [Linux `/proc/pid/maps`](https://man7.org/linux/man-pages/man5/proc_pid_maps.5.html)
   - 실행 파일, 공유 라이브러리, 힙, 스택의 실제 메모리 매핑 확인 방법
7. [GCC Link Options](https://gcc.gnu.org/onlinedocs/gcc/Link-Options.html)
   - `-pie`, `-no-pie`, `-static-pie`의 의미
8. [GCC Code Generation Options](https://gcc.gnu.org/onlinedocs/gcc/Code-Gen-Options.html)
   - 공유 라이브러리용 `-fpic`, `-fPIC`
   - 실행 파일용 `-fpie`, `-fPIE`
   - GOT와 위치 독립 코드의 관계
9. [OSTEP: Complete Virtual Memory Systems](https://pages.cs.wisc.edu/~remzi/OSTEP/vm-complete.pdf)
   - 고정된 주소를 이용하는 return-to-libc와 ROP 공격을 어렵게 만드는 ASLR의 역할
   - 스택 주소가 실행마다 달라지는 관찰 예제
10. [GNU binutils: glibc `libc.so`의 `GROUP` 사용 설명](https://sourceware.org/pipermail/binutils/2023-November/130741.html)
    - glibc의 `libc.so` 링크 스크립트가 `libc.so.6`, `libc_nonshared.a`,
      `AS_NEEDED(ld-linux)`를 묶는 구성
    - 일부 환경에서 동적 로더가 `DT_NEEDED`에도 기록될 수 있는 이유

## 11. 런타임 로딩과 언어 경계

1. [Itanium C++ ABI: External Names](https://itanium-cxx-abi.github.io/cxx-abi/abi.html#mangling)
   - 현재 ELF 계열 C++ 구현에서 널리 쓰이는 외부 이름 mangling 문법
   - 외부 mangled name이 보통 `_Z`로 시작하며, CSAPP aside의 단순 예시를 현대 ABI의
     정확한 형식으로 일반화할 수 없음을 확인
2. [Java Virtual Machine Specification §4.4 Constant Pool](https://docs.oracle.com/javase/specs/jvms/se26/html/jvms-4.html#jvms-4.4)
   - JVM 명령이 class file constant pool의 symbolic information을 참조
3. [Java Virtual Machine Specification §5.4.3 Resolution](https://docs.oracle.com/javase/specs/jvms/se26/html/jvms-5.html#jvms-5.4.3)
   - class, field, method 등의 symbolic reference를 런타임에 구체 값으로 해석
4. [npm package.json: `optionalDependencies`](https://docs.npmjs.com/files/package.json/)
   - 선택적 의존성 설치 실패를 전체 설치 실패로 처리하지 않는 규칙
   - 애플리케이션이 해당 모듈의 부재를 직접 처리해야 한다는 경계
5. [Python Import System](https://docs.python.org/3/reference/import.html)
   - 모듈을 찾지 못하면 `ModuleNotFoundError`가 발생하는 규칙
6. [Ruby `LoadError`](https://ruby-doc.org/core-2.5.7/LoadError.html)
   - `require`가 파일이나 확장 라이브러리를 불러오지 못했을 때의 예외
7. [Java `Class.forName`](https://docs.oracle.com/en/java/javase/26/docs/api/java.base/java/lang/Class.html)
   - 이름으로 클래스를 찾고 불러오며, 찾지 못하면 `ClassNotFoundException` 발생
8. [.NET `Assembly.Load`](https://learn.microsoft.com/en-us/dotnet/api/system.reflection.assembly.load)
   - assembly를 찾지 못했을 때의 `FileNotFoundException`
9. [.NET `AssemblyLoadContext`](https://learn.microsoft.com/en-us/dotnet/core/dependency-loading/understanding-assemblyloadcontext)
   - 사용자 정의 의존성 해석에서 실패를 `null`로 넘기는 방식

CSAPP 3판의 C++/Java name mangling aside는 오버로딩된 소스 이름이 더 풍부한 링커
식별자로 바뀐다는 학습 직관을 제공한다. 현대 JVM은 class file의 이름과 descriptor로
method resolution을 수행한다. Node.js, Python, Ruby, Java, .NET은 런타임 모듈 또는
클래스 로딩 실패를 처리해 선택적 의존성을 구현한다.

## 12. 로컬 재현 환경

검증 출력: [results/verified-linux-aarch64.txt](results/verified-linux-aarch64.txt)

- Host: macOS arm64, Docker/OrbStack
- Guest: Ubuntu 24.04 aarch64
- GCC 13.3.0
- Clang 18.1.3
- GNU ld/readelf/nm/objdump 2.42
- lld 18.1.3

추가 재현:

- `common-size`: `char arena[4]`와 `char arena[32]`의 COMMON을 병합해 최종 크기
  32바이트를 확인
- `weak-undefined`: unresolved weak가 `w`로 남고 archive member를 꺼내지 않는 동작 확인
- `weak-function`: 실제 `STB_WEAK` 함수가 기본 구현을 제공하고 strong 함수가 이를
  교체하는 동작 확인
- `common-mismatch` + `-flto`: `-Wlto-type-mismatch` 진단 확인
- `compiler-driver`: GCC, cc, Clang의 컴파일과 링크, raw `ld` 실패, `ldd` 출력,
  glibc 정적 링크 확인
- `static-library`: archive member 선택, 명시적 오브젝트와의 차이, 잘못된 GNU ld
  입력 순서 확인
- `archive-cycle`: archive 반복, GNU ld group, LLD의 재탐색 확인
- `aslr-pie`: 명시적 PIE와 비 PIE의 ELF 타입, `PT_INTERP`, `DT_NEEDED`,
  세 번 실행한 `main`, 스택, 힙 주소 비교
- `lto-dead-code`: 일반 컴파일, GNU ld section GC, GCC Full LTO, Clang ThinLTO의
  심볼 보존 차이와 실행 결과 확인

명령은 [verify-elf.sh](verify-elf.sh), 컨테이너 실행은
[verify-in-docker.sh](verify-in-docker.sh)에 기록했다.

## 확인하지 못했거나 범위에서 제외한 것

- GCC 9와 GCC 10의 기본값은 공식 변경 문서로 확인했다. 실행 결과는 현재 GCC에서
  `-fcommon`, `-fno-common`을 명시해 재현했다.
- 서로 다른 아키텍처, 오브젝트 포맷(Mach-O/COFF), 상용 Unix 링커의 선택 규칙은 검증하지
  않았다.
- 동적 링커의 interposition과 shared object 심볼 lookup은 7.10 이후 범위이므로 설명을
  확장하지 않았다.
- `--allow-multiple-definition`은 바이너리 분석과 의도적으로 중복 정의를 쓰는 특수
  빌드를 위한 escape hatch로 분류했다.
