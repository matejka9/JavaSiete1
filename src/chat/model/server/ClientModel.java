package chat.model.server;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by dusanmatejka on 2/26/17.
 */
public class ClientModel {

    private int id;
    private Socket socket;
    private OutputStream wr;
    private InputStream rd;

    public ClientModel(int id) {
        this.id = id;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public void setWr(OutputStream wr) {
        this.wr = wr;
    }

    public void setRd(InputStream rd) {
        this.rd = rd;
    }

    public int getId() {
        return id;
    }

    public Socket getSocket() {
        return socket;
    }

    public OutputStream getWr() {
        return wr;
    }

    public InputStream getRd() {
        return rd;
    }
}
