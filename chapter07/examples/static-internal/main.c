#include <stdio.h>

static int x = 11;
int other_value(void);

int main(void)
{
    printf("main.x=%d other.x=%d\n", x, other_value());
    return 0;
}
