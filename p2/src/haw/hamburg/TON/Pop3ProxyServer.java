package haw.hamburg.TON;

import haw.hamburg.TON.proxy.clinetSide.Pop3ProxyClientSide;
import haw.hamburg.TON.proxy.serverSide.Pop3ProxyServerSide;

public class Pop3ProxyServer {

	public static boolean serverAlive = false;
	public static USERList userList= new USERList(); 
	
	private static String pop3ServerAdress = "localhost";
	private static int pop3ServerPort = 12345;
	
	private static int emailClinetPort = 1300;

	private static int zeitueberschreitung = 3000000;
	
	
	public Pop3ProxyServer(int zeitUeberschreitung) {
		zeitueberschreitung = zeitUeberschreitung;
	}
	
	public static void main(String[] args) {

		
		serverAlive = true;
//		TODO: read from config file
		userList.add(new USER("Ferdinand", "Passwort"));
		
		new ServerCommandLineThread().start();
		
		Pop3ProxyClientSide popClinetSide = new Pop3ProxyClientSide(emailClinetPort, zeitueberschreitung);
		popClinetSide.start();
		
		Pop3ProxyServerSide popServerSide = new Pop3ProxyServerSide(pop3ServerAdress, pop3ServerPort, zeitueberschreitung);
		popServerSide.start();	
		
	}
	
	
	
	
}
