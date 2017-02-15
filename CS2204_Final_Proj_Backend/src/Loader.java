/**
 * Created by theLa on Feb-08-2017.
 */
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.PriorityQueue;
import java.net.Socket;

public class Loader {
    public static void main(String[] args) {
        System.out.println("Starting CS2205 Backend on port 81");

        try {
            ServerSocket server = new ServerSocket();

            server.setPerformancePreferences(1, 2, 0);
            server.bind(new InetSocketAddress("0.0.0.0", 81));

            System.out.println("Server started on port 81");

            while (true) {
                try {
                    new SockThread(server.accept()).start();
                } catch (IOException ex) {
                    System.out.println("Unable to establish connection to client");
                }
            }
        } catch (IOException ex) {
            System.out.println("Unable to start server: "+ex.toString());
            System.exit(-1);
        }
    }
}
