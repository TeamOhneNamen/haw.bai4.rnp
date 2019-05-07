package haw.hamburg.TON;

import java.util.concurrent.locks.ReentrantLock;

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
