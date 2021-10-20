package client;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class History {
    private static File file;


    public History(File file) {
        this.file = file;
    }

   public void writeToFile(String str) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.write(str);
            bw.newLine();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<String> read100LinesFromFile (){
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            ArrayList<String> list = new ArrayList<>();

            //копируем все строки из файла переписки в лист
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                list.add(line);
            }
            return list;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
