package testThread;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by yuguanxu on 4/11/17.
 */
public class process {
    private LinkedList<Integer> list = new LinkedList();
    private final int limit = 10;


    public void producer() throws InterruptedException {
        int value = 0;
        while(true){

            synchronized (this){
                while (list.size() == limit){
                    wait();
                }

                list.add(value++);
                notify();
            }
        }
    }

    public void consume() throws InterruptedException {
        while (true){

            synchronized (this){
                while (list.size() == 0){
                    wait();
                }

                System.out.print("List size is :" + list.size());
                int value = list.removeFirst();
                System.out.println("; value is : " + value);
                notify();
            }
            Thread.sleep(1000);

        }
    }
}
