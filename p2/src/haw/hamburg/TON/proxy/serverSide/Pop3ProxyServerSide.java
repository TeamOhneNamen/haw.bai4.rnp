package haw.hamburg.TON.proxy.serverSide;

import java.io.IOException;
import java.net.Socket;

import haw.hamburg.TON.Pop3ProxyServer;
import haw.hamburg.TON.USER;

public class Pop3ProxyServerSide extends Thread{
	
	private int zeitabstand = 30000;
	
	private USER user;
	
	
	public Pop3ProxyServerSide() {
	}
	
	public Pop3ProxyServerSide(int zeitAbstand) {
		zeitabstand = zeitAbstand;
	}
	
	public Pop3ProxyServerSide(int zeitueberschreitung, USER user) {
		zeitabstand = zeitueberschreitung;
		this.user = user;
		
	}

	@Override
	public void run() {

		Pop3ProxyServer.send2ProxyConsole("Pop3ProxyServerSide gestartet");
		
		try {
			

			RoutineThreadServerSide routineThread = new RoutineThreadServerSide(zeitabstand, user);
			routineThread.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void send2ProxyConsole(String msg, String user2) {
		System.out.println("[ProxyServer <ServerSide: "+user2+">]: " + msg);
	}
	
	
		
}
