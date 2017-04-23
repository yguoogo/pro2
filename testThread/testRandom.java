package testThread;

import java.util.Random;

/**
 * Created by yuguanxu on 4/14/17.
 */
public class testRandom {
    public static void main(String[] args) {
        Random rd = new Random();
        for(int i = 0; i<100;i++){
            System.out.println(rd.nextDouble());
        }
    }
}
