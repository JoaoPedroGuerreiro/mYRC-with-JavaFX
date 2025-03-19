package org.myproject.chatjfx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextFlow;

public class ChatController {
    @FXML
    private TextArea chatArea;
    @FXML
    private TextField messageField;

    Client client;

    public void initialize() {
        client = new Client("localhost", 8888, this);

        VBox.setVgrow(chatArea, Priority.ALWAYS);
    }

    @FXML
    protected void onSendButtonClick() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            client.sendMessage(message);
            chatArea.appendText("Me: " + message + "\n");
            messageField.clear();
        }
    }

    public void appendToChatArea(String message){
        chatArea.appendText(message + "\n");
    }
}
