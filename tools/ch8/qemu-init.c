#include <sys/mount.h>
#include <fcntl.h>
#include <unistd.h>
#include <stdlib.h>
#include <stdio.h>
int main(void) {
 mount("devtmpfs", "/dev", "devtmpfs", 0, NULL);
 int fd=open("/dev/console", O_RDWR);
 if(fd>=0){dup2(fd,0);dup2(fd,1);dup2(fd,2);}
 mount("proc", "/proc", "proc", 0, NULL);
 mount("sysfs", "/sys", "sysfs", 0, NULL);
 setenv("PATH", "/usr/sbin:/usr/bin:/sbin:/bin", 1);
 chdir("/lab");
 execl("/bin/sh","sh","-c","sh verify.sh; code=$?; echo CSAPP_QEMU_EXIT=$code; exec sleep 600",(char*)NULL);
 perror("exec");return 1;
}
