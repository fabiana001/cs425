package failure_detector;

import java.util.ArrayList;
import java.util.List;

/**
 * @brief A node in the cluster.
 * We use the SWIM failure detection protocol to detect failures in the cluster.
 * Failure detection is time bounded by 3 seconds for detection by a single node and takes 6 
 * seconds for propogation to the entire cluster.
 * @author vbry
 *
 */
public class Servent {

	protected String ip;
	protected List<NodeStatus> members;
	protected int protocolTime;
	protected List<FailedNode> failures;
	
	private Sender client;
	private Receiver server;
	private FailureChecker failureChecker;
	
	public Servent(int serverPort, int K) {
		this.protocolTime = 200;
		this.members = new ArrayList<NodeStatus>();
		this.client = new Sender(this.ip, serverPort);
		
		// TODO: change the passing of the servent node to the server and failureChecker or pass in only required info
		this.server = new Receiver(serverPort, this);
		this.failureChecker = new FailureChecker(this, client, K);
	}
	
	protected void updateFailures() {
		for (FailedNode node : this.failures)
			node.TTL--;
	}
	
	protected String getFailures() {
		StringBuilder failedNodes = new StringBuilder();
		
		for (FailedNode node : this.failures)
			failedNodes.append(node.toString() + ",");
		
		return failedNodes.toString().substring(0, failedNodes.length() - 1);
	}
	
}
