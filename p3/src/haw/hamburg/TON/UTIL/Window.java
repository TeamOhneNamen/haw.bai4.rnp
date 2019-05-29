package haw.hamburg.TON.UTIL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.FileCopyClient;
import haw.hamburg.TON.Exceptions.SeqNrNotInWindowException;

public class Window {

	ReentrantLock windowLock = new ReentrantLock();

	int sendTill = -1;
	private int windowPos = 0;
	int windowSize;
	ArrayList<FCpacket> list = new ArrayList<FCpacket>();
	FileCopyClient fileCopyClient;

	public Window(FCpacket fCpacket, int windowSize, FileCopyClient fileCopyClient) {
		this.list.add(fCpacket);
		this.windowSize = windowSize;
		this.fileCopyClient = fileCopyClient;

	}

	/**
	 * GETTER & SETTER
	 */
	public int getSendUntil() {
		return sendTill;
	}

	public synchronized int getWindowPos() {
		return windowPos;
	}

	public synchronized int incWindowPos() {
		return windowPos++;
	}

	public int getSize() {
		return list.size();

	}

	/**
	 *  put all msg from the byte[][] in new FCpackets 
	 *  PROBLEM: if the Data and the byte[] is to big, - Stack overflow 
	 *  bacause we load the whole Data in the Array.... that might be an problem
	 * @param list
	 */
	public void setupTheWindow(ArrayList<byte[]> list) {
		int seqNum = 1;
		for (int i = 0; i < list.size(); i++) {
			this.list.add(new FCpacket(seqNum, list.get(i), list.get(i).length));
			seqNum++;
		}
	}

	/**
	 * sendet alle Pakete im Windowbereich, die noch nicht gesendet wurden
	 * @throws IOException
	 */
	public void send() throws IOException {
		fileCopyClient.getWindowLock().lock();
		for (int i = 0; i < windowSize; i++) {
			if (getWindowPos() + i > sendTill) {
				if (getWindowPos() + i <= fileCopyClient.anzahlDerPackete - 1) {
					FCpacket fcp = list.get(getWindowPos() + i);
					fileCopyClient.testOut("send Packet: " + fcp.getSeqNum());
					sendTill = getWindowPos() + i;
					FileCopyClient.sends++;
					fileCopyClient.getUDP().send(fcp);
					fcp.setTimestamp(System.nanoTime());
					fileCopyClient.startTimer(fcp);

					fileCopyClient.testOutWindow();
				}

			}
		}
		fileCopyClient.getWindowLock().unlock();
	}

	/**
	 * receves packets and set the setValidACK from the recieved Packet to true. 
	 * if the Packet is @ the "sendTill" position then incWindowPos()
	 * @throws IOException
	 */
	public void revece() throws IOException {
		fileCopyClient.testOut("waiting for input");
		FCpacket fcp = fileCopyClient.getUDP().receve();

		fileCopyClient.getWindowLock().lock();
		try {
			fcp = getBySeqNum(fcp.getSeqNum());
			long duration = System.nanoTime() - fcp.getTimestamp();
			fileCopyClient.testOut("Packet " + fcp.getSeqNum() + " took " + duration + "ns");
			fileCopyClient.computeTimeoutValue(duration);

			// errechnung der Durchschnittlichen rtt
			if (FileCopyClient.avgRtt == 0) {
				FileCopyClient.avgRtt = duration;
			} else {
				FileCopyClient.avgRtt = FileCopyClient.avgRtt + duration;
			}

			getBySeqNum(fcp.getSeqNum()).setValidACK(true);
			fileCopyClient.testOut("ACK for Packet " + fcp.getSeqNum() + " receved!");
			fileCopyClient.cancelTimer(fcp);
			if (getWindowPos() != getSize()) {
				try {
					while (list.get(getWindowPos()).isValidACK()) {
						incWindowPos();
					}
				} catch (IndexOutOfBoundsException e) {
				}

			}

			fileCopyClient.testOutWindow();
		} catch (SeqNrNotInWindowException e) {
			System.out.println(e.getSeqNr() + " is not in window: " + fileCopyClient.getWindow().toString());
			// fileCopyClient.testOut(e.getSeqNr() + " is not in window: " +
			// fileCopyClient.getWindow().toString());
		}
		fileCopyClient.getWindowLock().unlock();

	}

	/**
	 * finds and return Packet with seqenceNumber: seqNum
	 * @param seqNum
	 * @return
	 * @throws SeqNrNotInWindowException
	 */
	
	public FCpacket getBySeqNum(long seqNum) throws SeqNrNotInWindowException {
		for (int i = 0; i < windowSize; i++) {
			if (getWindowPos() + i<list.size()) {
				if (list.get(getWindowPos() + i).getSeqNum() == seqNum) {
					return list.get(getWindowPos() + i);
				}
			}
			

		}
		throw new SeqNrNotInWindowException(seqNum, this);
	}

	/**
	 * return The Window as optical String
	 */
	@Override
	public String toString() {
		if (getWindowPos() == list.size()) {
			return "- empty -";
		}

		String output = list.get(getWindowPos()).getSeqNum() + " | ";
		for (int i = 0; i < windowSize; i++) {
			if (getWindowPos() + i <= list.size() - 1) {
				if (list.get(getWindowPos() + i).isValidACK()) {
					output += "#";
				} else {
					output += "-";
				}
			}

		}

		if (getWindowPos() + windowSize >= list.size()) {
			return output + " | " + list.get(list.size() - 1).getSeqNum();
		} else {
			return output + " | " + list.get(getWindowPos() + windowSize).getSeqNum();
		}
	}
}
