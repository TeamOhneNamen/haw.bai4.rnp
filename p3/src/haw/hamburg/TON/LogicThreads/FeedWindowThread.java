package haw.hamburg.TON.LogicThreads;

import java.util.concurrent.locks.ReentrantLock;

import haw.hamburg.TON.FileCopyClient;
import haw.hamburg.TON.UTIL.Window;

public class FeedWindowThread {

	String fileContent;
	Window window;
	int packSize;
	FileCopyClient fileCopyClient;


	ReentrantLock lock;
	
	public FeedWindowThread(String fileContent, Window window, int packSize, FileCopyClient fileCopyClient, ReentrantLock lock) {
		this.window = window;
		this.fileContent = fileContent;
		this.fileCopyClient = fileCopyClient;
		this.packSize = packSize;
		this.lock = lock;
	}
	
	
	
}
