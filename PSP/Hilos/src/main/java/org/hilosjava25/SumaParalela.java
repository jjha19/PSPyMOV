package org.hilosjava25;

import java.util.*;
import java.util.concurrent.*;

public class SumaParalela {

    public static void main(String[] args) throws Exception {

        int[] numeros = new int[10_000_000];

        Arrays.fill(numeros, 1);

        int numeroHilos = 4;
        int tamañoTrozo = numeros.length / numeroHilos;

        ExecutorService executor =
                Executors.newFixedThreadPool(numeroHilos);

        List<Future<Long>> futuros = new ArrayList<>();

        for (int i = 0; i < numeroHilos; i++) {

            int inicio = i * tamañoTrozo;

            int fin;

            if (i == numeroHilos - 1) {
                fin = numeros.length;
            } else {
                fin = inicio + tamañoTrozo;
            }

            Callable<Long> tarea = () -> {

                long suma = 0;

                for (int j = inicio; j < fin; j++) {
                    suma += numeros[j];
                }

                return suma;
            };

            Future<Long> future = executor.submit(tarea);

            futuros.add(future);
        }

        long sumaTotal = 0;

        for (Future<Long> future : futuros) {
            sumaTotal += future.get();
        }

        executor.shutdown();

        System.out.println("Suma total: " + sumaTotal);
    }
}