package com.waylau.netty.demo.websocketchat;


import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;

/**
 * 处理TextWebSocketFrame
 *
 * @author waylau.com
 *         2015年3月26日
 */
public class TextWebSocketFrameHandler extends
        SimpleChannelInboundHandler<TextWebSocketFrame> {

    private final static int model = 100;
    private final static int MAXNUM = 1000;
    private final static TextWebSocketFrameHandler[] connections = new TextWebSocketFrameHandler[1000];

    public static ChannelGroup channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    private int cid;
    private Channel channel;


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {


        if (msg instanceof Info) {
            if (((Info) msg).getType() == MesType.LOGin) {
                this.cid = Integer.parseInt(((Info) msg).getContent());
                connections[this.cid] = this;

                this.channel = ctx.channel();
                // 广播登录
                System.out.println("user" + cid + " join");
                broadCast(this.cid, "user " + this.cid + " join ");
            } else if (((Info) msg).getType() == MesType.MESSAGE) {
                System.out.println("user " + this.cid + " send mes");
                int uid_ = this.cid % model;
                for (; uid_ < MAXNUM; uid_ = uid_ + model) {
                    TextWebSocketFrameHandler chat = connections[uid_];
                    if (chat != null) {
                        try {
                            if (chat.channel != null) {
                                chat.channel.writeAndFlush(new TextWebSocketFrame(this.cid + " say :" + ((Info) msg).getContent()));
                            }
                        } catch (Exception e) {
                            connections[uid_] = null;
                            System.out.println("onmessage failed");
                        }
                    }
                }
            }
        }
        if (msg instanceof TextWebSocketFrame) {
            System.out.println("is " + ((TextWebSocketFrame) msg).text());
        }

    }

    private static void broadCast(int uid, String message) {

        int cid = uid % model;
        for (; cid < MAXNUM; cid = cid + model) {
            TextWebSocketFrameHandler chat = connections[cid];
            if (chat != null) {
                synchronized (chat) {
                    if (chat.channel != null)
                        chat.channel.writeAndFlush(new TextWebSocketFrame(message));
                }
            }
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx,
                                TextWebSocketFrame msg) throws Exception { // (1)
        Channel incoming = ctx.channel();

        for (Channel channel : channels) {
            if (channel != incoming) {
                channel.writeAndFlush(new TextWebSocketFrame("[" + incoming.remoteAddress() + "]" + msg.text()));
            } else {
                channel.writeAndFlush(new TextWebSocketFrame("[you]" + msg.text()));
            }
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {  // (2)
        Channel incoming = ctx.channel();

        // Broadcast a message to multiple Channels
        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 加入"));

        channels.add(incoming);
        System.out.println("Client:" + incoming.remoteAddress() + "加入");
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {  // (3)
        Channel incoming = ctx.channel();

        // Broadcast a message to multiple Channels
//        channels.writeAndFlush(new TextWebSocketFrame("[SERVER] - " + incoming.remoteAddress() + " 离开"));
        broadCast(this.cid ,"user " + cid + "离开");
        System.out.println("Client:" + this.cid + "离开");
        connections[this.cid] = null;
        // A closed Channel is automatically removed from ChannelGroup,
        // so there is no need to do "channels.remove(ctx.channel());"
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception { // (5)
        Channel incoming = ctx.channel();
        System.out.println("Client:" + incoming.remoteAddress() + "在线");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception { // (6)
        Channel incoming = ctx.channel();
        System.out.println("Client:" + this.cid + " 掉线");
        connections[this.cid] = null;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)    // (7)
            throws Exception {

                Channel incoming = ctx.channel();
                System.out.println("Client:" + this.cid + " 异常");
                connections[this.cid] = null;
        // 当出现异常就关闭连接
        cause.printStackTrace();
        ctx.close();
    }

}
