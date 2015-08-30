import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
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

    private static byte[] data = "hello I'm the server".getBytes();


    public static void main(String[] args) throws IOException {

        int port = 9000;

        // The buffer into which we'll read data when it's available
        ByteBuffer readBuffer = ByteBuffer.allocate(12);

        System.out.println("I am a new server");
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.socket().bind(new InetSocketAddress(port));

        // Register the server socket channel, indicating an interest in
        // accepting new connections
        Selector selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);

        // Iterate over the set of keys for which events are available
        while (true) {
            selector.select();
            Set readyKeys = selector.selectedKeys();


            Iterator iterator = readyKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = (SelectionKey) iterator.next();
                iterator.remove();

                if (key.isAcceptable()) {
//                    SocketChannel client = server.accept();
//                    System.out.println("Accepted connection from " + client);
//                    client.configureBlocking(false);
//                    ByteBuffer source = ByteBuffer.wrap(data);
//                    SelectionKey key2 = client.register(selector, SelectionKey.OP_WRITE);
//                    key2.attach(source);


                    // For an accept to be pending the channel must be a server socket channel.
                    ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();

                    // Accept the connection and make it non-blocking
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    System.out.println("Accepted connection from " + socketChannel);
                    Socket socket = socketChannel.socket();
                    socketChannel.configureBlocking(false);

                    // Register the new SocketChannel with our Selector, indicating
                    // we'd like to be notified when there's data waiting to be read
                    socketChannel.register(selector, SelectionKey.OP_READ);


                } else if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    ByteBuffer output = (ByteBuffer) key.attachment();
                    if (!output.hasRemaining()) {
                        output.rewind();
                    }
                    client.write(output);
                } else if(key.isReadable()){

                    SocketChannel socketChannel = (SocketChannel) key.channel();

                    // Clear out our read buffer so it's ready for new data
                    readBuffer.clear();

                    // Attempt to read off the channel
                    int numRead;
                    try {
                        numRead = socketChannel.read(readBuffer);
                        System.out.println("IN SelectionKey.OP_READ "+ numRead);

                    } catch (IOException e) {
                        // The remote forcibly closed the connection, cancel
                        // the selection key and close the channel.
                        key.cancel();
                        socketChannel.close();
                        return;
                    }

                    if (numRead == -1) {
                        // Remote entity shut the socket down cleanly. Do the
                        // same from our end and cancel the channel.
                        key.channel().close();
                        key.cancel();
                        return;
                    }
//                    while(readBuffer.hasRemaining()){
//                        System.out.print(readBuffer.get());
//                    }
//                    System.out.println();

                      Charset charset = Charset.forName("UTF-8");
//                    CharsetDecoder cd = charset.newDecoder();
//                    System.out.println(cd.decode(readBuffer).toString());

                    byte[] dataCopy = new byte[numRead];
                    System.arraycopy(readBuffer.array(), 0, dataCopy, 0, numRead);
                    System.out.println(new String(dataCopy));
                    readBuffer.clear();



                    continue;
                }

                key.channel().close();
            }
        }
    }
}