package server;

import java.io.IOException;
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
public class Server {
    //socket port
    private int port;

    ByteBuffer readBuffer;
    ServerSocketChannel server;
    Selector selector;
    HashMap<SocketChannel,String> dataMap = new HashMap<SocketChannel, String>();


    /**
     *
     * @param port socket port
     * @param bufferCapacity buffer's capacity, in bytes
     */
    public Server(int port, int bufferCapacity){
        this.port = port;
        readBuffer = ByteBuffer.allocate(bufferCapacity);
    }

    public void run(){

        try {
            server = ServerSocketChannel.open();
            server.configureBlocking(false);
            server.socket().bind(new InetSocketAddress("127.0.0.1",port));
            int ops = server.validOps();

            // Register the server socket channel, indicating an interest in accepting new connections
            selector = Selector.open();
            server.register(selector, ops, null);

            System.out.println("Server is listening on: "
                    + server.socket().getInetAddress().getHostAddress() + ":"
                    + server.socket().getLocalPort());
            // Iterate over the set of keys for which events are available
            while (true) {
                int numKeys = selector.select();
                System.out.println("Number of selected keys: " + numKeys);


                Set readyKeys = selector.selectedKeys();
                Iterator iterator = readyKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = (SelectionKey) iterator.next();
                    iterator.remove();

                    if (key.isAcceptable()) {
                        accept(key);
                    } else if (key.isWritable()) {
                        //TODO implement this part

                    } else if(key.isReadable()){
                        read(key);
                    } else if(key.isWritable()){
                        write(key);
                    }

                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }




    }

    private void write(SelectionKey key) throws IOException {
        String request = "Hi, I'm the server";
        CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
        SocketChannel clientChannel = (SocketChannel) key.channel();
        clientChannel.write(encoder.encode(CharBuffer.wrap(request)));
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

        //TODO sobstitute the println with Log4j
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
            key.cancel();
            clientChannel.close();
            return;
        }

        if (numRead == -1) {
            // Remote entity shut the socket down cleanly. Do the same from our end and cancel the channel.
            key.channel().close();
            key.cancel();

//            String request = "Hi, I'm the server";
//            CharsetEncoder encoder = Charset.forName("UTF-8").newEncoder();
//            clientChannel.write(encoder.encode(CharBuffer.wrap(request)));

            return;
        }

        // Hand the data off to our worker thread
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
        System.out.println(string);
    }

    public static void main(String[] args) {
        Server server = new Server(9999, 1000);
        server.run();
    }
}
