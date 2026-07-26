#!/usr/bin/env bash
set -euo pipefail

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
image_name="csapp-ch7-elf-lab"
result_file="${script_dir}/results/verified-linux-aarch64.txt"

mkdir -p "${script_dir}/results"

docker build -t "${image_name}" "${script_dir}"
docker run --rm \
    -v "${script_dir}:/work" \
    -w /work \
    "${image_name}" \
    bash ./verify-elf.sh 2>&1 | tee "${result_file}"
