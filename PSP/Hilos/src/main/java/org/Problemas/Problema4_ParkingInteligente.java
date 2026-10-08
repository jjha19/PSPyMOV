package org.Problemas;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
public class Problema4_ParkingInteligente {

    private static final int PLAZAS_NORMALES = 20;
    private static final int PLAZAS_VIP = 5;
    private static final int MAX_COLA = 10;
    private static final int TOTAL_COCHES = 200;
    private static final double MINUTOS_POR_SEGUNDO_SIM = 0.1;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static double escala = 1.0;

    enum TipoVehiculo {
        NORMAL(1.0),
        VIP(2.0);

        private final double tarifaPorMinuto;

        TipoVehiculo(double tarifaPorMinuto) {
            this.tarifaPorMinuto = tarifaPorMinuto;
        }

        double getTarifaPorMinuto() {
            return tarifaPorMinuto;
        }
    }

    
    private record Plaza(String id, Semaphore semaforo, Queue<String> idsLibres) {
        void liberar() {
            idsLibres.add(id);
            semaforo.release();
        }
    }
    
    private static final Semaphore normales = new Semaphore(PLAZAS_NORMALES, true);
    private static final Semaphore vips = new Semaphore(PLAZAS_VIP, true);
    private static final Queue<String> idsNormales = new ConcurrentLinkedQueue<>();
    private static final Queue<String> idsVip = new ConcurrentLinkedQueue<>();
    private static final Semaphore barreraEntrada = new Semaphore(1, true);
    private static final Semaphore barreraSalida = new Semaphore(1, true);

    
    private static final AtomicInteger enCola = new AtomicInteger();
    private static final AtomicInteger ocupadasMax = new AtomicInteger();
    private static final AtomicInteger atendidos = new AtomicInteger();
    private static final AtomicInteger rechazados = new AtomicInteger();
    private static final AtomicInteger completados = new AtomicInteger();
    private static final AtomicLong estanciaTotalMs = new AtomicLong();   
    private static final AtomicLong ingresosCentimos = new AtomicLong();
    private static final Deque<String> ultimosEventos = new ArrayDeque<>();

    private static void dormir(long ms) throws InterruptedException {
        Thread.sleep(Math.max(1, (long) (ms * escala)));
    }

    private static int ocupadas() {
        return (PLAZAS_NORMALES - normales.availablePermits()) + (PLAZAS_VIP - vips.availablePermits());
    }

    private static void evento(String texto) {
        String linea = "[" + LocalTime.now().format(HORA) + "] " + texto;
        synchronized (ultimosEventos) {
            ultimosEventos.addLast(linea);
            while (ultimosEventos.size() > 5) {
                ultimosEventos.removeFirst();
            }
        }
        System.out.println(linea);
    }

    
    private static Plaza intentarReservar(TipoVehiculo tipo) {
        if (normales.tryAcquire()) {
            return new Plaza(idsNormales.poll(), normales, idsNormales);
        }
        if (tipo == TipoVehiculo.VIP && vips.tryAcquire()) {
            return new Plaza(idsVip.poll(), vips, idsVip);
        }
        return null;
    }

    
    private static Plaza esperarPlaza(TipoVehiculo tipo) throws InterruptedException {
        if (tipo == TipoVehiculo.NORMAL) {
            normales.acquire(); 
            return new Plaza(idsNormales.poll(), normales, idsNormales);
        }
        while (true) { 
            if (normales.tryAcquire(20, TimeUnit.MILLISECONDS)) {
                return new Plaza(idsNormales.poll(), normales, idsNormales);
            }
            if (vips.tryAcquire(20, TimeUnit.MILLISECONDS)) {
                return new Plaza(idsVip.poll(), vips, idsVip);
            }
        }
    }

    
    private static boolean entrarEnCola() {
        while (true) {
            int actual = enCola.get();
            if (actual >= MAX_COLA) {
                return false;
            }
            if (enCola.compareAndSet(actual, actual + 1)) {
                return true;
            }
        }
    }

    private static void maximoOcupacion() {
        int ahora = ocupadas();
        ocupadasMax.accumulateAndGet(ahora, Math::max);
    }

    
    private static void coche(int id, TipoVehiculo tipo) {
        String nombre = String.format("Coche-%03d (%s)", id, tipo);
        String icono = tipo == TipoVehiculo.VIP ? "⭐" : "🚗";
        try {
            Plaza plaza = intentarReservar(tipo);
            if (plaza == null) {
                if (!entrarEnCola()) {
                    rechazados.incrementAndGet();
                    evento(nombre + " se va: parking y cola llenos");
                    return;
                }
                evento(icono + " " + nombre + " esperando en cola");
                try {
                    plaza = esperarPlaza(tipo);
                } finally {
                    enCola.decrementAndGet();
                }
            }
            maximoOcupacion();

            
            barreraEntrada.acquire();
            try {
                dormir(2000);
            } finally {
                barreraEntrada.release();
            }
            atendidos.incrementAndGet();
            evento(icono + " " + nombre + " entra - Plaza " + plaza.id());

            
            int estanciaSeg = ThreadLocalRandom.current().nextInt(10, 31);
            dormir(estanciaSeg * 1000L);

            
            barreraSalida.acquire();
            try {
                dormir(1000);
            } finally {
                barreraSalida.release();
            }

            double importe = estanciaSeg * MINUTOS_POR_SEGUNDO_SIM * tipo.getTarifaPorMinuto();
            ingresosCentimos.addAndGet(Math.round(importe * 100));
            estanciaTotalMs.addAndGet(estanciaSeg * 1000L);
            plaza.liberar();
            completados.incrementAndGet();
            evento(String.format(Locale.US, "%s %s sale - Plaza %s - Pagó: %.2f€", icono, nombre, plaza.id(), importe));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static String barra(int valor, int maximo, int ancho) {
        int llenos = (int) Math.round((double) valor / maximo * ancho);
        return "█".repeat(llenos) + "░".repeat(ancho - llenos);
    }

    private static void dashboard() {
        int normalesOcupadas = PLAZAS_NORMALES - normales.availablePermits();
        int vipOcupadas = PLAZAS_VIP - vips.availablePermits();
        System.out.println();
        System.out.println("=== PARKING INTELIGENTE ===");
        System.out.println("🅿️  Estado actual: [" + LocalTime.now().format(HORA) + "]");
        System.out.println("┌──────────────────────────────────────┐");
        System.out.printf("│ PLAZAS NORMALES: %s %2d/%-2d     │%n", barra(normalesOcupadas, PLAZAS_NORMALES, 10),
                normalesOcupadas, PLAZAS_NORMALES);
        System.out.printf("│ PLAZAS VIP:      %s %2d/%-2d     │%n", barra(vipOcupadas, PLAZAS_VIP, 10),
                vipOcupadas, PLAZAS_VIP);
        System.out.printf("│ COLA DE ESPERA:  %s %2d/%-2d     │%n", barra(enCola.get(), MAX_COLA, 10),
                enCola.get(), MAX_COLA);
        System.out.printf(Locale.US, "│ INGRESOS HOY:    %17.2f€  │%n", ingresosCentimos.get() / 100.0);
        System.out.println("└──────────────────────────────────────┘");
        System.out.println("Procesados: " + (atendidos.get() + rechazados.get()) + " | Rechazados: " + rechazados.get());
        System.out.println();
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            escala = Double.parseDouble(args[0]);
        }
        for (int i = 1; i <= PLAZAS_NORMALES; i++) {
            idsNormales.add("N-" + i);
        }
        for (int i = 1; i <= PLAZAS_VIP; i++) {
            idsVip.add("V-" + i);
        }

        ScheduledExecutorService panel = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Dashboard");
            t.setDaemon(true);
            return t;
        });
        long periodoMs = Math.max(50, (long) (10_000 * escala));
        panel.scheduleAtFixedRate(Problema4_ParkingInteligente::dashboard, periodoMs, periodoMs, TimeUnit.MILLISECONDS);

        System.out.println("=== PARKING INTELIGENTE === (" + TOTAL_COCHES + " coches, escala " + escala + ")\n");
        long inicio = System.nanoTime();

        try (ExecutorService coches = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= TOTAL_COCHES; i++) {
                final int id = i;
                
                final TipoVehiculo tipo = ThreadLocalRandom.current().nextInt(100) < 25
                        ? TipoVehiculo.VIP : TipoVehiculo.NORMAL;
                coches.submit(() -> coche(id, tipo));
                dormir(ThreadLocalRandom.current().nextInt(1000, 2001)); 
            }
        } 
        panel.shutdownNow();

        int procesados = atendidos.get() + rechazados.get();
        double segundos = (System.nanoTime() - inicio) / 1_000_000_000.0 / escala;
        System.out.println("\n--- RESUMEN DEL DÍA ---");
        System.out.println("Vehículos procesados: " + procesados);
        System.out.printf(Locale.US, "Vehículos atendidos: %d (%.1f%%)%n", atendidos.get(),
                100.0 * atendidos.get() / procesados);
        System.out.println("Vehículos rechazados: " + rechazados + " (parking+cola llenos)");
        System.out.printf(Locale.US, "Tiempo promedio de estancia: %.1fs%n",
                estanciaTotalMs.get() / 1000.0 / Math.max(1, completados.get()));
        System.out.printf(Locale.US, "Ingresos totales: %.2f€%n", ingresosCentimos.get() / 100.0);
        System.out.printf(Locale.US, "Ocupación máxima: %d/%d plazas (%.0f%%)%n", ocupadasMax.get(),
                PLAZAS_NORMALES + PLAZAS_VIP, 100.0 * ocupadasMax.get() / (PLAZAS_NORMALES + PLAZAS_VIP));
        System.out.printf(Locale.US, "Duración simulada: %.0fs%n", segundos);
    }
}
