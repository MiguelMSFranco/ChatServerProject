import com.sun.org.apache.xml.internal.resolver.readers.ExtendedXMLCatalogReader;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        try {

        int portNumber = 8069;
        ServerSocket serverSocket = new ServerSocket(portNumber);
        ExecutorService threadManager = Executors.newCachedThreadPool();
        LinkedList<ServerWorker> connectedUsers = new LinkedList<>();
        while (true) {
            ServerWorker serverWorker= new ServerWorker(serverSocket.accept(), connectedUsers);
            connectedUsers.add(serverWorker);
            threadManager.submit(serverWorker);
        }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
