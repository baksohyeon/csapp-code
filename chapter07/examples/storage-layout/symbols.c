int tentative;
int zero = 0;
int initialized = 7;
static int local_zero;
static int local_initialized = 9;
extern int only_declared;

int read_locals(void)
{
    return local_zero + local_initialized;
}
