package failure_detector;

import java.util.HashMap;
import java.util.List;

/**
 * @brief A node in the cluster, we use heartbeats to two random nodes for failure detection.
 * Failure detection is time bounded by 3 seconds for detection by a single node and takes 6 
 * seconds for propogation to the entire cluster by a broadcast.
 * @author vbry
 *
 */
public class Servent {

	protected String ip;
	protected List<String> members;
	protected HashMap<String, Long> servants;
	protected String[] masters;
	private int beat_time;
	
	private Sender client;
	private Receiver server;
	
	public Servent(int serverPort, int clientPort) {
		this.beat_time = 1500;
		client = new Sender(clientPort, this);
		server = new Receiver(serverPort, this);
	}
	
	
	
}
