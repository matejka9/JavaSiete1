package chat.application.client;

import chat.constants.ClientConstants;
import chat.model.client.ClientModel;
import chat.service.StreamService;

import java.applet.Applet;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import java.util.HashMap;

import static chat.constants.SocketConstants.*;

public class Client extends Applet implements Runnable {
    private static final long serialVersionUID = -4297335882692216363L;

    private StreamService streamService = new StreamService();

    private Socket socket;

    private TextField userLabel = new TextField(ClientConstants.DEFAULT_USERNAME);
    private TextField writeLabel = new TextField(ClientConstants.INPUT_CHAT_LABEL);
    private TextArea writeTextArea = new TextArea();
    private TextField sentLabel = new TextField(ClientConstants.SENT_MESSAGES_LABEL);
    private TextArea sentText = new TextArea();

    private HashMap<Integer, ClientModel> users;

    private OutputStream wr;
    private InputStream rd;

    public Client() {
    }

    public void init() {
        users = new HashMap<>();
        this.add(this.userLabel);
        this.add(this.writeLabel);
        this.writeLabel.setEditable(false);
        this.add(this.writeTextArea);
        this.sentLabel.setEditable(false);
        this.add(this.sentLabel);
        this.add(this.sentText);
        this.sentText.setEditable(false);
        this.listenery();
        Thread th = new Thread(this);
        th.start();
    }

    private void createConnection() throws IOException {
        this.socket = new Socket(HOST, PORT);
        System.out.println("Vytvoreny socket pre odosielanie");

        this.wr = this.socket.getOutputStream();
        this.rd = this.socket.getInputStream();
    }

    private void listenery() {
        this.userLabel.addTextListener(arg0 -> {
            String userName = Client.this.userLabel.getText();
            Client.this.streamService.posliUserName(userName, wr);
        });

        this.writeTextArea.addTextListener(arg0 -> {
            String messageOut = Client.this.writeTextArea.getText();
            if (messageOut.contains(System.lineSeparator())){
                Client.this.sentText.append(messageOut);
                Client.this.writeTextArea.setText("");
                Client.this.streamService.posliSpravu(messageOut, wr);
            }
        });
    }

    public void run() {
        try {
            this.createConnection();

            while(this.prijmiSpravu()) {
            }

            System.out.println("Koniec rozhovoru.");

            try {
                this.socket.close();
            } catch (Exception var2) {
                var2.printStackTrace();
            }

        } catch (IOException e) {
            System.out.println("SocketConstants nie je zapnuty.");
        }



    }

    private boolean prijmiSpravu() {
        try {
            int messageType = streamService.readInt(rd);
            System.out.println("Message type: " + messageType);
            int id = streamService.readInt(rd);
            System.out.println("Id: " + id);
            switch (messageType) {
                case NEW_MESSAGE:
                    return newMessage(id);
                case CLIENT_ENTER:
                    return clientEnter(id);
                case CLIENT_LEAVE:
                    return clientLeave(id);
                case CLIENT_USERNAME:
                    return clientUsername(id);
                default:
                    return false;
            }
        } catch (IOException var5) {
            return false;
        }
    }

    private boolean newMessage(int id) throws IOException {
        System.out.println("Zacalo primat spravu");
        String message = this.streamService.readMessage(rd);
        System.out.println("Skoncilo primanie spravi");

        ClientModel clientModel = this.users.get(id);
        if (clientModel != null) {
            clientModel.getSentText().append(message);
        } else {
            System.out.println(String.format("Client s id=%s nebol najdeny pri append Message", id));
        }
        return true;
    }

    private boolean clientLeave(int id) {
        ClientModel clientModel = this.users.remove(id);
        if (clientModel != null) {
            clientModel.removeFromAplet(this);
        } else {
            System.out.println(String.format("Client s id=%s nebol najdeny pri remove Client", id));
        }
        return true;
    }

    private boolean clientEnter(int id) {
        System.out.println(String.format("New client=%S", id));

        if (!this.users.containsKey(id)) {
            TextField textField = new TextField(ClientConstants.DEFAULT_USERNAME);
            textField.setEditable(false);
            TextArea textArea = new TextArea();
            textArea.setEditable(false);

            ClientModel clientModel = new ClientModel(textField, textArea);
            clientModel.addToApplet(this);

            this.users.put(id, clientModel);
        } else {
            System.out.println(String.format("Client s id=%s uz existuje", id));
        }
        return true;
    }

    private boolean clientUsername(int id) throws IOException {
        String userName = this.streamService.readMessage(rd);
        ClientModel clientModel = this.users.get(id);

        if (clientModel != null){
            clientModel.getUserName().setText(userName);
        } else {
            System.out.println(String.format("Client s id=%s nebol najdeny pri rename Client", id));
        }
        return true;
    }
}

