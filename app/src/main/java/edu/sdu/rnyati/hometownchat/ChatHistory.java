package edu.sdu.rnyati.hometownchat;

/**
 * Created by raghavnyati on 4/8/17.
 */

public class ChatHistory {

    public String getMessageUser() {
        return messageUser;
    }

    public void setMessageUser(String messageUser) {
        this.messageUser = messageUser;
    }

    private String messageUser;

    public ChatHistory(String messageUser) {
        this.messageUser = messageUser;
    }

    public ChatHistory(){
    }
}
