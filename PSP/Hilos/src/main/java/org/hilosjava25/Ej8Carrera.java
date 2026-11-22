package org.hilosjava25;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;

public class Ej8Carrera {

     static void main(String[] args) throws InterruptedException {

        int numeroCorredores = 5;

        CountDownLatch preparados =
                new CountDownLatch(numeroCorredores);

        CountDownLatch salida =
                new CountDownLatch(1);

        for (int i = 1; i <= numeroCorredores; i++) {

            int corredor = i;

            Thread hilo = new Thread(() -> {

                try {

                    System.out.println(
                            "Corredor " + corredor + " está calentando..."
                    );

                    int tiempo =
                            ThreadLocalRandom.current()
                                    .nextInt(1000, 5000);

                    Thread.sleep(tiempo);

                    System.out.println(
                            "Corredor " + corredor + " está listo."
                    );

                    preparados.countDown();

                    preparados.await();

                    System.out.println(
                            "Corredor " + corredor + " espera la salida..."
                    );

                    salida.await();

                    System.out.println(
                            "¡Corredor " + corredor + " SALE!"
                    );

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

            });

            hilo.start();
        }

        // El juez espera a que todos estén preparados
        preparados.await();

        System.out.println("Todos los corredores están listos.");

        Thread.sleep(1000);

        System.out.println("¡JUEZ: PREPARADOS... YA!");

        // Da la salida
        salida.countDown();
    }
}