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

expect_absent_symbol() {
    local file=$1
    local symbol=$2

    printf '$ nm -g --defined-only %q | grep %q\n' "${file}" "${symbol}"
    if nm -g --defined-only "${file}" | grep -Eq " ${symbol}$"; then
        printf 'UNEXPECTED SYMBOL: %s\n' "${symbol}" >&2
        exit 1
    fi
    printf '[symbol absent, as expected]\n'
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

heading "7b. LTO sees the cross-translation-unit type mismatch"
run gcc "${common_flags[@]}" -flto -fcommon -c "${examples}/common-mismatch/main.c" -o "${build}/lto-main.o"
run gcc "${common_flags[@]}" -flto -fcommon -c "${examples}/common-mismatch/worker.c" -o "${build}/lto-worker.o"
run gcc -flto -Wl,--warn-common "${build}/lto-main.o" "${build}/lto-worker.o" -o "${build}/lto-mismatch"
run "${build}/lto-mismatch"

heading "7c. different COMMON sizes: largest allocation wins"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-size/main.c" -o "${build}/cs-main.o"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-size/small.c" -o "${build}/cs-small.o"
run gcc "${common_flags[@]}" -fcommon -c "${examples}/common-size/large.c" -o "${build}/cs-large.o"
run nm -S "${build}/cs-small.o" "${build}/cs-large.o" | grep -E ' arena$'
run gcc -Wl,--warn-common "${build}/cs-main.o" "${build}/cs-small.o" "${build}/cs-large.o" -o "${build}/common-size"
run nm -S "${build}/common-size" | grep -E ' [Bb] arena$'
run "${build}/common-size"

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

heading "9b. undefined ELF weak: zero value and no archive extraction"
run gcc "${common_flags[@]}" -c "${examples}/weak-undefined/main.c" -o "${build}/wu-main.o"
run gcc "${common_flags[@]}" -c "${examples}/weak-undefined/provider.c" -o "${build}/wu-provider.o"
run ar rcs "${build}/liboptional.a" "${build}/wu-provider.o"
run gcc "${build}/wu-main.o" "${build}/liboptional.a" -o "${build}/weak-archive"
run nm "${build}/weak-archive" | grep -E ' optional_hook$'
run "${build}/weak-archive"
run gcc "${build}/wu-main.o" "${build}/wu-provider.o" -o "${build}/weak-explicit"
run nm "${build}/weak-explicit" | grep -E ' optional_hook$'
run "${build}/weak-explicit"

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

heading "12. compiler driver: gcc, cc, clang, raw ld, lld, and ldd"
run gcc -print-prog-name=cc1
run gcc -print-prog-name=ld
run gcc -print-file-name=crt1.o
run gcc -print-file-name=libc.a
run gcc "${common_flags[@]}" -c "${examples}/compiler-driver/main.c" -o "${build}/driver-main-gcc.o"
run gcc "${common_flags[@]}" -c "${examples}/compiler-driver/utils.c" -o "${build}/driver-utils-gcc.o"
run cc "${common_flags[@]}" -c "${examples}/compiler-driver/utils.c" -o "${build}/driver-utils-cc.o"
run clang "${common_flags[@]}" -c "${examples}/compiler-driver/utils.c" -o "${build}/driver-utils-clang.o"
run readelf -Wr "${build}/driver-main-gcc.o"
run objdump -dr "${build}/driver-main-gcc.o"
run gcc "${build}/driver-main-gcc.o" "${build}/driver-utils-gcc.o" -o "${build}/driver-gcc"
run cc "${build}/driver-main-gcc.o" "${build}/driver-utils-cc.o" -o "${build}/driver-cc"
run clang "${build}/driver-main-gcc.o" "${build}/driver-utils-clang.o" -o "${build}/driver-clang"
run bash -c "printf '4\\n' | '${build}/driver-gcc'"
expect_failure ld "${build}/driver-main-gcc.o" "${build}/driver-utils-gcc.o" -o "${build}/driver-raw-ld"
run clang -fuse-ld=lld "${build}/driver-main-gcc.o" "${build}/driver-utils-gcc.o" -o "${build}/driver-lld"
run readelf -p .comment "${build}/driver-lld"
run ldd "${build}/driver-gcc"

heading "13. glibc also supports static linking"
run gcc -static "${build}/driver-main-gcc.o" "${build}/driver-utils-gcc.o" -o "${build}/driver-static"
run file "${build}/driver-static"
expect_failure ldd "${build}/driver-static"
run bash -c "printf '3\\n' | '${build}/driver-static'"

heading "14. static archive: select referenced members only"
run gcc "${common_flags[@]}" -I"${examples}/static-library" -c "${examples}/static-library/main.c" -o "${build}/sl-main.o"
run gcc "${common_flags[@]}" -I"${examples}/static-library" -c "${examples}/static-library/addvec.c" -o "${build}/sl-addvec.o"
run gcc "${common_flags[@]}" -I"${examples}/static-library" -c "${examples}/static-library/multvec.c" -o "${build}/sl-multvec.o"
run ar rcs "${build}/libvector.a" "${build}/sl-addvec.o" "${build}/sl-multvec.o"
run ar t "${build}/libvector.a"
run nm -s "${build}/libvector.a"
run gcc "${build}/sl-main.o" "${build}/libvector.a" -o "${build}/vector-archive"
run "${build}/vector-archive"
run nm -g --defined-only "${build}/vector-archive" | grep -E ' (addvec|addvec_calls)$'
expect_absent_symbol "${build}/vector-archive" multvec
run gcc "${build}/sl-main.o" "${build}/sl-addvec.o" "${build}/sl-multvec.o" -o "${build}/vector-objects"
run nm -g --defined-only "${build}/vector-objects" | grep -E ' (addvec|multvec|addvec_calls|multvec_calls)$'
expect_failure gcc "${build}/libvector.a" "${build}/sl-main.o" -o "${build}/vector-wrong-order"
run clang -fuse-ld=lld "${build}/libvector.a" "${build}/sl-main.o" -o "${build}/vector-lld-backref"
run "${build}/vector-lld-backref"
run clang -fuse-ld=lld -Wl,--warn-backrefs "${build}/libvector.a" "${build}/sl-main.o" -o "${build}/vector-lld-warn-backref"

heading "15. circular archive dependency: repeat or group"
run gcc "${common_flags[@]}" -c "${examples}/archive-cycle/main.c" -o "${build}/cycle-main.o"
run gcc "${common_flags[@]}" -c "${examples}/archive-cycle/x.c" -o "${build}/cycle-x.o"
run gcc "${common_flags[@]}" -c "${examples}/archive-cycle/x_helper.c" -o "${build}/cycle-x-helper.o"
run gcc "${common_flags[@]}" -c "${examples}/archive-cycle/y.c" -o "${build}/cycle-y.o"
run ar rcs "${build}/libx.a" "${build}/cycle-x.o" "${build}/cycle-x-helper.o"
run ar rcs "${build}/liby.a" "${build}/cycle-y.o"
expect_failure gcc "${build}/cycle-main.o" "${build}/libx.a" "${build}/liby.a" -o "${build}/cycle-once"
run gcc "${build}/cycle-main.o" "${build}/libx.a" "${build}/liby.a" "${build}/libx.a" -o "${build}/cycle-repeat"
run "${build}/cycle-repeat"
run gcc "${build}/cycle-main.o" -Wl,--start-group "${build}/libx.a" "${build}/liby.a" -Wl,--end-group -o "${build}/cycle-group"
run "${build}/cycle-group"
run clang -fuse-ld=lld "${build}/cycle-main.o" "${build}/libx.a" "${build}/liby.a" -o "${build}/cycle-lld"
run "${build}/cycle-lld"

heading "all checks passed"
printf 'ELF experiments completed successfully.\n'
