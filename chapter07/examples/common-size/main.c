#include <stdio.h>

extern char arena[];

int main(void)
{
    arena[31] = 42;
    printf("arena[31]=%d\n", arena[31]);
    return 0;
}
