package Lesson_4;

public class Threads {
    public static void main (String[] args)  {

        WaitNotify waitNotify = new WaitNotify();
        Thread tA = new Thread(()-> {
            waitNotify.printA();
        });

        Thread tB = new Thread(()-> {
            waitNotify.printB();
        });

        Thread tC = new Thread(()-> {
            waitNotify.printC();
        });

        tA.start();

        tB.start();

        tC.start();



    }
}
