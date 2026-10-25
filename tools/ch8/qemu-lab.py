#!/usr/bin/env python3
"""Run the existing LAB under a full x86-64 kernel; no host reboot or global settings."""
import pathlib, subprocess, tempfile, time, sys, re
ROOT = pathlib.Path(__file__).resolve().parents[2]
KERNEL = 'https://dl-cdn.alpinelinux.org/alpine/v3.23/releases/x86_64/netboot/vmlinuz-lts'
def run(*args, **kw):
    return subprocess.run(args, check=True, **kw)
with tempfile.TemporaryDirectory(prefix='csapp-qemu-') as work:
    work = pathlib.Path(work)
    root = work / 'root'
    root.mkdir()
    run('docker', 'build', '--platform', 'linux/amd64', '-t', 'csapp-ch8-lab:amd64',
        str(ROOT / 'examples/0002-exceptional-control-flow'))
    cid = subprocess.check_output(['docker', 'create', '--platform', 'linux/amd64',
                                   'csapp-ch8-lab:amd64'], text=True).strip()
    try:
        with (work / 'root.tar').open('wb') as f:
            run('docker', 'export', cid, stdout=f)
        run('tar', '-xf', str(work / 'root.tar'), '-C', str(root))
    finally:
        run('docker', 'rm', cid)
    run('curl', '-fL', '--max-time', '60', KERNEL, '-o', str(work / 'vmlinuz'))
    run('docker', 'run', '--rm', '--platform', 'linux/amd64',
        '-v', f'{work}:/work', '-v', f'{ROOT / "tools/ch8"}:/tools:ro',
        'csapp-ch8-lab:amd64', 'gcc', '-static', '-O2', '/tools/qemu-init.c',
        '-o', '/work/root/init')
    # All three processes operate only on the rootfs exported from our LAB image.
    with (work / 'root.cpio.gz').open('wb') as archive:
        find = subprocess.Popen(['find', '.', '-print0'], cwd=root, stdout=subprocess.PIPE)
        cpio = subprocess.Popen(['cpio', '-0', '-o', '-H', 'newc'], cwd=root,
                                stdin=find.stdout, stdout=subprocess.PIPE)
        find.stdout.close()
        gzip = subprocess.Popen(['gzip', '-1'], stdin=cpio.stdout, stdout=archive)
        cpio.stdout.close()
        assert gzip.wait() == cpio.wait() == find.wait() == 0
    command = ['qemu-system-x86_64', '-accel', 'tcg', '-machine', 'q35',
               '-cpu', 'max', '-m', '1536', '-smp', '2', '-nographic',
               '-no-reboot', '-nic', 'none', '-kernel', str(work / 'vmlinuz'),
               '-initrd', str(work / 'root.cpio.gz'), '-append',
               'console=ttyS0 rdinit=/init loglevel=3']
    log = work / 'serial.txt'
    with log.open('wb') as f:
        process = subprocess.Popen(command, stdout=f, stderr=subprocess.STDOUT)
        try:
            deadline = time.monotonic() + 240
            while time.monotonic() < deadline:
                text = log.read_text(errors='replace')
                if 'CSAPP_QEMU_EXIT=' in text or process.poll() is not None:
                    break
                time.sleep(1)
            text = re.sub(r'\x1b\[[0-?]*[ -/]*[@-~]|\x1bc', '', text)
            print(text)
            if len(sys.argv) > 1:
                pathlib.Path(sys.argv[1]).write_text(text)
            if 'CSAPP_QEMU_EXIT=0' not in text:
                raise SystemExit('x86-64 LAB failed or timed out')
        finally:
            process.terminate()
            try:
                process.wait(timeout=10)
            except subprocess.TimeoutExpired:
                process.kill()
                process.wait()
