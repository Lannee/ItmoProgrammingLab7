package src.commands;

public class Task implements Runnable {
    int taskNumber;

    public Task(int taskNumber) {
        this.taskNumber = taskNumber;
    }

    @Override
    public void run() {
        System.out.println("Request catched #" + taskNumber + " by Thread " + Thread.currentThread().getName());
    }
}
