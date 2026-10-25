#define _POSIX_C_SOURCE 200809L
#include <stdio.h>
#include <sys/wait.h>
#include <time.h>
#include <unistd.h>

int main(void) {
    setvbuf(stdout, NULL, _IONBF, 0);
    pid_t child = fork();
    if (child < 0) { perror("fork"); return 1; }
    struct timespec pause = {.tv_sec = 0, .tv_nsec = 20000000};
    for (int i = 0; i < 3; ++i) {
        printf("%s pid=%ld step=%d\n", child == 0 ? "child" : "parent",
               (long)getpid(), i);
        nanosleep(&pause, NULL);
    }
    if (child > 0) {
        int status;
        if (waitpid(child, &status, 0) < 0) { perror("waitpid"); return 1; }
        return WIFEXITED(status) ? WEXITSTATUS(status) : 1;
    }
    return 0;
}
