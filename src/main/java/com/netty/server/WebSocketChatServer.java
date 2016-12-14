package com.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class WebSocketChatServer {
	private int port;
	
	public WebSocketChatServer(int port) {
		// TODO Auto-generated constructor stub
		this.port=port;
	}

	public void run() {
		// TODO Auto-generated method stub
        EventLoopGroup bossGroup=new NioEventLoopGroup();
        EventLoopGroup workGroup=new NioEventLoopGroup();
        try {
			 ServerBootstrap b=new ServerBootstrap();
			 b.group(bossGroup, workGroup);
			 b.channel(NioServerSocketChannel.class);
			 b.childHandler(new WebSocketChatServerInitializer());
			 b.option(ChannelOption.SO_BACKLOG, 128);
			 b.childOption(ChannelOption.SO_KEEPALIVE, true);
			 
			 System.out.println("WebSocketChatServer 启动了");
			 ChannelFuture f=b.bind(port).sync();
			 
			 f.channel().closeFuture().sync();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			System.out.println("WebSocketChatServer 异常");
		}finally{
			workGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
			
			System.out.println("WebSocketChatServer 关闭了");
		}
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        int port;
        if(args.length>0){
        	port=Integer.parseInt(args[0]);
        }else{
        	port=7878;
        }
        new WebSocketChatServer(port).run();
	}

}
