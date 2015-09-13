package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by fabiana on 8/31/15.
 */
public class Client {


    public static void connect(String ip, int port, String request) throws IOException {

        Selector selector = SelectorProvider.provider().openSelector();
        //Selector selector = Selector.open();
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();


        ByteBuffer buffer = ByteBuffer.allocateDirect(256);
        CharBuffer charBuffer = CharBuffer.allocate(256);
        int wait_time = 5000;

        SocketChannel client = SocketChannel.open();
        client.configureBlocking(false);
        client.connect(new InetSocketAddress(ip, port));
        client.register(selector, SelectionKey.OP_CONNECT, null);

        while (selector.select(wait_time) > 0) {
        //while (true) {
            //System.out.println("listening for keys");
            int numKeys = selector.select();
            //System.out.println("Number of selected keys: " + numKeys);

            Set readyKeys = selector.selectedKeys();
            Iterator readyItor = readyKeys.iterator();

            while (readyItor.hasNext()) {
                SelectionKey key = (SelectionKey) readyItor.next();
                readyItor.remove();
                SocketChannel keyChannel = (SocketChannel) key.channel();

                if (key.isConnectable()) {
                    System.out.println("In connection. Key channel: " + keyChannel + "Key: " + key);
                    if (keyChannel.isConnectionPending()) {
                        keyChannel.finishConnect();
                    }
                    
                    //send the request to the server
                    keyChannel.write(encoder.encode(CharBuffer.wrap(request)));
                    
                    // set the client to read responses from the server
                    SelectionKey key2 = client.register(selector, SelectionKey.OP_READ);
                    key2.attach(ByteBuffer.wrap("hello".getBytes()));

                } else if (key.isReadable()) {
                    //System.out.println("reading response from server...");
                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    // Clear out our read buffer so it's ready for new data
                    buffer.clear();

                    // Attempt to read off the channel
                    int numRead = -1;
                    try {
                        numRead = socketChannel.read(buffer);
                    } catch (IOException e) {
                        // The remote forcibly closed the connection, cancel
                        // the selection key and close the channel.
                        key.cancel();
                        socketChannel.close();

                    }

                    if (numRead == -1) {
                    	
                    	System.out.println("Shutting down the channel...");
                    	// Remote entity shut the socket down cleanly. Do the
                        // same from our end and cancel the channel.
                        key.channel().close();
                        key.cancel();
                    }
                    
                    // print out the response from the server
                	buffer.rewind();
                	//System.out.print("RESPONSE: ");
                	while (buffer.hasRemaining())
                		System.out.print((char)buffer.get());
                	System.out.println();
                	
                	// Clear out our read buffer so it's ready for new data
                    buffer.clear();

                } else if (key.isWritable()) {
                    System.err.println("Unknown key");
                }

                //key.cancel();
            }
        }

        //client.close();
        System.out.println("Connection with server closed");
    }

    public static void main(String[] args) {
    	
    	int []servers = {9999, 8888};
    	
    	for (int server : servers) {
    		try {
    			Client.connect("127.0.0.1", server, "\" 8 \"");
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }

}
