package com.yiwise.asr.transport;

public class AsrRecognizerServerInfo {
    private String scheme;
    private String ip;
    private int port;
    private String ticketValue;

    public AsrRecognizerServerInfo(String scheme, String ip, int port, String ticketValue) {
        this.ip = ip;
        this.port = port;
        this.scheme = scheme;
        this.ticketValue = ticketValue;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }

    public String getScheme() {
        return scheme;
    }

    public String getTicketValue() {
        return ticketValue;
    }
}
