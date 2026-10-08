package org.Problemas;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Problema3_RestauranteConcurrente {

    private static final int COCINEROS = 3;
    private static final int CAMAREROS = 5;
    private static final int CLIENTES = 100;
    private static final int CAPACIDAD_MESA = 10;
    private static final int LLEGADA_CLIENTES_MS = 500;
    private static final int TIEMPO_TOMAR_PEDIDO_MS = 1000;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");


    private static double escala = 1.0;

    enum TipoPlato {
        ENSALADA(2000),
        PASTA(3000),
        PIZZA(4000),
        CARNE(5000);

        private final int tiempoMs;

        TipoPlato(int tiempoMs) {
            this.tiempoMs = tiempoMs;
        }

        int getTiempoMs() {
            return tiempoMs;
        }
    }


    private static class Pedido {
        final int idCliente;
        final TipoPlato plato;
        final long creadoNanos = System.nanoTime();
        final CompletableFuture<Long> platoListo = new CompletableFuture<>();

        Pedido(int idCliente, TipoPlato plato) {
            this.idCliente = idCliente;
            this.plato = plato;
        }
    }

    private static final BlockingQueue<Pedido> pedidosDeClientes = new LinkedBlockingQueue<>();

    private static final BlockingQueue<Pedido> mesaDePedidos = new ArrayBlockingQueue<>(CAPACIDAD_MESA);

    private static final AtomicInteger platosServidos = new AtomicInteger();
    private static final AtomicInteger mesaLlenaVeces = new AtomicInteger();
    private static final AtomicLong esperaTotalClientesNanos = new AtomicLong();
    private static final AtomicLong cocinerosEsperandoNanos = new AtomicLong();
    private static final AtomicLong cocinerosCocinandoNanos = new AtomicLong();


    private static void dormir(long ms) throws InterruptedException {
        Thread.sleep(Math.max(1, (long) (ms * escala)));
    }


    private static double simulado(long nanos) {
        return nanos / 1_000_000_000.0 / escala;
    }

    private static synchronized void log(String mensaje) {
        System.out.printf("[%s] %s%n", LocalTime.now().format(HORA), mensaje);
    }

    private static String cliente(int id) {
        return String.format("Cliente-%03d", id);
    }

    private static Runnable camarero(int id) {
        return () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Pedido pedido = pedidosDeClientes.take();
                    log("Camarero-" + id + " toma pedido " + pedido.plato + " de " + cliente(pedido.idCliente));
                    dormir(TIEMPO_TOMAR_PEDIDO_MS);


                    if (!mesaDePedidos.offer(pedido)) {
                        mesaLlenaVeces.incrementAndGet();
                        log("Camarero-" + id + " ESPERA: mesa llena (" + CAPACIDAD_MESA + " pedidos)");
                        mesaDePedidos.put(pedido);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }


    private static Runnable cocinero(int id) {
        return () -> {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    long antes = System.nanoTime();
                    Pedido pedido = mesaDePedidos.take();
                    cocinerosEsperandoNanos.addAndGet(System.nanoTime() - antes);

                    long inicioCocina = System.nanoTime();
                    dormir(pedido.plato.getTiempoMs());
                    cocinerosCocinandoNanos.addAndGet(System.nanoTime() - inicioCocina);

                    log("Cocinero-" + id + " termina " + pedido.plato + " para " + cliente(pedido.idCliente));
                    pedido.platoListo.complete(System.nanoTime() - pedido.creadoNanos);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
    }


    private static void cliente(int id, TipoPlato plato) {
        log(cliente(id) + " pide " + plato);
        Pedido pedido = new Pedido(id, plato);
        pedidosDeClientes.add(pedido);
        long espera = pedido.platoListo.join();
        esperaTotalClientesNanos.addAndGet(espera);
        platosServidos.incrementAndGet();
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            escala = Double.parseDouble(args[0]);
        }

        System.out.println("=== RESTAURANTE CONCURRENTE ===");
        System.out.println("Iniciando servicio con " + COCINEROS + " cocineros y " + CAMAREROS + " camareros...\n");


        List<Thread> personal = new ArrayList<>();
        for (int i = 1; i <= CAMAREROS; i++) {
            personal.add(Thread.ofPlatform().name("Camarero-" + i).daemon().start(camarero(i)));
        }
        for (int i = 1; i <= COCINEROS; i++) {
            personal.add(Thread.ofPlatform().name("Cocinero-" + i).daemon().start(cocinero(i)));
        }

        long inicio = System.nanoTime();


        ScheduledExecutorService estadisticas = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Estadisticas");
            t.setDaemon(true);
            return t;
        });
        long periodoMs = Math.max(1, (long) (10_000 * escala));
        estadisticas.scheduleAtFixedRate(() -> log(String.format(Locale.US,
                        "📊 Servidos: %d/%d | Pedidos sin tomar: %d | Mesa: %d/%d | Mesa llena: %d veces",
                        platosServidos.get(), CLIENTES, pedidosDeClientes.size(),
                        mesaDePedidos.size(), CAPACIDAD_MESA, mesaLlenaVeces.get())),
                periodoMs, periodoMs, TimeUnit.MILLISECONDS);


        TipoPlato[] platos = TipoPlato.values();
        try (ExecutorService clientes = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= CLIENTES; i++) {
                final int id = i;
                final TipoPlato plato = platos[ThreadLocalRandom.current().nextInt(platos.length)];
                clientes.submit(() -> cliente(id, plato));
                if (i < CLIENTES) {
                    dormir(LLEGADA_CLIENTES_MS);
                }
            }
        }

        double duracion = simulado(System.nanoTime() - inicio);
        estadisticas.shutdownNow();
        personal.forEach(Thread::interrupt);


        double cocinando = simulado(cocinerosCocinandoNanos.get());
        double eficiencia = 100.0 * cocinando / (COCINEROS * duracion);

        System.out.println("\n--- ESTADÍSTICAS FINALES ---");

        System.out.println("Clientes atendidos: " + platosServidos + "/" + CLIENTES);

        if (platosServidos.get() == CLIENTES) {
            System.out.println("Todos los clientes han sido atendidos.");
        } else {
            System.out.println("No se han atendido todos los clientes.");
        }

        System.out.println("Platos servidos: " + platosServidos);

        double tiempoMedio = simulado(esperaTotalClientesNanos.get())
                / Math.max(1, platosServidos.get());

        System.out.printf(Locale.US, "Tiempo promedio de espera: %.1fs%n", tiempoMedio);

        System.out.println("Mesa llena (veces): " + mesaLlenaVeces);

        double tiempoCocineros = simulado(cocinerosEsperandoNanos.get());

        System.out.printf(Locale.US,
                "Cocineros esperando (tiempo): %.0fs total%n",
                tiempoCocineros);
        System.out.printf(Locale.US, "Eficiencia: %.0f%%%n", eficiencia);
        System.out.printf(Locale.US, "Duración total (simulada): %.0fs%n", duracion);
    }
}
