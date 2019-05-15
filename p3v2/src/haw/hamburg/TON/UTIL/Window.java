package haw.hamburg.TON.UTIL;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.FileCopyClient;
import haw.hamburg.TON.Exceptions.SeqNrNotInWindowException;

public class Window {

	ReentrantLock windowLock = new ReentrantLock();

	int sendTill = -1;
	int windowPos = 0;
	int windowSize;
	ArrayList<FCpacket> list = new ArrayList<FCpacket>();
	FileCopyClient fileCopyClient;

	public Window(FCpacket fCpacket, int windowSize, FileCopyClient fileCopyClient) {
		this.list.add(fCpacket);
		this.windowSize = windowSize;
		this.fileCopyClient = fileCopyClient;

	}

	public int getSendUntil() {
		return sendTill;
	}

	public int getWindowPos() {
		return windowPos;
	}

	public int getSize() {
		return list.size();

	}

	public void setupTheWindow(ArrayList<String> list) {
		int seqNum = 1;
		for (int i = 0; i < list.size(); i++) {
			this.list.add(new FCpacket(seqNum, list.get(i).getBytes(), list.get(i).length()));
			seqNum++;
		}
	}

	public void send() throws IOException {
		windowLock.lock();
		for (int i = 0; i < windowSize; i++) {
			if (windowPos + i > sendTill) {
				if (windowPos + i <= fileCopyClient.anzahlDerPackete - 1) {
					FCpacket fcp = list.get(windowPos + i);
					fileCopyClient.testOut("send Packet: " + fcp.getSeqNum());
					sendTill = windowPos + i;
					fileCopyClient.sends++;
					fileCopyClient.getUDP().send(fcp);
					fcp.setTimestamp(System.nanoTime());
					fileCopyClient.startTimer(fcp);

					fileCopyClient.testOutWindow();
				}

			}
		}
		windowLock.unlock();
	}

	public void revece() throws IOException {
		fileCopyClient.testOut("waiting for input");
		FCpacket fcp = fileCopyClient.getUDP().receve();

		windowLock.lock();
		try {
			fcp = getBySeqNum(fcp.getSeqNum());
			long duration = System.nanoTime() - fcp.getTimestamp();
			fileCopyClient.testOut("Packet " + fcp.getSeqNum() + " took " + duration + "ns");
			fileCopyClient.computeTimeoutValue(duration);
			
			//errechnung der Durchschnittlichen rtt
			if (FileCopyClient.avgRtt==0) {
				FileCopyClient.avgRtt = duration;
			}else {
				FileCopyClient.avgRtt = FileCopyClient.avgRtt + duration;
			}
			
			getBySeqNum(fcp.getSeqNum()).setValidACK(true);
			fileCopyClient.testOut("ACK for Packet " + fcp.getSeqNum() + " receved!");
			fileCopyClient.cancelTimer(fcp);
			if (windowPos != getSize()) {
				try {
					while (list.get(windowPos).isValidACK()) {
						windowPos++;
					}
				} catch (IndexOutOfBoundsException e) {
				}

			}

			fileCopyClient.testOutWindow();
		} catch (SeqNrNotInWindowException e) {
			e.printStackTrace();
		}
		windowLock.unlock();

	}

	public FCpacket getBySeqNum(long seqNum) throws SeqNrNotInWindowException {

		for (int i = 0; i < windowSize; i++) {
			if (list.get(windowPos + i).getSeqNum() == seqNum) {
				return list.get(windowPos + i);
			}
		}

		throw new SeqNrNotInWindowException(seqNum);
	}

	@Override
	public String toString() {
		if (windowPos == list.size()) {
			return "- empty -";
		}

		String output = list.get(windowPos).getSeqNum() + " | ";
		for (int i = 0; i < windowSize; i++) {
			if (windowPos + i <= list.size() - 1) {
				if (list.get(windowPos + i).isValidACK()) {
					output += "#";
				} else {
					output += "-";
				}
			}

		}

		if (windowPos + windowSize >= list.size()) {
			return output + " | " + list.get(list.size() - 1).getSeqNum();
		} else {
			return output + " | " + list.get(windowPos + windowSize).getSeqNum();
		}
	}
}
