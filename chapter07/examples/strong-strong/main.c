#include <stdio.h>

int conflict = 1;
int from_other(void);

int main(void)
{
    printf("conflict=%d other=%d\n", conflict, from_other());
    return 0;
}
