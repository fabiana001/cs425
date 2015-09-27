package failure_detector;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Collections;

/**
 * This class handles the logic for failure detection.
 * It is responsible for sending pings ad ping requests to processes for failure detection.
 * It also marks failed nodes and propagates the information to other nodes through ping messages.
 * @author vbry
 *
 */

public class FailureChecker implements Runnable {
	
	private int currentNode;
	private int K;
	private Servent node;
	private Sender client;
	
	FailureChecker (Servent node, Sender client, int K) {
		this.node = node;
		this.client = this.client;
		this.K = K;
	}

	public void run() {
		try {
			
			while (true) {
			
				// send a ping request to the next node in the randomly shuffled membership list
				NodeStatus pingNode = this.node.members.get(this.currentNode);
				
				// get the list of failed members to propogate to the group
				String failedMembers = this.node.getFailures();
				String message ="P|" + this.node.ip + "|" + failedMembers;
				
				this.client.sendMessage(message, pingNode.ip);
								
				// mark the current node as PENDING since we need to receive an ack from it
				pingNode.status = Status.PENDING;
			
				// we wait half the protocol period to receive an ack message from the pingNode
				Thread.sleep((long) (this.node.protocolTime / 2.));
				
				if (pingNode.status == Status.PENDING) {
					// the process has not yet responded, we need to send a ping request to K other processes to ping the process
					for (int i = 1; i <= K; i++) {
						int processId = (this.currentNode + i) % this.node.members.size();
						String msg = "R|" + this.node.ip + "|" + pingNode.ip;
						this.client.sendMessage(message, this.node.members.get(processId).ip);
						
					}
				}
			
				// we wait for another half of the protocol period and then determine whether to remove p_currentNode or not
				Thread.sleep((long) (this.node.protocolTime / 2.));
			
				// check if we should mark the node as failed
				if (pingNode.status == Status.PENDING) {
					pingNode.status = Status.FAILED;
					// mark the node as failed and put it in the list of failed nodes for 2 * N protocol periods 
					this.node.failures.add(new FailedNode(pingNode.ip, this.node.members.size() + 1));
					this.node.members.remove(this.currentNode);
					currentNode = currentNode++ % this.node.members.size();
					this.node.updateFailures();
				}
				
				// check if we need to permute the list
				if (currentNode == 0)
					Collections.shuffle(this.node.members);
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
