package org.example;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ThreadLocalRandom;

public class Ej8Proyecto {

    static void main(String[] args) {

        int numeroTrabajadores = 4;

        CyclicBarrier barrera =
                new CyclicBarrier(
                        numeroTrabajadores,
                        () -> System.out.println(
                                "Fase completada, todos avanzan."
                        )
                );

        for (int i = 1; i <= numeroTrabajadores; i++) {

            int trabajador = i;

            Thread hilo = new Thread(() -> {

                try {

                    for (int fase = 1; fase <= 3; fase++) {

                        System.out.println(
                                "Trabajador " + trabajador +
                                        " empieza fase " + fase
                        );

                        int tiempo =
                                ThreadLocalRandom.current()
                                        .nextInt(1000, 3000);

                        Thread.sleep(tiempo);

                        System.out.println(
                                "Trabajador " + trabajador +
                                        " termina fase " + fase
                        );

                        barrera.await();
                    }

                    System.out.println(
                            "Trabajador " + trabajador +
                                    " terminó el proyecto."
                    );

                } catch (Exception e) {
                    Thread.currentThread().interrupt();
                }

            });

            hilo.start();
        }
    }
}