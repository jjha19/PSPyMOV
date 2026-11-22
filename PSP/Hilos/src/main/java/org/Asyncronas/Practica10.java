package org.Asyncronas;

import java.util.concurrent.CompletableFuture;

public class Practica10 {

    static void main(String[] args) {

        CompletableFuture<String> resultado =
                CompletableFuture.supplyAsync(() -> {
                    throw new RuntimeException(
                            "Error al consultar el servidor"
                    );
                });

        resultado
                .exceptionally(error -> "DATOS POR DEFECTO")
                .thenAccept(System.out::println)
                .join();
    }
}