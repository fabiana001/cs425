/**
 * Created by fabiana on 8/30/15.
 */
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.SocketChannel;

public class ClientExample {
    public static void main(String[] args) throws Exception {
        int port = 9000;

        System.out.println("I am a new client");

        SocketAddress address = new InetSocketAddress("127.0.0.1", port);
        SocketChannel client = SocketChannel.open(address);
        ByteBuffer buffer = ByteBuffer.allocate(4);
        IntBuffer view = buffer.asIntBuffer();

        for (int expected = 0;; expected++) {
            client.read(buffer);
            int actual = view.get();
            buffer.clear();
            view.rewind();

            if (actual != expected) {
                System.err.println("Expected " + expected + "; was " + actual);
                break;
            }
            System.out.println(actual);
        }
    }
}
