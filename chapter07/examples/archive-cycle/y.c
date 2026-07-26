extern int x_helper(int value);

int y(int value)
{
    return x_helper(value) + 10;
}
