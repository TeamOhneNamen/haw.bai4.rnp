package router;

import packets.IpPacket;

public class RouteNotExistException extends Exception {

	private static final long serialVersionUID = 1L;

	public RouteNotExistException(IpPacket ipPack) {
		super("Route to: [" +ipPack.getDestinationAddress() + "] Not Exist");
	}
	
}
