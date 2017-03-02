package chat.service;

import chat.constants.SocketConstants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by dusanmatejka on 2/26/17.
 */
public class StreamService {

    public int readInt(InputStream rd) throws IOException {
        return (rd.read() << 24) +
                (rd.read() << 16) +
                (rd.read() << 8) +
                (rd.read());
    }

    public void writeInt(OutputStream wr, int number) throws IOException {
        wr.write(number >> 24);
        wr.write((number >> 16) & 255);
        wr.write((number >> 8) & 255);
        wr.write(number & 255);
    }

    public void writeMessageLength(OutputStream wr, int length) throws IOException {
        writeInt(wr, length);
    }

    public int readMessageLength(InputStream rd) throws IOException {
        return readInt(rd);
    }

    public void writeMessage(OutputStream wr, byte[] bts) throws IOException {
        wr.write(bts, 0, bts.length);
    }

    public String readMessage(InputStream rd, int length) throws IOException {
        byte[] bts = new byte[length];
        int i = 0;

        do {
            int j = rd.read(bts, i, bts.length - i);
            if(j <= 0) {
                break;
            }

            i += j;
        } while(i < bts.length);
        return new String(bts);
    }

    public String readMessage(InputStream rd) throws IOException {
        int length = readMessageLength(rd);
        return readMessage(rd, length);
    }

    public void posliSpravu(String message, OutputStream wr) {
        posliSpravu(null, message, wr);
    }

    private void posliSpravu(Integer idClient, String message, OutputStream wr) {
        interactClient(idClient, SocketConstants.NEW_MESSAGE, message, wr);
    }

    private void interactClient(Integer idClient, int type, String message, OutputStream wr) {
        try {
            //Message type
            writeInt(wr, type);

            if (idClient != null) {
                writeInt(wr, idClient);
            }

            //Message
            if (message != null) {
                byte[] bts = message.getBytes();
                //Message length
                writeMessageLength(wr, bts.length);

                //Message send
                writeMessage(wr, bts);
            }

            wr.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void posliUserName(String userName, OutputStream wr) {
        posliUserName(null, userName, wr);
    }

    private void posliUserName(Integer id, String userName, OutputStream wr){
        interactClient(id, SocketConstants.CLIENT_USERNAME, userName, wr);
    }
}
