package core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Server class, listens to incoming grep requests from clients and returns the appropriate lines from the log file on the machine
 * @author vbry
 *
 */
public class Server {

	private String serverId;
	private String logfile;
	private ServerSocket acceptSocket;
	
	protected static final int SERVER_PORT = 4444;
	
	public Server(String serverId, String logfile) {
		this.serverId = serverId;
		this.logfile = logfile;
		
		try {
			this.acceptSocket = new ServerSocket(SERVER_PORT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public boolean accept(Socket client) throws IOException {
		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		PrintWriter out = new PrintWriter(client.getOutputStream());
		
		String query = null;
		out.println("reply from: " + this.serverId);
		// read the query
		while((query = in.readLine()) != null) {
			
			// reply back to the client with the same data sent in
			out.println(query);
		}
		
		out.flush();
		out.close();
		in.close();
		client.close();
		return true;
	}
	
	
}
