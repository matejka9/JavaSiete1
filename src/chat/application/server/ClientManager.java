package chat.application.server;

import chat.constants.SocketConstants;
import chat.model.server.ClientModel;
import chat.service.StreamService;

import java.net.Socket;

/**
 * Created by dusanmatejka on 2/26/17.
 */
public class ClientManager implements Runnable{

    private Server server;
    private ClientModel clientModel;
    private StreamService streamService;


    public ClientManager(Server server, Socket socket, int id) {
        this.server = server;
        this.clientModel = new ClientModel(id);
        this.streamService = new StreamService();

        try {
            clientModel.setWr(socket.getOutputStream());
            clientModel.setRd(socket.getInputStream());
        } catch (Exception e)
        {
            System.out.println("Error obtaining input/output streams from socket.");
        }
        new Thread(this).start();
    }

    public ClientModel getClientModel() {
        return clientModel;
    }

    @Override
    public void run()
    {
        while (true)
        {
            try {
                int request = this.streamService.readInt(this.clientModel.getRd());
                switch (request)
                {
                    case SocketConstants.NEW_MESSAGE:
                        String message = streamService.readMessage(clientModel.getRd());
                        this.server.sendMessage(clientModel.getId(), message);
                        break;
                    case SocketConstants.CLIENT_USERNAME:
                        String userName = streamService.readMessage(clientModel.getRd());
                        this.server.sendUserName(clientModel.getId(), userName);
                        break;
                    case SocketConstants.END_COMMUNICATION:
                    case SocketConstants.UNSUCCES_COMMUNICATION:
                        System.out.println("Client left.");
                        server.removeClient(this);
                        return;
                    default:
                        System.out.println(String.format("Unknown socket type=%s", request));
                        break;
                }
            } catch (Exception e)
            {
                System.out.println("Error communicating with the client");
                server.removeClient(this);
                return;
            }
        }
    }

    public synchronized void sendMessage(int idClient, String message){
        streamService.posliSpravuZoSevera(idClient, message, clientModel.getWr());
        System.out.println(String.format("Message='%s' sent to=%s", message, clientModel.getId()));
    }

    public synchronized void addClient(int id) {
        streamService.newClient(id, clientModel.getWr());
    }

    public synchronized void removeClient(int id) {
        streamService.removeClient(id, clientModel.getWr());
    }

    public void sendUserName(int idClient, String userName) {
        streamService.posliUserNameZoServera(idClient, userName, clientModel.getWr());
    }
}
