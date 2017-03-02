package chat.application.server;

import chat.constants.SocketConstants;
import chat.model.server.ClientModel;

import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Server {

    private HashMap<SocketChannel, ClientModel> clients;
    private int generatingId;
    private Selector selector;

    public Server() {
        clients = new HashMap<>();
    }

    private void addClient(ClientModel clientModel) throws IOException {
        System.out.println("New client: " + clientModel.getId());
        ByteBuffer buffer = ClientManager.createNewClientBuffer(clientModel.getId());
        for (Map.Entry<SocketChannel, ClientModel> entry: clients.entrySet()){
            ClientManager.sendBuffer(buffer, entry.getKey());
            ClientManager.sendBuffer(ClientManager.createNewClientBuffer(entry.getValue().getId()), clientModel.getSocketChannel());
        }
        clients.put(clientModel.getSocketChannel(), clientModel);
    }

    private void removeClient(ClientModel clientModel) throws IOException {
        System.out.println("Remove client: " + clientModel.getId());
        ByteBuffer buffer = ClientManager.createRemoveClientBuffer(clientModel.getId());
        for (Map.Entry<SocketChannel, ClientModel> entry: clients.entrySet()){
            if (entry.getValue().getId() != clientModel.getId()){
                ClientManager.sendBuffer(buffer, entry.getKey());
            }
        }
        clients.remove(clientModel.getSocketChannel());
    }

    public void sendMessage(ClientModel clientModel, String message) throws IOException {
        System.out.println("Sending message: " + message);
        int times = 0;
        ByteBuffer buffer = ClientManager.createMessageBuffer(clientModel.getId(), message);
        for (Map.Entry<SocketChannel, ClientModel> entry: clients.entrySet()){
            System.out.println("Sending message times: " + (++times));
            if (entry.getValue().getId() != clientModel.getId()){
                ClientManager.sendBuffer(buffer, entry.getKey());
            }
        }
    }
    public void sendUserName(ClientModel clientModel, String userName) throws IOException {
        System.out.println("Sending userName: " + userName);
        ByteBuffer buffer = ClientManager.createUserNameBuffer(clientModel.getId(), userName);
        for (Map.Entry<SocketChannel, ClientModel> entry: clients.entrySet()){
            if (entry.getValue().getId() != clientModel.getId()){
                ClientManager.sendBuffer(buffer, entry.getKey());
            }
        }
    }

    private boolean start()
    {
        try {
            // first try to connect to other party, if it is already listening
            new Socket(SocketConstants.HOST, SocketConstants.PORT);
            System.out.println("Server is already running");
            return false;
        } catch (Exception e) {}

        // otherwise create a listening socket and wait for the other party to connect
        System.out.println("Server started. Listening for clients.");
        try {
            selector = Selector.open();
            ServerSocketChannel ssc1 = ServerSocketChannel.open();
            ssc1.configureBlocking( false );
            ServerSocket ss = ssc1.socket();
            InetSocketAddress address = new InetSocketAddress( SocketConstants.PORT );
            ss.bind( address );


            SelectionKey key1 = ssc1.register( selector, SelectionKey.OP_ACCEPT );

            while (true) {
                // wait for a new connection or new request
                if (selector.select() > 0) {
                    Set selectedKeys = selector.selectedKeys();
                    Iterator it = selectedKeys.iterator();

                    while (it.hasNext()) {
                        SelectionKey key = (SelectionKey) it.next();
                        it.remove();

                        if (!key.isValid()) {
                            continue;
                        }

                        if (key.isAcceptable()) {
                            this.addClient(key);
                        } else if (key.isReadable()) {
                            this.handle(key);
                        }
                    }
                }
            }
            
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    private void addClient(SelectionKey key) throws IOException {
        System.out.println("New client arriving.");
        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
        SocketChannel sc = ssc.accept();

        sc.configureBlocking(false);
        SelectionKey newKey = sc.register(selector, SelectionKey.OP_READ);

        ClientModel clientModel = new ClientModel(generatingId++, sc);
        this.addClient(clientModel);
    }

    private void handle(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ClientModel clientModel = this.clients.get(channel);

        //Read First bytes
        ByteBuffer buffer = ByteBuffer.allocate(8);
        int numRead;
        numRead = channel.read(buffer);

        if (numRead == -1) {
            clientModel = this.clients.remove(channel);
            if (clientModel != null) {
                this.removeClient(clientModel);
            } else {
                Socket socket = channel.socket();
                SocketAddress remoteAddr = socket.getRemoteSocketAddress();
                System.out.println("Connection closed by unknown client: " + remoteAddr);
            }
            channel.close();
            key.cancel();
            return;
        }

        buffer.rewind();
        int messageType = buffer.getInt();
        System.out.println("Message type: " + messageType);

        int messageLength = buffer.getInt();
        System.out.println("Message length expected: " + messageLength);

        buffer = ByteBuffer.allocate(messageLength);
        numRead = channel.read(buffer);
        System.out.println("Message length: " + numRead);

        String input = new String(buffer.array());
        System.out.println("Message: " + input);

        switch (messageType) {
            case SocketConstants.NEW_MESSAGE:
                sendMessage(clientModel, input);
                break;
            case SocketConstants.CLIENT_USERNAME:
                sendUserName(clientModel, input);
                break;
            default:
                System.out.println("Unknown message type!!!!!!!!!!!!!!!!!!: " + messageType);
                break;
        }
    }

    public static void main(String[] args){
        new Server().start();
    }


}
