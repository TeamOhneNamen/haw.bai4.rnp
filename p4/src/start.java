import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import client.Client;
import router.Router;
import server.Server;

public class start {

	public static void main(String[] args) {
		
		try {
			
			System.out.println("starting router1");
			String config1 = "C:\\Users\\FerdinandTrendelenbu\\git\\haw.bai4.rnp\\haw.bai4.rnp\\p4\\src\\configs\\Router1.conf";
			Router router1 = new Router(config1, 1, 2010);
			router1.start();

			System.out.println("starting router2");
			String config2 = "C:\\Users\\FerdinandTrendelenbu\\git\\haw.bai4.rnp\\haw.bai4.rnp\\p4\\src\\configs\\Router2.conf";
			Router router2 = new Router(config2, 2, 2020);
			router2.start();

			System.out.println("starting server1");
			String[] argServer1 = new String[1];
			argServer1[0] = "2012";
			Server server1 = new Server(argServer1);	

			System.out.println("starting server2");
			String[] argServer2 = new String[1];
			argServer2[0] = "2021";
			Server server2 = new Server(argServer2);
			

			System.out.println("starting client");
			String[] argClient = new String[7];
			argClient[0] = "Data";
			argClient[1] = "hallo";
			argClient[2] = "::1:2";
			argClient[3] = "2011";
			argClient[4] = "::1:0";
			argClient[5] = "2010";
			argClient[6] = "100";
			Client client = new Client(argClient);
			client.sendAndReceiveMessage();
			
			String[] argRouter2 = new String[1];
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	
	}
	
}
