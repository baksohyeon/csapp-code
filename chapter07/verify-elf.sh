#!/usr/bin/env bash
set -euo pipefail

root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
examples="${root}/examples"
build="${examples}/build"
common_flags=(-std=c11 -Wall -Wextra -Wpedantic -O0 -g)

rm -rf "${build}"
mkdir -p "${build}"

heading() {
    printf '\n===== %s =====\n' "$1"
}

run() {
    printf '$'
    printf ' %q' "$@"
    printf '\n'
    "$@"
}

expect_failure() {
    printf '$'
    printf ' %q' "$@"
    printf '\n'
    set +e
    "$@" 2>&1
    status=$?
    set -e
    if [[ ${status} -eq 0 ]]; then
        printf 'EXPECTED FAILURE, but command succeeded\n' >&2
        exit 1
    fi
    printf '[exit=%d, expected non-zero]\n' "${status}"
}

heading "toolchain"
uname -srm
gcc --version | head -n 1
clang --version | head -n 1
ld --version | head -n 1
ld.lld --version | head -n 1
readelf --version | head -n 1

heading "1. strong + strong: duplicate initialized global"
run gcc "${common_flags[@]}" -c "${examples}/strong-strong/main.c" -o "${build}/ss-main.o"
run gcc "${common_flags[@]}" -c "${examples}/strong-strong/other.c" -o "${build}/ss-other.o"
run nm -S "${build}/ss-main.o" "${build}/ss-other.o" | grep -E 'conflict|from_other| main$'
expect_failure gcc "${build}/ss-main.o" "${build}/ss-other.o" -o "${build}/strong-strong"

heading "2. strong + COMMON: legacy quiet override with -fcommon"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/strong-common/main.c" -o "${build}/sc-main.o"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/strong-common/worker.c" -o "${build}/sc-worker.o"
run nm -S "${build}/sc-main.o" "${build}/sc-worker.o" | grep -E ' x$'
run readelf -Ws "${build}/sc-worker.o" | grep -E ' x$'
run gcc "${build}/sc-main.o" "${build}/sc-worker.o" -o "${build}/strong-common"
run "${build}/strong-common"

heading "3. COMMON + COMMON: legacy merge with -fcommon"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-common/main.c" -o "${build}/cc-main.o"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-common/worker.c" -o "${build}/cc-worker.o"
run nm -S "${build}/cc-main.o" "${build}/cc-worker.o" | grep -E ' x$'
run gcc -Wl,--warn-common "${build}/cc-main.o" "${build}/cc-worker.o" -o "${build}/common-common"
run "${build}/common-common"

heading "4. duplicate function: strong + strong"
run gcc "${common_flags[@]}" -c "${examples}/duplicate-function/main.c" -o "${build}/df-main.o"
run gcc "${common_flags[@]}" -c "${examples}/duplicate-function/other.c" -o "${build}/df-other.o"
run nm "${build}/df-main.o" "${build}/df-other.o" | grep -E ' helper$'
expect_failure gcc "${build}/df-main.o" "${build}/df-other.o" -o "${build}/duplicate-function"

heading "5. tentative definition placement: -fcommon vs -fno-common"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/storage-layout/symbols.c" -o "${build}/layout-common.o"
run gcc "${common_flags[@]}" -fno-common -c "${examples}/storage-layout/symbols.c" -o "${build}/layout-nocommon.o"
run nm -S "${build}/layout-common.o" | grep -E ' (tentative|zero|initialized)$'
run nm -S "${build}/layout-nocommon.o" | grep -E ' (tentative|zero|initialized)$'
run readelf -Ws "${build}/layout-common.o" | grep -E ' (tentative|zero|initialized)$'
run readelf -Ws "${build}/layout-nocommon.o" | grep -E ' (tentative|zero|initialized)$'
run objdump -h "${build}/layout-nocommon.o" | grep -E '(Idx|\.data|\.bss)'

heading "6. GCC 10 semantic boundary: same source, different flags"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-common/main.c" -o "${build}/gcc-before-main.o"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-common/worker.c" -o "${build}/gcc-before-worker.o"
run gcc "${build}/gcc-before-main.o" "${build}/gcc-before-worker.o" -o "${build}/gcc-before"
run "${build}/gcc-before"
run gcc "${common_flags[@]}" -fno-common -c "${examples}/common-common/main.c" -o "${build}/gcc-after-main.o"
run gcc "${common_flags[@]}" -fno-common -c "${examples}/common-common/worker.c" -o "${build}/gcc-after-worker.o"
expect_failure gcc "${build}/gcc-after-main.o" "${build}/gcc-after-worker.o" -o "${build}/gcc-after"

heading "6b. Clang 18: default, -fcommon, and -fno-common"
run clang "${common_flags[@]}" -c "${examples}/common-common/main.c" -o "${build}/clang-default-main.o"
run clang "${common_flags[@]}" -c "${examples}/common-common/worker.c" -o "${build}/clang-default-worker.o"
run readelf -Ws "${build}/clang-default-main.o" | grep -E ' x$'
expect_failure clang -fuse-ld=lld "${build}/clang-default-main.o" "${build}/clang-default-worker.o" -o "${build}/clang-default"
run clang "${common_flags[@]}" -fcommon -c "${examples}/common-common/main.c" -o "${build}/clang-common-main.o"
run clang "${common_flags[@]}" -fcommon -c "${examples}/common-common/worker.c" -o "${build}/clang-common-worker.o"
run readelf -Ws "${build}/clang-common-main.o" | grep -E ' x$'
run clang -fuse-ld=lld "${build}/clang-common-main.o" "${build}/clang-common-worker.o" -o "${build}/clang-common"
run "${build}/clang-common"
run clang "${common_flags[@]}" -fno-common -c "${examples}/common-common/main.c" -o "${build}/clang-nocommon-main.o"
run clang "${common_flags[@]}" -fno-common -c "${examples}/common-common/worker.c" -o "${build}/clang-nocommon-worker.o"
expect_failure clang -fuse-ld=lld "${build}/clang-nocommon-main.o" "${build}/clang-nocommon-worker.o" -o "${build}/clang-nocommon"

heading "7. type mismatch: strong int + COMMON double"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-mismatch/main.c" -o "${build}/cm-main.o"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-mismatch/worker.c" -o "${build}/cm-worker.o"
run readelf -Ws "${build}/cm-main.o" | grep -E ' (x|y)$'
run readelf -Ws "${build}/cm-worker.o" | grep -E ' x$'
run gcc -Wl,--warn-common "${build}/cm-main.o" "${build}/cm-worker.o" -o "${build}/common-mismatch"
run "${build}/common-mismatch"

heading "8. static internal linkage: same source name, no collision"
run gcc "${common_flags[@]}" "${examples}/static-internal/main.c" "${examples}/static-internal/other.c" -o "${build}/static-internal"
run "${build}/static-internal"
run nm -a "${build}/static-internal" | grep -E ' [bd] x$'

heading "9. actual ELF STB_WEAK: explicit attribute"
run gcc "${common_flags[@]}" -c "${examples}/explicit-weak/main.c" -o "${build}/ew-main.o"
run gcc "${common_flags[@]}" -c "${examples}/explicit-weak/provider.c" -o "${build}/ew-provider.o"
run nm -S "${build}/ew-main.o" "${build}/ew-provider.o" | grep -E ' hook$'
run readelf -Ws "${build}/ew-provider.o" | grep -E ' hook$'
run gcc "${build}/ew-main.o" "${build}/ew-provider.o" -o "${build}/explicit-weak"
run "${build}/explicit-weak"

heading "10. weak + weak: observed link-order choice"
run gcc "${common_flags[@]}" -c "${examples}/weak-weak/main.c" -o "${build}/ww-main.o"
run gcc "${common_flags[@]}" -c "${examples}/weak-weak/left.c" -o "${build}/ww-left.o"
run gcc "${common_flags[@]}" -c "${examples}/weak-weak/right.c" -o "${build}/ww-right.o"
run gcc "${build}/ww-main.o" "${build}/ww-left.o" "${build}/ww-right.o" -o "${build}/ww-left-first"
run gcc "${build}/ww-main.o" "${build}/ww-right.o" "${build}/ww-left.o" -o "${build}/ww-right-first"
run "${build}/ww-left-first"
run "${build}/ww-right-first"
run clang -fuse-ld=lld "${build}/ww-main.o" "${build}/ww-left.o" "${build}/ww-right.o" -o "${build}/ww-lld-left-first"
run clang -fuse-ld=lld "${build}/ww-main.o" "${build}/ww-right.o" "${build}/ww-left.o" -o "${build}/ww-lld-right-first"
run "${build}/ww-lld-left-first"
run "${build}/ww-lld-right-first"

heading "11. GNU ld vs lld duplicate diagnostics"
expect_failure gcc -fuse-ld=bfd "${build}/ss-main.o" "${build}/ss-other.o" -o "${build}/dup-bfd"
expect_failure clang -fuse-ld=lld "${build}/ss-main.o" "${build}/ss-other.o" -o "${build}/dup-lld"

heading "all checks passed"
printf 'ELF experiments completed successfully.\n'
