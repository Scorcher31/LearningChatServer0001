/*package Server;

import Connection.*;
import org.junit.Before;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.lang.Thread;

import static org.junit.Assert.*;

public class ServerThreadTest {
    private ServerThread st;
    private static Server serv;
    private static ServerSocket ss;
    private static Socket serverSocket;
    private static Socket clientSocket;
    private Connection connection;
    private static final int port = 27001;

    @BeforeClass
    public static void beforeClass() throws Exception {
        serv = new Server();
        serv.acceptServerWithNoGui();
        serv.startServerWithNoGUI(port);
        ss = serv.getServerSocket();
        serverSocket = ss.accept();
        clientSocket = new Socket("localhost", port);
    }

    @Before
    public void setUp() {
        this.st = new ServerThread(serverSocket);
    }

   @After
    public void closeTest() throws IOException {
        connection.close();
    }

    @Test(timeout = 500)
    public void requestAndAddingUserTest() {
        try {
            connection = new Connection(clientSocket);
            connection.send(new Message(MessageType.USER_NAME, "Adolf"));
            assertEquals("Adolf", st.requestAndAddingUser(connection));
        } catch(Exception e){
            System.out.println("Nahui eto govno!");
            e.getMessage();
            e.printStackTrace();
        }
    }
}*/