package com.netty.server;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebSocketChatServerInitializer extends ChannelInitializer<SocketChannel>{

	//���� ChannelPipeline ��������ע��� Channel,��װ������Ҫ�ġ� ChannelHandler
	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		// TODO Auto-generated method stub
		ChannelPipeline p=ch.pipeline();
		p.addLast(new HttpServerCodec());
		p.addLast(new HttpObjectAggregator(64*1024));
		p.addLast(new ChunkedWriteHandler());
		p.addLast(new HttpRequestHandler("/ws"));
		p.addLast(new WebSocketServerProtocolHandler("/ws"));
		p.addLast(new TextWebSocketFramerHandler());
	}

}
