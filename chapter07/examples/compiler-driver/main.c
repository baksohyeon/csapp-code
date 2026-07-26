#include <stdio.h>

extern int foo(int value);

int main(void)
{
    int input;

    if (scanf("%d", &input) != 1) {
        return 1;
    }

    printf("output is %d\n", foo(input));
    return 0;
}
