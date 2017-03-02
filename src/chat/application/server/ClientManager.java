package chat.application.server;

import chat.constants.SocketConstants;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by dusanmatejka on 2/26/17.
 */
public class ClientManager {

    public static ByteBuffer createMessageBuffer(int id, String message) {
        return createBuffer(SocketConstants.NEW_MESSAGE, id, message);
    }

    public static ByteBuffer createUserNameBuffer(int id, String userName) {
        return createBuffer(SocketConstants.CLIENT_USERNAME, id, userName);
    }

    public static ByteBuffer createNewClientBuffer(int id) {
        return createBuffer(SocketConstants.CLIENT_ENTER, id, null);
    }

    public static ByteBuffer createRemoveClientBuffer(int id) {
        return createBuffer(SocketConstants.CLIENT_LEAVE, id, null);
    }

    private static ByteBuffer createBuffer(int messageType, int userId, String message){
        int length = 4*2 + (message == null ? 0 : message.getBytes().length + 4);
        ByteBuffer buffer = ByteBuffer.allocate(length);
        buffer.putInt(messageType);
        buffer.putInt(userId);
        if (message != null && message != null) {
            buffer.putInt(message.getBytes().length);
            buffer.put(message.getBytes());
        }
        buffer.flip();
        return buffer;
    }

    public static void sendBuffer(ByteBuffer buffer, SocketChannel socketChannel) throws IOException {
        buffer.rewind();
        socketChannel.write(buffer);
    }


}
