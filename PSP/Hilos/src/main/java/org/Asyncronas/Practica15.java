package org.Asyncronas;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class Practica15 {

    static void main(String[] args) {

        System.out.println("=== int normal ===");

        int[] contador = {0};

        CompletableFuture<?>[] tareas =
                new CompletableFuture[10];

        for (int i = 0; i < 10; i++) {

            tareas[i] = CompletableFuture.runAsync(() -> {

                for (int j = 0; j < 10000; j++) {
                    contador[0]++;
                }

            });
        }

        CompletableFuture.allOf(tareas).join();

        System.out.println(
                "Resultado con int: " + contador[0]
        );

        System.out.println();
        System.out.println("=== AtomicInteger ===");

        AtomicInteger contadorAtomic =
                new AtomicInteger(0);

        CompletableFuture<?>[] tareasAtomic =
                new CompletableFuture[10];

        for (int i = 0; i < 10; i++) {

            tareasAtomic[i] = CompletableFuture.runAsync(() -> {

                for (int j = 0; j < 10000; j++) {
                    contadorAtomic.incrementAndGet();
                }

            });
        }

        CompletableFuture.allOf(tareasAtomic).join();

        System.out.println(
                "Resultado con AtomicInteger: "
                        + contadorAtomic.get()
        );
    }
}