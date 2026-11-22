package org.Asyncronas;

import java.util.concurrent.CompletableFuture;

public class Practica8 {

    public static int consultarTemperatura() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return 25;
    }

    public static int consultarHumedad() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return 60;
    }

    public static void main(String[] args) {

        CompletableFuture<Integer> temperatura =
                CompletableFuture.supplyAsync(Practica8::consultarTemperatura);

        CompletableFuture<Integer> humedad =
                CompletableFuture.supplyAsync(Practica8::consultarHumedad);

        temperatura.thenCombine(
                humedad,
                (temp, hum) -> "Temperatura: " + temp + " ºC\nHumedad: " + hum + " %"
        ).thenAccept(System.out::println);

        // Esperamos para que el programa no termine antes de tiempo
        temperatura.join();
        humedad.join();
    }
}