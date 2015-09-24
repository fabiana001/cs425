package failure_detector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @brief The server class of the servent, it listens for new additions to the membership 
 * list of the cluster and for messages informing about failures
 * @author vbry
 *
 */
public class Receiver implements Runnable {

	private DatagramSocket serverSocket;
	private byte[] receiveData;
	private DatagramPacket receivePacket;
	private int port;
	private Servent node;
	
	public Receiver(int port, Servent node) {
		this.port = port;
		this.node = node;
	}
	
	public void run() {
		
		try {
			serverSocket = new DatagramSocket(this.port);
			receiveData = new byte[64];
			
			while (true) {
				receivePacket = new DatagramPacket(receiveData, receiveData.length);
				serverSocket.receive(receivePacket);
				String message = new String(receivePacket.getData());
				String []contents = message.split("|");
				String msgType = contents[0];
				String nodeIp = contents[1];
				//String port = contents[2];
				Long timestamp = Long.valueOf(contents[2]);
				
				// process a heartbeat event
				if (msgType.equals("H")) {
					System.out.println("received a heartbeat: " + timestamp.longValue() + " from: " + nodeIp);
					node.servants.put(nodeIp, Long.valueOf(timestamp));
				}
				
				// process a failure event
				if (msgType.equals("F")) {
					System.out.println("received a failure: " + timestamp.longValue() + " about: " + nodeIp);
					
				}
				
				// process a new member join event
				if (msgType.equals("N")) {
					System.out.println("received a new member: " + timestamp.longValue() + " at: " + nodeIp);
					node.members.add(nodeIp);
				}
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	
}
