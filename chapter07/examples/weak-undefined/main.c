#include <stdio.h>

extern void optional_hook(void) __attribute__((weak));

int main(void)
{
    if (optional_hook != NULL) {
        optional_hook();
    } else {
        puts("optional_hook: absent");
    }
    return 0;
}
