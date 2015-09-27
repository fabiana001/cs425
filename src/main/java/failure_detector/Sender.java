package failure_detector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * This is the client class of the Servent.
 * It handles the sending of messages to other processes.
 * @author vbry
 *
 */

// TODO: implement a queue to store messages that need to be sent and put the thread to sleep if the queue is empty
public class Sender implements Runnable {

	private String ip;
	private int serverPort;
	private DatagramSocket clientSocket;
	private DatagramPacket sendPacket;
	private byte[] sendData = new byte[64];
	
	
	public Sender(String ip, int serverPort) {
		this.ip = ip;
		this.serverPort = serverPort;
	}
	
	public void run() {
		try {
			clientSocket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message, String address) {
		try {
			sendData = message.getBytes();
			InetAddress addr = InetAddress.getByName(address);
			sendPacket = new DatagramPacket(sendData, sendData.length, addr, this.serverPort);
			clientSocket.send(sendPacket);
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
