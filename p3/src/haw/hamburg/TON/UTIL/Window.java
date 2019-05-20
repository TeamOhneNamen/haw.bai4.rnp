package haw.hamburg.TON.UTIL;

import java.util.ArrayList;

import haw.hamburg.TON.Exceptions.SeqNrNotInWindowException;

public class Window extends ArrayList<FCpacket>{

	private static final long serialVersionUID = 1L;
	
	int windowSize;
	
	public Window(int windowSize) {
		this.windowSize = windowSize;
	}
	
	@Override
	public synchronized boolean add(FCpacket e)  {
		if (this.size()<windowSize) {
			return super.add(e);
		}
		return false;
	}
	
	public synchronized FCpacket getBySeqNr(long seqNr) throws SeqNrNotInWindowException {
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).getSeqNum() == seqNr) {
				return this.get(i);
			}
		}
		throw new SeqNrNotInWindowException(seqNr, this);
		
	}
	
	@Override
	public String toString() {
		if (this.isEmpty()) {
			return "- empty -";
		}
		
		String output = this.get(0).getSeqNum() + " | ";
		for (int i = 0; i < this.size(); i++) {
			if (this.get(i).isValidACK()) {
				output += "#";
			} else {
				output += "-";
			}
		}
		
		return output + " | " + this.get(this.size()-1).getSeqNum();
	}

	public boolean isFull() {
		return this.size()==windowSize;
	}
	
}
