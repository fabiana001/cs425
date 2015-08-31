package client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by fabiana on 8/31/15.
 */
public class Client {


    public static void connect(String ip, int port) throws IOException {
        Selector selector = Selector.open() ;
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();


        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        CharBuffer charBuffer = CharBuffer.allocate(1024);
        int wait_time = 5000;

        SocketChannel client = SocketChannel.open();
        client.configureBlocking(false);
        client.connect(new InetSocketAddress(ip, port));
        client.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ |SelectionKey.OP_WRITE);

        while (selector.select(wait_time) > 0) {
            Set readyKeys = selector.selectedKeys();
            Iterator readyItor = readyKeys.iterator();

            while (readyItor.hasNext()) {
                SelectionKey key = (SelectionKey) readyItor.next();
                readyItor.remove();
                SocketChannel keyChannel = (SocketChannel) key.channel();

                if (key.isConnectable()) {
                    System.out.println("In connection. Key channel: "+ keyChannel+ "Key: "+key);
                    if (keyChannel.isConnectionPending()) {
                        keyChannel.finishConnect();
                    }
                    //send a message to the server
                    String request = "This is the string request from the client";
                    keyChannel.write(encoder.encode(CharBuffer.wrap(request)));
                } else if (key.isReadable()) {
                    System.out.println("In reading. Key channel: "+ keyChannel+ "Key: "+key);
                    keyChannel.read(buffer);
                    buffer.flip();

                    decoder.decode(buffer, charBuffer, false);
                    charBuffer.flip();
                    System.out.print(charBuffer);

                    buffer.clear();
                    charBuffer.clear();

                } else {
                    System.err.println("Unknown key");
                }

                key.cancel();
            }
        }

        client.close();
        System.out.println("Connection with server closed");
    }

    public static void main(String[] args) {
        try {
            Client.connect("127.0.0.1", 9999);
            Client.connect("127.0.0.1", 9999);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
