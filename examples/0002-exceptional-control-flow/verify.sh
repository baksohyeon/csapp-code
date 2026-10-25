#!/bin/sh
set -eu
lab_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
lab_build=$(mktemp -d)
trap 'rm -rf "$lab_build"' EXIT
cd "$lab_build"
uname -srmo
gcc --version | head -1
strace --version | head -1
for name in hello flows maps; do
    gcc -std=c11 -Wall -Wextra -Werror -O0 -g "$lab_dir/$name.c" -o "$name"
done
strace -qq -e trace=write,exit_group -o trace.txt ./hello > hello.txt
cat hello.txt trace.txt
test "$(cat hello.txt)" = 'hello, world'
grep -E 'write\(1, "hello, world\\n", 13\)[[:space:]]+= 13' trace.txt
grep -F 'exit_group(0)' trace.txt
./flows > flows.txt
cat flows.txt
test "$(grep -c '^parent ' flows.txt)" -eq 3
test "$(grep -c '^child ' flows.txt)" -eq 3
./maps > maps.txt &
lab_pid=$!
python3 - "$lab_pid" maps.txt <<'PY'
import pathlib, struct, sys, time
pid, output = sys.argv[1:]
for _ in range(100):
    text = pathlib.Path(output).read_text()
    if 'page=' in text:
        break
    time.sleep(.01)
else:
    raise SystemExit('mapping process did not become ready')
print(text.strip())
address = int(text.split('page=')[1].split()[0], 16)
lines = pathlib.Path(f'/proc/{pid}/maps').read_text().splitlines()
mapping = next(line for line in lines
               if int(line.split('-')[0], 16) <= address
               < int(line.split('-')[1].split()[0], 16))
print('maps:', mapping.rstrip())
assert mapping.split()[1] == 'rw-p', mapping
try:
    with open(f'/proc/{pid}/pagemap', 'rb') as f:
        import os
        f.seek((address // os.sysconf('SC_PAGE_SIZE')) * 8)
        entry, = struct.unpack('Q', f.read(8))
    print('pagemap: present=%d PFN=%d (PFN visibility depends on CAP_SYS_ADMIN)'
          % ((entry >> 63) & 1, entry & ((1 << 55) - 1)))
    assert (entry >> 63) & 1, 'touched page should be resident during this short LAB'
except PermissionError as e:
    print('pagemap: permission denied:', e)
PY
wait "$lab_pid"
echo 'PASS: write/exit_group, parent-child flows, private writable mapping'
