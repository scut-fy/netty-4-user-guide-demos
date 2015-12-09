package com.waylau.netty.demo.websocketchat;


import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

/**
 * @author bryan
 * @date 2015/12/9.
 */

public class LoginHandler extends SimpleChannelInboundHandler {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        if(o instanceof  TextWebSocketFrame){

            try{
                // 处理登录
                int cid = Integer.parseInt(((TextWebSocketFrame) o).text());
                Info info = new Info();
                info.setType(MesType.LOGin);
                info.setContent(cid+"");
                channelHandlerContext.fireChannelRead(info);
//                channelHandlerContext.channel().pipeline().remove("login");
            }catch(Exception e){
                // 消息内容是文字
                Info info = new Info();
                info.setType(MesType.MESSAGE);
                info.setContent(((TextWebSocketFrame) o).text());
                channelHandlerContext.fireChannelRead(info);
            }

        }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }




}
