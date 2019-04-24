package haw.hamburg.TON.proxy.serverSide;

import java.io.IOException;
import java.net.Socket;

public class Pop3ProxyServerSide extends Thread{
	
	private int port = 11000; 
	private String internetAdresse = "localhost"; 
	
	private Socket server;
	private int zeitabstand = 30000;
	
	
	public Pop3ProxyServerSide() {
	}
	
	public Pop3ProxyServerSide(int zeitAbstand) {
		zeitabstand = zeitAbstand;
	}
	
	public Pop3ProxyServerSide(int internetPort, int zeitAbstand) {
		port = internetPort;
		zeitabstand = zeitAbstand;
	}

	public Pop3ProxyServerSide(String internetadres, int internetPort, int zeitAbstand) {
		port = internetPort;
		zeitabstand = zeitAbstand;
		internetAdresse = internetadres;
	}
	
	@Override
	public void run() {
		
		try {
			server = new Socket(internetAdresse, port);
			send2ProxyConsole("Verbindung zu: >" + internetAdresse + ":" + port + "< hergestellt.");

			RoutineThreadServerSide routineThread = new RoutineThreadServerSide(zeitabstand, server);
			routineThread.start();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void send2ProxyConsole(String msg) {
		System.out.println("[ProxyServer <ServerSide>]: " + msg);
	}
	
	
		
}
