#include <stdio.h>

int x;
void update(void);

int main(void)
{
    x = 15213;
    update();
    printf("x = %d\n", x);
    return 0;
}
