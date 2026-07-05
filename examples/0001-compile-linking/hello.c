#include <stdio.h>
int sum(int num); // forward declaration 
// -> 정의가 없어도 되고, 모르는 함수여도 일단 컴파일 냅다 해줌
// linking 단계에서 정의가 필요함
// -> 컴파일러가 이 함수가 어디에 있는지 알 수 있도록 도와줌
// .o gitignore 
int main() {
    printf("Hello, World! %d\n", sum(100));
    return 0;
}
