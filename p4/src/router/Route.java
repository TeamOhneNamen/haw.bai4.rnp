package router;

import java.io.IOException;
import java.net.Inet6Address;
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

	public Inet6Address getSourceIp() {
		return nextIp;
	}

	public void setSourceIp(Inet6Address sourceIp) {
		this.nextIp = sourceIp;
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

	/**
	 * sends an IpPacket to the next IP of the Route
	 * @param revievedPack
	 * @throws IOException
	 */
	public void send2Route(IpPacket revievedPack) throws IOException {
		NetworkLayer nwLayer = new NetworkLayer(nextPort);
		if (revievedPack.getHopLimit() <= 1) {
			revievedPack.setDestinationAddress(revievedPack.getSourceAddress());
			revievedPack.setSourceAddress(revievedPack.getNextHopIp());
			Route route = router.findRightRoute(revievedPack);
			setupAsICMP(revievedPack);
			route.send2Route(revievedPack);
		} else {
			revievedPack.setHopLimit(revievedPack.getHopLimit()-1);
			revievedPack.setNextHopIp(nextIp);
			revievedPack.setNextPort(nextPort);
			nwLayer.sendPacket(revievedPack);
		}
	}
	
	/**
	 * setting up the IpPacket as a ICMP packet
	 * @param revievedPack
	 */
	private void setupAsICMP(IpPacket revievedPack){
		
		revievedPack.setControlPayload("Time Exceeded".getBytes());
		revievedPack.setHopLimit(180);
		
	}

}
