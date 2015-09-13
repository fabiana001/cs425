package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * Client class, accepts user input from the command line and queries all servers for log data
 * @author vbry
 *
 */

public class Client {

	private List<String> servers;
	private static StringBuilder result;
	
	private static Lock lock;
	
	public Client(List<String> servers) {
		this.servers = servers;
	}
	
	public String sendQuery(String query) {
		result = new StringBuilder();
		
		for (String server : servers) {
			Thread t;
			ResultCollector collector = new ResultCollector(query, server, Server.SERVER_PORT);
			t = new Thread(collector);
		}
		return result.toString();
	}
	
	public static void updateResult(String data) {
		try {
			lock.lock();
			result.append(data);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			lock.unlock();
		}
	}
	
	public class ResultCollector implements Runnable {
		
		private String server;
		private String query;
		private int port;
		
		public ResultCollector(String query, String server, int port) {
			this.server = server;
			this.query = query;
			this.port = port;
		}
		
		public void run() {
			try {
				Socket serverConn = new Socket(this.server, this.port);
				BufferedReader in = new BufferedReader(new InputStreamReader(serverConn.getInputStream()));
				PrintWriter out = new PrintWriter(serverConn.getOutputStream());
				
				out.println(this.query);
				out.flush();
				out.close();
				
				String response = null;
				
				
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
