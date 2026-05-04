package com.auction.common.protocol;

public class Request<T> {
    private MessageType type;
    private String requestId;
    private String token;
    private T data;

    public Request() {
    }

    public Request(MessageType type, String requestId, String token, T data) {
        this.type = type;
        this.requestId = requestId;
        this.token = token;
        this.data = data;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
