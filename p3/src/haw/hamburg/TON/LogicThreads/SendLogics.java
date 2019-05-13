package haw.hamburg.TON.LogicThreads;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.*;
import haw.hamburg.TON.UTIL.*;

public class SendLogics extends Thread {

	boolean copieFinished = false;
	UDP udp;
	long sendetUntil = -1;
	FileCopyClient fileCopyClient;

	ReentrantLock lock;

	public SendLogics(UDP udp, FileCopyClient fileCopyClient, ReentrantLock lock) {
		this.udp = udp;
		this.fileCopyClient = fileCopyClient;
		this.lock = lock;
	}

	@Override
	public void run() {

		while (!copieFinished) {

			fileCopyClient.lock.lock();
			for (int i = 0; i < fileCopyClient.getWindow().size(); i++) {
				if (fileCopyClient.getWindow().get(i).getSeqNum() > sendetUntil) {
					try {
						udp.send(fileCopyClient.getWindow().get(i));
						FileCopyClient.sends++;
						fileCopyClient.testOut("Paket: " + fileCopyClient.getWindow().get(i).getSeqNum() + " gesendet");
						fileCopyClient.startTimer(fileCopyClient.getWindow().get(i));
						fileCopyClient.getWindow().get(i).setTimestamp(System.nanoTime());
						sendetUntil = fileCopyClient.getWindow().get(i).getSeqNum();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

			fileCopyClient.lock.unlock();
		}

	}

	public void copyfinished() {
		copieFinished = true;
	}

}
