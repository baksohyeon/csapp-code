#define _POSIX_C_SOURCE 200809L
#include <err.h>
#include <errno.h>
#include <inttypes.h>
#include <poll.h>
#include <signal.h>
#include <stdio.h>
#include <sys/signalfd.h>
#include <sys/timerfd.h>
#include <unistd.h>

/* These two Linux fd types return complete, fixed-size records. */
static void read_record(int fd, void *buffer, size_t size) {
    ssize_t count;
    do {
        count = read(fd, buffer, size);
    } while (count == -1 && errno == EINTR);
    if (count == -1) err(1, "read");
    if ((size_t)count != size) errx(1, "incomplete event record");
}

int main(void) {
    sigset_t signals;
    sigemptyset(&signals);
    sigaddset(&signals, SIGUSR1);
    if (sigprocmask(SIG_BLOCK, &signals, NULL) == -1) err(1, "sigprocmask");

    int signal_fd = signalfd(-1, &signals, SFD_CLOEXEC);
    if (signal_fd == -1) err(1, "signalfd");
    int timer_fd = timerfd_create(CLOCK_MONOTONIC, TFD_CLOEXEC);
    if (timer_fd == -1) err(1, "timerfd_create");

    struct itimerspec once = {.it_value = {.tv_sec = 1}};
    if (timerfd_settime(timer_fd, 0, &once, NULL) == -1) err(1, "timerfd_settime");
    if (kill(getpid(), SIGUSR1) == -1) err(1, "kill");

    struct pollfd events[] = {
        {.fd = signal_fd, .events = POLLIN},
        {.fd = timer_fd, .events = POLLIN}
    };
    /* poll ignores negative fds. Retire each source after reading its event. */
    while (events[0].fd >= 0 || events[1].fd >= 0) {
        if (poll(events, 2, -1) == -1) {
            if (errno == EINTR) continue;
            err(1, "poll");
        }
        if ((events[0].revents | events[1].revents) & (POLLERR | POLLHUP | POLLNVAL))
            errx(1, "unexpected descriptor state");
        if (events[0].revents & POLLIN) {
            struct signalfd_siginfo info;
            read_record(signal_fd, &info, sizeof(info));
            if (info.ssi_signo != SIGUSR1) errx(1, "unexpected signal");
            puts("signalfd: SIGUSR1");
            events[0].fd = -1;
        }
        if (events[1].revents & POLLIN) {
            uint64_t expirations;
            read_record(timer_fd, &expirations, sizeof(expirations));
            printf("timerfd: expirations=%" PRIu64 "\n", expirations);
            events[1].fd = -1;
        }
    }
    close(signal_fd);
    close(timer_fd);
    return 0;
}
