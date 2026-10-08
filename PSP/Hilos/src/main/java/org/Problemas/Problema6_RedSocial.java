package org.Problemas;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
public class Problema6_RedSocial {

    
    private static final int NUM_USUARIOS = 1000;
    private static final int NUM_INFLUENCERS = 50;
    private static final int NUM_BOTS = 100;
    private static final int MAX_POSTS_POR_MINUTO = 10;
    private static final int POOL_BD = 20;
    private static final int LATENCIA_BD_MS = 100;
    private static final int TAMANO_CACHE = 50;
    private static final int LIKES_POPULAR = 100;      
    private static final int LIKES_VIRAL_POR_MINUTO = 100;
    private static final int MAX_ACCIONES_POR_SEGUNDO = 10; 
    private static final int TAMANO_FEED = 50;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final String[] HASHTAGS = {"#JavaConcurrency", "#VirtualThreads", "#GatosProgramando",
            "#ViralVideo", "#Tech", "#Madrid", "#Futbol", "#Musica"};

    private static long segundosSimulacion = 40;
    private static volatile boolean corriendo = true;
    private static final long INICIO = System.nanoTime();

    
    enum TipoActividad {
        PUBLICAR_POST(2000),
        DAR_LIKE(100),
        COMENTAR(1500),
        SEGUIR_USUARIO(200),
        COMPARTIR_POST(300);

        private final int tiempoMs;

        TipoActividad(int tiempoMs) {
            this.tiempoMs = tiempoMs;
        }

        int getTiempoMs() {
            return tiempoMs;
        }
    }

    enum TipoUsuario { NORMAL, INFLUENCER, BOT }

    enum TipoNotificacion { LIKE, COMENTARIO, NUEVO_SEGUIDOR, COMPARTIDO, POST_VIRAL }

    record Usuario(String id, TipoUsuario tipo) {
    }

    record Comentario(String autor, String texto, LocalDateTime timestamp) {
    }

    record Notificacion(String destinatario, TipoNotificacion tipo, String origen) {
    }

    static class Post {
        private final String id;
        private final String autor;
        private final String contenido;
        private final String hashtag;
        private final AtomicLong likes = new AtomicLong();
        private final AtomicLong compartidos = new AtomicLong();
        private final ConcurrentLinkedQueue<Comentario> comentarios = new ConcurrentLinkedQueue<>();
        private final LocalDateTime timestamp = LocalDateTime.now();
        
        private final AtomicLong inicioVentanaNanos = new AtomicLong(System.nanoTime());
        private final AtomicInteger likesVentana = new AtomicInteger();
        private final AtomicBoolean viral = new AtomicBoolean();

        Post(String id, String autor, String contenido, String hashtag) {
            this.id = id;
            this.autor = autor;
            this.contenido = contenido;
            this.hashtag = hashtag;
        }

        String getId() { return id; }
        String getAutor() { return autor; }
        String getContenido() { return contenido; }
        String getHashtag() { return hashtag; }
        long getLikes() { return likes.get(); }
        int getNumComentarios() { return comentarios.size(); }
        LocalDateTime getTimestamp() { return timestamp; }

        void comentar(Comentario c) {
            comentarios.add(c);
        }

        void compartir() {
            compartidos.incrementAndGet();
        }

        
        boolean registrarLike() {
            likes.incrementAndGet();
            long ahora = System.nanoTime();
            long inicio = inicioVentanaNanos.get();
            if (ahora - inicio > 60_000_000_000L && inicioVentanaNanos.compareAndSet(inicio, ahora)) {
                likesVentana.set(0); 
            }
            int enVentana = likesVentana.incrementAndGet();
            return enVentana > LIKES_VIRAL_POR_MINUTO && viral.compareAndSet(false, true);
        }
    }

    
    static class MetricasRedSocial {
        final AtomicLong postsCreados = new AtomicLong();
        final AtomicLong likesTotales = new AtomicLong();
        final AtomicLong comentarios = new AtomicLong();
        final AtomicLong shares = new AtomicLong();
        final AtomicLong seguimientos = new AtomicLong();
        final AtomicLong rateLimitActivado = new AtomicLong();
        final AtomicLong spamDetectado = new AtomicLong();
        final AtomicLong postsVirales = new AtomicLong();
        final AtomicInteger usuariosActivos = new AtomicInteger();
        final ScheduledExecutorService actualizadorMetricas = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Metricas");
            t.setDaemon(true);
            return t;
        });

        
        private static final int VENTANA = 61;
        private final long[][] historial = new long[VENTANA][4]; 
        private volatile long ticks = 0;
        private volatile double postsMin, likesSeg, comentariosMin, sharesMin;
        private volatile long ultimoLikes = 0;

        void iniciar() {
            actualizadorMetricas.scheduleAtFixedRate(this::muestrear, 1, 1, TimeUnit.SECONDS);
        }

        private void muestrear() {
            long t = ticks + 1;
            long[] ahora = {postsCreados.get(), likesTotales.get(), comentarios.get(), shares.get()};
            historial[(int) (t % VENTANA)] = ahora;
            long segs = Math.min(t, 60);
            long[] antes = t > 60 ? historial[(int) ((t - 60) % VENTANA)] : new long[4];
            postsMin = (ahora[0] - antes[0]) * 60.0 / segs;
            comentariosMin = (ahora[2] - antes[2]) * 60.0 / segs;
            sharesMin = (ahora[3] - antes[3]) * 60.0 / segs;
            likesSeg = (ahora[1] - ultimoLikes); 
            ultimoLikes = ahora[1];
            ticks = t;
        }
    }

    
    
    static class CacheLRU {
        private final Map<String, Post> mapa;
        private final ReentrantLock lock = new ReentrantLock();
        final LongAdder aciertos = new LongAdder();
        final LongAdder fallos = new LongAdder();

        CacheLRU(int maximo) {
            this.mapa = new LinkedHashMap<>(16, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, Post> eldest) {
                    return size() > maximo;
                }
            };
        }

        Post obtener(String id) {
            lock.lock(); 
            try {
                Post p = mapa.get(id);
                (p != null ? aciertos : fallos).increment();
                return p;
            } finally {
                lock.unlock();
            }
        }

        void guardar(Post p) {
            lock.lock();
            try {
                mapa.put(p.getId(), p);
            } finally {
                lock.unlock();
            }
        }

        double porcentajeAciertos() {
            long total = aciertos.sum() + fallos.sum();
            return total == 0 ? 0 : 100.0 * aciertos.sum() / total;
        }
    }

    
    static class BaseDatosSimulada {
        private final Semaphore poolConexiones = new Semaphore(POOL_BD, true); 
        private final Map<String, Post> datos = new ConcurrentHashMap<>();
        private final ExecutorService ejecutor = Executors.newVirtualThreadPerTaskExecutor();

        
        private <T> CompletableFuture<T> operar(Supplier<T> operacion) {
            return CompletableFuture.supplyAsync(() -> {
                try {
                    poolConexiones.acquire();
                    try {
                        Thread.sleep(LATENCIA_BD_MS);
                        return operacion.get();
                    } finally {
                        poolConexiones.release();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }, ejecutor);
        }

        CompletableFuture<Post> guardarPost(Post post) {
            return operar(() -> {
                datos.put(post.getId(), post);
                return post;
            });
        }

        CompletableFuture<Post> obtenerPost(String id) {
            return operar(() -> datos.get(id));
        }

        
        CompletableFuture<List<Post>> obtenerFeed(String usuario, Deque<String> idsFeed) {
            return operar(() -> {
                List<Post> posts = new ArrayList<>();
                for (String id : idsFeed) {
                    Post p = datos.get(id);
                    if (p != null) {
                        posts.add(p);
                    }
                    if (posts.size() >= 10) {
                        break;
                    }
                }
                return posts;
            });
        }

        int conexionesEnUso() {
            return POOL_BD - poolConexiones.availablePermits();
        }

        void cerrar() {
            ejecutor.shutdownNow();
        }
    }

    
    static class SistemaNotificaciones {
        private final BlockingQueue<Notificacion> colaNotificaciones = new LinkedBlockingQueue<>(50_000);
        private final ExecutorService procesadorNotificaciones;
        private final Map<String, Deque<Notificacion>> bandejas = new ConcurrentHashMap<>();
        final LongAdder procesadas = new LongAdder();
        final LongAdder descartadas = new LongAdder();

        SistemaNotificaciones(int hilos) {
            procesadorNotificaciones = Executors.newFixedThreadPool(hilos, r -> {
                Thread t = new Thread(r, "Notificaciones");
                t.setDaemon(true);
                return t;
            });
            for (int i = 0; i < hilos; i++) {
                procesadorNotificaciones.submit(this::procesar);
            }
        }

        private void procesar() {
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Notificacion n = colaNotificaciones.take();
                    Thread.sleep(2); 
                    Deque<Notificacion> bandeja = bandejas.computeIfAbsent(n.destinatario(),
                            k -> new ConcurrentLinkedDeque<>());
                    bandeja.addFirst(n);
                    while (bandeja.size() > 20) {
                        bandeja.pollLast();
                    }
                    procesadas.increment();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        
        void enviarNotificacion(String usuario, TipoNotificacion tipo, String origen) {
            if (!colaNotificaciones.offer(new Notificacion(usuario, tipo, origen))) {
                descartadas.increment();
            }
        }

        void enviarNotificacion(String usuario, TipoNotificacion tipo) {
            enviarNotificacion(usuario, tipo, "sistema");
        }

        List<Notificacion> obtenerNotificaciones(String usuario) {
            return new ArrayList<>(bandejas.getOrDefault(usuario, new ConcurrentLinkedDeque<>()));
        }

        int pendientes() {
            return colaNotificaciones.size();
        }

        void cerrar() {
            procesadorNotificaciones.shutdownNow();
        }
    }

    
    
    static class RateLimiter {
        private final Map<String, Deque<Long>> publicaciones = new ConcurrentHashMap<>();

        boolean permitirPublicacion(String usuario) {
            Deque<Long> marcas = publicaciones.computeIfAbsent(usuario, k -> new ArrayDeque<>());
            long ahora = System.nanoTime();
            synchronized (marcas) { 
                while (!marcas.isEmpty() && ahora - marcas.peekFirst() > 60_000_000_000L) {
                    marcas.pollFirst();
                }
                if (marcas.size() >= MAX_POSTS_POR_MINUTO) {
                    return false;
                }
                marcas.addLast(ahora);
                return true;
            }
        }
    }

    static class DetectorSpam {
        private final Map<String, Deque<Long>> acciones = new ConcurrentHashMap<>();
        private final Map<String, Long> bloqueadosHasta = new ConcurrentHashMap<>();

        boolean estaBloqueado(String usuario) {
            Long hasta = bloqueadosHasta.get(usuario);
            return hasta != null && System.nanoTime() < hasta;
        }

        
        boolean registrarAccion(String usuario) {
            Deque<Long> marcas = acciones.computeIfAbsent(usuario, k -> new ArrayDeque<>());
            long ahora = System.nanoTime();
            int enUnSegundo;
            synchronized (marcas) {
                marcas.addLast(ahora);
                while (!marcas.isEmpty() && ahora - marcas.peekFirst() > 1_000_000_000L) {
                    marcas.pollFirst();
                }
                enUnSegundo = marcas.size();
            }
            if (enUnSegundo > MAX_ACCIONES_POR_SEGUNDO) {
                bloqueadosHasta.put(usuario, ahora + 5_000_000_000L); 
                synchronized (marcas) {
                    marcas.clear();
                }
                return true;
            }
            return false;
        }
    }

    
    private static final MetricasRedSocial metricas = new MetricasRedSocial();
    private static final BaseDatosSimulada bd = new BaseDatosSimulada();
    private static final CacheLRU cache = new CacheLRU(TAMANO_CACHE);
    private static final SistemaNotificaciones notificaciones = new SistemaNotificaciones(4);
    private static final RateLimiter rateLimiter = new RateLimiter();
    private static final DetectorSpam detectorSpam = new DetectorSpam();

    private static final List<Usuario> usuarios = new ArrayList<>();
    private static final Map<String, Usuario> usuariosPorId = new ConcurrentHashMap<>();
    private static final Map<String, Set<String>> seguidores = new ConcurrentHashMap<>();
    private static final Map<String, Deque<String>> feeds = new ConcurrentHashMap<>();
    private static final Map<String, Post> posts = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedDeque<String> recientes = new ConcurrentLinkedDeque<>();
    private static final Map<String, LongAdder> trending = new ConcurrentHashMap<>();
    private static final Deque<String> ultimosEventos = new ArrayDeque<>();
    private static final AtomicLong idPost = new AtomicLong();
    
    private static final ConcurrentLinkedDeque<String> calientes = new ConcurrentLinkedDeque<>();

    private static void evento(String texto) {
        synchronized (ultimosEventos) {
            ultimosEventos.addLast("[" + LocalTime.now().format(HORA) + "] " + texto);
            while (ultimosEventos.size() > 4) {
                ultimosEventos.removeFirst();
            }
        }
    }

    
    
    private static void manejarViralizado(Post post) {
        metricas.postsVirales.incrementAndGet();
        
        for (String seguidor : seguidores.getOrDefault(post.getAutor(), Set.of())) {
            notificaciones.enviarNotificacion(seguidor, TipoNotificacion.POST_VIRAL, post.getAutor());
            
            Deque<String> feed = feeds.computeIfAbsent(seguidor, k -> new ConcurrentLinkedDeque<>());
            feed.remove(post.getId());
            feed.addFirst(post.getId());
        }
        
        trending.computeIfAbsent(post.getHashtag(), k -> new LongAdder()).add(50);
        
        cache.guardar(post);
        evento("🔥 Post viral alcanza " + LIKES_VIRAL_POR_MINUTO + " likes: " + post.getContenido());
    }

    private static void publicarPost(Usuario u) throws InterruptedException {
        if (!rateLimiter.permitirPublicacion(u.id())) {
            metricas.rateLimitActivado.incrementAndGet();
            evento("🚦 Rate limit activado para @" + u.id());
            Thread.sleep(500);
            return;
        }
        Thread.sleep(TipoActividad.PUBLICAR_POST.getTiempoMs()); 
        String hashtag = HASHTAGS[ThreadLocalRandom.current().nextInt(HASHTAGS.length)];
        Post post = new Post("P" + idPost.incrementAndGet(), u.id(), "Post sobre " + hashtag + " de " + u.id(), hashtag);
        bd.guardarPost(post).join(); 
        posts.put(post.getId(), post);
        recientes.addFirst(post.getId());
        while (recientes.size() > 200) {
            recientes.pollLast();
        }
        
        for (String seguidor : seguidores.getOrDefault(u.id(), Set.of())) {
            Deque<String> feed = feeds.computeIfAbsent(seguidor, k -> new ConcurrentLinkedDeque<>());
            feed.addFirst(post.getId());
            while (feed.size() > TAMANO_FEED) {
                feed.pollLast();
            }
        }
        trending.computeIfAbsent(hashtag, k -> new LongAdder()).increment();
        metricas.postsCreados.incrementAndGet();
        evento("📝 @" + u.id() + " publicó: \"" + post.getContenido() + "\"");
    }

    
    private static String elegirPost() {
        Object[] ids = recientes.toArray();
        if (ids.length == 0) {
            return null;
        }
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        if (azar.nextInt(100) < 40) { 
            Object[] tirando = calientes.toArray();
            if (tirando.length > 0) {
                return (String) tirando[azar.nextInt(tirando.length)];
            }
        }
        int limite = azar.nextInt(100) < 70 ? Math.min(20, ids.length) : ids.length;
        return (String) ids[azar.nextInt(limite)];
    }

    
    private static Post buscarPost(Usuario u, String id) {
        if (u.tipo() == TipoUsuario.BOT) {
            return posts.get(id);
        }
        Post p = cache.obtener(id);
        if (p == null) {
            p = bd.obtenerPost(id).join();
        }
        if (p != null && p.getLikes() > LIKES_POPULAR) {
            cache.guardar(p);
        }
        return p;
    }

    private static void darLike(Usuario u) throws InterruptedException {
        String id = elegirPost();
        if (id == null) {
            return;
        }
        if (u.tipo() != TipoUsuario.BOT) { 
            Thread.sleep(TipoActividad.DAR_LIKE.getTiempoMs());
        }
        Post post = buscarPost(u, id);
        if (post == null) {
            return;
        }
        boolean seHizoViral = post.registrarLike();
        metricas.likesTotales.incrementAndGet();
        if (post.getLikes() == 30) { 
            calientes.addLast(post.getId());
            if (calientes.size() > 10) {
                calientes.pollFirst();
            }
        }
        notificaciones.enviarNotificacion(post.getAutor(), TipoNotificacion.LIKE, u.id());
        if (seHizoViral) {
            manejarViralizado(post);
        }
    }

    private static void comentar(Usuario u) throws InterruptedException {
        String id = elegirPost();
        if (id == null) {
            return;
        }
        Thread.sleep(TipoActividad.COMENTAR.getTiempoMs()); 
        Post post = buscarPost(u, id);
        if (post != null) {
            post.comentar(new Comentario(u.id(), "¡Buen post!", LocalDateTime.now()));
            metricas.comentarios.incrementAndGet();
            notificaciones.enviarNotificacion(post.getAutor(), TipoNotificacion.COMENTARIO, u.id());
        }
    }

    private static void seguir(Usuario u) throws InterruptedException {
        Thread.sleep(TipoActividad.SEGUIR_USUARIO.getTiempoMs());
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        
        Usuario objetivo = azar.nextInt(100) < 40
                ? usuarios.get(azar.nextInt(NUM_INFLUENCERS))
                : usuarios.get(azar.nextInt(usuarios.size()));
        if (!objetivo.id().equals(u.id()) && seguidores.get(objetivo.id()).add(u.id())) {
            metricas.seguimientos.incrementAndGet();
            notificaciones.enviarNotificacion(objetivo.id(), TipoNotificacion.NUEVO_SEGUIDOR, u.id());
        }
    }

    private static void compartir(Usuario u) throws InterruptedException {
        String id = elegirPost();
        if (id == null) {
            return;
        }
        Thread.sleep(TipoActividad.COMPARTIR_POST.getTiempoMs());
        Post post = buscarPost(u, id);
        if (post != null) {
            post.compartir();
            metricas.shares.incrementAndGet();
            for (String seguidor : seguidores.getOrDefault(u.id(), Set.of())) {
                Deque<String> feed = feeds.computeIfAbsent(seguidor, k -> new ConcurrentLinkedDeque<>());
                feed.addFirst(post.getId());
                while (feed.size() > TAMANO_FEED) {
                    feed.pollLast();
                }
            }
            notificaciones.enviarNotificacion(post.getAutor(), TipoNotificacion.COMPARTIDO, u.id());
        }
    }

    
    private static TipoActividad elegirActividad(TipoUsuario tipo) {
        int p = ThreadLocalRandom.current().nextInt(100);
        return switch (tipo) {
            case NORMAL -> p < 3 ? TipoActividad.PUBLICAR_POST : p < 63 ? TipoActividad.DAR_LIKE
                    : p < 78 ? TipoActividad.COMENTAR : p < 90 ? TipoActividad.SEGUIR_USUARIO
                    : TipoActividad.COMPARTIR_POST;
            case INFLUENCER -> p < 10 ? TipoActividad.PUBLICAR_POST : p < 40 ? TipoActividad.DAR_LIKE
                    : p < 65 ? TipoActividad.COMENTAR : p < 72 ? TipoActividad.SEGUIR_USUARIO
                    : TipoActividad.COMPARTIR_POST;
            case BOT -> p < 55 ? TipoActividad.DAR_LIKE : p < 70 ? TipoActividad.SEGUIR_USUARIO
                    : TipoActividad.PUBLICAR_POST;
        };
    }

    
    private static long tiempoDePensar(TipoUsuario tipo) {
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        return switch (tipo) {
            case NORMAL -> azar.nextLong(2000, 8000);
            case INFLUENCER -> azar.nextLong(1000, 3000);
            case BOT -> azar.nextLong(20, 60);
        };
    }

    
    private static void vivir(Usuario u) {
        try {
            Thread.sleep(ThreadLocalRandom.current().nextLong(0, 5000)); 
            metricas.usuariosActivos.incrementAndGet();
            boolean avisado = false;
            while (corriendo) {
                if (detectorSpam.estaBloqueado(u.id())) {
                    Thread.sleep(1000);
                    continue;
                }
                TipoActividad actividad = elegirActividad(u.tipo());
                if (detectorSpam.registrarAccion(u.id())) {
                    metricas.spamDetectado.incrementAndGet();
                    
                    evento("🛡 Spam detectado: @" + u.id() + " bloqueado y avisados los moderadores");
                    continue; 
                }
                switch (actividad) {
                    case PUBLICAR_POST -> publicarPost(u);
                    case DAR_LIKE -> darLike(u);
                    case COMENTAR -> comentar(u);
                    case SEGUIR_USUARIO -> seguir(u);
                    case COMPARTIR_POST -> compartir(u);
                }
                Thread.sleep(tiempoDePensar(u.tipo()));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            metricas.usuariosActivos.decrementAndGet();
        }
    }

    
    private static String duracion(long segundos) {
        return String.format("%dh %02dm %02ds", segundos / 3600, segundos % 3600 / 60, segundos % 60);
    }

    private static void dashboard() {
        long uptime = (System.nanoTime() - INICIO) / 1_000_000_000L;
        Runtime rt = Runtime.getRuntime();
        double memoriaMB = (rt.totalMemory() - rt.freeMemory()) / (1024.0 * 1024.0);

        StringBuilder sb = new StringBuilder();
        sb.append("\n=== RED SOCIAL - DASHBOARD EN VIVO ===\n");
        sb.append("🕐 [").append(LocalTime.now().format(HORA)).append("] | Uptime: ").append(duracion(uptime)).append("\n\n");
        sb.append("📊 ACTIVIDAD EN TIEMPO REAL:\n┌─────────────────────────────────────┐\n");
        sb.append(String.format(Locale.US, "│ 👥 Usuarios activos:   %4d/%-6d  │%n", metricas.usuariosActivos.get(), NUM_USUARIOS));
        sb.append(String.format(Locale.US, "│ 📝 Posts/minuto:       %8.0f      │%n", metricas.postsMin));
        sb.append(String.format(Locale.US, "│ ❤️  Likes/segundo:      %8.0f      │%n", metricas.likesSeg));
        sb.append(String.format(Locale.US, "│ 💬 Comentarios/minuto: %8.0f      │%n", metricas.comentariosMin));
        sb.append(String.format(Locale.US, "│ 🔄 Shares/minuto:      %8.0f      │%n", metricas.sharesMin));
        sb.append("└─────────────────────────────────────┘\n\n💾 SISTEMA:\n┌─────────────────────────────────────┐\n");
        sb.append(String.format(Locale.US, "│ 🗄️  Pool BD:            %2d/%-2d en uso │%n", bd.conexionesEnUso(), POOL_BD));
        sb.append(String.format(Locale.US, "│ 🚀 Cache hits:         %7.1f%%      │%n", cache.porcentajeAciertos()));
        sb.append(String.format(Locale.US, "│ 📱 Notificaciones cola: %7d      │%n", notificaciones.pendientes()));
        sb.append(String.format(Locale.US, "│ 🧠 Memoria usada:      %7.0fMB      │%n", memoriaMB));
        sb.append(String.format(Locale.US, "│ ⚡ Threads virtuales:   %7d      │%n", metricas.usuariosActivos.get()));
        sb.append("└─────────────────────────────────────┘\n\n🔥 TRENDING NOW:\n");
        trending.entrySet().stream()
                .sorted(Comparator.comparingLong((Map.Entry<String, LongAdder> e) -> e.getValue().sum()).reversed())
                .limit(3)
                .forEach(e -> sb.append(String.format("%s (%d menciones)%n", e.getKey(), e.getValue().sum())));
        sb.append("\n📈 ÚLTIMOS EVENTOS:\n");
        synchronized (ultimosEventos) {
            ultimosEventos.forEach(l -> sb.append(l).append('\n'));
        }
        System.out.print(sb);
    }

    
    public static void main(String[] args) throws Exception {
        if (args.length > 0) {
            segundosSimulacion = Long.parseLong(args[0]);
        }

        
        for (int i = 0; i < NUM_USUARIOS; i++) {
            TipoUsuario tipo = i < NUM_INFLUENCERS ? TipoUsuario.INFLUENCER
                    : i < NUM_INFLUENCERS + NUM_BOTS ? TipoUsuario.BOT : TipoUsuario.NORMAL;
            String id = switch (tipo) {
                case INFLUENCER -> "Influencer_" + i;
                case BOT -> "SpamBot_" + i;
                case NORMAL -> "Usuario_" + i;
            };
            Usuario u = new Usuario(id, tipo);
            usuarios.add(u);
            usuariosPorId.put(id, u);
            seguidores.put(id, ConcurrentHashMap.newKeySet());
            feeds.put(id, new ConcurrentLinkedDeque<>());
        }

        
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        for (Usuario u : usuarios) {
            int n = azar.nextInt(5, 16);
            for (int k = 0; k < n; k++) {
                Usuario objetivo = azar.nextInt(100) < 40 ? usuarios.get(azar.nextInt(NUM_INFLUENCERS))
                        : usuarios.get(azar.nextInt(usuarios.size()));
                if (!objetivo.id().equals(u.id())) {
                    seguidores.get(objetivo.id()).add(u.id());
                }
            }
        }

        System.out.println("=== RED SOCIAL === " + NUM_USUARIOS + " usuarios (" + NUM_INFLUENCERS + " influencers, "
                + NUM_BOTS + " bots) durante " + segundosSimulacion + " s\n");

        metricas.iniciar();
        ScheduledExecutorService panel = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "Dashboard");
            t.setDaemon(true);
            return t;
        });
        panel.scheduleAtFixedRate(Problema6_RedSocial::dashboard, 5, 5, TimeUnit.SECONDS);

        
        ExecutorService hilosUsuarios = Executors.newVirtualThreadPerTaskExecutor();
        long inicioSim = System.nanoTime();
        for (Usuario u : usuarios) {
            hilosUsuarios.submit(() -> vivir(u));
        }

        Thread.sleep(segundosSimulacion * 1000);
        corriendo = false;
        hilosUsuarios.shutdownNow(); 
        hilosUsuarios.awaitTermination(5, TimeUnit.SECONDS);
        panel.shutdownNow();
        metricas.actualizadorMetricas.shutdownNow();
        double segundos = (System.nanoTime() - inicioSim) / 1_000_000_000.0;

        
        Post masPopular = posts.values().stream().max(Comparator.comparingLong(Post::getLikes)).orElse(null);
        long conSeguidores = seguidores.values().stream().mapToLong(Set::size).max().orElse(0);
        String influencerTop = seguidores.entrySet().stream()
                .max(Comparator.comparingInt(e -> e.getValue().size())).map(Map.Entry::getKey).orElse("-");
        System.out.println("\n--- RESUMEN FINAL ---");
        System.out.printf(Locale.US, "Duración: %.1fs%n", segundos);
        System.out.println("Posts creados: " + metricas.postsCreados);
        System.out.println("Likes totales: " + metricas.likesTotales);
        System.out.println("Comentarios: " + metricas.comentarios + " | Shares: " + metricas.shares
                + " | Nuevos seguimientos: " + metricas.seguimientos);
        System.out.println("Posts virales: " + metricas.postsVirales);
        System.out.println("Rate limit activado: " + metricas.rateLimitActivado + " veces");
        System.out.println("Spammers detectados: " + metricas.spamDetectado + " eventos");
        System.out.printf(Locale.US, "Cache hits: %.1f%% (%d aciertos / %d fallos)%n", cache.porcentajeAciertos(),
                cache.aciertos.sum(), cache.fallos.sum());
        System.out.println("Notificaciones procesadas: " + notificaciones.procesadas.sum()
                + " (descartadas: " + notificaciones.descartadas.sum() + ", pendientes: " + notificaciones.pendientes() + ")");
        if (masPopular != null) {
            System.out.println("Post más popular: \"" + masPopular.getContenido() + "\" con " + masPopular.getLikes()
                    + " likes y " + masPopular.getNumComentarios() + " comentarios");
        }
        System.out.println("Usuario con más seguidores: @" + influencerTop + " (" + conSeguidores + ")");
        System.out.println("Ejemplo de bandeja de @" + influencerTop + ": "
                + notificaciones.obtenerNotificaciones(influencerTop).size() + " notificaciones");

        
        String ejemplo = usuarios.get(NUM_USUARIOS - 1).id();
        List<Post> feed = bd.obtenerFeed(ejemplo, feeds.get(ejemplo)).join();
        System.out.println("Feed de @" + ejemplo + ": " + feed.size() + " posts recientes");

        notificaciones.cerrar();
        bd.cerrar();
    }
}
