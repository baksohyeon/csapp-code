// Type your code here, or load an example.
int sum(int num);

int sum(int num) {
    int sum = 0;
    for (int i=0; i < num; i++) {
        sum += i;
    };
    return sum;
}