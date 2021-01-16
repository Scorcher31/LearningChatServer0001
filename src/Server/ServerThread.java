package server;

import connection.*;

import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

//класс-поток, который запускается при принятии сервером нового сокетного соединения с клиентом, в конструктор
//передается объект класса Socket
public class ServerThread extends Thread{
    private volatile static ServerThread st;
    private Socket socket;
    private static ModelGuiServer model;
    private static Server server = Server.getInstance();
    private static ViewGuiServer gui = new ViewGuiServer(server);

    private ServerThread(Socket socket, ModelGuiServer model) {
        this.socket = socket;
        this.model = model;
    }

    public static ServerThread getInstance(Socket socket, ModelGuiServer model) {
        if(st == null) {
            synchronized (ServerThread.class) {
                st = new ServerThread(socket, model);
            }
        }
        return st;
    }

    public static void setSt(ServerThread st) {
        ServerThread.st = st;
    }

    //метод который реализует запрос сервера у клиента имени и добавлении имени в мапу
    public String requestAndAddingUser(Connection connection) {
        while (true) {
            try {
                //посылаем клиенту сообщение-запрос имени
                connection.send(new Message(MessageType.REQUEST_NAME_USER));
                Message responseMessage = connection.receive();
                String userName = responseMessage.getTextMessage();

                //получили ответ с именем и проверяем не занято ли это имя другим клиентом
                if (responseMessage.getTypeMessage() == MessageType.USER_NAME && userName != null && !userName.isEmpty() && !model.getAllUsersMultiChat().containsKey(userName)) {
                    //добавляем имя в мапу
                    model.addUser(userName, connection);
                    Set<String> listUsers = new HashSet<>();
                    for (Map.Entry<String, Connection> users : model.getAllUsersMultiChat().entrySet()) {
                        listUsers.add(users.getKey());
                    }
                    //отправляем клиенту множетство имен всех уже подключившихся пользователей
                    connection.send(new Message(MessageType.NAME_ACCEPTED, listUsers));
                    //отправляем всем клиентам сообщение о новом пользователе
                    server.sendMessageAllUsers(new Message(MessageType.USER_ADDED, userName));
                    return userName;
                }
                //если такое имя уже занято отправляем сообщение клиенту, что имя используется
                else connection.send(new Message(MessageType.NAME_USED));
            } catch (Exception e) {
                gui.refreshDialogWindowServer("Возникла ошибка при запросе и добавлении нового пользователя\n");
            }
        }
    }

    //метод, реализующий обмен сообщениями между пользователями
    public void messagingBetweenUsers(Connection connection, String userName) {
        while (true) {
            try {
                Message message = connection.receive();
                //приняли сообщение от клиента, если тип сообщения TEXT_MESSAGE то пересылаем его всем пользователям
                if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                    String textMessage = String.format("%s: %s\n", userName, message.getTextMessage());
                    server.sendMessageAllUsers(new Message(MessageType.TEXT_MESSAGE, textMessage));
                }
                //если тип сообщения DISABLE_USER, то рассылаем всем пользователям, что данный пользователь покинул чат,
                //удаляем его из мапы, закрываем его connection
                if (message.getTypeMessage() == MessageType.DISABLE_USER) {
                    server.sendMessageAllUsers(new Message(MessageType.REMOVED_USER, userName));
                    model.removeUser(userName);
                    connection.close();
                    gui.refreshDialogWindowServer(String.format("Пользователь с удаленным доступом %s отключился.\n", socket.getRemoteSocketAddress()));
                    break;
                }
            } catch (Exception e) {
                gui.refreshDialogWindowServer(String.format("Произошла ошибка при рассылке сообщения от пользователя %s, либо отключился!\n", userName));
                break;
            }
        }
    }

    @Override
    public void run() {
        gui.refreshDialogWindowServer(String.format("Подключился новый пользователь с удаленным сокетом - %s.\n", socket.getRemoteSocketAddress()));
        try {
            //получаем connection при помощи принятого сокета от клиента и запрашиваем имя, регистрируем, запускаем
            //цикл обмена сообщениями между пользователями
            Connection connection = new Connection(socket);
            String nameUser = requestAndAddingUser(connection);
            messagingBetweenUsers(connection, nameUser);
        } catch (Exception e) {
            gui.refreshDialogWindowServer(String.format("Произошла ошибка при рассылке сообщения от пользователя!\n"));
        }
    }
}