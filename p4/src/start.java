import java.io.IOException;

import client.Client;
import router.Router;
import server.Server;

public class start {

	public static void main(String[] args) {

		try {

			System.err.println("starting router1");
			String config1 = "C:\\Users\\FerdinandTrendelenbu\\git\\haw.bai4.rnp\\haw.bai4.rnp\\p4\\src\\configs\\Router1.conf";
			Router router1 = new Router(config1, 1, 2010, "::1:0");
			router1.start();

			System.err.println("starting router2");
			String config2 = "C:\\Users\\FerdinandTrendelenbu\\git\\haw.bai4.rnp\\haw.bai4.rnp\\p4\\src\\configs\\Router2.conf";
			Router router2 = new Router(config2, 2, 2020, "::2:0");
			router2.start();

			System.err.println("starting server1");
			String[] argServer1 = new String[1];
			argServer1[0] = "2012";
			Server server1 = new Server(argServer1);
			Thread server1Thread = new Thread(new Runnable() {

				@Override
				public void run() {
					server1.serve();
				}
			});
			server1Thread.start();

			System.err.println("starting server2");
			String[] argServer2 = new String[1];
			argServer2[0] = "2021";
			Server server2 = new Server(argServer2);
			Thread server2Thread = new Thread(new Runnable() {

				@Override
				public void run() {
					server2.serve();
				}
			});
			server2Thread.start();

			System.err.println("starting client");
			String[] argClient = new String[7];
			argClient[0] = "Data";
			argClient[1] = "hallo";
			argClient[2] = "::2:1";
			argClient[3] = "2011";
			argClient[4] = "::1:0";
			argClient[5] = "2010";
			argClient[6] = "255";
			Client client = new Client(argClient);
			client.sendAndReceiveMessage();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
