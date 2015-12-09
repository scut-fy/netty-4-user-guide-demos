package com.waylau.netty.demo.websocketchat;

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author bryan
 * @date 2015/12/9.
 */

public class Info {

    private MesType type;
    private String content;

    public MesType getType() {
        return type;
    }

    public void setType(MesType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
