package haw.hamburg.TON;

public class Mail{

	String[] msg;
	int octets;
	boolean deleteFlag;
	
	public Mail(String[] msg2, Integer octets2) {
		this.msg = msg2;
		this.octets = octets2;
		this.deleteFlag = false;
	}

	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
	public String[] getMsg() {
		return msg;
	}

	public void setMsg(String[] msg) {
		this.msg = msg;
	}

	public int getOctets() {
		return octets;
	}

	public void setOctets(int octets) {
		this.octets = octets;
	}
	
}
