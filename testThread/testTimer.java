package testThread;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by yuguanxu on 4/12/17.
 */

    public class testTimer {
        private static class TimerTaskTest01 extends TimerTask {
            Timer timer;
            public TimerTaskTest01(Timer t){
                timer = t;
            }
            public void run() {
                try {
                    System.out.println("now thread sleep");
                    Thread.sleep(3000);
                    timer.cancel();
                    System.out.println("wake up");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Time's up!!!!");
            }
        }


        public static void main(String[] args) throws InterruptedException {
            System.out.println("timer begin....");


            int time = 3;
            Timer timer;
            timer = new Timer();
            timer.schedule(new TimerTaskTest01(timer), time * 1000);
            timer.cancel();


            timer = new Timer();
            for(int i = 0; i < 3; i++){
                timer.schedule(new TimerTaskTest01(timer), time * 1000);
                Thread.sleep(5000);
            }

        }
    }

