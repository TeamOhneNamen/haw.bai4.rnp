package haw.hamburg.TON;

/**
 * 
 * @author Ferdinand Trendelenburg AND Thorben Schomacker
 *
 * Mail Calss Used For The POP3-Proxy
 */
public class Mail{

	String[] msg;
	Long octets;
	boolean deleteFlag;
	
	/**
	 * Constructor for Mail
	 * @param msg2 = Message of the Mail
	 * @param octets2 = Size of the Mail
	 */
	public Mail(String[] msg2, Long octets2) {
		this.msg = msg2;
		this.octets = octets2;
		this.deleteFlag = false;
	}

	/**
	 * isDeleteFlag
	 * @return deleteFlag
	 */
	public boolean isDeleteFlag() {
		return deleteFlag;
	}

	/**
	 * setDeleteFlag
	 * @param deleteFlag = new Value
	 */
	public void setDeleteFlag(boolean deleteFlag) {
		this.deleteFlag = deleteFlag;
	}
	
	/**
	 * getMsg
	 * @return msg
	 */
	public String[] getMsg() {
		return msg;
	}

	/**
	 * setMsg
	 * @param msg = new Value
	 */
	public void setMsg(String[] msg) {
		this.msg = msg;
	}

	/**
	 * getOctets
	 * @return octets
	 */
	public Long getOctets() {
		return octets;
	}

	/**
	 * setOctets
	 * @param octets = new Value
	 */
	public void setOctets(Long octets) {
		this.octets = octets;
	}
	
}
