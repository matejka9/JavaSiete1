package chat.model.server;

import java.nio.channels.SocketChannel;

/**
 * Created by dusanmatejka on 2/26/17.
 */
public class ClientModel {

    private int id;
    private SocketChannel socketChannel;

    public ClientModel(int id, SocketChannel socketChannel) {
        this.id = id;
        this.socketChannel = socketChannel;
    }

    public int getId() {
        return id;
    }

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }
}
