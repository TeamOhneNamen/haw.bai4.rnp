package haw.hamburg.TON.proxy.clinetSide;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import haw.hamburg.TON.Pop3ProxyServer;

public class Pop3ProxyClientSide extends Thread {

	private static int port; 
	private static ServerSocket server;
	
	// erstelle eine neue "BusinessThreadList"(BTL) zum verwalten der ClientThreads
	public static BusinessThreadList buisThreadList = new BusinessThreadList();
	
	public Pop3ProxyClientSide() {
	}
	
	
	public Pop3ProxyClientSide(int emailClinetPort) {
		port = emailClinetPort;
	}
	
	@Override
	public void run() {

		try {

			for (int i = 0; i < Pop3ProxyServer.maxVerbindungen; i++) {
				buisThreadList.add(new RoutineThreadClientSide(new Socket(), 0));
			}
			server = new ServerSocket(port);
			send2ProxyConsole("Server wurde auf port: " + port + " gestartet.");
			
			
			while(Pop3ProxyServer.serverAlive) {
				

				send2ProxyConsole("Warte auf eingehende Verbundungen...");
				
				Socket tempClient = server.accept();
				
				int free = buisThreadList.getFree();
				if (free!=-1) {
					send2ProxyConsole("Neue Verbindung mit Client: " + tempClient.getInetAddress().getHostAddress() + " eingegangen.");
					RoutineThreadClientSide btc = new RoutineThreadClientSide(tempClient, Pop3ProxyServer.timeout);
					buisThreadList.set(free, btc);
					btc.start();
				}else {
					send2ProxyConsole("client " + tempClient.getInetAddress().getHostAddress() + " kann nicht verbinden (TO MANNY CONNECTIONS)");
					PrintWriter out2Client = new PrintWriter(new OutputStreamWriter(tempClient.getOutputStream(), StandardCharsets.UTF_8), true);
					out2Client.println("-ERR TO MANY CONNECTION");
				}
				
				
					
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
