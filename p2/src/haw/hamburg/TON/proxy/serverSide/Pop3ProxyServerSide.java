package haw.hamburg.TON.proxy.serverSide;

import java.io.IOException;

import haw.hamburg.TON.*;

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 * Pop3ProxyServerSide starts the ServerSide-Routine
 *
 */
public class Pop3ProxyServerSide extends Thread{
	
	private int zeitabstand = 30000;
	
	private USER user;
	
	
	/**
	 * 
	 * @param zeitueberschreitung = Time between two ServersideRoutine 
	 * @param user = the Userdata includes The Password, username, serverAdress, port
	 */
	public Pop3ProxyServerSide(int zeitueberschreitung, USER user) {
		zeitabstand = zeitueberschreitung;
		this.user = user;
		
	}

	/**
	 * starts The ServerSide Thread
	 */
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
	
	/**
	 * Sends a Message to the Consol in Format: "[ProxyServer |ServerSide: "username"|]: " + Message
	 * @param msg = Message
	 * @param user2 = User who caused the message
	 */
	public static void send2ProxyConsole(String msg, String user2) {
		System.out.println("[ProxyServer <ServerSide: "+user2+">]: " + msg);
	}
	
	
		
}
