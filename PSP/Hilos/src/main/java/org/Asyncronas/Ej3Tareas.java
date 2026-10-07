package org.Asyncronas;

import java.util.concurrent.*;

public class Ej3Tareas {
    static void main() {
        Callable tarea1 = () -> {
            System.out.println("Ejecutando tarea 1...");
            Thread.sleep(2000);
            return 10;
        };
        Callable tarea2 = () -> {
            System.out.println("Ejecutando tarea 2...");
            Thread.sleep(1000);
            return 20;
        };
        Callable tarea3 = () -> {
            System.out.println("Ejecutando tarea 3...");
            Thread.sleep(3000);
            return 30;
        };
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            Future<Integer> resultado3 = executor.submit(tarea3);
            Future<Integer> resultado = executor.submit(tarea1);
            Future<Integer> resultado2 = executor.submit(tarea2);


            System.out.println("Resultado final: " + (resultado.get() + resultado2.get() + resultado3.get()));

        } catch (RuntimeException e) {
            System.err.println("Error en la ejecución de la tarea: " + e.getMessage());
        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
        executor.shutdown();


        CompletableFuture<Integer> completable = CompletableFuture.supplyAsync(() -> 2 + 20);
        CompletableFuture<Integer> resultado = completable.thenApply(numero -> numero * 2);
        resultado.thenAccept(System.out::println);

        CompletableFuture<Void> completable2 = CompletableFuture
                .supplyAsync(() -> 10)
                .thenApply(n -> n * 2)
                .thenApply(n -> n + 5)
                .thenApply(String::valueOf)
                .thenAccept(result ->
                        System.out.println("Resultado: " + result)
                );

        completable2.join();


    }
}
