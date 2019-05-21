package haw.hamburg.TON.Exceptions;

import haw.hamburg.TON.UTIL.Window;

public class SeqNrNotInWindowException extends Exception {
	private static final long serialVersionUID = 1L;

	private long seqNr;
	
	public SeqNrNotInWindowException(long seqNr, Window window) {
		super("Seq Nr " + seqNr + " Not In Window " + window.toString());
		this.seqNr = seqNr;
	}
	
	public long getSeqNr() {
		return seqNr;
	}
}
