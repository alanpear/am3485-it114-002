package Project.Client;

import java.io.Serializable;

import Project.Common.Player;

public class ClientPlayer extends Player implements Serializable{
    private long clientId;
    private String clientName;

    public long getClientId() {
        return clientId;
    }

    public void setClientId(long clientId) {
        this.clientId = clientId;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
}