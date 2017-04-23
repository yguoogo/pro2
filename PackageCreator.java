import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * Created by yuguanxu on 4/23/17.
 */
public class PackageCreator {
    private int sequenceNum;
    private byte[] data;
    private short packetType;

    private byte[] calculateCheckSum(int sequenceNum, byte[] data, short packetTpye){

        byte[] checkSumBytes = new byte[2];

        int checkSumSize = 0xffff;
        int checkSum_32 = 0;

        checkSum_32 += sequenceNum;
        checkSum_32 += (packetTpye & checkSumSize);

        short checkSum_16 = (short) (checkSum_32 & checkSumSize);

        int bit_16_num = data.length / 2;

        int offsite = 0;
        for(long i = 0; i < bit_16_num; i++){
            ByteBuffer bf = ByteBuffer.wrap(data, offsite, 2);
            offsite += 2;
            checkSum_16 += bf.getShort();
        }

        if(data.length % 2 != 0){
            checkSum_16 = (short)( (short) data[data.length -1] + checkSum_16 );
        }

        ByteBuffer bf = ByteBuffer.allocate(2);
        bf.putShort(checkSum_16);
        checkSumBytes = bf.array();
        return checkSumBytes;
    }

    private byte[] joinByteArray(byte[] first, byte[] second){
        byte[] combinedArray = new byte[first.length + second.length];
        System.arraycopy(first, 0, combinedArray, 0, first.length);
        System.arraycopy(second, 0, combinedArray, first.length, second.length);

        return combinedArray;
    }


    public DatagramPacket createPacket(int sequenceNum, byte[] data, short packetType) throws UnknownHostException {

        // add data into checkSum
        ByteBuffer bf = ByteBuffer.allocate(4);
        bf.putInt(sequenceNum);
        byte[] seqNumBytes = bf.array();

        ByteBuffer pt = ByteBuffer.allocate(2);
        pt.putShort(packetType);
        byte[] pkTypeBytes = pt.array();
        byte[] checkSum = calculateCheckSum(sequenceNum,data,packetType);

        // create packet data field
        byte[] d1 = joinByteArray(seqNumBytes, pkTypeBytes);
        byte[] header = joinByteArray(d1, checkSum);
        byte[] dataField = joinByteArray(header, data);

        InetAddress ad = InetAddress.getLocalHost();
        DatagramPacket dp = new DatagramPacket(dataField,dataField.length, ad,7735);
        return dp;
    }
}
