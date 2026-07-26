#include <stdio.h>

#include "vector.h"

int main(void)
{
    int left[2] = {1, 2};
    int right[2] = {3, 4};
    int result[2];

    addvec(left, right, result, 2);
    printf("result = [%d, %d]\n", result[0], result[1]);
    return 0;
}
