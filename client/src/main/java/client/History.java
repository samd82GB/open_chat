package client;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class History {
    private static File file;
    private static FileOutputStream out;
    private static String [] arr;
    private static List<String> strings;

    public History() {
    }

    //1. создание файла истории каждого авторизованного пользователя с передачей в метод текущего имени пользователя

    public static void createFile (String nickname) {

        file = new File("client/src/main/java/client/history_"+nickname+".txt"); //адрес нового файла
        try {
            file.createNewFile(); //создаём новый файл по адресу, если уже есть, то не создаётся

         } catch (IOException e) {
            e.printStackTrace();
        }

    }

    //2. запись текста в файл, входящие сообщения от сервера и исходящие от клиента
        public  static void writeToFile (String str) {

        byte [] writeData = str.getBytes(StandardCharsets.UTF_8); //преобразуем данные из строки в массив байт
            try {
                out = new FileOutputStream(file, true); //байтовый файловый поток с записью в конец файла
                out.write(writeData);  //запись в файл данных от сервера или от других клиентов
            } catch (IOException e) {
                e.printStackTrace();
            }


        }



    //3. вывод только последних 100 сообщений в текстовую область чата в момент авторизации
    // для этого считываем все строки в лист, а лист преобразовываем в массив, который отправляем в текстовую зону

     public static String [] readLinesToArray (String nickname) {


        // получаем адрес файла в формате path
         Path filePath = Paths.get("client/src/main/java/client/history_"+nickname+".txt");
         try {
             strings = Files.readAllLines(filePath); //считываем все строки из файла в лист строк

             arr = new String[strings.size()]; //массив строк
             strings.toArray(arr);             //переписываем лист в массив


         } catch (IOException e) {
             e.printStackTrace();
         }
            return arr;
     }




    //4. закрытие потока данных

    public static void closeFile () {

        if (out!=null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

}
