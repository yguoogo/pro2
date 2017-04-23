import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * Created by yuguanxu on 4/11/17.
 */
public class Client {
    private DatagramSocket clientSocket;
    private int N;
    private int MSS;
    private String fileName;
    private int winLow;
    private int winHigh;

    private TreeMap<Integer, DatagramPacket> unsendPackets = new TreeMap<>();
    private TreeMap<Integer, DatagramPacket> unAckPackets = new TreeMap<>();

    private Object lock = new Object();
    private Object timer_lock = new Object();
    private Timer timer;
    private int timerCounter;
    private int lastPackatNum;

    private PackageCreator pkCreator = new PackageCreator();


    public Client(int N_num, int MSS_num, String fileName_str, int timer_time) throws IOException {
        clientSocket = new DatagramSocket(9999); // sender socket set port number
        N = N_num;
        MSS = MSS_num;
        fileName = fileName_str;
        winLow = 0;
        winHigh = winLow + N - 1;
        short pkType = 0b0101010101010101;
        InputStream filedata = new FileInputStream(fileName);
        int sequenceNum = 0;
        timer = new Timer();
        timerCounter = timer_time;
        byte[] b = new byte[MSS];

        while (filedata.read(b) != -1){
            unsendPackets.put(sequenceNum, pkCreator.createPacket(sequenceNum, b, pkType));
            sequenceNum++;
            b = new byte[MSS];
        }
        lastPackatNum = sequenceNum - 1;

    }

    private void rdt_send(int sequenceNumber, byte[] data, short pkType, DatagramSocket clientSocket) throws IOException {
        DatagramPacket pk = pkCreator.createPacket(sequenceNumber, data, pkType);
        clientSocket.send(pk);
    }

    private class TimerTaskTest01 extends TimerTask {
        Timer timer;
        public TimerTaskTest01(Timer t){
            timer = t;
        }
        public void run() {
            synchronized (lock){
                for(Map.Entry<Integer, DatagramPacket> entry : unAckPackets.entrySet()){
                    try {
                        clientSocket.send(entry.getValue());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                synchronized (timer_lock) {
                    timer = new Timer();
                    timer.schedule(new TimerTaskTest01(timer), timerCounter);
                }
            }
        }
    }

    public void sender() throws IOException, InterruptedException {
        System.out.println("Start sending packets");
        while(true){
            synchronized (lock){
                if(unsendPackets.size() == 0 && unAckPackets.size() == 0){
                    System.exit(0);
                }
                int currentUnAckedNum = unAckPackets.size();
                for(int i = winLow + currentUnAckedNum; i <= winHigh; i++){
                    if(i > lastPackatNum){
                        break;
                    }
                    System.out.println("Prepare to send packet " + i);
                    DatagramPacket nextPacket = unsendPackets.remove(i); // delete
                    unAckPackets.put(i, nextPacket);  // put into unacknowledged map
                    clientSocket.send(nextPacket);// retrieve packet from unAcked packets
                    System.out.println("Start sending packet " + i);
                    if(i == winLow){ // only the first window need sender to set timer
                        synchronized (timer_lock) {
                            timer.schedule(new TimerTaskTest01(timer), timerCounter);
                        }
                    }

                }
                lock.wait();
            }
        }


        /*while(filedata.read(b) != -1){
            rdt_send(sequenceNum, b, pkType, clientSocket);
            b = new byte[MSS];
            sequenceNum++;
        }*/
    }

    public void receiver() throws IOException, InterruptedException {
        int firstPacket = 0;
        byte[] data = new byte[4];
        DatagramPacket pk = new DatagramPacket(data, data.length);
        int expectAckNum = 0;


        while (true){
            clientSocket.receive(pk); // equal to wait
            int sequenceNumber = ByteBuffer.wrap(data).getInt();
            System.out.println("Receive ack " + sequenceNumber);

            if(sequenceNumber >= expectAckNum){
                expectAckNum = sequenceNumber + 1;
                synchronized (lock){
                    int firstUnAckKey = unAckPackets.firstEntry().getKey();
                    for(int k = firstUnAckKey; k <= sequenceNumber; k++){
                        unAckPackets.remove(k);
                    }
                    winLow = sequenceNumber + 1;
                    winHigh = winLow + N - 1;
                    synchronized (timer_lock){
                        timer.cancel();
                        if(sequenceNumber != lastPackatNum) {
                            timer = new Timer();
                            timer.schedule(new TimerTaskTest01(timer), timerCounter);
                        }else {
                            lock.notify();
                            System.exit(0);
                        }
                    }
                    lock.notify();
                }
            }
        }
    }
}