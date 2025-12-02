#include <stdio.h>

int main(void) {
    char name[100];

    printf("Enter a name: ");
    scanf(" %s", name);
//    fgets(name, sizeof(name), stdin);

    printf("You entered: %s", name);
    return 0;
}
