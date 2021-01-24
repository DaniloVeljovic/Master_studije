#include <linux/module.h>
#include <linux/kernel.h>
#include <linux/sched.h>
#include <linux/init.h>
#include <linux/syscalls.h>
#include <asm/uaccess.h>
#include <linux/fs.h> 
#include <linux/cdev.h>
#include <linux/proc_fs.h>
#include <linux/module.h>
#include <linux/moduleparam.h>
#include <linux/pid.h>
#include <linux/string.h>
/**
 * Performs a DFS on a given task's children.
 *
 * @void
 */
 
MODULE_LICENSE("GPL");
MODULE_AUTHOR("VEKS");
MODULE_DESCRIPTION("PRIMER");
MODULE_VERSION("0.01");
 
 static int p_id;
 
module_param(p_id, int, S_IRUSR | S_IWUSR | S_IRGRP | S_IWGRP);
MODULE_PARM_DESC(myint, "An integer");

 

void DFS(struct task_struct *task, char* prefix, char* childrenPrefix)
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
        DFS(child, toPass, toPass);
    	
    }
    
}

 

/**
 * This function is called when the module is loaded. 
 *
 * @return 0  upon success
 */ 
int task_lister_init(void)
{
    
    
    struct pid *pid_struct;
    struct task_struct *task;
    char prefix[90];
    char childrenPrefix[90];
    strcpy(prefix, "");
    strcpy(prefix, "");
    
    printk(KERN_INFO "Loading Task Lister Module...\n");
    
    printk("DEBUG: Received pid: %d\n", p_id);
    
    pid_struct = find_get_pid(p_id);
    task = pid_task(pid_struct,PIDTYPE_PID);

 

    //len = sprintf(buf,"\nname %s\n ",task->comm);
    printk("DEBUG: Name of the process: %s\n", task->comm);
    
    DFS(task, prefix, childrenPrefix);
    //DFS(&init_task);

 

    return 0;
}

 

/**
 * This function is called when the module is removed.
 *
 * @void
 */
void task_lister_exit(void)
{
    printk(KERN_INFO "Removing Task Lister Module...\n");
}

 

// Macros for registering module entry and exit points.
module_init(task_lister_init);
module_exit(task_lister_exit);
