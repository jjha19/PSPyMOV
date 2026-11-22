package org.Asyncronas;

import java.util.concurrent.CompletableFuture;

public class Practica7 {

    public static CompletableFuture<String> obtenerUsuario() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return "Oscar";
        });
    }

    public static CompletableFuture<String> obtenerEmail(String usuario) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            return usuario + "@example.com";
        });
    }

    static void main(String[] args) {

        CompletableFuture<String> resultado = obtenerUsuario()
                .thenCompose(usuario -> obtenerEmail(usuario));

        System.out.println("Email: " + resultado.join());
    }
}