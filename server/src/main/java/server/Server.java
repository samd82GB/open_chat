package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class Server {

    private static ServerSocket server;
    private static Socket socket;
    private static final int PORT = 8189;
    private List<ClientHandler> clients;
    private AuthService authService;


    public Server() {
        clients = new CopyOnWriteArrayList<>(); //создаём новый лист клиентов потокобезопасный
        /* authService = new SimpleAuthService();*/


        //проверка на соединение с базой данных
        if (!SQLHandler.connect()) {
            throw new RuntimeException("Не удалось подключиться к базе данных!");
        }
        // работа с пользователями через базу данных
        authService = new DBauthService();


        try {
            server = new ServerSocket(PORT);
            System.out.println("Сервер запустился");

            while (true) {
                socket = server.accept(); //ждём подключения клиента
                System.out.println(socket.getLocalSocketAddress()); //берём локальный адрес и печатаем в консоль
                System.out.println("Клиент подключен" + socket.getRemoteSocketAddress()); //берём адрес клиента и печатаем в консоль
                new ClientHandler(this, socket); //добавляем нового клиента в лист клиента с сокетом клиента и сокетом сервера
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            SQLHandler.disconnect();      //закрываем соединение с базой данных
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //широковещательное сообщение, передача всем клиентам в листе
    public void broadcastMsg(ClientHandler sender, String msg) {
        String message = String.format("%s : %s", sender.getNickname(), msg);

        for (ClientHandler c : clients) {
            c.sendMsg(message);
        }
    }

    //метод для отправки личных сообщений
    //входные переменные: отправитель, имя получателя, сообщение
    public void indMsg(ClientHandler sender, String nickReceiver, String msg) {
        String message = String.format("%s -> %s: %s", sender.getNickname(), nickReceiver, msg);
        for (ClientHandler c : clients) {
            if (c.getNickname().equals(nickReceiver)) { //если имя клиента совпадает с имененм получателя, то отправляем
                c.sendMsg(message);
                if (sender.equals(c)) {//если отправитель и имя получателя одинаковы, то выходим из цикла и не отправляем
                    return;
                }
                sender.sendMsg(message);
                return;
            }
        }
        sender.sendMsg("Пользователь не найден: " + nickReceiver);
    }

    //метод добавления клиента в лист
    public void subscribe(ClientHandler clientHandler) {
        clients.add(clientHandler);
        broadcastClientList();
    }

    //метод удаления клиента из листа
    public void unsubscribe(ClientHandler clientHandler) {
        clients.remove(clientHandler);
        broadcastClientList();
    }

    public AuthService getAuthService() {
        return authService;
    }

    public boolean isLoginAuthenticated(String login) {
        for (ClientHandler c : clients) {
            if (c.getLogin().equals(login)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastClientList() {
        StringBuilder sb = new StringBuilder("/clientlist");
        for (ClientHandler c : clients) {
            sb.append(" ").append(c.getNickname());
        }
        String msg = sb.toString();
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }
}






