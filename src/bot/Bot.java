package bot;

import server.Server;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class Bot {
    public final static String NAME_BOT = "Радость";

    public final static String COMMAND_PRINT_ALL_NAMES = "names";

    private static Bot bot;

    private Socket socket;
    private Scanner inMessage;
    private PrintWriter outMessage;

    public Bot(Server server){
        try {
            socket = new Socket("localhost", 8080);
            inMessage = new Scanner(socket.getInputStream());
            outMessage = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
                /*если есть входящее сообщение*/
                if (inMessage.hasNext()) {
                    String text = inMessage.nextLine();
                    if (text.contains(COMMAND_PRINT_ALL_NAMES)) {
                        server.makeAllNamesPrinted();
                        inMessage.
                    } else {
                        generateAnswer(inMes.replace(NAME_BOT, ""));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        outMessage.flush();
        outMessage.close();
        inMessage.close();
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
