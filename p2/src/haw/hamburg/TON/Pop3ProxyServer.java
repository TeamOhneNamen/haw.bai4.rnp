package haw.hamburg.TON;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import haw.hamburg.TON.proxy.clinetSide.Pop3ProxyClientSide;
import haw.hamburg.TON.proxy.serverSide.Pop3ProxyServerSide;
/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 * 
 * Pop3ProxyServer mail Class to start the Proxy Server
 * Loading some ConfigDatas from "src/config.txt":
 * USER_NAME: Usernames of POP3 EMAIL_Accounts
 * USER_PASSWORT: Passwords of POP3 EMAIL_Accounts
 * EMAIL_SERVER: ServerAdress of the POP3-Server of the EMAIL_Accounts
 * EMAIL_SERVER_PORT: ServerPortrs of the POP3-Server of the EMAIL_Accounts
 * 
 * !!!
 * multible User: seperate with "," like:
 * USER_NAME = me,you
 * USER_PASSWORT = mePass,youPass
 * EMAIL_SERVER = server1, server2
 * EMAIL_SERVER_PORT = serverPort1, serverPort2
 * !!!
 *
 */
public class Pop3ProxyServer {

	private final static String CONFIG_FILE_PATH = "src/config.txt";
	private final static String USER_NAME = "USER_NAME";
	private final static String USER_PASSWORT = "USER_PASSWORT";
	private final static String EMAIL_SERVER = "EMAIL_SERVER";
	private final static String EMAIL_SERVER_PORT = "EMAIL_SERVER_PORT";
	private final static int emailClinetPort = 1300;
	public final static int maxVerbindungen = 4;
	
	public static boolean serverAlive = false;
	public static USERList userList = new USERList();
	public static int timeout = 3000;
	private static int zeitueberschreitung = 30000;


	public Pop3ProxyServer(int zeitUeberschreitung, int timeOut) {
		zeitueberschreitung = zeitUeberschreitung;
		timeout = timeOut;
	}

	/**
	 * main Mathod starts the Proxy server With Arguments From the ConfigFile
	 * @param args = NOTHING
	 */
	public static void main(String[] args) {
		
		serverAlive = true;

		
		//Lese Propertys aus
		Properties properties = retrieveProperties(CONFIG_FILE_PATH);
		String[] usernames = properties.getProperty(USER_NAME).split(",");
		String[] passworts = properties.getProperty(USER_PASSWORT).split(",");
		String[] emailservers = properties.getProperty(EMAIL_SERVER).split(",");
		String[] emailserverports = properties.getProperty(EMAIL_SERVER_PORT).split(",");
		
		for (int i = 0; i < usernames.length; i++) {
			userList.add(new USER(usernames[i], passworts[i], emailservers[i], emailserverports[i]));
		}
		
		for (int i = 0; i < userList.size(); i++) {
			System.out.println(userList.get(i).getUsername() + " " + userList.get(i).getPasswort() + " " + userList.get(i).getServerName() + " " + userList.get(i).getServerPort());
		}
		
		new ServerCommandLineThread().start();

		Pop3ProxyClientSide popClinetSide = new Pop3ProxyClientSide(emailClinetPort);
		popClinetSide.start();

		for (int i = 0; i < userList.size(); i++) {
			new Pop3ProxyServerSide(zeitueberschreitung, userList.get(i)).start();
		}
		

	}
	
	/**
	 * reads properties from a config file at the given path
	 * @param path -> of the config file
	 * @return properties(ACCDATA)
	 */
	
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

	/**
	 * Prints The "string" on the console with [ProxyServer |CORE|]: "string"
	 * @param string = print out Message
	 */
	
	public static void send2ProxyConsole(String string) {
		System.out.println("[ProxyServer <CORE>]: " + string);
		
	}

}
