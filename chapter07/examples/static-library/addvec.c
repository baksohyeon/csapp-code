#include "vector.h"

int addvec_calls;

void addvec(const int *left, const int *right, int *result, int length)
{
    int index;

    ++addvec_calls;
    for (index = 0; index < length; ++index) {
        result[index] = left[index] + right[index];
    }
}
