int hook __attribute__((weak)) = 3;

int provider_value(void)
{
    return hook;
}
