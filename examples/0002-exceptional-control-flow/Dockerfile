FROM debian:bookworm-slim
RUN apt-get update && apt-get install -y --no-install-recommends \
    gcc libc6-dev binutils strace python3 procps \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /lab
COPY . .
CMD ["sh", "verify.sh"]
