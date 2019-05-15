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
				if (windowPos + i <= fileCopyClient.anzahlDerPackete-1) {
					System.out.println("sende: " + list.get(windowPos + i).getSeqNum());
					sendTill = windowPos + i;
					fileCopyClient.sends++;
					fileCopyClient.getUDP().send(list.get(windowPos + i));
					fileCopyClient.startTimer(list.get(windowPos + i));

					System.out.println(toString());
				}

			}
		}
		windowLock.unlock();
	}

	public void revece() throws IOException {
		System.out.println("warte auf daten");
		FCpacket fcp = fileCopyClient.getUDP().receve();

		windowLock.lock();
		try {
			fcp = getBySeqNum(fcp.getSeqNum());
			getBySeqNum(fcp.getSeqNum()).setValidACK(true);
			System.out.println(fcp.getSeqNum() + " empfangen!");
			fileCopyClient.cancelTimer(fcp);
			if (windowPos+1!=getSize()) {
				while (list.get(windowPos).isValidACK()) {
					windowPos++;
				}
			}
			
			System.out.println(toString());
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
