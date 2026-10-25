# Exceptional Control Flow LAB

소스는 `hello.c`(write), `flows.c`(fork), `maps.c`(private anonymous mapping)다.
바이너리는 임시 디렉터리에 생성하고 종료 시 삭제한다.

레포 root에서:

```sh
docker build --platform linux/amd64 -t csapp-ch8-lab:amd64 examples/0002-exceptional-control-flow
docker run --rm --platform linux/amd64 --cap-add=SYS_PTRACE csapp-ch8-lab:amd64
```

Linux/x86-64 native 환경의 전체 LAB 명령이다. macOS ARM64의 amd64 에뮬레이션에서는
ptrace/strace가 syscall을 완전히 보여주지 못할 수 있다. 이 경우 아래 native ARM64 LAB을 실행하고
관측 환경을 ARM64로 기록한다.

```sh
docker build --platform linux/arm64 -t csapp-ch8-lab:arm64 examples/0002-exceptional-control-flow
docker run --rm --platform linux/arm64 --cap-add=SYS_PTRACE csapp-ch8-lab:arm64
```

필요 도구가 설치된 Linux host에서는 `sh examples/0002-exceptional-control-flow/verify.sh`로 실행한다.
스크립트는 syscall, 부모·자식 출력, `rw-p` mapping을 검사한다. pagemap 권한 거절은 그 상태를
출력한다. PFN 0은 권한에 의해 가려진 값일 수 있다. kernel·container 정책에 따라 ptrace 자체가
제한되면 syscall LAB은 실패하며 결과를 생략해 성공으로 처리하지 않는다.

베이스 이미지는 Debian bookworm-slim이며 패키지 업데이트에 따라 결과 환경이 달라질 수 있다.
실행 시 kernel·ISA·GCC·strace 버전을 출력한다.

Apple Silicon에서 x86-64 커널을 포함한 전체 LAB 검증:

```sh
python3 tools/ch8/qemu-lab.py
```

QEMU full-system TCG로 별도 guest를 실행한다. Orb의 amd64 userspace에서 strace trace가
불완전할 때 사용할 수 있다. guest 종료 뒤 임시 rootfs를 자동 정리한다.
