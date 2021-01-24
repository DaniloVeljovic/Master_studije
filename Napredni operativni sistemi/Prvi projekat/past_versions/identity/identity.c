#include <linux/kernel.h>
#include <linux/sched.h>
#include <linux/init.h>
#include <linux/syscalls.h>
#include <linux/module.h>
#include <asm/uaccess.h>
#include <linux/fs.h> 
#include <linux/cdev.h>
#include <linux/proc_fs.h>
#include <linux/pid.h>

 

void DFS(struct task_struct *task, char* prefix)
{   
    struct task_struct *child;
    struct list_head *list;
    char toPass[90];
    strcpy(toPass, prefix);
    int i = 0;
    //printk(KERN_DEFAULT "%s", prefix);
    

    printk("%sname: %s, pid: [%d], state: %li\n", prefix, task->comm, task->pid, task->state);
    
    //printk(KERN_DEFAULT "\n");
    strcat(toPass, "     ");
    //printk("added one tab");
    list_for_each(list, &task->children) {
        //i+=1;
        child = list_entry(list, struct task_struct, sibling);
        //printk("Calling DFS for the %d child of task: %s", i, task->comm);
        //printk("The child is %s", child->comm);
        DFS(child, toPass);
    	
    }
    
}


 

SYSCALL_DEFINE1(identity, int, p_id)
{
    struct pid *pid_struct;
    struct task_struct *task;
    char prefix[90];
    
    strcpy(prefix, "");

    
    printk("DEBUG: !!!!!!!STARTING THE SYSCALL!!!!!!!!!!.\n");
    printk("DEBUG: Received pid: %d\n", p_id);
    
    pid_struct = find_get_pid(p_id);
    task = pid_task(pid_struct,PIDTYPE_PID);

 

    //len = sprintf(buf,"\nname %s\n ",task->comm);
    printk("DEBUG: Name of the process: %s\n", task->comm);
    
    DFS(task, prefix);
    
    return 0;
}
