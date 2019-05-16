package haw.hamburg.TON;

public class Mail{

	String[] msg;
	Long octets;
	boolean deleteFlag;
	
	public Mail(String[] msg2, Long octets2) {
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

	public Long getOctets() {
		return octets;
	}

	public void setOctets(Long octets) {
		this.octets = octets;
	}
	
}
