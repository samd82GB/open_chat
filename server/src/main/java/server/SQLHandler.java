package server;

import java.sql.*;

public class SQLHandler {


    //получение имени по логину и паролю
    public static String getNicknameByLoginAndPassword(String login, String password) {
        Connection connection = DatabaseConnection.getConnection();
        String nickName = "";
        try {
            PreparedStatement psGetNickName = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND password = ?;");
            psGetNickName.setString(1, login);      //записываем в запрос логин
            psGetNickName.setString(2, password);   //записываем в запрос пароль
            ResultSet rs = psGetNickName.executeQuery();         //запрос на получение ника
            if (rs.next()) {
                nickName = rs.getString(1);           //ник возвращается в первой колонке результсета
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            DatabaseConnection.close(connection);
        }
        return nickName;                                //возвращаем полученное имя из базы данных
    }

    //регистрация нового пользователя
    public static boolean registration(String login, String password, String nickname) {
        Connection connection = DatabaseConnection.getConnection();

        try {
            connection.setAutoCommit(false);

            PreparedStatement psRegistration = connection.prepareStatement("INSERT INTO users (nick, login, password) VALUES (?, ?, ?);");
            psRegistration.setString(1, nickname);  //записываем в запрос имя
            psRegistration.setString(2, login);     //записываем в запрос логин
            psRegistration.setString(3, password);  //записываем в запрос пароль

            psRegistration.executeUpdate();                     //выполняем запрос на обновление таблицы
            connection.commit();

            return true;                                        //при удачной регистрации возвращаем "1"

        } catch (SQLException e) {
            DatabaseConnection.rollback(connection);
            e.printStackTrace();
            return false;                                       //в случае получения исключения возвращаем "0"
        } finally {
            DatabaseConnection.close(connection);
        }

    }

    //измненение имени пользователя
    public static boolean changeNick(String oldNickName, String newNickName) {
        Connection connection = DatabaseConnection.getConnection();

        try {
            connection.setAutoCommit(false);

            PreparedStatement psChangeNick = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?;");
            psChangeNick.setString(1, newNickName); //записываем в запрос новое имя для обновления в базе данных
            psChangeNick.setString(2, oldNickName); //записываем в запрос старое имя для поиска по базе данных
            psChangeNick.executeUpdate();                       //выполняем запрос
            connection.commit();

            return true;
        } catch (SQLException e) {
            DatabaseConnection.rollback(connection);
            e.printStackTrace();
            return false;
        } finally {
            DatabaseConnection.close(connection);
        }

    }


}



