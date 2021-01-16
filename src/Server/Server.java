package server;

import connection.*;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;

public class Server {
    private static Server server;
    private ServerSocket serverSocket;
    private static ViewGuiServer gui; //объект класса представления
    private static ModelGuiServer model = new ModelGuiServer(); //объект класса модели
    private static volatile boolean isServerStart = false; //флаг отражающий состояние сервера запущен/остановлен

    private Server() { }

    public static Server getInstance() {
        if(server == null) {
            synchronized (Server.class) {
                server = new Server();
            }
        }
        return server;
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    //метод, запускающий сервер
    protected void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isServerStart = true;
            gui.refreshDialogWindowServer("Сервер запущен.\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Не удалось запустить сервер.\n");
        }
    }

    public void startServerWithNoGUI(int port) {
        try {
            serverSocket = new ServerSocket(port);
            isServerStart = true;
            System.out.println("Сервер запущен");
        } catch (Exception e) {
            System.out.println("Не удалось запустить сервер");
        }
    }

    //метод останавливающий сервер
    protected void stopServer() {
        try {
            //если серверныйСокет не имеет ссылки или не запущен
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getAllUsersMultiChat().clear();
                gui.refreshDialogWindowServer("Сервер остановлен.\n");
            } else gui.refreshDialogWindowServer("Сервер не запущен - останавливать нечего!\n");
        } catch (Exception e) {
            gui.refreshDialogWindowServer("Остановить сервер не удалось.\n");
        }
    }

    protected void stopServerWithNoGUI() {
        try {
            //если серверныйСокет не имеет ссылки или не запущен
            if (serverSocket != null && !serverSocket.isClosed()) {
                for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
                    user.getValue().close();
                }
                serverSocket.close();
                model.getAllUsersMultiChat().clear();
                System.out.println("Сервер остановлен.\n");
            } else System.out.println("Сервер не запущен - останавливать нечего!\n");
        } catch (Exception e) {
            System.out.println("Остановить сервер не удалось.\n");
        }
    }

    protected void acceptServerWithNoGui() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ServerThread.getInstance(socket, model).start();
            } catch (Exception e) {
                System.out.println("Связь с сервером потеряна.\n");
                break;
            }
        }
    }

     //метод, в котором в бесконечном цикле сервер принимает новое сокетное подключение от клиента
    protected void acceptServer() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                ServerThread.getInstance(socket, model).start();
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Связь с сервером потеряна.\n");
                break;
            }
        }
    }

    //метод, рассылающий заданное сообщение всем клиентам из мапы
    protected void sendMessageAllUsers(Message message) {
        for (Map.Entry<String, Connection> user : model.getAllUsersMultiChat().entrySet()) {
            try {
                user.getValue().send(message);
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Ошибка отправки сообщения всем пользователям!\n");
            }
        }
    }

    //точка входа для приложения сервера
    public static void main(String[] args) {
        Server server = new Server();
        gui = new ViewGuiServer(server);
        model = new ModelGuiServer();
        gui.initFrameServer();
        //цикл снизу ждет true от флага isServerStart (при старте сервера в методе startServer устанавливается в true)
        //после чего запускается бесконечный цикл принятия подключения от клиента в  методе acceptServer
        //до тех пор пока сервер не остановится, либо не возникнет исключение
        while (true) {
            if (isServerStart) {
                server.acceptServer();
                isServerStart = false;
            }
        }
    }
}
