package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Controller implements Initializable {
    @FXML
    public TextArea textArea;
    @FXML
    public TextField textField;
    @FXML
    public TextField loginField;
    @FXML
    public PasswordField passwordField;
    @FXML
    public HBox authPanel;
    @FXML
    public HBox msgPanel;
    @FXML
    public ListView<String> clientList;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    private final String IP_ADDRESS = "localhost";
    private final int PORT = 8189;

    private boolean authenticated;
    private String nickname;
    private Stage stage;
    private Stage regStage;
    private RegController regController;

    private File file;


    public void setAuthenticated(boolean authenticated) {
        this.authenticated = authenticated;
        authPanel.setVisible(!authenticated); //видимое и управляемое если нет аутентификации
        authPanel.setManaged(!authenticated); //оставляет место под панель, если нет аутентификации
        msgPanel.setVisible(authenticated);  //видимое и управляемое если есть аутентификация
        msgPanel.setManaged(authenticated); //оставляет место под панель, если есть аутентификация
        clientList.setVisible(authenticated);  //видимое и управляемое если есть аутентификация
        clientList.setManaged(authenticated); //оставляет место под панель, если есть аутентификация

        if (!authenticated) {  //если нет аутентификации, то стираем имя
            nickname = "";
        }
        setTittle(nickname);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        Platform.runLater(() -> {
            stage = (Stage) textArea.getScene().getWindow();

            //обработка нажатия крестика на окне
            stage.setOnCloseRequest(event -> { //нажали крестик
                System.out.println("Прощайте!"); //попрощались
                if (socket != null && !socket.isClosed()) { //если сокет не пустой или не закрытый, то отправляем серверу /end
                    try {
                        out.writeUTF("/end");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        });
        setAuthenticated(false);

    }

    private void connect() {
        try {
            socket = new Socket(IP_ADDRESS, PORT);
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {

                    //цикл авторизации
                    while (true) {
                        String str = in.readUTF();

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                System.out.println("Клиент: " + socket.getLocalSocketAddress() + "   отключился");
                                break;
                            }
                            if (str.startsWith("/auth_ok")) {
                                nickname = str.split("\\s+")[1];
                                setAuthenticated(true);

                                file = new File ("client/src/main/resources/history_"+nickname);
                                if (file.createNewFile()) {
                                    System.out.println("Создан файл истории пользователя "+nickname);
                                }
                                //тут добавляем последние 100 записей в окно пользователя из файла
                                //получаем лист записей из файла

                                History history = new History(file);
                                ArrayList <String> list = history.read100LinesFromFile();
                                int startIndex;
                                if (list.size()<=100) {
                                    startIndex = 0;
                                } else startIndex = list.size() - 100;
                                //отправляем 100 последних записей в текстовое поле пользователя
                                for (int i=startIndex; i< list.size(); i++ ) {
                                    textArea.appendText(list.get(i) + "\n");
                                }
                                break;
                            }
                            if (str.startsWith("/reg_ok")) {
                                regController.showResult("/reg_ok");
                            }
                            if (str.startsWith("/reg_no")) {
                                regController.showResult("/reg_no");
                            }
                        } else {
                            textArea.appendText(str + "\n");

                        }
                    }

                    //цикл работы
                    while (authenticated) {
                        String str = in.readUTF();
                        History history = new History(file);

                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                break;
                            }
                            //добавление имён клиентов в список подключенных клиентов
                            if (str.startsWith("/clientlist")) {
                                String[] token = str.split("\\s+");
                                Platform.runLater(() -> {
                                    clientList.getItems().clear();
                                    for (int i = 1; i < token.length; i++) {
                                        clientList.getItems().add(token[i]);
                                    }
                                });
                            }

                            //входящее сообщение с новым именем после изменения
                            if (str.startsWith("/ynnis")) {
                                nickname = str.split(" ")[1]; //записываем новое имя пользователя
                                setTittle(nickname);                //установка в заголовок нового имени пользователя
                            }


                        } else {
                            textArea.appendText(str + "\n");
                            history.writeToFile(str);
                        }

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    public void sendMsg() {
        try {
            out.writeUTF(textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
//метод попытки авториации
    //если сокет не открыт или закрыт, то выполняем метод соединения
    //отправляем серверу данные по логину и паролю

    public void tryToAuth(ActionEvent actionEvent) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/auth %s %s", loginField.getText().trim(), passwordField.getText().trim());
        try {
            out.writeUTF(msg);
            passwordField.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setTittle(String nickname) {
        Platform.runLater(() -> {
            if (nickname.equals("")) {
                stage.setTitle("Open chat");
            } else {
                stage.setTitle(String.format("Open chat: [%s]", nickname));
            }

        });

    }

    public void clickClient(MouseEvent mouseEvent) {
        String receiver = clientList.getSelectionModel().getSelectedItem();
        textField.setText("/w " + receiver + " ");
    }

    private void createRegWindow() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/reg.fxml"));
            Parent root = fxmlLoader.load();
            regStage = new Stage();
            regStage.setTitle("Open chat регистрация");
            regStage.setScene(new Scene(root, 400, 320));

            regStage.initModality(Modality.APPLICATION_MODAL);
            regStage.initStyle(StageStyle.UTILITY);

            regController = fxmlLoader.getController();
            regController.setController(this);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void tryToReg(ActionEvent actionEvent) {
        if (regStage == null) {
            createRegWindow();
        }
        Platform.runLater(() -> {
            regStage.show();
        });

    }

    public void registration(String login, String password, String nickname) {
        if (socket == null || socket.isClosed()) {
            connect();
        }
        String msg = String.format("/reg %s %s %s", login, password, nickname);
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //метод для автоматической отправки запроса на изменение имени пользователя
    @FXML
    public void sendMsgCNN() {
        try {
            out.writeUTF("/cnn " + textField.getText());
            textField.clear();
            textField.requestFocus();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}