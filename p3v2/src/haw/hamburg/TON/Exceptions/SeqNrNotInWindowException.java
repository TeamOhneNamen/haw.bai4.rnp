package haw.hamburg.TON.Exceptions;

public class SeqNrNotInWindowException extends Exception {
	private static final long serialVersionUID = 1L;

	private long seqNr;
	
	public SeqNrNotInWindowException(long seqNr) {
		super("Seq Nr Not In Window");
		this.seqNr = seqNr;
	}
	
	public long getSeqNr() {
		return seqNr;
	}
}
