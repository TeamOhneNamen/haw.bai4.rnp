package layers;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.SocketException;

import packets.IpPacket;

/**
 * The emulation of the network layer.
 *
 * Emulates the network layer, sending packets of the emulated
 * protocol hierarchy embedded in DatagramPackets to next hops
 * and receiving them.
 *
 * @author  Christian Keil
 * @version 1.0
 */
public class NetworkLayer {
    private DatagramSocket socket;

    /**
     * Initializes the network layer.
     *
     * Initializes a DatagramSocket on the provided port to
     * send and receive packets via UDP.
     *
     * @param port  the port to use
     */
    public NetworkLayer(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    /**
     * Gets an IpPacket from the network.
     *
     * Receives a DatagramPacket and extracts the IpPacket
     * from it.
     *
     * @return the received IpPacket
     */
    public IpPacket getPacket() throws IOException {
        byte[] buf = new byte[IpPacket.MAX_SIZE];
        DatagramPacket packet = new DatagramPacket(buf, buf.length);
        socket.receive(packet);
        return new IpPacket(packet);
    }

    /**
     * Sends an IpPacket to the network.
     *
     * Embeds the IpPacket in a DatagramPacket and sends it to
     * the next hop specified in the IpPacket.
     *
     * @param ipPacket  the packet to send
     */
    public void sendPacket(IpPacket ipPacket) throws IOException {
        byte[] data = ipPacket.getBytes();
        Inet6Address localhost = (Inet6Address) Inet6Address.getByName("2001:16B8:608A:9C00:D5C9:1DC6:4D8F:6C45");
        DatagramPacket packet = new DatagramPacket(data, data.length, localhost, ipPacket.getNextHopPort()); 
        //DatagramPacket packet = new DatagramPacket(data, data.length, ipPacket.getNextHopIp(), ipPacket.getNextHopPort());
        socket.send(packet);
    }
}
