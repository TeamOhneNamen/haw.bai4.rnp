package haw.hamburg.TON;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
			printOut("Server wurde auf Port " + port + " gestartet!");
		} catch (IOException e) {
			printOut("error to bind Port: " +port);
			System.exit(-1);
		}
		
		// warte auf eingehende VErbindungen
		while (true) {
			
			try {
				
				//blockiert bis neue Verbindung eintrifft
				Socket client = sServer.accept();
				int freeThread = buisThreadList.getFree();
				
				if (freeThread!=-1) {
					
					BusinessThread tempClient = new BusinessThread(client);
					
					//startet einen behandelnden Thread
					buisThreadList.set(freeThread, tempClient) ;
					tempClient.start();

					//sende Clienten bestätigung über die Verbindung
					sendOkay("Verbindung zu " + sServer.getInetAddress().getHostAddress() + ":" + sServer.getLocalPort() + " hergestellt!", client);

					
					//Serverausgaben
					printOut("Client wurde mit Thread " + freeThread +" verbunden!");
//					printOut(buisThreadList.manyConnections() + "/" +maxVerbindungen);
					
				}else {
					
					//sende Clienten Fehler über die Verbindung
					sendError("NO MORE CLIENT POSSIBLE", client);
					client.close();
					
					//Serverausgaben
					printOut("Client wurde nicht verbunden!");
					
				}
				
			} catch (IOException e) {
				printOut("Ein Fehler ist aufgetreten: " + e.getMessage());
			}

		}

	}
	
	private static void sendError(String msg, Socket client) throws IOException {
		String output = "ERROR " + msg;
		send(output, client);
	}

	private static void sendOkay(String msg, Socket client) throws IOException {
		String output = "OK " + msg;
		send(output, client);
	}
	
	private static void send(String output, Socket client) throws IOException {
		PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true);
		if (output.getBytes().length < 255) {
			out.println(output);
		} else {
			out.println("ERROR STRING TOO LONG");
		}

	}
	
	private static void printOut(String msg) {
		System.out.println("[SERVER] \"" + msg + "\"");
	}
	
}
