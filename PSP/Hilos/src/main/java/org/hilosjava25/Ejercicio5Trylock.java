package org.hilosjava25;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Ejercicio5Trylock {
    static void main(String[] args) {
        ReentrantLock lock = new ReentrantLock();
        final int[] contador = {0};
        Random rd = new Random();
        for (int i = 0; i < 100; i++) {
            Thread hilo = new Thread(
                    () -> {
                        try {
                            boolean cambiado = false;
                            do {


                                if (lock.tryLock(500, TimeUnit.MILLISECONDS)) {
                                    try {
                                        contador[0]++;
                                        cambiado = true;
                                        //Tuve que hacerlo dormir pq sino es demasiado rápido
                                        Thread.sleep(rd.nextInt(10));

                                    } finally {
                                        lock.unlock();
                                    }
                                } else {
                                    System.out.println("Ocupado. Intento mas tarde");
                                    Thread.sleep(rd.nextInt(1000));
                                }
                            } while (!cambiado);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
            hilo.start();
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Valor actual del contador: " + contador[0]);
    }
}
