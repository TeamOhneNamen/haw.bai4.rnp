package haw.hamburg.TON;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	static int port;
	static ServerSocket sServer;
	static int verbindungen;
	static int moeglicheVerbindungen;
	
	public Server() {
		port = 25615;
	}
	
	public static void main(String[] args) {
		
		try {
			sServer = new ServerSocket(port);
			
			
		} catch (IOException e) {
			System.out.println("[Server:] error to bind Port: " + port);
			System.exit(-1);
		}
		
		while (true)
	    {
	      try
	      {
	        //Blocken, bis eine Anfrage kommt:
	        System.out.println ("ServerSocket - accepting");
	        Socket clientSocket = sServer.accept();
	        
	        //Wenn die Anfrage da ist, dann wird ein Thread gestartet, der 
	        //die weitere Verarbeitung übernimmt.
	        System.out.println ("ServerSocket - accept done");
	      }
	      catch (IOException e)
	      {
	    	//TODO
	        System.out.println("Ein Fehler ist aufgetreten ");
	        System.exit(-1);
	      }
	      
	    }
		
	}
	
}
