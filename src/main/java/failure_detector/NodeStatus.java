package failure_detector;

public class NodeStatus {

	protected String ip;
	protected Status status;
	
	NodeStatus(String ip, Status status) {
		this.ip = ip;
		this.status = status;
	}
}
