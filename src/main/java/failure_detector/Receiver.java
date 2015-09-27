package failure_detector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * @brief The server class of the servent.
 * It listens for -
 * 		'Join' messages indicating new nodes added to the cluster
 * 		'Ack' messages indicating whether nodes are alive or potentially dead
 * 		'Ping' messages from other processes to respond with an Ack
 * 		'Ping request' messages to request Acks from a specified process and relay them back
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
				//Long timestamp = Long.valueOf(contents[2]);
				
				// process a ping event
				if (msgType.equals("P")) {
					System.out.println("received a ping from: " + nodeIp);
				}
				
				// process an ack event
				if (msgType.equals("A")) {
					System.out.println("received an ack from: " + nodeIp);
					
				}
				
				// process a ping request event
				if (msgType.equals("R")) {
					String toPingIp = contents[2];
					System.out.println("received a ping request from: " + nodeIp + " for: " + toPingIp);
				}
				
				// process a new member join event
				if (msgType.equals("J")) {
					System.out.println("a new member has joined at: " + nodeIp);
					node.members.add(new NodeStatus(nodeIp, Status.ALIVE));
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
