package com.s_clustr.server;

import java.net.Socket;

public class ClientInfo {
    private Socket socket;
    private String type;
    private String id;

    public ClientInfo(Socket socket, String type, String id) {
        this.socket = socket;
        this.type = type;
        this.id = id;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

}
