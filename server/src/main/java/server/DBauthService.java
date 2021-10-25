package server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBauthService implements AuthService {  //авторизация пользователей из базы данных


    //метод интерфейса для выдачи имени по паролю и логину
    @Override
    public String getNicknameByLoginAndPassword(String login, String password) {
        return SQLHandler.getNicknameByLoginAndPassword(login, password);
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        return SQLHandler.registration(login, password, nickname);
    }

    @Override
    public boolean changeNick(String oldNickName, String newNickName) {
        return SQLHandler.changeNick(oldNickName, newNickName);
    }


}


