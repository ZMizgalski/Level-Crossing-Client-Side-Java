package trainlookerclientside.clientside.nfc;

public class WriteRFID {

    /**
     * Default configuration example;
     *
     * @param rc522    = new RC522();
     * @param back_len = new int[1];
     * @param tagId    = new byte[5];
     * @param sector   = 15;
     * @param block    = 3;
     */
    public WriteRFID(RC522 rc522, int[] back_len, byte[] tagId, byte sector, byte block) {
        int status;
        if (rc522.Request(RC522.PICC_REQIDL, back_len) == RC522.MI_OK) {
            System.out.println("Card detected: " + back_len[0]);
        }
        if (rc522.AntiColl(tagId) != RC522.MI_OK) {
            System.out.println("anticoll error");
            return;
        }
        String strUID = Converter.bytesToHex(tagId);
        System.out.println("Card Read UID:" + strUID.substring(0, 2) + "," +
                strUID.substring(2, 4) + "," +
                strUID.substring(4, 6) + "," +
                strUID.substring(6, 8));
        byte[] defaultkey = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        int size = rc522.Select_Tag(tagId);
        System.out.println("Size=" + size);
        status = rc522.Auth_Card(RC522.PICC_AUTHENT1A, sector, block, defaultkey, tagId);
        if (status != RC522.MI_OK) {
            System.out.println("Authenticate error");
            return;
        }
        byte[] data = new byte[16];
        byte[] controlBytes = new byte[]{(byte) 0x08, (byte) 0x77, (byte) 0x8f, (byte) 0x69};
        System.arraycopy(controlBytes, 0, data, 6, 4);
        byte[] keyA = new byte[]{(byte) 0x03, (byte) 0x03, (byte) 0x00, (byte) 0x01, (byte) 0x02, (byte) 0x03};
        byte[] keyB = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
        System.arraycopy(keyA, 0, data, 0, 6);
        System.arraycopy(keyB, 0, data, 10, 6);
        status = rc522.Write(sector, block, data);
        if (status == RC522.MI_OK)
            System.out.println("Write data finished");
        else {
            System.out.println("Write data error,status=" + status);
        }
    }
}
