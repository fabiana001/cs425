package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by fabiana on 8/31/15.
 * It is implemented with ServerSocketChannel and Selector to allow asynchronous operations
 */

//TODO sobstitute the println with Log4j

public class Server implements Runnable{
    //socket port
    private int port;
    private String server_ip = "127.0.0.1";
    private String logfile;

    ByteBuffer readBuffer;
    ServerSocketChannel server;
    Selector selector;
    HashMap<SocketChannel,String> dataMap = new HashMap<SocketChannel, String>();

    Charset charset = Charset.forName("UTF-8");
    CharsetEncoder encoder = charset.newEncoder();


    /**
     *
     * @param port socket port
     * @param bufferCapacity buffer's capacity, in bytes
     * @param the logfile to grep from for answering queries
     */
    public Server(int port, int bufferCapacity, String logfile){
        this.port = port;
        readBuffer = ByteBuffer.allocate(bufferCapacity);
        this.logfile = logfile;
    }

    public void run(){

        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress(server_ip, port));
            int ops = server.validOps();

            // Register the server socket channel, indicating an interest in accepting new connections
            selector = Selector.open();
            server.register(selector, SelectionKey.OP_ACCEPT , null);


            System.out.println("Server is listening on: "
                    + server.socket().getInetAddress().getHostAddress() + ":"
                    + server.socket().getLocalPort());
            // Iterate over the set of keys for which events are available
            while (true) {
                int numKeys = selector.select();
                Set readyKeys = selector.selectedKeys();
                Iterator iterator = readyKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        accept(key);
                    }else if(key.isReadable()){
                        read(key);
                    }else if(key.isWritable()){
                        write(key);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void write(SelectionKey key) throws IOException {
    	System.out.println("writing to client...");
    	
        StringBuilder response = new StringBuilder();
        response.append("Server " + server_ip + ":" + port + "\n");

        SocketChannel client = (SocketChannel) key.channel();
        String request = dataMap.get(client);
        
        String []cmds = {"/bin/sh", "-c", "cat /home/vbry/coding/school/cs425/logs/" + this.logfile + " | grep " + request};
        
        try {
        	Process child = Runtime.getRuntime().exec(cmds);
        	// wait for the process to finish executing (blocking operation - might need to run in a new thread)
        	child.waitFor();
        	
        	// read the result
        	BufferedReader command_response = new BufferedReader(new InputStreamReader(child.getInputStream()));
        	String line = "";
        	while ((line = command_response.readLine()) != null) {
        		response.append(line + "\n");
        	}
        }
        
        catch (Exception e) {
        	System.out.println("Error when reading stream from process...");
        	e.printStackTrace();
        }

        finally {
        	// send the result to the client
        	client.write(encoder.encode(CharBuffer.wrap(response.toString())));
        	key.cancel();
        }
    }

    /**
     *
     * @param key
     */
    private void accept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

        // Accept the connection and make it non-blocking
        SocketChannel clientChannel = serverSocketChannel.accept();
        clientChannel.configureBlocking(false);

        // Register the new SocketChannel with our Selector, indicating we'd like to be notified when there's data waiting to be read
        clientChannel.register(selector, SelectionKey.OP_READ);

        System.out.println("Accepted connection from " + clientChannel);
    }

    private void read(SelectionKey key) throws IOException {
        SocketChannel clientChannel = (SocketChannel) key.channel();

        System.out.println("Reading data from " + clientChannel);

        // Clear out our read buffer so it's ready for new data
        this.readBuffer.clear();

        // Attempt to read off the channel
        int numRead;

        try {
            numRead = clientChannel.read(this.readBuffer);
        } catch (IOException e) {
            // The remote forcibly closed the connection, cancel the selection key and close the channel.
            System.err.println("ERROR");
            key.cancel();
            clientChannel.close();
            return;
        }

        if (numRead> 0){
            SelectionKey key2 = clientChannel.register(selector, SelectionKey.OP_WRITE);
            key2.attach(this.readBuffer);
        }
        
        else if(numRead == -1) {

        	// print out the response from the server
        	this.readBuffer.rewind();
        	System.out.println("REQUEST: ");
        	while (this.readBuffer.hasRemaining())
        		System.out.print(this.readBuffer.getChar());
        	System.out.println();

            return;
        }

        // save the data so that each request is associated with the correct client
        saveData(clientChannel, this.readBuffer.array(), numRead);
        readBuffer.clear();
    }

    private void saveData(SocketChannel socketchannel, byte[] data, int capacity) throws IOException {
        Charset charset = Charset.forName("UTF-8");
        byte[] dataCopy = new byte[capacity];
        System.arraycopy(data, 0, dataCopy, 0, capacity);

        //TODO trasform the print operation in a operation to store data
        String string = new String(dataCopy,charset);
        dataMap.put(socketchannel,string);
        System.out.println("Request: " + string);
    }

    public static void main(String[] args) {
        Server s1 = new Server(9999, 48, "short-log-0.txt");
        Thread t1 = new Thread(s1);
        t1.start();
        
        Server s2 = new Server(8888, 48, "short-log-1.txt");
        Thread t2 = new Thread(s2);
        t2.start();
    }


}
