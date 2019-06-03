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
import java.util.NoSuchElementException;

import layers.NetworkLayer;
import packets.IpPacket;
import packets.ControlPacket;

public class Router extends Thread {

	// Konstanten
	final String FILENOTFOUND = "no file found";
	final String DESTINATIONUNREACHABLE = "Destination Unreachable";
	final String TIMEEXCEEDET = "Time Exceedet";
	final int HOPLIMIT = 255;

	int id;
	int port;
	String inet6Address;
	NetworkLayer netLayer;

	ArrayList<Route> routes = new ArrayList<Route>();
	
	// Constructor
	public Router(String configPath, int id, int port, String inet6Address) {
		try {
			netLayer = new NetworkLayer(port);
			this.inet6Address = inet6Address;
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

		IpPacket ipPacket = null;
		try {

			if (id == 0) {
				Inet6Address sourceAddr = (Inet6Address) Inet6Address.getByName(this.inet6Address);
				Inet6Address destination = (Inet6Address) Inet6Address.getByName("::2:1");
				ipPacket = new IpPacket(sourceAddr, destination, 100, null, 0);
				ipPacket.setDataPayload("hallo".getBytes());
				Route route = findRightRoute(ipPacket);
				ipPacket.setNextHopIp(route.getNextIp());
				ipPacket.setNextPort(route.getNextPort());

				printPacket(ipPacket);
				netLayer.sendPacket(ipPacket);
			}

			while (true) {
				ipPacket = netLayer.getPacket();
				Route route = findRightRoute(ipPacket);
				if (route == null) {
					outERR("System not Reachable");
				} else {
					send2Route(ipPacket);
				}
			}

		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			outERR("IP not in right format");
		} catch (RouteNotExistException e) {
			routeNotExist(ipPacket);
		}

	}

	/**
	 * reads the config-file of the Router
	 * 
	 * @param configPath
	 * @throws FileNotFoundException
	 * @throws UnknownHostException
	 * @throws NumberFormatException
	 * @throws IOException
	 */
	private void readConfig(String configPath)
			throws FileNotFoundException, NumberFormatException, UnknownHostException {
		try {
			FileReader fr = new FileReader(new File(configPath));
			BufferedReader br = new BufferedReader(fr);

			String fileLine = br.readLine();
			while (fileLine != null) {
				String[] splitfileLine = fileLine.split(";");
				if (splitfileLine[0].contains("/")) {
					routes.add(new Route(splitfileLine[0].split("/")[0], splitfileLine[1],
							Integer.parseInt(splitfileLine[2]), this));
				} else {
					routes.add(new Route(splitfileLine[0], splitfileLine[1], Integer.parseInt(splitfileLine[2]), this));
				}
				fileLine = br.readLine();
			}
			br.close();
			out2Console(routes.size() + " routes read:");
			for (int i = 0; i < routes.size(); i++) {
				Route route = routes.get(i);
				out2Console("Read Route: [TO: " + route.destIp + "] via [NextIP " + route.nextIp
						+ "; PORT:" + route.nextPort + "]");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * find right Route for the IpPacket in The Router by iterating throw all Routes
	 * of the Router and check witch is the best via "betterIP()"
	 * (Longest-Prefix-Match)
	 * 
	 * @param ipPack
	 * @return the Right Route
	 * @throws RouteNotExistException
	 */
	public Route findRightRoute(IpPacket ipPack) throws RouteNotExistException {

		Route bestRoute = null;
		String string2BestRoute = null;
		String stringDestinationAddress = toBinary(ipPack.getDestinationAddress());

		for (int i = 0; i < routes.size(); i++) {

			Route tempRoute = routes.get(i);

			if (tempRoute.destIp.equals(ipPack.getDestinationAddress())) {
				boolean isBetter = (bestRoute == null
						|| betterIP(string2BestRoute, toBinary(routes.get(i).nextIp), stringDestinationAddress));

				if (isBetter) {
					string2BestRoute = toBinary(routes.get(i).nextIp);
					bestRoute = tempRoute;
				}
			}
		}

		if (bestRoute == null) {
			throw new RouteNotExistException(ipPack);
		}
		return bestRoute;

	}

	/**
	 * sends an IpPacket to the next IP of the Route
	 * 
	 * @param revievedPack
	 * @throws IOException
	 */
	public void send2Route(IpPacket revievedPack) {

		try {
			if (revievedPack.getHopLimit() <= 1) {
				revievedPack = setUpAsICMP(revievedPack, TIMEEXCEEDET);
				printPacket(revievedPack);
				netLayer.sendPacket(revievedPack);
			} else {
				Route newRoute = findRightRoute(revievedPack);
				revievedPack.setHopLimit(revievedPack.getHopLimit() - 1);
				revievedPack.setNextHopIp(newRoute.getNextIp());
				revievedPack.setNextPort(newRoute.getNextPort());
				printPacket(revievedPack);
				netLayer.sendPacket(revievedPack);
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (RouteNotExistException e) {
			routeNotExist(revievedPack);
		}

	}

	/**
	 * whenn The Route not Exist, send a "DESTINATIONUNREACHABLE" controllpack
	 * @param revievedPack
	 */
	public void routeNotExist(IpPacket revievedPack) {
		try {
			System.out.println("Route zu: " + revievedPack.getDestinationAddress() + " exestiert nicht!");
			revievedPack = setUpAsICMP(revievedPack, DESTINATIONUNREACHABLE);
			printPacket(revievedPack);
			netLayer.sendPacket(revievedPack);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * changing the setting of Packet "ipPack" as ICMP packet with the Message:
	 * "msg"
	 * 
	 * @param ipPack
	 * @param msg
	 * @return
	 * @throws UnknownHostException
	 */
	public IpPacket setUpAsICMP(IpPacket ipPack, String msg) throws UnknownHostException {

		Inet6Address dest = ipPack.getSourceAddress();
		Inet6Address source = (Inet6Address) Inet6Address.getByName(this.inet6Address);

		IpPacket newIpPacket = new IpPacket(source, dest, HOPLIMIT, null, 0);

		try {
			Route route = findRightRoute(newIpPacket);

			newIpPacket.setNextHopIp(route.getNextIp());
			newIpPacket.setNextPort(route.getNextPort());
			newIpPacket.setHopLimit(HOPLIMIT);
			ControlPacket controlPacket;
			if (msg.equals(TIMEEXCEEDET)) {
				controlPacket = new ControlPacket(ControlPacket.Type.TimeExceeded, new byte[0]);
			} else {
				controlPacket = new ControlPacket(ControlPacket.Type.DestinationUnreachable, new byte[0]);
			}
			newIpPacket.setControlPayload(controlPacket.getBytes());
		} catch (RouteNotExistException e) {
			e.printStackTrace();
		}

		return newIpPacket;
	}

	/**
	 * check if "newAddr" is closer to "destAddr" as "oldAddr"
	 * (Longest-Prefix-Match)
	 * 
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
				} else {
					return true;
				}
			}
			if (charDestAddr[i] != charNewAddr[i]) {
				if (charDestAddr[i] != charOldAddr[i]) {
					return false;
				} else {
					return false;
				}
			}
		}
		return false;

	}

	/**
	 * transform an Inet6Address into an binary string of the Address
	 * 
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
		return output;
	}

	private void outERR(String msg) {
		System.err.println("Router " + id + ": " + msg);
	}

	private void out2Console(String msg) {
		System.out.println("Router " + id + ": " + msg);
	}

	private void printPacket(IpPacket revievedPack) {
		
		boolean ismsg = false;
		String msg = "";
		try {
			msg = ("MSG: " + revievedPack.getDataPacket().toString());
			out2Console("-----------------------" + "MSG" + "-----------------------");
		} catch (NoSuchElementException e) {
			outERR("------------" + "ICMP Type: " + revievedPack.getControlPacket().toString() + "------------");
		}
		out2Console("from: " + revievedPack.getSourceAddress());
		out2Console("to:   " + revievedPack.getDestinationAddress());
		out2Console("via:  " + revievedPack.getNextHopIp() + "/" + revievedPack.getNextHopPort());
		out2Console("HOPLIMIT: " + revievedPack.getHopLimit());
		
		if (ismsg) {
			out2Console("MSG: " + msg);
		}
		
		out2Console("-------------------------------------------------");
		
	}
}
