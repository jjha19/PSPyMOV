package org.example;

public class Ejercicio1Runnable implements Runnable {

    @Override
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
            Thread hilo = new Thread(new Ejercicio1Runnable());
            hilo.start();
            try {
                hilo.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
