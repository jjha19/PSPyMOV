package org.Asyncronas;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PracticaFinal {

    static void main(String[] args) {

        ExecutorService executor =
                Executors.newFixedThreadPool(3);

        CompletableFuture<String> usuario =
                CompletableFuture.supplyAsync(
                        PracticaFinal::consultarUsuario,
                        executor
                );

        CompletableFuture<Integer> pedidos =
                CompletableFuture.supplyAsync(
                        PracticaFinal::consultarPedidos,
                        executor
                );

        CompletableFuture<Integer> estadisticas =
                CompletableFuture.supplyAsync(
                                PracticaFinal::consultarEstadisticas,
                                executor
                        )
                        .orTimeout(3, TimeUnit.SECONDS)
                        .exceptionally(error -> {
                            System.out.println(
                                    "Error en estadísticas: "
                                            + error.getMessage()
                            );

                            return -1;
                        });


        CompletableFuture<Void> todas =
                CompletableFuture.allOf(
                        usuario,
                        pedidos,
                        estadisticas
                );

        todas.join();


        System.out.println();
        System.out.println("===== RESULTADO =====");
        System.out.println("Usuario: " + usuario.join());
        System.out.println("Pedidos: " + pedidos.join());

        if (estadisticas.join() == -1) {
            System.out.println(
                    "Estadísticas: NO DISPONIBLES"
            );
        } else {
            System.out.println(
                    "Estadísticas: "
                            + estadisticas.join()
                            + " visitas"
            );
        }

        System.out.println("=====================");


        executor.shutdown();
    }


    public static String consultarUsuario() {

        System.out.println(
                "Servicio de usuario - hilo: "
                        + Thread.currentThread().getName()
        );

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return "Oscar";
    }


    public static int consultarPedidos() {

        System.out.println(
                "Servicio de pedidos - hilo: "
                        + Thread.currentThread().getName()
        );

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return 8;
    }


    public static int consultarEstadisticas() {

        System.out.println(
                "Servicio de estadísticas - hilo: "
                        + Thread.currentThread().getName()
        );

        try {
            Thread.sleep(2500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return 1250;
    }
}