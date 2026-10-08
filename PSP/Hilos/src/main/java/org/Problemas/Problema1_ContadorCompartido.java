package org.Problemas;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;


public class Problema1_ContadorCompartido {

    private static final int VISITANTES = 1000;

    interface ContadorVisitas {
        void incrementarVisita();
        int obtenerVisitas();
    }

private static void trabajoDelIncremento() {
        LockSupport.parkNanos(50_000); // ~50 microsegundos
    }

    static class ContadorSinSincronizar implements ContadorVisitas {
        private int visitas = 0;

        @Override
        public void incrementarVisita() {
            int temporal = visitas;      // leer
            trabajoDelIncremento();      // otro hilo puede leer el mismo valor aquí
            visitas = temporal + 1;      // escribir (pisa el resultado del otro hilo)
        }

        @Override
        public int obtenerVisitas() {
            return visitas;
        }
    }

    static class ContadorSynchronized implements ContadorVisitas {
        private int visitas = 0;

        @Override
        public synchronized void incrementarVisita() {
            int temporal = visitas;
            trabajoDelIncremento();
            visitas = temporal + 1;
        }

        @Override
        public synchronized int obtenerVisitas() {
            return visitas;
        }
    }

    static class ContadorAtomico implements ContadorVisitas {
        private final AtomicInteger visitas = new AtomicInteger();

        @Override
        public void incrementarVisita() {
            visitas.incrementAndGet();
        }

        @Override
        public int obtenerVisitas() {
            return visitas.get();
        }
    }

    private static void ejecutarPrueba(String titulo, ContadorVisitas contador) throws InterruptedException {
        System.out.println("--- " + titulo + " ---");

        List<Thread> hilos = new ArrayList<>(VISITANTES);
        for (int i = 0; i < VISITANTES; i++) {
            hilos.add(new Thread(() -> {
                try {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(50, 151)); // 50-150 ms
                    contador.incrementarVisita();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Visitante"));
        }

        long inicio = System.nanoTime();
        hilos.forEach(Thread::start);
        for (Thread hilo : hilos) {
            hilo.join();
        }
        long ms = (System.nanoTime() - inicio) / 1_000_000;

        int contadas = contador.obtenerVisitas();
        boolean correcto = contadas == VISITANTES;
        System.out.println("Visitas esperadas: " + VISITANTES);
        System.out.println("Visitas contadas: " + contadas + (correcto ? " ✅ CORRECTO" : " ❌ INCORRECTO"));
        System.out.println("Tiempo: " + ms + "ms\n");
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== CONTADOR DE VISITAS WEB ===");
        System.out.println("Esperando " + VISITANTES + " visitantes...\n");

        ejecutarPrueba("SIN SINCRONIZACIÓN", new ContadorSinSincronizar());
        ejecutarPrueba("CON SYNCHRONIZED", new ContadorSynchronized());
        ejecutarPrueba("CON ATOMICINTEGER", new ContadorAtomico());
    }
}
