package com.netty.server;

import java.io.File;
import java.io.RandomAccessFile;
import java.net.URISyntaxException;
import java.net.URL;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.stream.ChunkedNioFile;
//����http����
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
	private final String wsUri;
	private static final File INDEX;

	static{
		URL location=HttpRequestHandler.class.getProtectionDomain().getCodeSource().getLocation();
		try{
			String path=location.toURI()+"WebSocketChatClient.html";
			path=!path.contains("file:")?path:path.substring(5);
			INDEX=new File(path);
		}catch(URISyntaxException e){
			throw new IllegalStateException("Unable to locate WebsocketChatClient.html",e);
		}
	}
	
	public HttpRequestHandler(String wsUri) {
		// TODO Auto-generated constructor stub
	    this.wsUri=wsUri;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request)
			throws Exception {
		// TODO Auto-generated method stub
		if(wsUri.equalsIgnoreCase(request.getUri())){
			//��������� WebSocket �������������ü����������������ҽ������ݸ��� ChannelPipeline �е��¸� ChannelInboundHandler
			ctx.fireChannelRead(request.retain());
		}else{
			if(HttpHeaders.is100ContinueExpected(request)){
				//������� HTTP 1.1�� "100 Continue" ����
				send100Continue(ctx);
			}
			
			//��ȡĬ�ϵ� WebsocketChatClient.html ҳ��
			RandomAccessFile file=new RandomAccessFile(INDEX, "r");
			
			HttpResponse respone=new DefaultHttpResponse(request.getProtocolVersion(),HttpResponseStatus.OK);
			respone.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html;charset=utf-8");
			boolean keepAlive=HttpHeaders.isKeepAlive(request);
			//�ж� keepalive �Ƿ�������ͷ����
			if(keepAlive){
				respone.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
				respone.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}
			//д HttpResponse ���ͻ���
			ctx.write(respone);
			
//д index.html ���ͻ��ˣ��ж� SslHandler �Ƿ��� ChannelPipeline ��������ʹ�� DefaultFileRegion ����ChunkedNioFile
			if(ctx.pipeline().get(SslHandler.class)==null){
				ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
			}else{
				ctx.writeAndFlush(new ChunkedNioFile(file.getChannel()));
			}
			//д��ˢ�� LastHttpContent ���ͻ��ˣ������Ӧ���
			ChannelFuture future=ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			//��� keepalive û��Ҫ�󣬵�д���ʱ���ر� Channel
			if(!keepAlive){
				future.addListener(ChannelFutureListener.CLOSE);
			}
			
			file.close();
		}
	}

	private static void send100Continue(ChannelHandlerContext ctx){
		FullHttpResponse respone=new DefaultFullHttpResponse(HttpVersion.HTTP_1_1,HttpResponseStatus.CONTINUE);
		ctx.writeAndFlush(respone);
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		// TODO Auto-generated method stub
		Channel incoming=ctx.channel();
		System.out.println("Client:"+incoming.remoteAddress()+"�쳣");
		cause.printStackTrace();
		ctx.close();
	}
}
