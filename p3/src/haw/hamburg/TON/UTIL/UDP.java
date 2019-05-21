package haw.hamburg.TON.UTIL;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class UDP {

	InetAddress inet_adr;
	int inet_port;

	long seqNr;
	int dataSize;
	byte[] receiveData;
	FC_Timer fct;
	
	DatagramSocket udp_Socket = new DatagramSocket();
	
	public UDP(InetAddress adr, int port, int dataSize) throws SocketException {
		this.dataSize = dataSize;
		this.inet_adr = adr;
		this.inet_port = port;
		this.udp_Socket.connect(inet_adr, port);
		this.receiveData = new byte[dataSize];
		
	}
	
	public void send(FCpacket packet) throws IOException {
		
		receiveData = packet.getSeqNumBytesAndData();
		dataSize = packet.getSeqNumBytesAndData().length;
		seqNr = packet.getSeqNum();
		DatagramPacket data = new DatagramPacket(receiveData, dataSize);
		udp_Socket.send(data);
		
	}
	
	public FCpacket receve() throws IOException {
		
		receiveData = new byte[dataSize];
		DatagramPacket data = new DatagramPacket(receiveData, dataSize);
		udp_Socket.receive(data);
		FCpacket newFCpacket = new FCpacket(data.getData(), data.getLength());
		return newFCpacket;
	}
	
}
