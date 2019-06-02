package router;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.SocketException;
import java.net.UnknownHostException;

import layers.NetworkLayer;
import packets.IpPacket;

public class Route {

	Inet6Address destIp;
	Inet6Address nextIp;
	int nextPort;
	Router router;

	/**
	 * GETTER AND SETTER
	 */
	
	
	public Inet6Address getDestIp() {
		return destIp;
	}

	public void setDestIp(Inet6Address destIp) {
		this.destIp = destIp;
	}

	public Inet6Address getNextIp() {
		return nextIp;
	}

	public void setNextIp(Inet6Address nextIp) {
		this.nextIp = nextIp;
	}

	public int getNextPort() {
		return nextPort;
	}

	public void setNextPort(int port) {
		this.nextPort = port;
	}

	// constructor
	public Route(String destIp, String nextIp, int nextPort, Router router) throws UnknownHostException {
		this.router = router;
		this.destIp = (Inet6Address) Inet6Address.getByName(destIp);
		this.nextIp = (Inet6Address) Inet6Address.getByName(nextIp);
		this.nextPort = nextPort;
	}

}
