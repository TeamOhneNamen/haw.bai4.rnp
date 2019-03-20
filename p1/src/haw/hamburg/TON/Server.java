package haw.hamburg.TON;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	
	// port festlegen
	
	static ServerSocket sServer;
	static int verbindungen;
	static int maxVerbindungen = 3;
	static int port = 25615;
	
	static BusinessThreadList buisThreadList = new BusinessThreadList();
	
	//Constructor 
	public Server() {
		port = 25615;
	}
	
	public static void main(String[] args) {

		for (int i = 0; i < maxVerbindungen; i++) {
			buisThreadList.add(new BusinessThread(null));
		}
		
		
		// versuche den Server auf dem port zu starten
		try {
			sServer = new ServerSocket(port);
			System.out.println("[Server] Server wurde auf Port " + port + " gestartet!");
		} catch (IOException e) {
			System.out.println("[Server] error to bind Port: " +port);
			System.exit(-1);
		}
		
		while (true) {
			
			try {
				
				//blockiert bis neue Verbindung eintrifft
				Socket client = sServer.accept();
				
				if (buisThreadList.getFree()!=-1) {
					
					
					BusinessThread tempClient = new BusinessThread(client);
					int freeThread = buisThreadList.getFree();
					buisThreadList.set(freeThread, tempClient) ;
					tempClient.start();

					//sende Clienten bestätigung über die Verbindung
					PrintWriter out = new PrintWriter(client.getOutputStream(), true);
					out.println("Verbindung hergestellt!");
					
					//Serverausgaben
					System.out.println("[Server] Client wurde mit Thread " +freeThread +" verbunden!");
					System.out.println("[Server] " + buisThreadList.manyConnections() + "/" +maxVerbindungen);
					
				}else {
					PrintWriter out = new PrintWriter(client.getOutputStream(), true);
					out.println("Der Server ist ausgelastet!");
					System.out.println("[Server] Client wurde nicht verbunden!");
					
					client.close();
				}
				
			} catch (IOException e) {
				// TODO
				System.out.println("Ein Fehler ist aufgetreten ");
				Thread.currentThread().interrupt();
			}

		}

	}
}
