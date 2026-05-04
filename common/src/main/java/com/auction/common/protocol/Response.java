package com.auction.common.protocol;

/**
 * Generic response wrapper used by the socket JSON protocol.
 *
 * @param <T> type of response payload
 */
public class Response<T> {

    private MessageType type;
    private String requestId;
    private boolean success;
    private String message;
    private T data;

    public Response() {}

    public Response(
        MessageType type,
        String requestId,
        boolean success,
        String message,
        T data
    ) {
        this.type = type;
        this.requestId = requestId;
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public static <T> Response<T> ok(
        MessageType type,
        String requestId,
        String message,
        T data
    ) {
        return new Response<>(type, requestId, true, message, data);
    }

    public static <T> Response<T> fail(
        MessageType type,
        String requestId,
        String message
    ) {
        return new Response<>(type, requestId, false, message, null);
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

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
