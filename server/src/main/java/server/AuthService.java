package server;

public interface AuthService {
    /**
     * Метод получения никнейма по логину и паролю.
     * Если учётки с таким логином и паролем нет, то вернёт null
     * Если учётка есть, то вернёт никнейм.
     * @return никнейм, если есть совпадения по логину и паролю
     * **/

    String getNicknameByLoginAndPassword(String login, String password);


}
