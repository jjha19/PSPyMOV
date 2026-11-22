package org.Asyncronas;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Practica13 {

    public static void main(String[] args) {

        ExecutorService executor =
                Executors.newFixedThreadPool(3);

        CompletableFuture<Void> tarea1 =
                CompletableFuture.runAsync(
                        () -> mostrarTarea(1),
                        executor
                );

        CompletableFuture<Void> tarea2 =
                CompletableFuture.runAsync(
                        () -> mostrarTarea(2),
                        executor
                );

        CompletableFuture<Void> tarea3 =
                CompletableFuture.runAsync(
                        () -> mostrarTarea(3),
                        executor
                );

        CompletableFuture<Void> tarea4 =
                CompletableFuture.runAsync(
                        () -> mostrarTarea(4),
                        executor
                );

        CompletableFuture<Void> tarea5 =
                CompletableFuture.runAsync(
                        () -> mostrarTarea(5),
                        executor
                );

        CompletableFuture.allOf(
                tarea1,
                tarea2,
                tarea3,
                tarea4,
                tarea5
        ).join();

        executor.shutdown();
    }

    public static void mostrarTarea(int numero) {

        System.out.println(
                "Tarea " + numero +
                        " - hilo: " +
                        Thread.currentThread().getName()
        );

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}