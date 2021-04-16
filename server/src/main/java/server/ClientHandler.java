package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private Server server;
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String nickname;

    public ClientHandler(Server server, Socket socket) {
        try{
            this.server = server;
            this.socket = socket;
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            new Thread(() -> {
                try {
                    //цикл авторизации
                    while (true) {
                        String str = in.readUTF();

                        if (str.equals("/end")) {
                            out.writeUTF("/end");
                            break;
                        }
                        if (str.startsWith("/auth")){
                            String[] token = str.split("\\s+"); //запись в массив строк после разделения по пробелам
                            String newNick = server
                                    .getAuthService()
                                    .getNicknameByLoginAndPassword(token[1], token[2]);
                        if (newNick !=null) {
                            nickname = newNick;
                            sendMsg("/auth_ok "+nickname);
                            server.subscribe(this);
                            System.out.println("Клиент авторизован: " + nickname + " Адрес: " + socket.getRemoteSocketAddress());
                            break;
                        }else {
                            sendMsg("Неверный логин или пароль");
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

                               break;
                           }

                       } else  {server.broadcastMsg(this, str);}

                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    server.unsubscribe(this);
                    System.out.println("Клиент: "+socket.getRemoteSocketAddress()+ "   отключился");
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

    public void sendMsg(String msg){
        try {
            out.writeUTF(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getNickname() {
        return nickname;
    }
}
