package chat.application.server;
import chat.constants.SocketConstants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private HashMap<Integer, ClientManager> clients;
    private int generatingId;

    public Server() {
        clients = new HashMap<>();
    }

    public synchronized void addClient(ClientManager clientManager){
        System.out.println("New client: " + clientManager.getClientModel().getId());
        for (Map.Entry<Integer, ClientManager> entry: clients.entrySet()){
            entry.getValue().addClient(clientManager.getClientModel().getId());
            clientManager.addClient(entry.getValue().getClientModel().getId());
        }
        clients.put(clientManager.getClientModel().getId(), clientManager);
    }

    public synchronized void removeClient(ClientManager clientManager){
        System.out.println("Remove client: " + clientManager.getClientModel().getId());
        for (Map.Entry<Integer, ClientManager> entry: clients.entrySet()){
            if (entry.getKey() != clientManager.getClientModel().getId()){
                entry.getValue().removeClient(clientManager.getClientModel().getId());
            }
        }
        clients.remove(clientManager.getClientModel().getId());
    }

    public synchronized void sendMessage(int idClient, String message){
        System.out.println("Sending message: " + message);
        for (Map.Entry<Integer, ClientManager> entry: clients.entrySet()){
            if (entry.getKey() != idClient){
                entry.getValue().sendMessage(idClient, message);
            }
        }
    }
    public synchronized void sendUserName(int idClient, String userName) {
        System.out.println("Sending userName: " + userName);
        for (Map.Entry<Integer, ClientManager> entry: clients.entrySet()){
            if (entry.getKey() != idClient){
                entry.getValue().sendUserName(idClient, userName);
            }
        }
    }

    private boolean start()
    {
        Socket socket;

        try {
            // first try to connect to other party, if it is already listening
            new Socket(SocketConstants.HOST, SocketConstants.PORT);
            System.out.println("Server is already running");
            return false;
        } catch (Exception e) {}

        // otherwise create a listening socket and wait for the other party to connect
        System.out.println("Server started. Listening for clients.");
        try {
            ServerSocket srv = new ServerSocket(SocketConstants.PORT);
            while (true)
            {
                socket = srv.accept();
                System.out.println("New client arriving.");
                this.addClient(new ClientManager(this, socket, generatingId++));
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    public static void main(String[] args){
        new Server().start();
    }


}
