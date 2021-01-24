#include <linux/kernel.h>
#include <sys/syscall.h>
#include <stdio.h>
#include <unistd.h>
#include <string.h>
#include <errno.h>

#define __NR_identity 441

long identity_syscall(int p_id)
{
    return syscall(__NR_identity, p_id);
}

int main(int argc, char *argv[])
{
    long activity;
    int p_id = 1391;
    activity = identity_syscall(p_id);

    if(activity < 0)
    {
        perror("Sorry, Jasper. Your system call appears to have failed.");
    }

    else
    {
        printf("Congratulations, Jasper! Your system call is functional. Run the command dmesg in the terminal and find out!\n");
    }

    return 0;
}
