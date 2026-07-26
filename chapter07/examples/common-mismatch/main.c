#include <stdio.h>

int y = 0x3b6c;
int x = 0x3b6d;
void overwrite(void);

int main(void)
{
    overwrite();
    printf("x = 0x%x y = 0x%x\n", x, y);
    return 0;
}
