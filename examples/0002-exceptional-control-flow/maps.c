#define _GNU_SOURCE
#include <stdio.h>
#include <stdlib.h>
#include <sys/mman.h>
#include <unistd.h>

int main(void) {
    long size = sysconf(_SC_PAGESIZE);
    if (size <= 0) return 1;
    char *page = mmap(NULL, (size_t)size, PROT_READ | PROT_WRITE,
                      MAP_PRIVATE | MAP_ANONYMOUS, -1, 0);
    if (page == MAP_FAILED) { perror("mmap"); return 1; }
    page[0] = 'A';
    printf("pid=%ld page=%p value=%c\n", (long)getpid(), (void *)page, page[0]);
    fflush(stdout);
    sleep(3);
    return munmap(page, (size_t)size) == 0 ? 0 : 1;
}
