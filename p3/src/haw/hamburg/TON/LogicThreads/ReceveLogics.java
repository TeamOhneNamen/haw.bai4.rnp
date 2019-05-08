package haw.hamburg.TON.LogicThreads;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.FileCopyClient;
import haw.hamburg.TON.Exceptions.SeqNrNotInWindowException;
import haw.hamburg.TON.UTIL.FCpacket;
import haw.hamburg.TON.UTIL.UDP;
import haw.hamburg.TON.UTIL.Window;

public class ReceveLogics extends Thread {

	boolean copieNotFinished = false;
	Window window;
	UDP udp;
	FileCopyClient fileCopyClient;

	ReentrantLock lock;
	
	public ReceveLogics(Window window, UDP udp, FileCopyClient fileCopyClient, ReentrantLock lock) {
		this.window = window;
		this.udp = udp;
		this.fileCopyClient = fileCopyClient;
		this.lock = lock;
	}

	@Override
	public void run() {

		copieNotFinished = true;

		while (copieNotFinished) {
			
			try {
				FCpacket newPack = udp.receve();
				long seq = newPack.getSeqNum();
				lock.lock();
				if (seq >= fileCopyClient.getWindow().get(0).getSeqNum() && seq <= fileCopyClient.getWindow().get(fileCopyClient.getWindow().size()-1).getSeqNum()) {
					fileCopyClient.getWindow().getBySeqNr(seq).setValidACK(true);
					long duration = System.nanoTime() - newPack.getTimestamp();
					fileCopyClient.computeTimeoutValue(duration);
					fileCopyClient.cancelTimer(fileCopyClient.getWindow().getBySeqNr(seq));
				}
				lock.unlock();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (SeqNrNotInWindowException e) {
				System.out.println("SeqNr: " + e.getSeqNr() + " auqerhalb des Windows");
			}

			lock.lock();
			for (int i = 0; i < fileCopyClient.getWindow().size(); i++) {
				if (fileCopyClient.getWindow().get(0).isValidACK()) {
//					System.out.println(fileCopyClient.getWindow().get(0).getSeqNum() + " removed");
					fileCopyClient.getWindow().remove(0);
				}else {
					break;
				}
			}
			lock.unlock();
			
		}

	}

	public void setFinished() {
		copieNotFinished = false;
	}

}
