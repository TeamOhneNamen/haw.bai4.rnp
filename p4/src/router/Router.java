package router;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import layers.NetworkLayer;
import packets.IpPacket;

public class Router extends Thread {

	final String FILENOTFOUND = "no file found";

	ArrayList<Route> routes = new ArrayList<Route>();
	int id;	
	int port;
	
	NetworkLayer netLayer;

	public Router(String configPath, int id, int port) {
		try {
			this.port = port;
			this.id = id;
			readConfig(configPath);
		} catch (IOException e) {
			outERR(FILENOTFOUND);
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		
		try {
			netLayer = new NetworkLayer(port);
			while (true) {
				IpPacket ipPacket = netLayer.getPacket();
	            Route route = findRightRoute(ipPacket);
	            if (route==null) {
					System.err.println("System not Reachable");
				}else {
					route.send2Route(ipPacket);
				}
			}
            
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			outERR("IP not in right format");
		} 
		

	}
	
	/**
	 * reads the config-file of the Router
	 * @param configPath
	 * @throws FileNotFoundException 
	 * @throws UnknownHostException 
	 * @throws NumberFormatException 
	 * @throws IOException
	 */
	private void readConfig(String configPath) throws FileNotFoundException, NumberFormatException, UnknownHostException  {
		try {
			FileReader fr = new FileReader(new File(configPath));
			BufferedReader br = new BufferedReader(fr);
			
			String fileLine = br.readLine();
			while (fileLine != null) {
				String[] splitfileLine = fileLine.split(";");
				if (splitfileLine[0].contains("/")) {
					routes.add(new Route(splitfileLine[0].split("/")[0], splitfileLine[1], Integer.parseInt(splitfileLine[2]), this));
				} else {
					routes.add(new Route(splitfileLine[0], splitfileLine[1], Integer.parseInt(splitfileLine[2]), this));
				}
				fileLine = br.readLine();
			}
			br.close();
			System.out.println(routes.size() + " routes read:");
			for (int i = 0; i < routes.size(); i++) {
				Route route = routes.get(i);
				System.out.println("Router " + id + " read Route: [TO: " + route.destIp + "] via [NextIP " + route.nextIp + "; PORT:" +route.nextPort + "]" );
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	/**
	 * find right Route for the IpPacket in The Router by iterating throw all Routes of the
	 * Router and check witch is the best via "betterIP()" (Longest-Prefix-Match)
	 * @param ipPack
	 * @return the Right Route
	 */
	public Route findRightRoute(IpPacket ipPack) {

		Route bestRoute = null;
		String string2BestRoute = null;
		String stringDestinationAddress = toBinary(ipPack.getDestinationAddress());

		for (int i = 0; i < routes.size(); i++) {

			boolean isBetter = (bestRoute == null || betterIP(string2BestRoute, toBinary(routes.get(i).destIp), stringDestinationAddress));
			
			if (isBetter) {
				bestRoute = routes.get(i);
				string2BestRoute = toBinary(routes.get(i).destIp);
			}
		}
		return bestRoute;

	}

	/**
	 * check if "newAddr" is closer to "destAddr" as "oldAddr" (Longest-Prefix-Match)
	 * @param oldAddr
	 * @param newAddr
	 * @param destAddr
	 * @return
	 */
	private boolean betterIP(String oldAddr, String newAddr, String destAddr) {
		
		char[] charDestAddr = destAddr.toCharArray();
		char[] charOldAddr = oldAddr.toCharArray();
		char[] charNewAddr = newAddr.toCharArray();


		for (int i = 0; i < charDestAddr.length; i++) {
			if (charDestAddr[i] != charOldAddr[i]) {
				if (charDestAddr[i] != charNewAddr[i]) {
					return false;
				}else {
					return true;
				}
			}
			if (charDestAddr[i] != charNewAddr[i]) {
				if (charDestAddr[i] != charOldAddr[i]) {
					return false;
				}else {
					return false;
				}
			}
		}
		return false;

	}

	/**
	 * transform an Inet6Address into an binary string of the Address
	 * @param i6addr
	 * @return
	 */
	private String toBinary(Inet6Address i6addr) {
		String longString = i6addr.getHostAddress().replace(":", "");
		String output = "";
		for (int i = 0; i < longString.length(); i++) {
			char c = longString.toCharArray()[i];
			output += String.format("%4s", Integer.toBinaryString(c));
		}
		System.out.println(output);
		return output;
	}
	

	private void outERR(String msg) {
		System.err.println("Router " + id + ": " + msg);
	}
}
