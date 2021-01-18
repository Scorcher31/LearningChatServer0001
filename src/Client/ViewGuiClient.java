package client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Set;

public class ViewGuiClient {
    private Client client;
    private JFrame frame = new JFrame("Чат");
    private JTextArea messages = new JTextArea(30, 20);
    private JTextArea users = new JTextArea(30, 15);
    private JPanel panel = new JPanel();
    private JTextField textField = new JTextField(40);
    private JButton buttonDisable = new JButton("Отключиться");
    private JButton buttonConnect = new JButton("Подключиться");
    private JButton pushMessage = new EnterButton();

    public ViewGuiClient(Client client) {
        this.client = client;
        initFrameClient();
    }

    //метод, инициализирующий графический интерфейс клиентского приложения
    public void initFrameClient() {
        messages.setEditable(false);
        users.setEditable(false);
        frame.add(new JScrollPane(messages), BorderLayout.CENTER);
        frame.add(new JScrollPane(users), BorderLayout.EAST);
        panel.add(textField);
        panel.add(pushMessage);
        panel.add(buttonConnect);
        panel.add(buttonDisable);
        frame.add(panel, BorderLayout.SOUTH);
        frame.pack();
        frame.setLocationRelativeTo(null); // при запуске отображает окно по центру экрана
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        frame.setForeground(Color.getColor("Blue"));

        //класс обработки события при закрытии окна приложения Сервера
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (client.isConnect()) {
                    client.disableClient();
                }
                System.exit(0);
            }
        });
        frame.setVisible(true);
        pushMessage.addActionListener(e -> client.pushMessage());
        buttonDisable.addActionListener(e -> client.disableClient());
        buttonConnect.addActionListener(e -> client.connectToServer());
        textField.addActionListener(e -> {
            client.sendMessageOnServer(textField.getText());
            textField.setText("");
        });
    }

    protected void addMessage(String text) {
        messages.append(text);
    }

    //метод обновляющий списо имен подлючившихся пользователей
    protected void refreshListUsers(Set<String> listUsers) {
        users.setText("");
        if (client.isConnect()) {
            StringBuilder text = new StringBuilder();
            text.append(String.format("Всего пользователей: %d\n", listUsers.size()));
            text.append("Список пользователей:\n");
            for (String user : listUsers) {
                text
                        .append(user)
                        .append("\n");
            }
            users.append(text.toString());
        }
    }

    //вызывает окно для ввода адреса сервера
    public String getServerAddressFromOptionPane() {
        while (true) {
            String addressServer = JOptionPane.showInputDialog(
                    frame, "Введите адрес сервера:",
                    "Ввод адреса сервера",
                    JOptionPane.QUESTION_MESSAGE
            );
            return addressServer.trim();
        }
    }

    //вызывает окно для ввода порта сервера
    public int getPortServerFromOptionPane() {
        while (true) {
            String port = JOptionPane.showInputDialog(
                    frame, "Введите порт сервера:",
                    "Ввод порта сервера",
                    JOptionPane.QUESTION_MESSAGE
            );
            try {
                return Integer.parseInt(port.trim());
            } catch (Exception e) {
                JOptionPane.showMessageDialog(
                        frame, "Введен неккоректный порт сервера. Попробуйте еще раз.",
                        "Ошибка ввода порта сервера", JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    //вызывает окна для ввода имени пользователя
    public String getNameUser() {
        return JOptionPane.showInputDialog(
                frame, "Введите имя пользователя:",
                "Ввод имени пользователя",
                JOptionPane.QUESTION_MESSAGE
        );
    }

    //вызывает окно ошибки с заданным текстом
    public void errorDialogWindow(String text) {
        JOptionPane.showMessageDialog(
                frame, text,
                "Ошибка", JOptionPane.ERROR_MESSAGE
        );
    }

    public JTextField getTextField() {
        return textField;
    }
}
