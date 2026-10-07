package org.example;

import java.util.*;
import java.util.concurrent.*;

public class ConsultasAPI {

    static void main(String[] args) throws Exception {

        List<Callable<String>> tareas = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {

            int numeroApi = i;

            tareas.add(() -> {

                int segundos = ThreadLocalRandom
                        .current()
                        .nextInt(1, 4);

                Thread.sleep(segundos * 1000L);

                return "Respuesta de API " + numeroApi
                        + " después de " + segundos + " segundos";
            });
        }

        ExecutorService executor =
                Executors.newFixedThreadPool(5);

        List<Future<String>> futuros =
                executor.invokeAll(tareas);

        for (Future<String> future : futuros) {

            System.out.println(future.get());
        }

        executor.shutdown();
    }
}
