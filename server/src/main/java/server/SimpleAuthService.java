package server;

import java.util.ArrayList;
import java.util.List;

public class SimpleAuthService implements AuthService{
    private class UserData {    //вложенный класс для создания типа данных пользователя
        String login;
        String password;
        String nickname;

        public UserData (String login, String password, String nickname) {
            this.login=login;
            this.password=password;
            this.nickname=nickname;
        }

    }

    private List<UserData> users;
//этим методом создаём пользователей с именами и паролями
    public SimpleAuthService(){
        users=new ArrayList<>(); //лист пользователей, в который добавляем данные пользователей типа UserData
        users.add(new UserData("qwe", "qwe", "qwe"));
        users.add(new UserData("asd", "asd", "asd"));
        users.add(new UserData("zxc", "zxc", "zxc"));

        for(int i=0; i<10; i++) {
            users.add(new UserData("login"+i, "password"+i, "zxc"+i));
        }



    }
//метод интерфейса для выдачи имени по паролю и логину
    @Override
    public String getNicknameByLoginAndPassword (String login, String password) {
        for (UserData u : users) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nickname;
            }
        }
        return null;
    }

    @Override
    public boolean registration(String login, String password, String nickname) {
        for (UserData u : users) {
            if (u.login.equals(login) && u.nickname.equals(nickname)) {
                return false;
            }
        }

        users.add(new UserData(login, password, nickname));
        return true;
    }

    @Override
    public boolean changeNick(String oldNickName, String NewNickName) {
        return false;
    }
}
