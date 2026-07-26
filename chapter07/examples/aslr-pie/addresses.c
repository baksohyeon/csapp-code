#include <stdio.h>
#include <stdlib.h>

int main(void)
{
    int stack_value = 0;
    int *heap_value = malloc(sizeof(*heap_value));

    if (heap_value == NULL) {
        return 1;
    }

    printf("main=%p stack=%p heap=%p\n",
           (void *)&main, (void *)&stack_value, (void *)heap_value);
    free(heap_value);
    return 0;
}
