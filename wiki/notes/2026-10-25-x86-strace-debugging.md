---
type: Note
status: done
date: 2026-10-25
topics:
  - "[[exceptional-control-flow]]"
  - "[[virtualization]]"
  - "[[debugging]]"
related_to:
  - "[[2026-10-04-csapp-ch8-exceptional-control-flow]]"
  - "[[2026-10-25-csapp-ch8-validation]]"
source_url: https://www.qemu.org/docs/master/system/introduction.html
artifact_path: wiki/notes/2026-10-25-x86-strace-debugging.html
---

# 실행은 되는데 strace가 비어 있을 때

## 요약: 실행 경로와 관측 경로를 따로 확인한다

**확인 기준일: 2026-10-25 KST.** CSAPP Ch8 LAB을 macOS ARM64에서 검증하며 겪은 사례다.

프로그램은 `hello, world`를 출력했지만, 기대한 `write` syscall trace는 나타나지 않았다.
OrbStack의 Docker amd64 환경과 `orb` Ubuntu amd64 환경에서 이 현상을 재현했다.
이후 x86-64 커널을 포함한 QEMU full-system 환경에서 같은 LAB을 두 차례 통과했다.

이 사례의 핵심은 **프로그램 실행 성공과 도구의 관측 성공을 각각 검사하는 것**이다.
검증을 마친 뒤에도 Orb/Rosetta 내부의 정확한 결함 위치는 미확인으로 남았다.
해결한 범위는 x86-64 LAB을 신뢰할 수 있게 실행하고 결과를 기록하는 일이다.

[HTML 학습 노트](2026-10-25-x86-strace-debugging.html) · [Ch8 강의노트](../lectures/2026/2026-10-04-csapp-ch8-exceptional-control-flow.html) · [검증 보고서](2026-10-25-csapp-ch8-validation.md)

본문의 **[관측]**은 실제 실행 결과, **[공식]**은 1차 문서의 설명,
**[가설]**은 결과를 설명하려고 세운 추론, **[결정]**은 선택한 조치다.
예상 출력은 **EXPECTED**로 표시한다. **[판정]**과 **[해석]**은 관측에 대한 작성자의 설명이다.

## 1. 먼저 무엇을 검증하려 했는가

### LAB의 목표

학습 목표는 Linux 프로그램이 커널 기능을 호출하는 경계를 직접 보는 것이다.
다음 세 예제를 사용했다.

| 예제 | 프로그램의 동작 | 확인할 근거 |
|---|---|---|
| hello.c | stdout에 문자열 출력 | write의 fd·길이·반환값, exit_group |
| flows.c | fork로 부모·자식 생성 | 각 PID가 step 0→1→2 출력 |
| maps.c | private anonymous mapping 생성·쓰기 | 주소를 포함하는 rw-p 구간, pagemap |

[소스와 실행 스크립트](../../examples/0002-exceptional-control-flow/)를 함께 읽으면 된다.

`hello.c`의 핵심은 다음과 같다.

```c
static const char message[] = "hello, world\n";
ssize_t written = write(STDOUT_FILENO, message, sizeof message - 1);
```

문자열은 newline을 포함해 13바이트다. 전체 코드의 반환값 검사는 이 짧은 출력이 성공했는지 확인한다.
일반적인 출력 코드는 partial write와 `EINTR`도 처리해야 한다. [공식: write(2)](https://man7.org/linux/man-pages/man2/write.2.html)

### 성공 조건을 구체적인 문장으로 쓰기

| 검사 | 성공 조건 |
|---|---|
| 컴파일 | 컴파일러 종료 코드 0 |
| 실행 | 프로그램이 예상 문자열을 출력 |
| 관측 | strace에 write와 exit_group이 나타남 |
| 해석 | fd=1·요청 13바이트·반환 13을 읽을 수 있음 |

`./hello`의 성공은 두 번째 행까지의 근거다. syscall 관측에는 세 번째 행의 출력이 필요하다.

## 2. strace는 어떤 경계를 관측하는가

**[공식]** `ptrace`는 한 프로세스가 다른 프로세스의 실행과 레지스터 등을 관측·제어할 수 있게 하는
Linux 인터페이스다. syscall tracing과 debugger 구현에 사용한다.
[ptrace(2)](https://man7.org/linux/man-pages/man2/ptrace.2.html)

**[공식]** strace는 프로그램의 syscall과 signal을 추적한다.
[프로젝트 문서](https://strace.io/)와 [strace(1)](https://man7.org/linux/man-pages/man1/strace.1.html)

### ELI10: 달리는 차와 계기판

자동차가 움직이는지 확인하는 일과 계기판이 속도를 정확하게 표시하는지 확인하는 일은 서로 다른 검사다.
프로그램의 출력은 차가 움직였다는 관측에, strace 출력은 계기판의 관측에 대응한다.
실제 strace는 커널 tracing 인터페이스와 아키텍처별 상태를 사용한다. 계기판 비유는 이 구현 세부까지 설명하지 않는다.

![프로그램의 출력 확인과 kernel tracing을 통한 관측 확인의 두 경로](../assets/2026-10-25-x86-strace-debugging/observation-paths.svg)

## 3. 첫 증상: 출력은 있고 trace는 없다

### 기대한 결과

다음은 EXPECTED 예시다. 공백 정렬은 strace 버전에 따라 달라질 수 있다.

```text
write(1, "hello, world\n", 13) = 13
exit_group(0)                  = ?
```

### 실제 관측

**[관측]** OrbStack Docker의 Linux/amd64 환경에서 hello 프로그램은 실행됐다.
필터링된 trace에는 필요한 syscall 행이 나타나지 않아 LAB이 멈췄다.

[당시 로그](../../validation/ch8/2026-10-25/lab-amd64-strace-incomplete.txt):

```text
Linux 7.2.12-orbstack-00394-gbecdd6130068 x86_64 GNU/Linux
gcc (Debian 12.2.0-14+deb12u1) 12.2.0
strace -- version 6.1
hello, world
```

**[관측]** ARM64 native 컨테이너에서는 같은 예제·검사 스크립트가 통과했다.
[ARM64 전체 로그](../../validation/ch8/2026-10-25/lab-arm64.txt)

**[가설]** 프로그램 로직 이외에 tracing 도구와 실행 환경의 조합을 조사할 필요가 있다.
ARM64 통과는 스크립트가 적어도 한 환경에서 정상 동작했다는 근거다.
x86-64 전체 환경에서의 정확성을 보장하는 근거는 별도 검사가 필요하다.

## 4. orb CLI로 기존 Ubuntu를 직접 확인했다

### 환경을 먼저 목록화한다

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

### 첫 실패는 도구 부재였다

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

### 도구 설치 뒤의 실패를 따로 기록한다

**[관측]** GCC 14.2.0·strace 6.14가 준비된 뒤에도 LAB은 exit 1이었다.
[Orb amd64 로그](../../validation/ch8/2026-10-25/lab-orb-amd64.txt):

```text
Linux 7.2.12-orbstack-00394-gbecdd6130068 x86_64 GNU/Linux
gcc (Ubuntu 14.2.0-19ubuntu2) 14.2.0
strace -- version 6.14
hello, world
```

### 예제를 더 작게 줄였다

```sh
orb -m ubuntu strace -e trace=write,exit_group /bin/echo hello
```

**[관측]** 아래 mode 메시지가 반복됐고 마지막에 `hello`와 종료 코드 0이 나타났다.
필요한 `write`·`exit_group` trace는 나타나지 않았다.
아래는 직접 관측한 출력 일부이며 전체 반복 횟수는 생략했다. [최소 재현 trace](../../validation/ch8/2026-10-25/debug-note/orb-echo-trace.txt)에서 전체 결과를 확인한다.

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

## 5. 왜 uname의 x86_64만으로 충분하지 않았는가

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

![amd64 userspace 번역 실행과 x86-64 kernel을 포함한 full-system 실행의 비교](../assets/2026-10-25-x86-strace-debugging/environment-layers.svg)

## 6. 가설을 어떻게 좁혔는가

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

## 7. 왜 QEMU full-system을 선택했는가

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

## 8. QEMU 환경을 어떻게 만들었는가

### 용어 세 개

| 용어 | 이번 작업에서의 의미 |
|---|---|
| rootfs | guest 안의 /bin, /lib, /usr, LAB 소스 등을 담은 파일 트리 |
| initramfs | 커널이 초기 파일시스템으로 사용할 cpio archive |
| init | 커널이 처음 실행할 사용자 프로그램. 이번 guest는 작은 전용 init 사용 |

### 구성 순서와 이유

1. **기존 Dockerfile로 amd64 LAB image를 준비한다.** GCC·strace·Python·예제의 사용자 공간 구성을 재사용한다.
2. **image의 파일 트리를 export한다.** Docker를 rootfs를 만드는 도구로 사용한다.
3. **x86-64 Linux kernel을 준비한다.** Alpine의 공식 v3.23 netboot kernel을 사용했고 관측 버전은 6.18.58-0-lts다.
4. **작은 init을 정적 링크로 컴파일한다.** early boot에서 필요한 mount와 LAB 실행을 담당한다.
5. **rootfs를 cpio+gzip으로 묶는다.** 이를 initramfs로 커널에 전달한다.
6. **QEMU를 TCG·q35·CPU max로 실행한다.** guest CPU 2개·RAM 1536 MiB를 설정하고 guest network는 끈다.
7. **guest serial log와 LAB 종료 코드를 확인한다.** 출력 문구와 프로세스 종료 상태를 함께 평가한다.

[init 소스](../../tools/ch8/qemu-init.c)는 `/dev`, `/proc`, `/sys`를 mount한 다음 LAB을 실행한다.
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

[재현 도구](../../tools/ch8/qemu-lab.py)는 guest process와 임시 rootfs를 정리한다.
완료 시 init은 결과 marker를 출력하고 대기하며, host의 실행 도구가 QEMU process를 종료한다.
실제 host의 shutdown·reboot는 실행하지 않는다.

## 9. 실제로 무엇을 확인했는가

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
[첫 실행 전체 로그](../../validation/ch8/2026-10-25/lab-x86-system.txt)에서 생략 없는 결과를 확인한다.

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
[재실행 로그](../../validation/ch8/2026-10-25/lab-x86-system-reproduced.txt)

kernel URL·SHA-256·image ID·QEMU 버전은 [환경 기록](../../validation/ch8/2026-10-25/x86-system-environment.json)에 있다.
현재 재현 도구의 kernel URL과 Debian package는 업데이트될 수 있다. 과거와 동일한 바이트를 재현하려면 이 기록의 hash·image ID도 대조해야 한다.

## 10. 해결한 범위와 남은 질문

### 확인된 결론

- Orb CLI의 기존 Ubuntu amd64를 실제로 시도했고 tracing 증상을 재현했다.
- x86-64 커널을 포함한 QEMU full-system에서 같은 LAB을 두 번 통과했다.
- x86-64 syscall LAB의 관측 공백을 대체 실행 경로로 해소했다.
- 글로벌 Rosetta 설정은 유지했다.

### 근본 원인에 대한 현재 판단

**[가설]** 해당 Orb amd64 번역 실행과 syscall tracing 사이의 호환성 문제가 유력한 조사 방향이다.
근거는 예제를 줄여도 유지된 증상, 두 strace 버전에서의 재현, 실제 번역 설정,
ARM64와 QEMU full-system에서의 성공이다.

**[미확인]** 정확히 어느 계층의 어떤 구현이 잘못됐는지는 분리해서 검증하지 않았다.
Rosetta 설정을 바꾼 A/B, `ptrace` 요청·반환값·레지스터 구조의 비교,
OrbStack·Rosetta·strace의 더 작은 재현 코드와 버전별 대조가 후속 조사다.

**[중단 기준]** 이 작업의 LAB 검증 목표는 충족됐다.
Orb 자체의 근본 원인 분석은 별도 목표와 실험 범위를 정해 이어갈 수 있다.

## 11. 다른 문제에 적용하는 디버깅 순서

1. **성공 조건을 쪼갠다.** 실행·관측·해석의 각 결과가 무엇이어야 하는지 적는다.
2. **도구와 환경을 목록화한다.** 실행 파일 경로, 버전, ISA, 커널, 권한, 번역 계층을 확인한다.
3. **실패한 지점을 좁힌다.** 명령 부재·권한 오류·빈 결과·포맷 불일치를 구분한다.
4. **입력을 최소화한다.** 사용자 프로그램에서 표준 명령으로 바꿔 증상이 유지되는지 본다.
5. **가설마다 검사와 판정을 연결한다.** 예상과 실제 결과를 기록한다.
6. **목표에 맞는 대조 환경을 선택한다.** 바꾼 조건과 그 때문에 남는 한계도 적는다.
7. **재실행 가능한 근거를 남긴다.** 명령·환경·원본 로그·종료 코드·성공 조건을 보관한다.
8. **결론의 범위를 제한한다.** 검증 경로 확보와 도구 자체의 원인 규명을 각각 보고한다.

### 확인 질문

- **프로그램이 출력하고 종료 코드 0이면 LAB도 성공인가?** LAB이 요구한 trace와 상태 검사까지 확인한다.
- **uname이 x86_64면 ptrace 호환성도 확인된 것인가?** 실제 tracing 결과와 커널·번역 실행 경로를 함께 확인한다.
- **QEMU에서 통과하면 Rosetta 결함을 확정할 수 있는가?** 여러 환경 조건이 함께 바뀌었으므로 구체적인 결함 위치는 추가 실험으로 좁힌다.
- **왜 동일 PID·주소·PFN을 기대하지 않는가?** 실행마다 변하는 값이며 검사의 목적은 관계·권한·동작을 확인하는 데 있다.

## 출처와 증거

문서 확인일은 **2026-10-25 KST**다. 실행 결과와 구현 일반론의 출처를 구분한다.

| 자료 | 사용한 근거 |
|---|---|
| [OrbStack Linux machines](https://docs.orbstack.dev/machines/) | Apple Silicon의 amd64 Linux 머신 실행 방식 |
| [OrbStack settings](https://docs.orbstack.dev/settings) | rosetta 설정의 의미 |
| [QEMU system introduction](https://www.qemu.org/docs/master/system/introduction.html) | guest CPU·memory·devices·TCG 모델 |
| [ptrace(2)](https://man7.org/linux/man-pages/man2/ptrace.2.html) | 프로세스·레지스터·syscall tracing |
| [strace(1)](https://man7.org/linux/man-pages/man1/strace.1.html) | trace 옵션과 출력 |
| [write(2)](https://man7.org/linux/man-pages/man2/write.2.html) | 요청 길이·반환값·partial write |
| [exit_group(2)](https://man7.org/linux/man-pages/man2/exit_group.2.html) | thread group 종료·비복귀 |
| [proc_pid_maps(5)](https://man7.org/linux/man-pages/man5/proc_pid_maps.5.html) | mapping 권한 |
| [kernel pagemap](https://docs.kernel.org/admin-guide/mm/pagemap.html) | PFN과 접근 권한 |

공식 문서는 도구가 제공하는 기능의 근거다. 이번 환경에서의 성공·실패는 위에 연결한 실제 LAB 로그가 근거다.
