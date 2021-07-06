package trainlookerclientside.clientside.nfc;

import com.pi4j.wiringpi.Spi;

public class ReadRFID {

    /**
     * Default configuration example;
     *
     * @param rc522  = new RC522();
     * @param tagId  = new byte[5];
     * @param sector = 15;
     * @param block  = 2;
     */
    public ReadRFID(RC522 rc522, byte[] tagId, byte sector, byte block) {
        int i, status;
        rc522.Select_MirareOne(tagId);
        String strUID = Converter.bytesToHex(tagId);
        System.out.println("Card Read UID:" + strUID.substring(0, 2) + "," +
                strUID.substring(2, 4) + "," +
                strUID.substring(4, 6) + "," +
                strUID.substring(6, 8));
        byte[] keyA = new byte[]{(byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03};
        byte[] keyB = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        byte[] data = new byte[16];
        status = rc522.Auth_Card(RC522.PICC_AUTHENT1A, sector, block, keyA, tagId);
        if (status != RC522.MI_OK) {
            System.out.println("Authenticate A error");
            return;
        }
        status = rc522.Read(sector, block, data);
        System.out.println("Successfully authenticated,Read data=" + Converter.bytesToHex(data));
        status = rc522.Read(sector, (byte) 3, data);
        System.out.println("Read control block data=" + Converter.bytesToHex(data));
        for (i = 0; i < 16; i++) {
            data[i] = (byte) 0x00;
        }
        status = rc522.Auth_Card(RC522.PICC_AUTHENT1B, sector, block, keyB, tagId);
        if (status != RC522.MI_OK) {
            System.out.println("Authenticate B error");
            return;
        }
        status = rc522.Write(sector, block, data);
        if (status == RC522.MI_OK)
            System.out.println("Write data finished");
        else {
            System.out.println("Write data error,status=" + status);
        }
    }

    public static void rfidReaderLoop(int sleeptime) throws InterruptedException {
        int count = 0;
        while (count++ < 3) {
            int packetlength = 5;
            byte[] packet = new byte[packetlength];
            packet[0] = (byte) 0x80; // FIRST PACKET GETS IGNORED BUT HAS
            // TO BE SET TO READ
            packet[1] = (byte) 0x80; // ADDRESS 0 Gives data of Address 0
            packet[2] = (byte) 0x82; // ADDRESS 1 Gives data of Address 1
            packet[3] = (byte) 0x84; // ADDRESS 2 Gives data of Address 2
            packet[4] = (byte) 0x86; // ADDRESS 3 Gives data of Address 3

            System.out.println("-----------------------------------------------");
            System.out.println("Data to be transmitted:");
            System.out.println("[TX] " + Converter.bytesToHex(packet));
            System.out.println("[TX1] " + packet[1]);
            System.out.println("[TX2] " + packet[2]);
            System.out.println("[TX3] " + packet[3]);
            System.out.println("[TX4] " + packet[4]);
            System.out.println("Transmitting data...");
            // Send data to Reader and receive answerpacket.
            packet = readFromRFID(0, packet, packetlength);
            System.out.println("Data transmitted, packets received.");
            System.out.println("Received Packets (First packet to be ignored!)");
            System.out.println("[RX] " + Converter.bytesToHex(packet));
            System.out.println("[RX1] " + packet[1]);
            System.out.println("[RX2] " + packet[2]);
            System.out.println("[RX3] " + packet[3]);
            System.out.println("[RX4] " + packet[4]);
            System.out.println("-----------------------------------------------");
            if (packet.length == 0) {
                //Reset when no packet received
                //ResetPin.high();
                Thread.sleep(50);
                //ResetPin.low();
            }
            Thread.sleep(sleeptime);
        }
    }

    public static byte[] readFromRFID(int channel, byte[] packet, int length) {
        Spi.wiringPiSPIDataRW(channel, packet, length);
        return packet;
    }

    public static boolean writeToRFID(int channel, byte fullAddress, byte data) {
        byte[] packet = new byte[2];
        packet[0] = fullAddress;
        packet[1] = data;
        return Spi.wiringPiSPIDataRW(channel, packet, 1) >= 0;
    }
}
