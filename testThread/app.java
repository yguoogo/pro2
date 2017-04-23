package testThread;

/**
 * Created by yuguanxu on 4/11/17.
 */
public class app {
    public static void main(String[] args) {
        process processer = new process();
        Thread t1 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    processer.producer();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    processer.consume();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        t1.start();
        t2.start();
    }
}
