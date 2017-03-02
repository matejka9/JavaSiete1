package chat.model.client;

import chat.application.client.Client;

import java.awt.*;

/**
 * Created by dusanmatejka on 2/26/17.
 */
public class ClientModel {

    private TextField userName;
    private TextArea sentText;

    public ClientModel(TextField userName, TextArea sentText) {
        this.userName = userName;
        this.sentText = sentText;
    }


    public TextField getUserName() {
        return userName;
    }

    public TextArea getSentText() {
        return sentText;
    }

    public void removeFromAplet(Client client) {
        client.remove(userName);
        client.remove(sentText);
    }

    public void addToApplet(Client client) {
        client.add(userName);
        client.add(sentText);
    }
}
