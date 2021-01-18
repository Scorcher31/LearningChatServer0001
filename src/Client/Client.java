package client;

import connection.*;
import java.io.IOException;
import java.net.Socket;

public class Client {
    private Connection connection;
    private static ModelGuiClient model;
    private static ViewGuiClient gui;
    private volatile boolean isConnect = false;

    public boolean isConnect() {
        return isConnect;
    }

    public void setConnect(boolean connect) {
        isConnect = connect;
    }

    //точка входа в клиентское приложение
    public static void main(String[] args) {
        Client client = new Client();
        model = new ModelGuiClient();
        gui = new ViewGuiClient(client);
        while(true) {
            if(client.isConnect()) {
                client.nameUserRegistration();
                client.receiveMessageFromServer();
                client.setConnect(false);
            }
        }
    }

    //метод подключения клиента  серверу
    protected void connectToServer() {
        if(!isConnect) {
            while(true) {
                try {
                    String adressServer = gui.getServerAddressFromOptionPane();
                    int port = gui.getPortServerFromOptionPane();
                    Socket socket = new Socket(adressServer, port);
                    connection = new Connection(socket);
                    isConnect = true;
                    gui.addMessage("Сервисное сообщение: Вы подключились к серверу.\n");
                    break;
                } catch(Exception e) {
                    gui.errorDialogWindow("Произошла ошибка! Возможно Вы ввели не верный адрес сервера или порт. Попробуйте еще раз");
                    break;
                }
            }
        } else gui.errorDialogWindow("Вы уже подключены!");
    }

    //метод, реализующий регистрацию имени пользователя со стороны клиентского приложения
    protected void nameUserRegistration() {
        while(true) {
            try {
                Message message = connection.receive();
                //приняли от сервера сообщение, если это запрос имени, то вызываем окна ввода имени, отправляем на сервер имя
                if(message.getTypeMessage() == MessageType.REQUEST_NAME_USER) {
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }

                if(message.getTypeMessage() == MessageType.NAME_USED) {
                    gui.errorDialogWindow("Данное имя уже используется, введите другое");
                    String nameUser = gui.getNameUser();
                    connection.send(new Message(MessageType.USER_NAME, nameUser));
                }

                //если имя принято, получаем множество всех подключившихся пользователей, выходим из цикла
                if (message.getTypeMessage() == MessageType.NAME_ACCEPTED) {
                    gui.addMessage("Сервисное сообщение: ваше имя принято!\n");
                    model.setUsers(message.getListUsers());
                    model.setName(message.getName());
                    break;
                }
            } catch(Exception e) {
                e.printStackTrace();
                gui.errorDialogWindow("Произошла ошибка при регистрации имени. Попробуйте переподключиться");
                try {
                    connection.close();
                    isConnect = false;
                    break;
                } catch (IOException ex) {
                    gui.errorDialogWindow("Ошибка при закрытии соединения");
                }
            }
        }
    }

    //метод отправки сообщения предназначенного для других пользователей на сервер
    protected void sendMessageOnServer(String text) {
        try {
            text = text.trim();
            if(text.charAt(0) == '-' && text.charAt(1) == '-') {
                connection.send(new Message(MessageType.COMMAND_MESSAGE, text));
            } else {
                connection.send(new Message(MessageType.TEXT_MESSAGE, text));
            }
        } catch (Exception e) {
            gui.errorDialogWindow("Ошибка при отправки сообщения");
        }
    }

    //метод принимающий с сервера собщение от других клиентов
    protected void receiveMessageFromServer() {
        while (isConnect) {
            try {
                Message message = connection.receive();
                //если тип TEXT_MESSAGE, то добавляем текст сообщения в окно переписки
                if (message.getTypeMessage() == MessageType.TEXT_MESSAGE) {
                    gui.addMessage(message.getTextMessage());
                }
                //если сообщение с типо USER_ADDED добавляем сообщение в окно переписки о новом пользователе
                if (message.getTypeMessage() == MessageType.USER_ADDED) {
                    model.addUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("Сервисное сообщение: пользователь %s присоединился к чату.\n", message.getTextMessage()));
                    gui.addMessage(String.format("На данный момент на сервере %d человек\n", model.getUsers().size()));
                }
                //аналогично для отключения других пользователей
                if (message.getTypeMessage() == MessageType.REMOVED_USER) {
                    model.removeUser(message.getTextMessage());
                    gui.refreshListUsers(model.getUsers());
                    gui.addMessage(String.format("Сервисное сообщение: пользователь %s покинул чат.\n", message.getTextMessage()));
                    if(!message.getTextMessage().equals(model.getName())) {
                        gui.addMessage(String.format("На данный момент на сервере %d человек\n", model.getUsers().size()));
                    }
                }
            } catch (Exception e) {
                gui.errorDialogWindow("Ошибка при приеме сообщения от сервера.");
                setConnect(false);
                gui.refreshListUsers(model.getUsers());
                break;
            }
        }
    }

    //метод реализующий отключение нашего клиента от чата
    protected void disableClient() {
        try {
            if (isConnect) {
                connection.send(new Message(MessageType.DISABLE_USER));
                model.getUsers().clear();
                isConnect = false;
                gui.refreshListUsers(model.getUsers());
            } else gui.errorDialogWindow("Вы уже отключены.");
        } catch (Exception e) {
            gui.errorDialogWindow("Сервисное сообщение: произошла ошибка при отключении.");
        }
    }

    protected void pushMessage() {
        try {
            connection.send(new Message(MessageType.TEXT_MESSAGE, gui.getTextField().getText()));
        } catch (Exception e) {
            gui.errorDialogWindow("Ошибка при отправки сообщения");
        }
    }
}
