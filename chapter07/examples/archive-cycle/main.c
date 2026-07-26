#include <stdio.h>

extern int x(int value);

int main(void)
{
    printf("x(3) = %d\n", x(3));
    return 0;
}
