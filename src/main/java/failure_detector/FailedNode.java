package failure_detector;

public class FailedNode {

	protected String ip;
	protected int TTL;
	
	FailedNode(String ip, int TTL) {
		this.ip = ip;
		this.TTL = TTL;
	}
	
	@Override
	public String toString() {
		return this.ip + ":" + this.TTL;
	}
}
