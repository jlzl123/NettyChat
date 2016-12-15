package com.netty.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.netty.server.WebSocketChatServer;


public class WebSocketChatServerListener implements ServletContextListener{

	public void contextDestroyed(ServletContextEvent ctx) {
		// TODO Auto-generated method stub
		
	}

	public void contextInitialized(ServletContextEvent ctx) {
		// TODO Auto-generated method stub
		new WebSocketChatServer(7878).run();
	}

}
