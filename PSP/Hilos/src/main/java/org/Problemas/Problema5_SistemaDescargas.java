package org.Problemas;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
public class Problema5_SistemaDescargas {

    private static final int CONEXIONES_SERVIDOR = 5;
    private static final int TOTAL_ARCHIVOS = 100;
    private static final int TOTAL_USUARIOS = 50;
    private static final int MAX_INTENTOS = 3;
    private static final double VELOCIDAD_SERVIDOR_MBPS = 10.0;
    private static final double PROBABILIDAD_FALLO_INTENTO = 0.30;
    private static final int TICK_MS = 200; 
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");

    private static double escala = 1.0;

    enum TipoArchivo {
        DOCUMENTO(10, "doc", "pdf"),
        IMAGEN(50, "imagen", "jpg"),
        VIDEO(500, "video", "mp4"),
        JUEGO(2000, "juego", "zip");

        private final int sizeMB;
        final String prefijo;
        final String extension;

        TipoArchivo(int sizeMB, String prefijo, String extension) {
            this.sizeMB = sizeMB;
            this.prefijo = prefijo;
            this.extension = extension;
        }

        int getSizeMB() {
            return sizeMB;
        }
    }

    record Archivo(int id, TipoArchivo tipo) {
        String nombre() {
            return tipo.prefijo + "_" + id + "." + tipo.extension;
        }
    }

    record Usuario(int id, boolean premium, double limiteMBps) {
        String nombre() {
            return "Usuario-" + String.format("%02d", id) + (premium ? "★" : "");
        }
    }

    private record Resultado(boolean exito, int intentos, double mbTransferidos) {
    }

    
    private static final AtomicLong secuencia = new AtomicLong();
    private static final Set<DescargaTask> activas = ConcurrentHashMap.newKeySet();
    private static final AtomicInteger completadas = new AtomicInteger();
    private static final AtomicInteger fallidas = new AtomicInteger();
    private static final AtomicInteger premiumOk = new AtomicInteger();
    private static final AtomicInteger normalesOk = new AtomicInteger();
    private static final AtomicLong tiempoDescargasMs = new AtomicLong(); 
    private static final AtomicLong mbTotales = new AtomicLong();

    private static void dormir(double ms) throws InterruptedException {
        Thread.sleep(Math.max(1, (long) (ms * escala)));
    }

    private static synchronized void log(String texto) {
        System.out.println("[" + LocalTime.now().format(HORA) + "] " + texto);
    }

    



    private static class DescargaTask implements Runnable, Comparable<DescargaTask> {
        final Usuario usuario;
        final Archivo archivo;
        final int intento;
        final long orden = secuencia.incrementAndGet();
        final CompletableFuture<Resultado> futuro = new CompletableFuture<>();

        private volatile double descargadoMB = 0;
        private boolean pausada = false; 

        DescargaTask(Usuario usuario, Archivo archivo, int intento) {
            this.usuario = usuario;
            this.archivo = archivo;
            this.intento = intento;
        }

        double progreso() {
            return Math.min(1.0, descargadoMB / archivo.tipo().getSizeMB());
        }

        synchronized void pausar() {
            pausada = true;
        }

        synchronized void reanudar() {
            pausada = false;
            notifyAll();
        }

        
        private synchronized void esperarSiPausada() throws InterruptedException {
            while (pausada) {
                wait();
            }
        }

        @Override
        public int compareTo(DescargaTask otra) {
            if (usuario.premium() != otra.usuario.premium()) {
                return usuario.premium() ? -1 : 1; 
            }
            return Long.compare(orden, otra.orden);
        }

        @Override
        public void run() {
            activas.add(this);
            log("▶ " + usuario.nombre() + " inicia: " + archivo.nombre()
                    + (intento > 1 ? " (intento " + intento + "/" + MAX_INTENTOS + ")" : ""));
            boolean exito = false;
            try {
                
                double velocidad = Math.min(VELOCIDAD_SERVIDOR_MBPS, usuario.limiteMBps());
                int sizeMB = archivo.tipo().getSizeMB();
                
                double puntoFallo = ThreadLocalRandom.current().nextDouble() < PROBABILIDAD_FALLO_INTENTO
                        ? ThreadLocalRandom.current().nextDouble(0.1, 0.95) : -1;

                while (descargadoMB < sizeMB) {
                    dormir(TICK_MS);
                    esperarSiPausada();
                    descargadoMB += velocidad * TICK_MS / 1000.0;
                    if (puntoFallo > 0 && progreso() >= puntoFallo) {
                        log("⚠ " + usuario.nombre() + " conexión perdida en " + archivo.nombre()
                                + " (" + (int) (progreso() * 100) + "%)");
                        return; 
                    }
                }
                descargadoMB = sizeMB;
                exito = true;
                log("✅ " + usuario.nombre() + " completó: " + archivo.nombre());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                activas.remove(this);
                futuro.complete(new Resultado(exito, intento, exito ? archivo.tipo().getSizeMB() : 0));
            }
        }
    }

    
    private static final ThreadPoolExecutor servidor = new ThreadPoolExecutor(
            CONEXIONES_SERVIDOR, CONEXIONES_SERVIDOR, 0L, TimeUnit.MILLISECONDS,
            new PriorityBlockingQueue<>(64, (a, b) -> ((DescargaTask) a).compareTo((DescargaTask) b)));

    



    private static CompletableFuture<Resultado> descargarConReintentos(Usuario u, Archivo a, int intento) {
        DescargaTask tarea = new DescargaTask(u, a, intento);
        servidor.execute(tarea);
        return tarea.futuro.thenCompose(r -> {
            if (r.exito() || intento >= MAX_INTENTOS) {
                return CompletableFuture.completedFuture(r);
            }
            log("🔁 " + u.nombre() + " reintentando (" + (intento + 1) + "/" + MAX_INTENTOS + "): " + a.nombre());
            return descargarConReintentos(u, a, intento + 1);
        });
    }

    
    private static String barra(double fraccion, int ancho) {
        int llenos = (int) Math.round(fraccion * ancho);
        return "█".repeat(llenos) + "░".repeat(ancho - llenos);
    }

    private static void dashboard() {
        List<DescargaTask> lista = new ArrayList<>(activas);
        StringBuilder sb = new StringBuilder();
        sb.append("\n=== GESTOR DE DESCARGAS ===\n");
        sb.append(String.format("Servidor: %d/%d conexiones activas%n", servidor.getActiveCount(), CONEXIONES_SERVIDOR));
        sb.append("\n🔄 Descargas activas:\n┌──────────────────────────────────────────────────┐\n");
        for (DescargaTask t : lista) {
            sb.append(String.format("│ %-11s │ %-16s %s %3d%% │%n", t.usuario.nombre(), t.archivo.nombre(),
                    barra(t.progreso(), 9), (int) (t.progreso() * 100)));
        }
        sb.append("└──────────────────────────────────────────────────┘\n");
        sb.append(String.format("⏳ Cola de espera: %d | ❌ Fallos: %d | ✅ Completadas: %d%n",
                servidor.getQueue().size(), fallidas.get(), completadas.get()));
        synchronized (Problema5_SistemaDescargas.class) {
            System.out.print(sb);
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            escala = Double.parseDouble(args[0]);
        }

        
        List<Archivo> catalogo = new ArrayList<>();
        for (int i = 1; i <= TOTAL_ARCHIVOS; i++) {
            int p = ThreadLocalRandom.current().nextInt(100);
            TipoArchivo tipo = p < 50 ? TipoArchivo.DOCUMENTO : p < 80 ? TipoArchivo.IMAGEN
                    : p < 95 ? TipoArchivo.VIDEO : TipoArchivo.JUEGO;
            catalogo.add(new Archivo(i, tipo));
        }

        
        List<Usuario> usuarios = new ArrayList<>();
        for (int i = 1; i <= TOTAL_USUARIOS; i++) {
            boolean premium = ThreadLocalRandom.current().nextInt(100) < 30;
            double limite = (!premium && ThreadLocalRandom.current().nextInt(100) < 25) ? 5.0 : 10.0;
            usuarios.add(new Usuario(i, premium, limite));
        }
        long totalPremium = usuarios.stream().filter(Usuario::premium).count();

        System.out.println("=== GESTOR DE DESCARGAS === (" + TOTAL_USUARIOS + " usuarios, " + totalPremium
                + " premium, " + CONEXIONES_SERVIDOR + " conexiones, escala " + escala + ")\n");

        ScheduledExecutorService programador = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Programador");
            t.setDaemon(true);
            return t;
        });
        long periodo = Math.max(50, (long) (5000 * escala));
        programador.scheduleAtFixedRate(Problema5_SistemaDescargas::dashboard, periodo, periodo, TimeUnit.MILLISECONDS);

        
        long periodoPausa = Math.max(50, (long) (8000 * escala));
        programador.scheduleAtFixedRate(() -> {
            List<DescargaTask> lista = new ArrayList<>(activas);
            if (lista.isEmpty()) {
                return;
            }
            DescargaTask t = lista.get(ThreadLocalRandom.current().nextInt(lista.size()));
            t.pausar();
            log("⏸ " + t.usuario.nombre() + " pausa: " + t.archivo.nombre());
            programador.schedule(() -> {
                t.reanudar();
                log("⏯ " + t.usuario.nombre() + " reanuda: " + t.archivo.nombre());
            }, Math.max(1, (long) (2000 * escala)), TimeUnit.MILLISECONDS);
        }, periodoPausa, periodoPausa, TimeUnit.MILLISECONDS);

        long inicio = System.nanoTime();

        
        try (ExecutorService clientes = Executors.newVirtualThreadPerTaskExecutor()) {
            for (Usuario u : usuarios) {
                clientes.submit(() -> {
                    try {
                        dormir(ThreadLocalRandom.current().nextInt(0, 10_000)); 
                        Archivo archivo = catalogo.get(ThreadLocalRandom.current().nextInt(catalogo.size()));
                        long t0 = System.nanoTime();
                        Resultado r = descargarConReintentos(u, archivo, 1).join();
                        long ms = (long) ((System.nanoTime() - t0) / 1_000_000 / escala);
                        if (r.exito()) {
                            completadas.incrementAndGet();
                            (u.premium() ? premiumOk : normalesOk).incrementAndGet();
                            mbTotales.addAndGet((long) r.mbTransferidos());
                            tiempoDescargasMs.addAndGet(ms);
                        } else {
                            fallidas.incrementAndGet();
                            log("❌ " + u.nombre() + " agotó los reintentos: " + archivo.nombre());
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
        programador.shutdownNow();
        servidor.shutdown();

        double segundos = (System.nanoTime() - inicio) / 1_000_000_000.0 / escala;
        long normalesTotal = TOTAL_USUARIOS - totalPremium;
        int ok = completadas.get();
        System.out.println("\n--- ESTADÍSTICAS DEL SERVIDOR ---");
        System.out.printf(Locale.US, "Tiempo total funcionamiento: %dm %02ds%n", (long) segundos / 60, (long) segundos % 60);
        System.out.println("Archivos descargados: " + ok + "/" + TOTAL_USUARIOS + " solicitudes");
        System.out.printf(Locale.US, "Descargas exitosas: %d (%.1f%%)%n", ok, 100.0 * ok / TOTAL_USUARIOS);
        System.out.println("Descargas fallidas: " + fallidas + " (reintentos agotados)");
        System.out.printf(Locale.US, "Datos transferidos: %.1f GB%n", mbTotales.get() / 1024.0);
        System.out.printf(Locale.US, "Velocidad promedio: %.1f MB/s%n", mbTotales.get() / segundos);
        System.out.printf(Locale.US, "Tiempo promedio por descarga: %.1fs (incluye espera en cola)%n",
                tiempoDescargasMs.get() / 1000.0 / Math.max(1, ok));
        System.out.printf(Locale.US, "Usuarios premium atendidos: %d/%d (%.1f%%)%n", premiumOk.get(), totalPremium,
                100.0 * premiumOk.get() / Math.max(1, totalPremium));
        System.out.printf(Locale.US, "Usuarios normales atendidos: %d/%d (%.1f%%)%n", normalesOk.get(), normalesTotal,
                100.0 * normalesOk.get() / Math.max(1, normalesTotal));
    }
}
