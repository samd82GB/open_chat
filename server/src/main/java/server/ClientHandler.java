package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;
    private String login;

    public ClientHandler(Server server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //устанавливаем время контроля бездействия сокета на 120 секунд
                    socket.setSoTimeout(120000);
                    //цикл авторизации
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            throw new RuntimeException("Клиент завершил работу");
                        }
                        //авторизация
                        if (str.startsWith("/auth")) {
                            String[] token = str.split("\\s+", 3); //запись в массив строк после разделения по пробелам
                            if (token.length < 3) {
                                continue;
                            }
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);//возвращаем никнейм по логину и паролю
                            if (newNick != null) {
                                login = token[1];
                                //если пользователь уже авторизован, то не даём ещё одному пользователю зайти под таким же именем
                                if (!server.isLoginAuthenticated(login)) {
                                    nickname = newNick;
                                    sendMsg("/auth_ok " + nickname);
                                    server.subscribe(this);
                                    System.out.println("Клиент авторизован: " + nickname + " Адрес: " + socket.getRemoteSocketAddress());

                                    //сбрасываем контроль времени бездействия при удачной авторизации
                                    socket.setSoTimeout(0);
                                    break;
                                } else {
                                    sendMsg("Уже есть пользователь с этим логином!");
                                }

                            } else {
                                sendMsg("Неверный логин или пароль");
                            }

                        }

                        //регистрация

                        if (str.startsWith("/reg")) {
                            String[] token = str.split("\\s+", 4); //запись в массив строк после разделения по пробелам
                            if (token.length < 4) {
                                continue;
                            }
                            boolean b = server.getAuthService().registration(token[1], token[2], token[3]);
                            if (b) {
                                sendMsg("/reg_ok");
                            } else {
                                sendMsg("/reg_no");
                            }
                        }
                    }

                    //цикл работы
                    while (true) {
                        String str = in.readUTF();
                        //если сообщение начинается на косую черту, то обрабатываем их на окончание работы, или на личное сообщение
                        if (str.startsWith("/")) {
                            if (str.equals("/end")) {
                                out.writeUTF("/end");
                                break;
                            }
                            //если сообщение начинается на /w, то разделяем его на 3 части,
                            //1 - /w
                            //2 - имя пользователя, кому отправляем сообщение
                            //3 - само сообщение
                            if (str.startsWith("/w")) {
                                String[] individMsg = str.split("\\s+", 3); //запись в массив строк после разделения по пробелам
                                server.indMsg(this, individMsg[1], individMsg[2]);
                            }

                            //изменение имени пользователя

                            if (str.startsWith("/cnn")) { //если входящие данные начинаются с /cnn, то это запрос на изменение имени пользователя
                                String[] token = str.split("\\s+", 2); // запись в массив двух слов (старого и нового имени)
                                if (token.length < 2) {
                                    continue;
                                }
                                if (token[1].contains(" ")) {   //если новое имя содержит пробел, то сообщаем, что такое имя недопустимо и идём дальше
                                    sendMsg("Имя не может содержать пробелов");
                                    continue;
                                }

                                if (server.getAuthService().changeNick(this.nickname, token[1])) { //вызываем метод изменения имени, старое имя текущее, новое из массива
                                    sendMsg("/ynnis " + token[1]);  //отправляем клиенту /ynnis и новое имя, если удачно заменили имя пользователя
                                    sendMsg("Ваше имя пользователя изменено на " + token[1]);
                                    this.nickname = token[1];
                                    server.broadcastClientList();

                                } else {
                                    sendMsg("Не удалось изменит имя пользователя. Имя " + token[1] + " уже существует");
                                }

                            }

                        } else {
                            server.broadcastMsg(this, str);
                        }
                    }
                    //обработка исключения по таймауту сокета клиента
                } catch (SocketTimeoutException e) {
                    try {
                        out.writeUTF("/end"); //если поймали исключение по таймауту, то отправляем клиенту команду на отключение
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }

                } catch (RuntimeException e) {
                    System.out.println(e.getMessage());

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент: " + socket.getRemoteSocketAddress() + "   отключился");
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

    public void sendMsg(String msg) {
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }

    public String getLogin() {
        return login;
    }
}
