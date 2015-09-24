package failure_detector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This is the client class of the Servent.
 * It is responsible for sending periodic heartbeats to the masters of this node 
 * and broadcasting any failures it detects.
 * @author vbry
 *
 */
public class Sender implements Runnable {

	private int port;
	private DatagramSocket clientSocket;
	private DatagramPacket sendPacket;
	private byte[] sendData = new byte[64];
	private Servent node;
	
	public Sender(int port, Servent node) {
		this.port = port;
		this.node = node;
	}
	
	public void run() {
		try {
			clientSocket = new DatagramSocket();
			
			// send heartbeats to this node's masters
			for (String master : node.masters) {
				String []info = master.split(":");
				String timestamp = Long.toString(System.currentTimeMillis());
				sendData = ("H|" + node.ip + "|" + timestamp).getBytes();
				InetAddress address = InetAddress.getByName(info[0]);
				sendPacket = new DatagramPacket(sendData, sendData.length, address, Integer.valueOf(info[1]));
				clientSocket.send(sendPacket);
			}
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
}
