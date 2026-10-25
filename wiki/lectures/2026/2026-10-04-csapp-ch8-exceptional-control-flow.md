---
type: Lecture
status: done
date: 2026-10-04
topics:
  - exceptional-control-flow
  - processes
  - concurrency
  - virtual-address-space
  - privilege-modes
related_to:
  - "[[memory-hierarchy]]"
artifact_path: wiki/lectures/2026/2026-10-04-csapp-ch8-exceptional-control-flow.html
source_url: https://i.hyeon.me/csapp
---

# 2026-10-04 CSAPP Ch8 Exceptional Control Flow

## 요약과 학습 지도

프로세스는 실행 흐름과 가상 주소 공간을 제공한다. CPU의 예외 처리와 권한 검사는 운영체제가 이 실행 환경을 관리하는 기반이다.

**범위:** CSAPP 3판 §8.1~§8.2.4. **확인 기준일:** 2026-10-25 KST. 노트 생성일과 스터디 세션 표기는 2026-10-04를 유지한다.

[HTML 강의노트](2026-10-04-csapp-ch8-exceptional-control-flow.html) · [실습 소스](../../../examples/0002-exceptional-control-flow/) · [검증 보고서](../../notes/2026-10-25-csapp-ch8-validation.md)

### 목차

- [기초 개념과 교재 대응](#기초-개념과-교재-대응)
- [8.1 Exceptions](#81-exceptions)
- [8.2 Processes: 실행 흐름](#82-processes-실행-흐름)
- [8.2.3 Private Address Space](#823-private-address-space)
- [8.2.4 User and Kernel Modes](#824-user-and-kernel-modes)
- [보충: 서버 동시성과 런타임](#보충-서버-동시성과-런타임)
- [LAB와 적용 로드맵](#lab와-적용-로드맵)
  - [실습 디버깅 사례](#lab-debugging)
- [적용 조건과 미확인](#적용-조건과-미확인)
- [출처](#출처)

**읽는 기준:** `[CSAPP]`는 교재 범위·모델, `[LECTURE]`는 기존 강의 메모에 명시된 확장 범위, `[OFFICIAL]`은 공식 문서·구현, `[COMMENTARY]`는 작성자 설명이다. `[사실]`, `[미확인]`, `[추론]`, `[제안]`은 근거 상태다. LAB의 EXPECTED는 예상이며 OBSERVED는 실제 관측이다.

## 기초 개념과 교재 대응

### ELI10: 실행 순서를 잠시 맡기기

[COMMENTARY] 도서관에서 책을 읽다가 직원에게 대출을 요청한다고 생각해 보자. 읽던 위치를 기억하고 직원의 처리를 기다린 뒤 돌아온다. 예외 처리에서도 CPU와 운영체제는 재개에 필요한 상태를 보존하고 handler로 제어를 옮긴다. 이 비유의 책갈피는 재개 위치를 설명한다. 실제 복귀 주소와 종료 여부는 사건 종류에 따라 달라진다.

| 용어 | 의미 | 확인할 대상 |
|---|---|---|
| Control flow | 명령어 실행 순서 | program counter와 명령어 |
| Exception handler | 사건을 처리하는 OS 코드 | vector·handler·복귀 상태 |
| Process | 실행 중인 프로그램의 인스턴스 | PID·실행 상태·주소 공간 |
| Virtual address | 프로세스가 사용하는 주소 | mapping·접근 권한 |
| Privilege mode | CPU 코드 실행 권한 | user/kernel 경계 |

![프로그램에서 하드웨어까지의 실행 계층과 syscall 경계](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/abstraction-layers.svg)

[OFFICIAL][사실] libc와 언어 runtime은 사용자 공간에서 동작할 수 있다. syscall은 커널 진입 인터페이스다. CPU exception, Linux signal, 언어 exception·panic은 각 계층의 메커니즘이다. syscall을 직접 호출하는 경로도 있다. [S3][S7][S10]

### 교재 절 대응표

[CSAPP][사실] 공식 3판 목차에서 이번 범위는 §8.1~§8.2.4다. §8.2.5 Context Switches는 다음 범위로 연결한다. 아래 쪽수는 북미판 공식 목차의 인쇄 쪽수다. [S1]

| 절 | 교재 주제 | 이 문서의 연결 |
|---|---|---|
| 8.1 · p.723 | Exceptions | 사건과 제어 이전 |
| 8.1.1 · p.724 | Exception Handling | handler 선택·상태 보존 |
| 8.1.2 · p.726 | Classes of Exceptions | 분류·재개 위치 |
| 8.1.3 · p.729 | Linux/x86-64 | #DE·signal·syscall ABI |
| 8.2 · p.732 | Processes | 실행 흐름·주소 공간 |
| 8.2.1 · p.732 | Logical Control Flow | 프로세스별 명령어 흐름 |
| 8.2.2 · p.733 | Concurrent Flows | 타임라인·병렬 실행 |
| 8.2.3 · p.734 | Private Address Space | mapping·보호 |
| 8.2.4 · p.734 | User and Kernel Modes | 권한·커널 진입 |

## 8.1 Exceptions

### 8.1.1 Exception Handling

[CSAPP][사실] 현재 프로그램 바깥의 사건과 명령어 실행 중 생기는 사건을 처리하려면, 정상적인 분기·함수 호출 이외의 제어 이전이 필요하다. CSAPP의 exception 모델은 사건에 따라 운영체제 handler로 제어를 옮긴다. 교재 그림과 CMU 강의 자료에서 이 구조를 확인할 수 있다. [S2][S18]

1. CPU가 사건을 인식한다.
2. 아키텍처에 정의된 진입 규칙으로 상태를 저장하고 handler를 선택한다.
3. 커널 handler가 원인을 처리한다.
4. 복귀 가능한 경우 저장된 상태로 실행을 재개한다. 종료가 필요한 경우 해당 실행을 끝낸다.

![사건 인식에서 handler와 재개 또는 종료까지의 예외 흐름](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/exception-flow.svg)

[OFFICIAL][사실] x86의 interrupt/exception vector와 IDT는 handler 선택에 사용된다. 64비트 `SYSCALL` 진입은 MSR에 설정된 경로를 사용한다. syscall의 구체적인 진입 경로는 ISA 규칙을 함께 읽는다. [S4]

### 8.1.2 Classes of Exceptions

| CSAPP 분류 | 시점·원인 | 재개 모델 | 예 |
|---|---|---|---|
| Interrupt | 비동기 외부 사건 | 중단된 흐름 재개 | 타이머·장치 통지 |
| Trap | 명령어와 동기인 의도적 사건 | 사건 이후 실행 계속 | syscall의 교재 모델 |
| Fault | 명령어와 동기인 처리 필요 상태 | 해결되면 해당 명령 재시도 | page fault |
| Abort | 심각한 상태 오류 | 안정적인 재시작 보장 없음 | double fault |

[CSAPP][사실] 이 표는 교재의 넓은 exception 분류다. [S2][S18]

[OFFICIAL][사실] Intel은 interrupt와 exception을 구분하고, exception을 fault·trap·abort로 분류한다. fault의 저장된 명령어 위치는 원인 명령어를, trap은 해당 명령 실행 이후 이어질 위치를 나타낸다. [S4] (Vol.3A §7.5~§7.6)

> **REMIND** Fault의 재시도 가능성은 원인이 해결됐을 때의 복귀 모델이다. 접근 위반처럼 프로세스 종료로 이어지는 fault도 있다. [S4][S7]

### 8.1.3 Linux/x86-64: divide error와 signal

[OFFICIAL][사실] x86의 `DIV`·`IDIV`는 제수가 0이거나 몫이 목적지 범위에 들어가지 않으면 divide-error fault `#DE`를 발생시킨다. Linux는 산술 오류를 `SIGFPE` 같은 signal로 사용자 프로세스에 전달한다. 기본 동작은 signal 종류와 프로세스 설정에 따라 결정된다. [S4] (Event 0)[S7]

[COMMENTARY] 원인을 조사할 때 `명령어 → CPU 예외 → 커널 처리 → signal → 프로세스 동작`을 따라간다. C의 정수 0 나눗셈 예제에는 undefined behavior가 있으므로, 재현하려면 생성된 명령어와 컴파일 옵션까지 확인해야 한다. 여기서는 해당 fault 실험을 실행하지 않았다. [S19]

### System call과 함수 호출 ABI

[OFFICIAL][사실] ABI는 호출 경계에서 인자·반환값을 전달하는 규약이다. Linux syscall 규약은 아키텍처별로 정의된다. 다음 표의 x86-64는 LP64, arm64는 AArch64 Linux 기준이다. [S3]

| 항목 | x86-64 syscall | arm64 syscall |
|---|---|---|
| 진입 명령 | `syscall` | `svc #0` |
| syscall 번호 | `eax` / `rax` | `w8` / `x8` |
| 인자 1~3 | `rdi`, `rsi`, `rdx` | `x0`, `x1`, `x2` |
| 인자 4~6 | `r10`, `r8`, `r9` | `x3`, `x4`, `x5` |
| 반환 레지스터 | `rax` | `x0` |

[OFFICIAL][사실] System V AMD64 C 함수 호출의 4번째 정수·포인터 인자는 `rcx`다. Linux x86-64 syscall의 4번째 인자는 `r10`이다. `SYSCALL` 명령은 `rcx`와 `r11`을 사용해 복귀 위치·flags를 저장한다. libc wrapper는 커널 오류를 `-1` 반환과 `errno`로 변환한다. [S3][S4][S20]

### stdin·stdout·stderr와 strace

[OFFICIAL][사실] 일반적인 프로세스 시작 상태에서 표준 입력·출력·오류의 file descriptor는 0·1·2다. libc의 `stdin`·`stdout`·`stderr`는 `FILE` stream이며 버퍼링을 제공한다. `write(fd, ...)`는 fd를 사용한다. [S8][S9]

[COMMENTARY] `printf()` 호출 횟수와 `write` syscall 횟수는 버퍼링에 따라 달라진다. strace는 syscall·signal을 관측한다. 아래 출력은 LAB의 EXPECTED 예시다.

```text
write(1, "hello, world\n", 13) = 13
exit_group(0)                  = ?
```

[OFFICIAL][사실] `exit_group`은 호출 프로세스의 thread group을 종료하고 복귀하지 않는다. 종료 syscall과 반환하는 syscall의 복귀 경로는 구분해 읽는다. [S6]

## 8.2 Processes: 실행 흐름

[CSAPP][사실] 프로세스는 실행 중인 프로그램의 인스턴스다. 논리적 제어 흐름과 가상 주소 공간은 프로그램이 시스템을 사용하는 핵심 추상화다. [S18]

### 8.2.1 Logical Control Flow

[CSAPP][사실] 한 프로세스의 program counter가 가리킨 명령어들을 시간순으로 보면 그 프로세스의 logical control flow를 얻는다. CPU가 다른 프로세스를 실행하는 동안에도 이 흐름의 상태가 보존되어 다음 실행으로 이어진다. [S18]

[COMMENTARY] 한 사람이 책 두 권을 번갈아 읽어도 각 책의 읽기 순서는 이어진다. 이 비유는 실행 순서의 보존을 설명한다. 실제 프로세스 상태에는 레지스터·메모리·OS 자원이 포함된다.

### 8.2.2 Concurrent Flows

| 자료 | 동시성의 관점 | 병렬성과의 연결 |
|---|---|---|
| CSAPP·CMU | 두 흐름의 시작~종료 시간 구간이 겹침 | 서로 다른 코어에서 동시에 진행 가능 |
| Rob Pike | 독립 실행 부분을 조합하는 프로그램 구조 | 구조를 실제 동시 실행으로 실현 가능 |
| OSTEP §26 | 한 프로세스 안의 여러 thread와 공유 상태 | 실행 순서·동기화 문제로 연결 |

![단일 코어 교대 실행과 두 코어 병렬 실행의 타임라인](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/concurrency-timeline.svg)

[CSAPP][사실] 단일 코어에서도 흐름의 전체 실행 기간은 겹칠 수 있다. 병렬 실행은 여러 코어에서 같은 시점에 명령어를 진행하는 상황이다. [S18]

[OFFICIAL][사실] Pike는 구조와 실행의 관점을 제시한다. OSTEP은 thread마다 PC·레지스터·stack이 있고 주소 공간을 공유한다는 모델에서 동기화로 전개한다. [S12][S13]

[COMMENTARY] 타임라인의 색 막대는 실제 CPU 실행 구간이다. concurrency는 막대 하나의 겹침과 각 흐름의 시작~종료 구간을 구분해 판단한다. LAB의 출력 순서만으로 서로 다른 코어의 병렬 실행을 입증할 수 있는 범위는 제한된다.

## 8.2.3 Private Address Space

[CSAPP][사실] 프로세스는 자신의 가상 주소 공간을 사용한다. CPU의 MMU와 운영체제가 관리하는 page table이 주소 변환과 접근 보호를 지원한다. [S18][S4]

### ELI10: 건물마다 같은 방 번호

[COMMENTARY] 서로 다른 건물에 모두 101호가 있을 수 있다. 가상 주소는 건물 안의 방 번호에, 주소 변환 상태는 어느 건물인지 알려주는 정보에 비유할 수 있다. 같은 가상 주소도 프로세스의 mapping에 따라 서로 다른 물리 페이지로 이어진다. 공유 mapping은 두 프로세스가 같은 물리 페이지를 사용하는 예다.

| 구분 | 프로세스 A | 프로세스 B |
|---|---|---|
| 가상 주소 예시 | `0x1000` | `0x1000` |
| 변환 상태 | A의 page table | B의 page table |
| 물리 페이지 예시 | X | Y 또는 공유 X |

![프로세스별 가상 주소가 다른 물리 페이지 또는 공유 페이지로 연결되는 구조](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/virtual-address-space.svg)

[OFFICIAL][사실] `MAP_PRIVATE`는 copy-on-write mapping을, `MAP_SHARED`는 공유 mapping을 지정한다. private mapping도 fork 직후 등에는 물리 페이지를 공유할 수 있다. [S15]

### 교재 주소 공간과 실제 mapping

| 영역 | 학습할 역할 | Linux 관측 |
|---|---|---|
| 코드·읽기 전용 데이터 | 명령어·상수 | 실행 파일의 `r-xp`, `r--p` 등 |
| 쓰기 가능한 데이터 | 전역·정적 상태 | 실행 파일의 `rw-p`, anonymous 영역 등 |
| Heap | 동적 할당 | `[heap]`과 별도 mmap 영역 가능 |
| Mapped region | 라이브러리·파일·anonymous mapping | 경로·offset·권한 |
| Stack | 함수 실행 상태 | `[stack]` 등 |
| Kernel 영역 | 커널 자원·보호 | 교재 개념도와 실제 page-table 구성 구분 |

[OFFICIAL][사실] `/proc/PID/maps`는 가상 주소 구간과 접근 권한을 보여준다. `r/w/x`는 읽기·쓰기·실행, `s/p`는 공유·private copy-on-write를 나타낸다. [S10]

[OFFICIAL][사실] pagemap은 가상 페이지 단위 정보를 제공한다. Linux 4.2부터 `CAP_SYS_ADMIN`이 없으면 PFN 필드가 0으로 가려질 수 있다. 파일 접근 자체에도 ptrace 권한 검사가 적용된다. kernel 문서와 man-pages의 비트 설명은 버전별로 대조해야 한다. [S11]

[COMMENTARY] 주소 배치·ASLR·공유 영역·KPTI 설정은 실행 환경의 조건이다. 이 노트의 그림은 주소 변환 모델을 표현하며 특정 커널의 전체 page-table 배치를 확정하지 않는다. 실제 mapping은 LAB에서 확인한다.

## 8.2.4 User and Kernel Modes

[CSAPP][사실] CPU의 실행 권한은 사용자 코드의 접근과 특권 동작을 제한한다. 운영체제는 커널 진입·복귀 경계를 관리한다. [S18]

| 동작 | 권한과 경계 | 예 |
|---|---|---|
| 사용자 함수 호출 | 사용자 공간의 호출 규약 | `main → helper` |
| syscall 진입 | 커널 코드 실행으로 전환 | `write` |
| handler에서 복귀 | 저장된 사용자 상태 복원 | 반환 가능한 syscall |
| context switch | 실행할 task 변경 | 다음 절 §8.2.5의 주제 |

![같은 task의 syscall 진입과 사용자 공간 복귀](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/user-kernel-mode.svg)

[COMMENTARY] syscall이 같은 task로 복귀하는 경우에도 권한 전환은 일어난다. task 변경 여부는 스케줄링의 별도 질문이다.

### x86 특권 명령과 조건

[OFFICIAL][사실] control register에 쓰는 `mov %rax, %cr3`와 `hlt`에는 특권 조건이 있다. `cli`·`sti`는 CPL·IOPL과 가상화 관련 제어 조건에 따라 동작한다. 일반 Linux 사용자 프로그램은 허용된 커널 인터페이스로 권한 동작을 요청한다. [S4] (Vol.2A/2B)

```asm
mov %rax, %cr3    # 주소 변환 상태 변경
hlt               # 특권 조건에서 CPU 대기
```

[OFFICIAL][사실] 전원 끄기 syscall은 `CAP_SYS_BOOT` 등 권한 조건을 검사한다. power-off의 실제 결과는 플랫폼에 의존한다. orderly shutdown의 사용자 공간 서비스 정리와 raw `reboot(POWER_OFF)` 요청은 각 단계의 책임을 구분해야 한다. man-pages는 호출 전 `sync`가 없을 때 데이터 손실 가능성을 명시한다. [S5]

> **WARNING** 이 노트의 종료·전원 제어 설명은 문서 분석이다. 실습에서는 host의 reboot·shutdown을 실행하지 않는다.

## 보충: 서버 동시성과 런타임

[LECTURE] 기존 강의 메모는 C10K, blocking I/O, green thread, Go/Erlang을 확장 주제로 다룬다. 아래 구현 설명과 Rust 보충은 공식 자료 및 작성자 해설로 읽는다.

### 많은 요청이 I/O를 기다릴 때

[OFFICIAL][사실] C10K 원문은 많은 client connection을 다루는 I/O 전략과 OS 설정을 비교한다. 이 명칭은 서버 아키텍처를 설명하는 역사적 문제다. [S14]

| 설계 | 대기 처리 | 조사할 비용·조건 |
|---|---|---|
| Thread-per-request | OS thread가 blocking 대기 가능 | stack·thread 수·스케줄링 |
| Event loop | readiness 통지 뒤 작업 재개 | 긴 handler·blocking 호출 |
| Runtime task | runtime이 task와 thread를 연결 | runtime 지원 I/O·실행 정책 |

![blocking worker와 readiness 기반 task 재개의 비교](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/blocking-io.svg)

[COMMENTARY][추론] 동시 connection 증가 시에는 thread 수·stack·대기 시간·CPU를 함께 측정해야 한다. thread-per-request의 한계 수치는 workload와 환경으로 결정된다. 이 문서는 성능 benchmark 수치를 제시하지 않는다.

### Scheduling과 function coloring

[COMMENTARY] Cooperative scheduling은 task의 yield·대기 지점에서 실행 기회를 넘기는 방식이다. Preemptive scheduling은 scheduler가 실행 중인 작업을 중단할 수 있는 방식이다. green thread는 runtime이 관리하는 실행 단위를 설명할 때 쓰는 용어이며 구체적인 선점·I/O 동작은 구현별로 확인한다.

[COMMENTARY] function coloring은 async 함수와 호출 체인의 작성 제약을 설명하는 비공식 표현이다. Rust에서는 `async` 함수가 Future를 만들고 `.await`로 결과를 기다린다. executor가 future를 poll하며, poll 중 긴 blocking 작업을 하면 해당 worker가 점유된다. [S17]

### Go G/M/P: 실행 중인 G와 I/O 대기 G

[OFFICIAL][사실] 아래 구조는 Go `go1.26.0` runtime 문서·소스를 기준으로 한다. [S16]

| 이름 | 의미 | 연결 |
|---|---|---|
| G | goroutine과 실행 상태 | 실행·runnable·대기 상태 |
| M | OS thread | Go·runtime·syscall 실행 가능 |
| P | 사용자 Go 실행에 필요한 scheduler·allocator 자원 | 수는 `GOMAXPROCS` |

![실행 중인 G P M과 netpoll에서 대기하는 G의 관계](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/go-gmp.svg)

[OFFICIAL][사실] 사용자 Go 코드를 실행하는 M에는 P가 필요하다. 지원되는 network I/O는 netpoll로 G를 대기시킬 수 있다. syscall 진입·복귀와 P 회수는 `entersyscall`, `entersyscallblock`, `retake` 등의 경로에서 관리한다. 모든 blocking call이 같은 경로를 따르는지 확인하려면 호출 구현도 읽어야 한다. [S16]

[OFFICIAL][사실] Go 1.14는 지원 플랫폼에서 비동기 선점을 도입했다. 현재 scheduler를 설명할 때 함수 호출·협력적 대기 지점과 비동기 선점의 조건을 함께 읽는다. [S21]

### Erlang/BEAM과 native blocking

[OFFICIAL][사실] Erlang/OTP `29.1.2` 문서는 긴 NIF 작업이 일반 scheduler의 응답성을 떨어뜨릴 수 있다고 설명한다. 긴 native 작업은 dirty scheduler에 배치할 수 있다. CPU-bound와 I/O-bound 분류를 맞춰야 하며, CPU 작업을 dirty I/O로 잘못 분류하면 일반 scheduler가 영향을 받을 수 있다. [S22]

[COMMENTARY] runtime task의 수와 OS thread 수, native 작업의 배치 위치를 각각 확인한다. Go·BEAM·Rust의 실행 단위를 비교할 때 메모리 격리·선점·I/O 지원도 함께 비교한다.

## LAB와 적용 로드맵

### 환경과 재현 명령

[제안] Linux/x86-64에서 syscall과 mapping을 확인한 뒤 프로그램의 요청 대기 구조를 조사한다. 실습 소스는 [hello.c](../../../examples/0002-exceptional-control-flow/hello.c), [flows.c](../../../examples/0002-exceptional-control-flow/flows.c), [maps.c](../../../examples/0002-exceptional-control-flow/maps.c)다.

```bash
# 레포 root에서 실행
# macOS ARM64에서는 Docker의 amd64 실행 지원이 필요하다.
docker build --platform linux/amd64 -t csapp-ch8-lab:amd64 \
  examples/0002-exceptional-control-flow
docker run --rm --platform linux/amd64 --cap-add=SYS_PTRACE \
  csapp-ch8-lab:amd64
```

### LAB 1: syscall·종료 경계

1. `hello.c`를 `gcc -std=c11 -Wall -Wextra -Werror -O0 -g`로 컴파일한다.
2. `strace -qq -e trace=write,exit_group ./hello`를 실행한다.
3. fd·길이·반환값과 `exit_group`을 읽는다.

[EXPECTED] `write(1, "hello, world\n", 13) = 13`과 종료 호출이 보인다. 짧은 문자열 출력 성공을 검사하는 학습 예제다. 일반 출력 루프는 partial write와 `EINTR` 처리도 필요하다. [S8]

### LAB 2: 두 프로세스 흐름

1. `flows.c`가 `fork`로 만든 부모·자식을 확인한다.
2. 각 PID의 `step=0,1,2` 순서를 읽는다.
3. 전체 출력 순서를 다시 실행해 비교한다.

[EXPECTED] 부모·자식이 각각 세 줄을 출력한다. 매 실행의 교차 순서는 달라질 수 있다. CPU 코어의 동시 사용은 별도 tracing·CPU affinity 조건을 갖춰 측정한다.

### LAB 3: 주소와 mapping

1. `maps.c`가 `MAP_PRIVATE|MAP_ANONYMOUS`로 만든 주소와 PID를 출력한다.
2. 실행 중 `/proc/PID/maps`에서 그 주소를 포함한 구간을 찾는다.
3. `rw-p`와 anonymous mapping을 확인한다.
4. pagemap의 present 비트를 읽고 PFN 접근 조건을 기록한다.

[EXPECTED] 쓰기 후 해당 가상 페이지의 mapping과 상태가 보인다. PFN이 0이면 권한으로 가려졌을 가능성을 먼저 확인한다. 이 결과만으로 물리 주소를 확정하지 않는다. [S10][S11]

### 관측과 다음 적용

[사실][OBSERVED] 2026-10-25에 OrbStack Linux에서 다음 결과를 얻었다. GCC 12.2.0, strace 6.1, Debian bookworm 컨테이너 기준이다.

| 실행 환경 | 검사 | 관측 |
|---|---|---|
| Linux ARM64 native | 전체 LAB | write 13바이트·exit_group, 부모/자식 각 3줄, rw-p, present=1·PFN=0 |
| Linux x86-64 에뮬레이션 | 세 C 예제 컴파일·실행 | 성공. syscall register 직접 관측 없음 |
| OrbStack amd64 userspace | strace | syscall trace 불완전. 전체 LAB 실패 |
| QEMU full-system x86-64 | 전체 LAB | write·exit_group·부모/자식·rw-p·pagemap PASS |

QEMU 검증은 Linux 6.18.58-0-lts x86-64 커널, Debian userspace, GCC 12.2.0, strace 6.1에서 수행했다. 이는 전체 시스템 에뮬레이션의 관측이다. 실제 x86 하드웨어 성능 측정과는 환경이 다르다. macOS에서 `python3 tools/ch8/qemu-lab.py`로 재현할 수 있다.

ARM64 전체 LAB을 재현하려면 위 두 명령의 `linux/amd64`와 이미지 tag를 각각 `linux/arm64`, `csapp-ch8-lab:arm64`로 바꾼다. ISA별 syscall ABI는 공식 표로 확인하고 실험 환경과 구분한다.

실제 환경·명령·전체 출력은 [검증 보고서](../../notes/2026-10-25-csapp-ch8-validation.md)에 기록한다. 다음 단계는 `syscall/signal 관측 → PID·mapping 확인 → runtime 대기 위치 확인 → 가설 → 측정`이다.

| 단계 | 할 일 | 도구 |
|---|---|---|
| 1 | 커널 경계에서 사건 기록 | strace |
| 2 | 주소와 접근 권한 확인 | maps·pmap·gdb |
| 3 | worker와 I/O 대기 조사 | thread 목록·runtime profiler |
| 4 | 변경 전후 같은 부하 비교 | 지연·CPU·메모리 측정 |

### 확인 질문

- **같은 가상 주소의 데이터는?** 프로세스의 mapping과 공유 관계로 결정된다.
- **syscall과 context switch의 관계는?** syscall은 커널 진입이다. 다른 task 실행 여부는 스케줄링 결과로 확인한다.
- **fault 뒤 같은 명령어를 재실행하는 이유는?** 원인을 해결한 뒤 해당 메모리 접근 등을 다시 수행하기 위해서다.
- **Go에서 P와 CPU 코어는?** P는 runtime 자원이다. 실제 코어 실행 배치는 OS scheduler와 실행 환경에 따른다.

### 심층 사례: strace 관측 실패 디버깅

[사실][OBSERVED] 프로그램은 출력했지만 syscall trace가 비어 있었다. Orb의 기존 Ubuntu amd64에서 재현한 뒤, **x86-64 커널을 부팅한 QEMU에서 전체 LAB을 두 번 통과했다.**

이 사례는 앞의 syscall·프로세스·주소 공간 LAB을 검증하는 과정이다. 아래에서는 관측한 사실, 세운 가설, 확인 실험, 실행 환경 선택의 근거를 차례로 연결한다. LAB의 관측 공백은 해소했고, Orb/Rosetta 내부의 정확한 결함 위치는 미확인이다.

표기: [관측]은 실제 실행, [공식]은 1차 자료, [가설]은 추론, [결정]은 조치다. [판정]·[해석]은 관측에 대한 작성자 설명이다. 예상은 EXPECTED로 표시한다.

#### 1. 먼저 무엇을 검증하려 했는가

##### LAB의 목표

학습 목표는 Linux 프로그램이 커널 기능을 호출하는 경계를 직접 보는 것이다.
다음 세 예제를 사용했다.

| 예제 | 프로그램의 동작 | 확인할 근거 |
|---|---|---|
| hello.c | stdout에 문자열 출력 | write의 fd·길이·반환값, exit_group |
| flows.c | fork로 부모·자식 생성 | 각 PID가 step 0→1→2 출력 |
| maps.c | private anonymous mapping 생성·쓰기 | 주소를 포함하는 rw-p 구간, pagemap |

[소스와 실행 스크립트](../../../examples/0002-exceptional-control-flow/)를 함께 읽으면 된다.

`hello.c`의 핵심은 다음과 같다.

```c
static const char message[] = "hello, world\n";
ssize_t written = write(STDOUT_FILENO, message, sizeof message - 1);
```

문자열은 newline을 포함해 13바이트다. 전체 코드의 반환값 검사는 이 짧은 출력이 성공했는지 확인한다.
일반적인 출력 코드는 partial write와 `EINTR`도 처리해야 한다. [공식: write(2)](https://man7.org/linux/man-pages/man2/write.2.html)

##### 성공 조건을 구체적인 문장으로 쓰기

| 검사 | 성공 조건 |
|---|---|
| 컴파일 | 컴파일러 종료 코드 0 |
| 실행 | 프로그램이 예상 문자열을 출력 |
| 관측 | strace에 write와 exit_group이 나타남 |
| 해석 | fd=1·요청 13바이트·반환 13을 읽을 수 있음 |

`./hello`의 성공은 두 번째 행까지의 근거다. syscall 관측에는 세 번째 행의 출력이 필요하다.

#### 2. strace는 어떤 경계를 관측하는가

**[공식]** `ptrace`는 한 프로세스가 다른 프로세스의 실행과 레지스터 등을 관측·제어할 수 있게 하는
Linux 인터페이스다. syscall tracing과 debugger 구현에 사용한다.
[ptrace(2)](https://man7.org/linux/man-pages/man2/ptrace.2.html)

**[공식]** strace는 프로그램의 syscall과 signal을 추적한다.
[프로젝트 문서](https://strace.io/)와 [strace(1)](https://man7.org/linux/man-pages/man1/strace.1.html)

##### ELI10: 달리는 차와 계기판

자동차가 움직이는지 확인하는 일과 계기판이 속도를 정확하게 표시하는지 확인하는 일은 서로 다른 검사다.
프로그램의 출력은 차가 움직였다는 관측에, strace 출력은 계기판의 관측에 대응한다.
실제 strace는 커널 tracing 인터페이스와 아키텍처별 상태를 사용한다. 계기판 비유는 이 구현 세부까지 설명하지 않는다.

![프로그램의 출력 확인과 kernel tracing을 통한 관측 확인의 두 경로](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/debug-observation-paths.svg)

#### 3. 첫 증상: 출력은 있고 trace는 없다

##### 기대한 결과

다음은 EXPECTED 예시다. 공백 정렬은 strace 버전에 따라 달라질 수 있다.

```text
write(1, "hello, world\n", 13) = 13
exit_group(0)                  = ?
```

##### 실제 관측

**[관측]** OrbStack Docker의 Linux/amd64 환경에서 hello 프로그램은 실행됐다.
필터링된 trace에는 필요한 syscall 행이 나타나지 않아 LAB이 멈췄다.

[당시 로그](../../../validation/ch8/2026-10-25/lab-amd64-strace-incomplete.txt):

```text
Linux 7.2.12-orbstack-00394-gbecdd6130068 x86_64 GNU/Linux
gcc (Debian 12.2.0-14+deb12u1) 12.2.0
strace -- version 6.1
hello, world
```

**[관측]** ARM64 native 컨테이너에서는 같은 예제·검사 스크립트가 통과했다.
[ARM64 전체 로그](../../../validation/ch8/2026-10-25/lab-arm64.txt)

**[가설]** 프로그램 로직 이외에 tracing 도구와 실행 환경의 조합을 조사할 필요가 있다.
ARM64 통과는 스크립트가 적어도 한 환경에서 정상 동작했다는 근거다.
x86-64 전체 환경에서의 정확성을 보장하는 근거는 별도 검사가 필요하다.

#### 4. orb CLI로 기존 Ubuntu를 직접 확인했다

##### 환경을 먼저 목록화한다

실제로 사용한 조회 명령은 다음과 같다.

```sh
command -v orb orbctl
orbctl list
orbctl info ubuntu
orb -m ubuntu uname -a
orb -m ubuntu sh -c 'command -v gcc; command -v strace; command -v python3'
```

**[관측]** 이미 실행 중인 `ubuntu` 머신이 있었다.
`orbctl info`는 Ubuntu 25.04, architecture amd64를 표시했다.
처음에는 Python만 있었고 GCC와 strace가 없었다.

##### 첫 실패는 도구 부재였다

```text
verify.sh: gcc: not found
verify.sh: strace: not found
```

**[판정]** 이 결과는 필수 도구가 준비되지 않았다는 뜻이다.
이 단계의 로그로 tracing 호환성을 평가할 수 있는 범위는 없다.

**[결정]** 해당 Ubuntu에 필요한 패키지를 설치하고 같은 LAB을 다시 실행했다.
실제 설치는 이 머신의 시스템 패키지에 변화를 만든다.

```sh
orb -m ubuntu -u root sh -c \
  'apt-get update && apt-get install -y gcc libc6-dev strace'
orb -m ubuntu sh examples/0002-exceptional-control-flow/verify.sh
```

##### 도구 설치 뒤의 실패를 따로 기록한다

**[관측]** GCC 14.2.0·strace 6.14가 준비된 뒤에도 LAB은 exit 1이었다.
[Orb amd64 로그](../../../validation/ch8/2026-10-25/lab-orb-amd64.txt):

```text
Linux 7.2.12-orbstack-00394-gbecdd6130068 x86_64 GNU/Linux
gcc (Ubuntu 14.2.0-19ubuntu2) 14.2.0
strace -- version 6.14
hello, world
```

##### 예제를 더 작게 줄였다

```sh
orb -m ubuntu strace -e trace=write,exit_group /bin/echo hello
```

**[관측]** 아래 mode 메시지가 반복됐고 마지막에 `hello`와 종료 코드 0이 나타났다.
필요한 `write`·`exit_group` trace는 나타나지 않았다.
아래는 직접 관측한 출력 일부이며 전체 반복 횟수는 생략했다. [최소 재현 trace](../../../validation/ch8/2026-10-25/debug-note/orb-echo-trace.txt)에서 전체 결과를 확인한다.

```text
[ Process PID=2682 runs in x32 mode. ]
[ Process PID=2682 runs in 64 bit mode. ]
...
hello
+++ exited with 0 +++
```

**[추론]** 사용자 C 예제를 `/bin/echo`로 줄여도 증상이 유지됐다.
따라서 조사 우선순위를 예제의 C 로직에서 tracing·실행 환경의 조합으로 옮겼다.

이 메시지만으로 프로그램이 실제 x32 ABI로 반복 전환했다고 결론 내리지 않았다.
관측 도구가 보고한 상태와 실제 target 상태를 대조할 추가 근거가 필요하기 때문이다.

#### 5. 왜 uname의 x86_64만으로 충분하지 않았는가

**[관측]** 다음 설정과 등록 상태를 조회했다.

```sh
orbctl config get rosetta
orb -m ubuntu cat /proc/sys/fs/binfmt_misc/rosetta
```

설정 값은 `true`, 등록된 interpreter 표기는 `[rosetta]`였다.

**[공식]** OrbStack은 Apple Silicon에서 x86/amd64 Linux 머신의 Intel 바이너리를 Rosetta로 실행한다고 설명한다.
[Linux machines의 Intel emulation](https://docs.orbstack.dev/machines/#intel-x86-emulation)

**[공식]** `rosetta` 설정은 Intel 코드 실행에 Rosetta를 사용할지 제어한다.
[OrbStack settings](https://docs.orbstack.dev/settings#use-rosetta-to-run-intel-code)

**[해석]** `amd64` 이미지·실행 파일·호환 환경과 x86-64 커널 전체 부팅은 구분해서 기록해야 한다.
`uname`은 환경 식별의 단서이며, 이번 실행에서 tracing 경계가 기대대로 동작하는지는 실제 trace로 검사한다.

![amd64 userspace 번역 실행과 x86-64 kernel을 포함한 full-system 실행의 비교](../../assets/2026-10-04-csapp-ch8-exceptional-control-flow/debug-environment-layers.svg)

#### 6. 가설을 어떻게 좁혔는가

| 가설 | 확인 방법 | 실제 결과 | 그때의 판단 |
|---|---|---|---|
| 필요한 도구가 없다 | command -v와 버전 조회 | Orb에 GCC·strace가 없었음 | 설치 후 재검사 |
| C 예제 구현 문제다 | /bin/echo로 축소 | 같은 trace 누락 재현 | 환경·도구 조합 조사 우선 |
| 단순한 출력 공백 차이다 | 실제 trace 내용 확인 | syscall 행 자체가 없음 | 공백 정규식으로 해결할 대상과 구분 |
| strace 6.1에만 생긴다 | Orb의 6.14로 실행 | 같은 증상 | 6.1 한 버전에 국한된 가설의 설명력 감소 |
| tracing과 번역 실행의 경계에 호환성 문제가 있다 | 설정 확인·환경 비교 | Rosetta 사용 상태, ARM64 통과 | full-system 대조 실험 선택 |
| x86-64 LAB 자체를 실행할 수 없다 | x86-64 kernel 부팅 후 실행 | 전체 LAB 두 번 PASS | 검증 가능한 실행 경로 확보 |

초기 Docker 실험에는 **별도의 검사 코드 문제**도 있었다.
`write(...) = 13`의 공백 수를 고정해 비교하던 부분을 정규식으로 고쳤다.
ARM64 trace에는 syscall 행이 있었으므로 이 수정으로 검사를 진행할 수 있었다.
Orb amd64의 syscall 행 누락은 그 수정 뒤에도 남았다.

```sh
# 공백 정렬을 허용하면서 필요한 의미를 검사한다.
grep -E 'write\(1, "hello, world\\n", 13\)[[:space:]]+= 13' trace.txt
```

**[판정]** 프로그램 실행, trace 생성, trace 해석, 검사 스크립트의 포맷 가정을 나눠야 한다.
이번 사례에서는 공백 비교와 trace 누락을 각각 수정·조사했다.

#### 7. 왜 QEMU full-system을 선택했는가

**[목표]** x86-64 Linux의 syscall·프로세스·mapping LAB을 실제로 통과시키고 근거를 남기는 것이다.
이 단계의 목표는 Orb 내부 구현의 결함 위치를 찾는 것과 구분했다.

| 선택지 | 얻을 수 있는 것 | 비용·영향 | 선택 |
|---|---|---|---|
| ARM64 통과 결과 사용 | ARM64 동작 근거 | x86-64 관측 공백이 남음 | 보조 근거 유지 |
| Orb amd64 실행 | 기존 환경을 활용한 직접 재현 | 이번 조합에서 trace 불완전 | 실행하고 실패 기록 |
| 전역 Rosetta 설정 변경 | 번역 방식의 다른 조건 비교 | 다른 머신·컨테이너에 영향 가능 | 이번 실험에서 유지 |
| 별도 QEMU x86-64 system | x86-64 kernel 안에서 ptrace·syscall 관측 | boot·rootfs 구성과 실행 시간 | 채택 |
| 실제 x86-64 컴퓨터 | 실제 하드웨어의 관측 | 별도 접근 환경 필요 | 이번 기록의 실행 범위 밖 |

**[공식]** QEMU system emulation은 CPU·메모리·장치 모델을 제공해 guest OS를 실행한다.
TCG는 여러 CPU 아키텍처를 에뮬레이션할 수 있는 실행 엔진이다.
[QEMU Introduction](https://www.qemu.org/docs/master/system/introduction.html)

**[결정 근거]** host에 이미 `qemu-system-x86_64`가 있었고, 기존 Debian LAB image를 rootfs로 재사용할 수 있었다.
별도 guest를 만들면 Orb의 전역 설정을 바꾸지 않고 x86-64 커널의 관측 경로를 검사할 수 있었다.

이 비교에서는 커널·부팅 방식 등 여러 조건이 함께 바뀐다.
따라서 특정 Rosetta 함수나 특정 커널 코드가 원인이라고 분리해 증명한 실험으로 해석하지 않는다.

#### 8. QEMU 환경을 어떻게 만들었는가

##### 용어 세 개

| 용어 | 이번 작업에서의 의미 |
|---|---|
| rootfs | guest 안의 /bin, /lib, /usr, LAB 소스 등을 담은 파일 트리 |
| initramfs | 커널이 초기 파일시스템으로 사용할 cpio archive |
| init | 커널이 처음 실행할 사용자 프로그램. 이번 guest는 작은 전용 init 사용 |

##### 구성 순서와 이유

1. **기존 Dockerfile로 amd64 LAB image를 준비한다.** GCC·strace·Python·예제의 사용자 공간 구성을 재사용한다.
2. **image의 파일 트리를 export한다.** Docker를 rootfs를 만드는 도구로 사용한다.
3. **x86-64 Linux kernel을 준비한다.** Alpine의 공식 v3.23 netboot kernel을 사용했고 관측 버전은 6.18.58-0-lts다.
4. **작은 init을 정적 링크로 컴파일한다.** early boot에서 필요한 mount와 LAB 실행을 담당한다.
5. **rootfs를 cpio+gzip으로 묶는다.** 이를 initramfs로 커널에 전달한다.
6. **QEMU를 TCG·q35·CPU max로 실행한다.** guest CPU 2개·RAM 1536 MiB를 설정하고 guest network는 끈다.
7. **guest serial log와 LAB 종료 코드를 확인한다.** 출력 문구와 프로세스 종료 상태를 함께 평가한다.

[init 소스](../../../tools/ch8/qemu-init.c)는 `/dev`, `/proc`, `/sys`를 mount한 다음 LAB을 실행한다.
`/proc`는 mapping·pagemap 검사에 필요하다. init의 mount 호출 자체는 개별 반환값을 검사하지 않으므로,
최종 LAB의 `/proc` 읽기 성공까지 함께 확인했다.

정적 링크 init은 필요한 라이브러리를 실행 파일에 포함한다.
이 선택은 초기 부팅 시 동적 loader 경로에 대한 의존을 줄인다.

실제 사용한 QEMU 옵션의 핵심은 다음과 같다.

```sh
qemu-system-x86_64 \
  -accel tcg -machine q35 -cpu max -m 1536 -smp 2 \
  -nographic -no-reboot -nic none \
  -kernel /path/to/vmlinuz \
  -initrd /path/to/root.cpio.gz \
  -append 'console=ttyS0 rdinit=/init loglevel=3'
```

`console=ttyS0`는 serial console로 로그를 받고, `rdinit=/init`은 initramfs의 전용 init을 실행하도록 지정한다. [Kernel parameters](https://docs.kernel.org/admin-guide/kernel-parameters.html)
`-nic none`은 guest의 가상 network 장치를 만들지 않는다. [QEMU Invocation](https://www.qemu.org/docs/master/system/invocation.html) kernel·패키지 준비 단계에는 host의 인터넷 연결을 사용한다.

**[재현]** 위 파일 구성을 직접 만들지 않아도 다음 명령으로 같은 절차를 실행할 수 있다.

```sh
# 레포 root에서. Docker와 QEMU가 준비되어 있어야 한다.
python3 tools/ch8/qemu-lab.py /tmp/csapp-x86-result.txt
```

[재현 도구](../../../tools/ch8/qemu-lab.py)는 guest process와 임시 rootfs를 정리한다.
완료 시 init은 결과 marker를 출력하고 대기하며, host의 실행 도구가 QEMU process를 종료한다.
실제 host의 shutdown·reboot는 실행하지 않는다.

#### 9. 실제로 무엇을 확인했는가

**[관측]** 첫 실행의 핵심 결과다. 주소·PID·PFN은 그 실행의 값이다.

```text
Linux 6.18.58-0-lts x86_64 GNU/Linux
gcc (Debian 12.2.0-14+deb12u1) 12.2.0
strace -- version 6.1
write(1, "hello, world\n", 13)          = 13
exit_group(0)                           = ?
...
maps: 7fc809dee000-7fc809df1000 rw-p 00000000 00:00 0
pagemap: present=1 PFN=255844
PASS: write/exit_group, parent-child flows, private writable mapping
CSAPP_QEMU_EXIT=0
```

발췌문의 `...`는 부모·자식 출력 등을 생략한 표시다.
[첫 실행 전체 로그](../../../validation/ch8/2026-10-25/lab-x86-system.txt)에서 생략 없는 결과를 확인한다.

| 관측 | 해석 | 남는 조건 |
|---|---|---|
| write의 fd=1 | 표준 출력 fd 사용 | 실제 연결 대상은 실행 환경에 의존 |
| 요청 13·반환 13 | 해당 호출에서 13바이트 출력 성공 | 일반적인 모든 출력 성공 보장 범위는 별도 |
| exit_group(0) | thread group 종료 경계 관측 | 사용자 코드로 반환하는 값과 구분 |
| 부모·자식 각 3줄 | 두 프로세스 실행과 각각의 순서 확인 | 출력만으로 두 코어의 동시 실행 입증 범위 제한 |
| rw-p | 읽기·쓰기 가능한 private mapping | private mapping과 물리 페이지 독점은 다른 조건 |
| present=1·PFN 표시 | 해당 guest 권한으로 페이지 정보 확인 | PFN은 guest 물리 페이지 기준이며 host 주소와 구분 |
| LAB exit 0 | 작성한 검사 모두 통과 | 범위 밖 runtime·성능 비교까지 보장하지 않음 |

**[관측]** 재현 도구를 저장한 뒤 다시 실행해 같은 검사들을 통과했다.
두 번째 PFN은 `329732`였다. 고정 주소를 성공 조건으로 사용하지 않고 mapping 권한·상태를 검사했다.
[재실행 로그](../../../validation/ch8/2026-10-25/lab-x86-system-reproduced.txt)

kernel URL·SHA-256·image ID·QEMU 버전은 [환경 기록](../../../validation/ch8/2026-10-25/x86-system-environment.json)에 있다.
현재 재현 도구의 kernel URL과 Debian package는 업데이트될 수 있다. 과거와 동일한 바이트를 재현하려면 이 기록의 hash·image ID도 대조해야 한다.

#### 10. 해결한 범위와 남은 질문

##### 확인된 결론

- Orb CLI의 기존 Ubuntu amd64를 실제로 시도했고 tracing 증상을 재현했다.
- x86-64 커널을 포함한 QEMU full-system에서 같은 LAB을 두 번 통과했다.
- x86-64 syscall LAB의 관측 공백을 대체 실행 경로로 해소했다.
- 글로벌 Rosetta 설정은 유지했다.

##### 근본 원인에 대한 현재 판단

**[가설]** 해당 Orb amd64 번역 실행과 syscall tracing 사이의 호환성 문제가 유력한 조사 방향이다.
근거는 예제를 줄여도 유지된 증상, 두 strace 버전에서의 재현, 실제 번역 설정,
ARM64와 QEMU full-system에서의 성공이다.

**[미확인]** 정확히 어느 계층의 어떤 구현이 잘못됐는지는 분리해서 검증하지 않았다.
Rosetta 설정을 바꾼 A/B, `ptrace` 요청·반환값·레지스터 구조의 비교,
OrbStack·Rosetta·strace의 더 작은 재현 코드와 버전별 대조가 후속 조사다.

**[중단 기준]** 이 작업의 LAB 검증 목표는 충족됐다.
Orb 자체의 근본 원인 분석은 별도 목표와 실험 범위를 정해 이어갈 수 있다.

#### 11. 다른 문제에 적용하는 디버깅 순서

1. **성공 조건을 쪼갠다.** 실행·관측·해석의 각 결과가 무엇이어야 하는지 적는다.
2. **도구와 환경을 목록화한다.** 실행 파일 경로, 버전, ISA, 커널, 권한, 번역 계층을 확인한다.
3. **실패한 지점을 좁힌다.** 명령 부재·권한 오류·빈 결과·포맷 불일치를 구분한다.
4. **입력을 최소화한다.** 사용자 프로그램에서 표준 명령으로 바꿔 증상이 유지되는지 본다.
5. **가설마다 검사와 판정을 연결한다.** 예상과 실제 결과를 기록한다.
6. **목표에 맞는 대조 환경을 선택한다.** 바꾼 조건과 그 때문에 남는 한계도 적는다.
7. **재실행 가능한 근거를 남긴다.** 명령·환경·원본 로그·종료 코드·성공 조건을 보관한다.
8. **결론의 범위를 제한한다.** 검증 경로 확보와 도구 자체의 원인 규명을 각각 보고한다.

##### 디버깅 사례 확인 질문

- **프로그램이 출력하고 종료 코드 0이면 LAB도 성공인가?** LAB이 요구한 trace와 상태 검사까지 확인한다.
- **uname이 x86_64면 ptrace 호환성도 확인된 것인가?** 실제 tracing 결과와 커널·번역 실행 경로를 함께 확인한다.
- **QEMU에서 통과하면 Rosetta 결함을 확정할 수 있는가?** 여러 환경 조건이 함께 바뀌었으므로 구체적인 결함 위치는 추가 실험으로 좁힌다.
- **왜 동일 PID·주소·PFN을 기대하지 않는가?** 실행마다 변하는 값이며 검사의 목적은 관계·권한·동작을 확인하는 데 있다.



## 적용 조건과 미확인

| 항목 | 상태 | 읽는 조건 |
|---|---|---|
| 교재 절·인쇄 쪽수 | 사실 | 북미판 공식 목차. 번역판 쪽수와 구분 |
| Exception 분류 | 조건부 일치 | CSAPP의 넓은 exception 용법과 Intel의 interrupt/exception 구분 |
| Syscall 레지스터 | 사실 | Linux LP64 x86-64·AArch64 표. 다른 ABI는 별도 확인 |
| 주소 공간 그림 | 해설 | 개념 mapping. 실제 주소·kernel mapping 설정은 환경 의존 |
| Runtime 설명 | 사실·해설 | Go go1.26.0·OTP 29.1.2·Rust 문서. 세 runtime의 성능 비교 미실행 |
| Divide fault·전원 제어 | 미실행 | 자료 검증. 실제 fault 재현과 host 전원 제어 실행 없음 |
| 강의 세부 발언 | 미확인 | 이 문서는 기존 노트의 확장 범위를 보존. 원 녹취 대조 없음 |

## 출처

확인 날짜는 모두 **2026-10-25 KST**다. 동적 문서의 변경 가능성과 아래 명시 버전을 함께 읽는다. 링크를 열어 필요한 절로 이동한다.

- **[S1] CSAPP 3판 공식 목차·서문**: [TOC PDF](https://csapp.cs.cmu.edu/3e/pieces/preface3e.pdf), Ch8 절·북미판 쪽수. [정오표](https://csapp.cs.cmu.edu/3e/errata.html)도 함께 확인.
- **[S2] CSAPP 공식 그림 목록**: [Chapter 8 figures](https://csapp.cs.cmu.edu/3e/figures.html), exception·trap·fault 도식.
- **[S3] Linux syscall ABI**: [syscall(2)](https://man7.org/linux/man-pages/man2/syscall.2.html), Architecture calling conventions·wrapper.
- **[S4] Intel SDM**: [공식 다운로드](https://www.intel.com/content/www/us/en/developer/articles/technical/intel-sdm.html), revision 093. [Vol.3 PDF](https://cdrdv2.intel.com/v1/dl/getContent/671447) §7.5~§7.6·Event 0, Vol.2A/2B의 CLI·STI·HLT·MOV·SYSCALL.
- **[S5] Linux 전원 제어**: [reboot(2)](https://man7.org/linux/man-pages/man2/reboot.2.html), POWER_OFF·권한·RETURN VALUE.
- **[S6] Thread group 종료**: [exit_group(2)](https://man7.org/linux/man-pages/man2/exit_group.2.html), DESCRIPTION·RETURN VALUE.
- **[S7] Linux signal**: [signal(7)](https://man7.org/linux/man-pages/man7/signal.7.html), signal disposition·SIGFPE.
- **[S8] 출력 syscall**: [write(2)](https://man7.org/linux/man-pages/man2/write.2.html), 반환 길이·오류.
- **[S9] 표준 stream**: [stdio(3)](https://man7.org/linux/man-pages/man3/stdio.3.html), stream·버퍼링·표준 fd.
- **[S10] 가상 주소 mapping**: [proc_pid_maps(5)](https://man7.org/linux/man-pages/man5/proc_pid_maps.5.html), man-pages 6.19·권한 필드.
- **[S11] 페이지 관측**: [proc_pid_pagemap(5)](https://man7.org/linux/man-pages/man5/proc_pid_pagemap.5.html), man-pages 6.19. [Kernel pagemap](https://docs.kernel.org/admin-guide/mm/pagemap.html), PFN 권한·버전별 비트 정의.
- **[S12] Rob Pike 원발표**: [Concurrency is not Parallelism](https://go.dev/talks/2012/waza.slide), 2012-01-11, structure·execution.
- **[S13] OSTEP 공식 교재**: [§26 Concurrency: An Introduction](https://pages.cs.wisc.edu/~remzi/OSTEP/threads-intro.pdf), thread·공유 주소 공간.
- **[S14] C10K 원문**: [The C10K problem](https://www.kegel.com/c10k.html), 역사적 I/O 전략 비교.
- **[S15] Mapping 의미**: [mmap(2)](https://man7.org/linux/man-pages/man2/mmap.2.html), MAP_PRIVATE·MAP_SHARED.
- **[S16] Go go1.26.0 runtime**: [HACKING.md](https://github.com/golang/go/blob/go1.26.0/src/runtime/HACKING.md), G/M/P. [proc.go](https://github.com/golang/go/blob/go1.26.0/src/runtime/proc.go), entersyscall·entersyscallblock·retake·netpoll.
- **[S17] Rust async**: [The Rust Book §17](https://doc.rust-lang.org/book/ch17-00-async-await.html), async·await·future·runtime와 [동시 실행](https://doc.rust-lang.org/book/ch17-02-concurrency-with-async.html), executor·blocking.
- **[S18] CMU 공식 강의**: [Exceptions and Processes](https://www.cs.cmu.edu/afs/cs/academic/class/15213-m22/www/lectures/14-ecf-procs.pdf), 교재 동반 강의 자료. 이 스터디 강의자의 발언과는 출처를 구분.
- **[S19] C11 위원회 초안**: [N1570 PDF](https://www.open-std.org/jtc1/sc22/wg14/www/docs/n1570.pdf), §6.5.5의 정수 나눗셈 조건.
- **[S20] AMD64 psABI**: [공식 프로젝트](https://gitlab.com/x86-psABIs/x86-64-ABI), commit `e1ce098331da5dbd66e1ffc74162380bcc213236`의 `x86-64-ABI/low-level-sys-info.tex`, System V 함수 호출 규약 §3.2.3.
- **[S21] Go 1.14 릴리스 노트**: [runtime](https://go.dev/doc/go1.14#runtime), 비동기 선점·플랫폼 조건.
- **[S22] Erlang/OTP 29.1.2**: [erl_nif](https://www.erlang.org/doc/apps/erts/erl_nif.html), Long-running NIFs·Dirty NIFs.

- **[S23] OrbStack Linux machines**: [공식 문서](https://docs.orbstack.dev/machines/), Intel/amd64 실행 방식.
- **[S24] OrbStack settings**: [rosetta 설정](https://docs.orbstack.dev/settings), 번역 실행 설정의 의미.
- **[S25] QEMU system introduction**: [CPU·memory·device·TCG](https://www.qemu.org/docs/master/system/introduction.html).
- **[S26] Linux tracing**: [ptrace(2)](https://man7.org/linux/man-pages/man2/ptrace.2.html), 프로세스·레지스터·syscall 관측.
- **[S27] strace**: [공식 프로젝트](https://strace.io/)와 [strace(1)](https://man7.org/linux/man-pages/man1/strace.1.html), 옵션·trace 출력.
- **[S28] QEMU Invocation**: [실행 옵션](https://www.qemu.org/docs/master/system/invocation.html), guest 구성·network·kernel 부팅.
- **[S29] Linux kernel parameters**: [공식 문서](https://docs.kernel.org/admin-guide/kernel-parameters.html), console·rdinit.
