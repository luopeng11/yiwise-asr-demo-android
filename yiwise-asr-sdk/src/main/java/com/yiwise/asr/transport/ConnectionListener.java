package com.yiwise.asr.transport;

public interface ConnectionListener {

    void onOpen();

    void onClose(int closeCode, String reason);

    void onError(Throwable throwable);

    void onFail(int status, String reason);

    void onMessage(String message);

}
