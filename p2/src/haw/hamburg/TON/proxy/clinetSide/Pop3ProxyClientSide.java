package haw.hamburg.TON.proxy.clinetSide;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import haw.hamburg.TON.Pop3ProxyServer;

public class Pop3ProxyClientSide extends Thread {

	private static int port = 11000; 
	private static ServerSocket server;
//	private static int zeitabstand = 30000;
	
	
	public Pop3ProxyClientSide() {
	}
	
	public Pop3ProxyClientSide(int zeitAbstand) {
//		zeitabstand = zeitAbstand;
	}
	
	public Pop3ProxyClientSide(int emailClinetPort, int zeitAbstand) {
		port = emailClinetPort;
//		zeitabstand = zeitAbstand;
	}
	
	@Override
	public void run() {

		try {
			
			server = new ServerSocket(port);
			send2ProxyConsole("Server wurde auf port: " + port + " gestartet.");
			
			
			while(Pop3ProxyServer.serverAlive) {
				

				send2ProxyConsole("Warte auf eingehende Verbundungen...");
				
				Socket tempClient = server.accept();
				send2ProxyConsole("Neue Verbindung mit Client: " + tempClient.getInetAddress().getHostAddress() + " eingegangen.");
				RoutineThreadClientSide btc = new RoutineThreadClientSide(tempClient);
				btc.start();
				
				
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				server.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		
		
	}
	
//	private static void newUserKonto(String User, String Passwort, String Serveradresse, int Port) {
//			
//	}
	

	
	public static void send2ProxyConsole(String msg) {
		System.out.println("[ProxyServer <ClientSide>]: " + msg);
	}

	
}
