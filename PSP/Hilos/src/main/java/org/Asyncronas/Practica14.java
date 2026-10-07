package org.Asyncronas;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Practica14 {

    static void main(String[] args) {

        List<Integer> numeros =
                List.of(1, 2, 3, 4, 5);

        ExecutorService executor =
                Executors.newFixedThreadPool(5);

        List<CompletableFuture<Integer>> futuros =
                numeros.stream()
                        .map(numero ->
                                CompletableFuture.supplyAsync(
                                        () -> calcularCuadrado(numero),
                                        executor
                                )
                        )
                        .toList();

        CompletableFuture.allOf(
                futuros.toArray(new CompletableFuture[0])
        ).join();

        for (int i = 0; i < numeros.size(); i++) {

            int numero = numeros.get(i);
            int resultado = futuros.get(i).join();

            System.out.println(
                    numero + " → " + resultado
            );
        }

        executor.shutdown();
    }

    public static int calcularCuadrado(int numero) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return numero * numero;
    }
}