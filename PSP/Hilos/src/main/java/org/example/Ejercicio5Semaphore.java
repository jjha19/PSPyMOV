package org.example;

import java.util.Random;
import java.util.concurrent.Semaphore;

public class Ejercicio5Semaphore {
    static void main() {
        Semaphore semaphore = new Semaphore(3);

        Random rd = new Random();
        for (int i = 1; i <= 10; i++) {
            Thread hilo = new Thread(
                    () -> {
                        try {
                            boolean haAparcado = false;
                            do {
                                if (semaphore.tryAcquire()) {
                                    haAparcado = true;
                                    System.out.println("Hilo " + Thread.currentThread().getName() + " ha aparcado.");
                                    Thread.sleep(rd.nextInt(1000,2000));
                                    semaphore.release();
                                    System.out.println("Hilo " + Thread.currentThread().getName() + " se ha ido.");
                                }else {
                                    System.out.println("Hilo " + Thread.currentThread().getName() + " no ha podido aparcar y está esperando");
                                    Thread.sleep(rd.nextInt(1000,2000));
                                }
                            }while (!haAparcado);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
            );
            hilo.start();
        }
    }
}
