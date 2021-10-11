package server;

import java.sql.*;

public class SQLHandler {
    //работа с базой данных
    private static Connection connection;                                           //соединение с базой данных

    private static PreparedStatement psGetNickName;
    private static PreparedStatement psRegistration;
    private static PreparedStatement psChangeNick;                                    //подготовка данных для обмена с базой

    public static boolean connect() {                                             //метод установки соединения с базой данных
        try {
            Class.forName("org.sqlite.JDBC");                                      //указываем адрес драйвера базы данных
            connection = DriverManager.getConnection("jdbc:sqlite:maindb.db"); //указываем к какой базе данных подключиться
            prepareAllStatements();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //подготовка всех видов запросов в базу данных
    //запроса на получение имени по логину и паролю,
    //запроса на регистрацию нового пользователя,
    //запроса на изменение ника
    private static void prepareAllStatements() throws SQLException {
        psGetNickName = connection.prepareStatement("SELECT nick FROM users WHERE login = ? AND password = ?;");
        psRegistration = connection.prepareStatement("INSERT INTO users (nick, login, password) VALUES (?, ?, ?);");
        psChangeNick = connection.prepareStatement("UPDATE users SET nick = ? WHERE nick = ?;");

    }

    //получение имени по логину и паролю
    public static String getNicknameByLoginAndPassword(String login, String password) {

        String nickName = "";
        try {
            psGetNickName.setString(1, login);      //записываем в запрос логин
            psGetNickName.setString(2, password);   //записываем в запрос пароль
            ResultSet rs = psGetNickName.executeQuery();         //запрос на получение ника
            if (rs.next()) {
                nickName = rs.getString(1);           //ник возвращается в первой колонке результсета
            }
            rs.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return nickName;                                        //возвращаем полученное имя из базы данных
    }

    //регистрация нового пользователя
    public static boolean registration(String login, String password, String nickname) {
        try {
            psRegistration.setString(1, nickname);  //записываем в запрос имя
            psRegistration.setString(2, login);     //записываем в запрос логин
            psRegistration.setString(3, password);  //записываем в запрос пароль
            psRegistration.executeUpdate();                     //выполняем запрос на обновление таблицы
            return true;                                        //при удачной регистрации возвращаем "1"
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;                                       //в случае получения исключения возвращаем "0"
        }

    }

    //измненение имени пользователя
    public static boolean changeNick(String oldNickName, String newNickName) {
        try {
            psChangeNick.setString(1, newNickName); //записываем в запрос новое имя для обновления в базе данных
            psChangeNick.setString(2, oldNickName); //записываем в запрос старое имя для поиска по базе данных
            psChangeNick.executeUpdate();                       //выполняем запрос
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return false;
        }

    }


    public static void disconnect() {
        try {
            psGetNickName.close();
            psChangeNick.close();
            psRegistration.close();
            connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }


    }


}
