import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;

public class ServerExample {

    private static byte[] data = new byte[255];


    public static void main(String[] args) throws IOException {
        for (int i = 0; i < data.length; i++)
            data[i] = (byte) i;
        int port = 9000;
        System.out.println("I am a new server");
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);

        server.socket().bind(new InetSocketAddress(port));
        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (true) {
            selector.select();
            Set readyKeys = selector.selectedKeys();


            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();
                if (key.isAcceptable()) {
                    SocketChannel client = server.accept();
                    System.out.println("Accepted connection from " + client);
                    client.configureBlocking(false);
                    ByteBuffer source = ByteBuffer.wrap(data);
                    SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
                    key2.attach(source);

                } else if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer output = (ByteBuffer) key.attachment();
                    if (!output.hasRemaining()) {
                        output.rewind();
                    }
                    client.write(output);
                } else if(key.isReadable()){
                    SocketChannel client = (SocketChannel) key.channel();

                    // Read byte coming from the client
                    int BUFFER_SIZE = 32;
                    ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                    try {
                        client.read(buffer);
                    }
                    catch (Exception e) {
                        // client is no longer active
                        e.printStackTrace();
                        continue;
                    }

                    // Show bytes on the console
                    buffer.flip();
                    Charset charset=Charset.forName("ISO-8859-1");
                    CharsetDecoder decoder = charset.newDecoder();
                    CharBuffer charBuffer = decoder.decode(buffer);
                    System.out.print(charBuffer.toString());
                    continue;
                }

                key.channel().close();
            }
        }
    }
}