/**
 * Created by fabiana on 8/30/15.
 */
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.Iterator;
import java.util.Set;

public class ClientExample {
    public static void main(String[] args) throws Exception {
        int port = 9000;
        Charset charset = Charset.forName("UTF-8");
        CharsetDecoder decoder = charset.newDecoder();
        CharsetEncoder encoder = charset.newEncoder();
        Selector selector = Selector.open();

        ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
        CharBuffer charBuffer = CharBuffer.allocate(1024);

        System.out.println("I am a new client");

        SocketChannel client = SocketChannel.open();
        client.configureBlocking(false);
        client.connect(new InetSocketAddress("127.0.0.1", port));
        //SocketAddress address = new InetSocketAddress("127.0.0.1", port);
        //SocketChannel client = SocketChannel.open(address);

        client.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ);

        while (selector.select(50000) > 0) {
            Set readyKeys = selector.selectedKeys();
            Iterator readyItor = readyKeys.iterator();

            while (readyItor.hasNext()) {

                SelectionKey key = (SelectionKey) readyItor.next();
                readyItor.remove();
                SocketChannel keyChannel = (SocketChannel) key.channel();

                if (key.isConnectable()) {
                    System.out.println("Connection Key");
                    if (keyChannel.isConnectionPending()) {
                        keyChannel.finishConnect();
                    }
                    String request = "hello I m the client!!";
                    keyChannel.write(encoder.encode(CharBuffer.wrap(request)));
                } else if (key.isReadable()) {
                    System.out.println("I m reading ");
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
            }
        }
        client.close();
        System.out.println("\nDone.");


    }
}
