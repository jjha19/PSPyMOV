package org.Asyncronas;
import java.util.concurrent.CompletableFuture;

public class Practica9 {

    public static void descargar(int numero, int segundos) {
        try {
            Thread.sleep(segundos * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Archivo " + numero + " descargado");
    }

    static void main(String[] args) {

        CompletableFuture<Void> archivo1 =
                CompletableFuture.runAsync(() -> descargar(1, 2));

        CompletableFuture<Void> archivo2 =
                CompletableFuture.runAsync(() -> descargar(2, 1));

        CompletableFuture<Void> archivo3 =
                CompletableFuture.runAsync(() -> descargar(3, 3));

        CompletableFuture<Void> archivo4 =
                CompletableFuture.runAsync(() -> descargar(4, 2));

        CompletableFuture<Void> archivo5 =
                CompletableFuture.runAsync(() -> descargar(5, 1));

        CompletableFuture<Void> todasLasDescargas =
                CompletableFuture.allOf(
                        archivo1,
                        archivo2,
                        archivo3,
                        archivo4,
                        archivo5
                );

        todasLasDescargas.thenRun(() ->
                System.out.println("Todas las descargas han terminado")
        ).join();
    }
}