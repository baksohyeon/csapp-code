#include "vector.h"

int multvec_calls;

void multvec(const int *left, const int *right, int *result, int length)
{
    int index;

    ++multvec_calls;
    for (index = 0; index < length; ++index) {
        result[index] = left[index] * right[index];
    }
}
