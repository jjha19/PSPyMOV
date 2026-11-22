package org.Problemas;

import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Restaurante {
    static void main() {
        int numeroCocineros = 3;
        int numeroCamareros = 5;
        int numeroClientes = 30;

        ExecutorService cocineros = Executors.newFixedThreadPool(numeroCocineros);
        ExecutorService camareros = Executors.newFixedThreadPool(numeroCamareros);

        BlockingQueue<Pedido> pedidos = new ArrayBlockingQueue<>(10);
        BlockingQueue<Pedido> recepcion = new LinkedBlockingQueue<>();

        AtomicInteger mesaLlenaVeces = new AtomicInteger(0);
        AtomicLong tiempoEsperaCocineros = new AtomicLong(0);
        AtomicInteger platosServidos = new AtomicInteger(0);
        AtomicInteger clientesServidos = new AtomicInteger(0);
        AtomicLong esperaTotal = new AtomicLong(0);
        AtomicInteger ganancias = new AtomicInteger(0);

        Runnable camarero = () -> {
            try {
                while (true) {
                    Pedido p = recepcion.take();
                    Thread.sleep(1000);
                    System.out.println("Camarero toma pedido del cliente " + p.clienteId + " que pidio " + p.plato.toString());
                    if (!pedidos.offer(p)) {
                        mesaLlenaVeces.incrementAndGet();
                        pedidos.put(p);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Runnable cocinero = () -> {
            try {
                while (true) {
                    long t0 = System.nanoTime();
                    Pedido p = pedidos.take();
                    tiempoEsperaCocineros.addAndGet(System.nanoTime() - t0);
                    Thread.sleep(p.plato.getTiempoMs());

                    System.out.println("El cocinero termina el plato " + p.plato.toString() + " para el cliente " + p.clienteId);
                    platosServidos.incrementAndGet();
                    p.listo.countDown();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        Random random = new Random();
        Runnable cliente = () -> {
            long llegada = System.nanoTime();
            Pedido p = new Pedido((int) Thread.currentThread().getId(), TipoPlato.values()[random.nextInt(TipoPlato.values().length)]);
            System.out.println("El cliente " + p.clienteId + " pide " + p.plato.toString());
            try {
                recepcion.put(p);
                p.listo.await();
                long espera = System.nanoTime() - llegada ;
                esperaTotal.addAndGet(espera);
                ganancias.addAndGet(p.plato.getPrecio());
                System.out.println("El cliente " + p.clienteId + " paga y se va satisfecho");
                clientesServidos.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };

        for (int i = 0; i < numeroCamareros; i++) {
            camareros.submit(camarero);
        }
        for (int i = 0; i < numeroCocineros; i++) {
            cocineros.submit(cocinero);
        }
        try (ExecutorService clientes = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < numeroClientes; i++) {
                clientes.submit(cliente);
                Thread.sleep(500);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        camareros.shutdownNow();
        cocineros.shutdownNow();
        System.out.println("\n=== ESTADÍSTICAS ===");
        System.out.println("Platos servidos: " + platosServidos.get());
        System.out.println("Clientes servidos: " + clientesServidos.get());
        System.out.println("Mesa llena veces: " + mesaLlenaVeces.get());
        System.out.println("Tiempo de espera de cocineros en segundos: " + tiempoEsperaCocineros.get() / 1_000_000_000.0);
        System.out.println("Tiempo promedio de espera de clientes en segundos: " + ((esperaTotal.get() / clientesServidos.get()) / 1_000_000_000.0));
        System.out.println("Ganancias: " + ganancias.get());
    }

}

enum TipoPlato {
    ENSALADA(2000,8),
    PASTA(3000,12),
    PIZZA(4000,13),
    CARNE(5000,17);

    private final int tiempoMs;
    private final int precio;

    TipoPlato(int tiempoMs, int precio) {
        this.tiempoMs = tiempoMs;
        this.precio = precio;
    }

    public int getTiempoMs() {
        return tiempoMs;
    }

    public int getPrecio() {
        return precio;
    }
}

class Pedido {
    final int clienteId;
    final TipoPlato plato;
    CountDownLatch listo = new CountDownLatch(1);

    Pedido(int clienteId, TipoPlato plato) {
        this.clienteId = clienteId;
        this.plato = plato;
    }
}

