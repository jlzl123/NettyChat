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
//处理http请求
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest>{
	private final String wsUri;
	private static final File INDEX;

	static{
		//获得类运行时的本地路径
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
			//如果请求是 WebSocket 升级，递增引用计数器（保留）并且将它传递给在 ChannelPipeline 中的下个 ChannelInboundHandler
			ctx.fireChannelRead(request.retain());
		}else{
			if(HttpHeaders.is100ContinueExpected(request)){
				//处理符合 HTTP 1.1的 "100 Continue" 请求
				send100Continue(ctx);
			}
			
			//读取默认的 WebsocketChatClient.html 页面,"r" 以只读方式打开 
			RandomAccessFile file=new RandomAccessFile(INDEX, "r");
			
			HttpResponse respone=new DefaultHttpResponse(request.getProtocolVersion(),HttpResponseStatus.OK);
			respone.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html;charset=utf-8");
			boolean keepAlive=HttpHeaders.isKeepAlive(request);
			//判断 keepalive 是否在请求头里面
			if(keepAlive){
				respone.headers().set(HttpHeaders.Names.CONTENT_LENGTH, file.length());
				respone.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
			}
			//写 HttpResponse 到客户端
			ctx.write(respone);
			
//写 index.html 到客户端，判断 SslHandler 是否在 ChannelPipeline 来决定是使用 DefaultFileRegion 还是ChunkedNioFile
			if(ctx.pipeline().get(SslHandler.class)==null){
				ctx.write(new DefaultFileRegion(file.getChannel(), 0, file.length()));
			}else{
				ctx.writeAndFlush(new ChunkedNioFile(file.getChannel()));
			}
			//写并刷新 LastHttpContent 到客户端，标记响应完成
			ChannelFuture future=ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
			//如果 keepalive 没有要求，当写完成时，关闭 Channel
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
		System.out.println("Client:"+incoming.remoteAddress()+"异常");
		cause.printStackTrace();
		ctx.close();
	}
}
