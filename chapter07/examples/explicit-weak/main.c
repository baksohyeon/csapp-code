#include <stdio.h>

int hook = 7;
int provider_value(void);

int main(void)
{
    printf("hook=%d provider-sees=%d\n", hook, provider_value());
    return 0;
}
