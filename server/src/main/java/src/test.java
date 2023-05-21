package src;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import src.commands.Task;

public class test {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();
        for (int i = 0; i < 3; i++) {
            executorService.submit(new Task(i));
        }

        try {
            TimeUnit.SECONDS.sleep(1);

            System.out.println(executorService); // (1)

            TimeUnit.SECONDS.sleep(10);

            executorService.submit(new Task(3));
            TimeUnit.SECONDS.sleep(1);
            System.out.println(executorService); // (2)

            TimeUnit.SECONDS.sleep(10);

            System.out.println(executorService); // (3)

            for (int i = 0; i < 7; i++) {
                executorService.submit(new Task(i));
            }

            TimeUnit.SECONDS.sleep(1);
            System.out.println(executorService); // (4)
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executorService.shutdown();
    }
}
