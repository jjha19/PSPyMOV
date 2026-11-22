package org.hilosjava25;

import java.util.concurrent.atomic.AtomicInteger;

public class Ejercicio5Contador {
    static void main(String[] args) {
        AtomicInteger contador = new AtomicInteger(0);
        for (int i = 0; i < 1000; i++) {
            Thread hilo = new Thread(
                    () -> {
                        contador.incrementAndGet();
                    }
            );
            hilo.start();
        }
        try {
            Thread.sleep(1000);
            System.out.println("Valor final del contador: " + contador.get());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
