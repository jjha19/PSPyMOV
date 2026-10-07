package org.Asyncronas;

import java.sql.SQLOutput;
import java.util.concurrent.*;

public class Ej1Future {
    static void main() {

        ExecutorService executor = Executors.newFixedThreadPool(2);

        Callable<Integer> tarea = () -> {
            System.out.println(Thread.currentThread().getName() + " está ejecutando la tarea...");
            Thread.sleep(2000);
            return 42;
        };

        Future<Integer> future = executor.submit(tarea);
        System.out.println("Tarea enviada, esperando resultado...");

//        try {
//            int resultado = future.get();
//            System.out.println("Resultado recibido: " + resultado);
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }

        System.out.println("Esperando a que la tarea se complete...");

        while (!future.isDone()) {
            System.out.println("Esperando...");
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        try {
            int resultado = future.get();
            System.out.println("Resultado recibido: " + resultado);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        executor.shutdown();
    }
}
