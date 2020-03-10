package com.yiwise.asr;

public class Token {
    private String token;
    private long expireTime;

    public Token(String token, long expireTime) {
        this.token = token;
        this.expireTime = expireTime;
    }

    public String getToken() {
        return token;
    }

    /**
     * 判断token是否已过期
     *
     * @return 是否已经过期
     */
    public boolean isDue() {
        return (System.currentTimeMillis() / 1000) > expireTime;
    }
}
