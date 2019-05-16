package haw.hamburg.TON;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import haw.hamburg.TON.proxy.clinetSide.Pop3ProxyClientSide;
import haw.hamburg.TON.proxy.serverSide.Pop3ProxyServerSide;

public class Pop3ProxyServer {

	private final static String CONFIG_FILE_PATH = "src/config.txt";
	private final static String USER_NAME = "USER_NAME";
	private final static String USER_PASSWORT = "USER_PASSWORT";
	private final static int emailClinetPort = 1300;
	public final static int maxVerbindungen = 4;
	private final static String pop3ServerAdress = "lab30.cpt.haw-hamburg.de";
//	private final static String pop3ServerAdress = "localhost";
	private final static int pop3ServerPort = 11000;
	
	public static boolean serverAlive = false;
	public static USERList userList = new USERList();
	public static int timeout = 3000;
	private static int zeitueberschreitung = 30000;


	public Pop3ProxyServer(int zeitUeberschreitung, int timeOut) {
		zeitueberschreitung = zeitUeberschreitung;
		timeout = timeOut;
	}

	public static void main(String[] args) {

//		String zeitUeberschreitungString = args[0];
//		String timeoutString = args[1];
//		
//		zeitueberschreitung = Integer.valueOf(zeitUeberschreitungString);
//		timeout = Integer.valueOf(timeoutString);
		
		serverAlive = true;

		Properties properties = retrieveProperties(CONFIG_FILE_PATH);
		userList.add(new USER(properties.getProperty(USER_NAME), properties.getProperty(USER_PASSWORT)));
		
		new ServerCommandLineThread().start();

		Pop3ProxyClientSide popClinetSide = new Pop3ProxyClientSide(emailClinetPort);
		popClinetSide.start();

		Pop3ProxyServerSide popServerSide = new Pop3ProxyServerSide(pop3ServerAdress, pop3ServerPort,
				zeitueberschreitung);
		popServerSide.start();

	}
	//reads properties from a config file at the given path
	private static Properties retrieveProperties(String path) {
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(path));
			return properties;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void send2ProxyConsole(String string) {
		System.out.println("[ProxyServer <CORE>]: " + string);
		
	}

}
