package org.Asyncronas;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

public class Practica12 {

    static void main(String[] args) {

        System.out.println("=== orTimeout() ===");

        CompletableFuture<String> tarea1 =
                CompletableFuture.supplyAsync(() -> {

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return "Tarea terminada";
                });

        try {
            String resultado = tarea1
                    .orTimeout(3, java.util.concurrent.TimeUnit.SECONDS)
                    .join();

            System.out.println(resultado);

        } catch (Exception e) {
            System.out.println("La tarea ha superado el tiempo máximo");
        }


        System.out.println();
        System.out.println("=== completeOnTimeout() ===");

        CompletableFuture<String> tarea2 =
                CompletableFuture.supplyAsync(() -> {

                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }

                    return "Tarea terminada";
                });

        String resultado2 = tarea2
                .completeOnTimeout(
                        "Resultado no disponible",
                        3,
                        java.util.concurrent.TimeUnit.SECONDS
                )
                .join();

        System.out.println(resultado2);
    }
}