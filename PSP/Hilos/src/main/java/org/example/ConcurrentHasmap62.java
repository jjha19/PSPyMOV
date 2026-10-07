package org.example;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConcurrentHasmap62 {
    private ConcurrentHashMap<String, Integer> map;

    public ConcurrentHasmap62() {
        this.map = new ConcurrentHashMap<>();
    }

    public void put(String key, Integer value) {
        map.put(key, value);
    }

    public Integer get(String key) {
        return map.get(key);
    }

    static void main(String[] args) {
        String texto = "hola java hola buenas tardes java tardes hola hola";
        String[] palabras = texto.split(" ");
        ConcurrentHasmap62 map = new ConcurrentHasmap62();
        int numeroHilos = 2;

        ConcurrentHashMap<String, Integer> contador =
                new ConcurrentHashMap<>();

        ExecutorService executor =
                Executors.newFixedThreadPool(numeroHilos);

        int tamaño = palabras.length / numeroHilos;

        for (int i = 0; i < numeroHilos; i++) {

            int inicio = i * tamaño;

            int fin;

            if (i == numeroHilos - 1) {
                fin = palabras.length;
            } else {
                fin = inicio + tamaño;
            }

            executor.submit(() -> {

                for (int j = inicio; j < fin; j++) {

                    String palabra = palabras[j];

                    contador.merge(
                            palabra,
                            1,
                            Integer::sum
                    );
                }
            });
        }

        executor.shutdown();

        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(contador);
    }
}