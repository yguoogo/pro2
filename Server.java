import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Random;

/**
 * Created by yuguanxu on 4/9/17.
 */
public class Server {

    private static boolean checkChecksum(byte[] buffer){
        short checkError = 0;
        int offsite = 0;
        for(int i = 0; i < buffer.length/2; i++){
            ByteBuffer bf = ByteBuffer.wrap(buffer, offsite, 2);
            offsite += 2;
            checkError += bf.getShort();
        }
        checkError = (short) (0xffff ^ checkError);
        if (checkError == 0){
            return true;
        }else {
            return false;
        }
    }
    public static void main(String[] args) throws IOException{
        //double p = 0.05; // p is the probability to lose the package
        double p = Double.parseDouble(args[0]);
        int MSS = 500; // 4 bytes + 4bytes(header) = 8 bytes
        //int MSS = Integer.parseInt(args[0]);
        DatagramSocket serverSocket = new DatagramSocket(7735);
        InetAddress ad = InetAddress.getLocalHost();
        OutputStream out = new FileOutputStream(System.getProperty("user.dir")+"/words.txt.zip");
        int expectedSeq = 0;
        int flag = 0;
        Random rd = new Random();
        System.out.println("server is ready");
        while (true){
            byte[] dataBf = new byte[MSS];
            byte[] buffer = new byte[8+MSS];
            DatagramPacket dp = new DatagramPacket(buffer,buffer.length);
            serverSocket.receive(dp);

            //System.out.printf("receive ");
            System.arraycopy(buffer, 8, dataBf, 0, MSS);

            byte[] sequenceNumBytes = new byte[4];
            System.arraycopy(buffer, 0, sequenceNumBytes, 0, 4);
            int sequenceNum = ByteBuffer.wrap(sequenceNumBytes).getInt();

            //System.out.println(sequenceNum);


            if(rd.nextDouble() > p && checkChecksum(buffer)) {
                if (sequenceNum == expectedSeq) {
                    expectedSeq++; // increase by 1
                    out.write(dataBf);
                    DatagramPacket ack = new DatagramPacket(sequenceNumBytes, sequenceNumBytes.length, dp.getAddress(), dp.getPort());
                    serverSocket.send(ack);
                }
            }else{
                System.out.println("Packate loss, sequence number = " + sequenceNum );
                if(!checkChecksum(buffer)){
                    //System.out.println("checksum error");
                }
            }
        }
    }
}
