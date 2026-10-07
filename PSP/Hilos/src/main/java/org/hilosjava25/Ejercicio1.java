package org.hilosjava25;

public class Ejercicio1 extends Thread {
    public void run() {
        try {
            for (int i = 1; i <= 4; i++) {
                Thread.sleep(200);
                System.out.println("Hilo " + Thread.currentThread().getName() + " descargando archivo... - Progreso: " + i * 25 + "%");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 4; i++) {
            Ejercicio1 ej = new Ejercicio1();
            ej.start();
            try {
                ej.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
