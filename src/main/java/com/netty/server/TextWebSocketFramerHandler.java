package com.netty.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

public class TextWebSocketFramerHandler extends SimpleChannelInboundHandler<TextWebSocketFrame>{
    public static ChannelGroup channels=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx,
			TextWebSocketFrame msg) throws Exception {
		// TODO Auto-generated method stub
		Channel incoming=ctx.channel();
		for(Channel channel:channels){
			if(channel!=incoming){
				channel.writeAndFlush(new TextWebSocketFrame("["+incoming.remoteAddress()+"]"+msg.text()));				
			}else{
				channel.writeAndFlush(new TextWebSocketFrame("[you]"+msg.text()));
			}
		}
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		Channel incoming=ctx.channel();
		for(Channel channel:channels){
			channel.writeAndFlush(new TextWebSocketFrame("[SERVER]-"+incoming.remoteAddress()+"加入"));			
		}
		channels.add(incoming);
		System.out.println("Client:"+incoming.remoteAddress()+"加入");
	}
	
	@Override
	public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		Channel incoming=ctx.channel();
		for(Channel channel:channels){
			channel.writeAndFlush(new TextWebSocketFrame("[SERVER]-"+incoming.remoteAddress()+"离开"));
		}
		System.out.println("Client:"+incoming.remoteAddress()+"离开");
		channels.remove(incoming);
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		Channel incoming=ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"在线");
	}
	
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		// TODO Auto-generated method stub
		Channel incoming=ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"下线");
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		Channel incoming=ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"异常");
		cause.printStackTrace();
		ctx.close();
	}
}
