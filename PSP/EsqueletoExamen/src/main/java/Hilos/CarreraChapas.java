package Hilos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class  CarreraChapas {
    static void main() {
        Random rd = new Random();
        int cantidadJugadores = 5;
        int numeroMeta = 100;

        System.out.println("Carrera de chapas\n La carrera comienza con " + cantidadJugadores + " chapas");
        BlockingQueue colaMensajes = new LinkedBlockingQueue();
        AtomicBoolean carreraFinalizada = new AtomicBoolean(false);

        CyclicBarrier barrier = new CyclicBarrier(cantidadJugadores, () -> {
            System.out.println("Todos han avanzado");
            try {
                colaMensajes.put("finRonda");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        Thread presentador = new Thread(() -> {
            List<Chapita> chapas = new ArrayList();
            while (!carreraFinalizada.get()) {
                try {
                    chapas.add((Chapita) colaMensajes.take());
                } catch (ClassCastException | InterruptedException e) {
                    chapas.stream().forEach((chapita) -> {
                        System.out.println("La chapita " + chapita.getId() + " va en la casilla " + chapita.getCasillasAvanzadas());
                        if (chapita.getCasillasAvanzadas() > numeroMeta) {
                            carreraFinalizada.set(true);
                        }
                    });
                }
            }
            chapas.stream().forEach(chapita -> {
                System.out.println("La chapita " + chapita.getId() + " ha terminado en la casilla " + chapita.getCasillasAvanzadas());
            });
            Thread.currentThread().interrupt();
        });
        presentador.start();

        for (int i = 0; i < cantidadJugadores; i++) {
            int numJugador = i;
            Thread jugador = new Thread(() -> {
                Chapita chapa = new Chapita(numJugador);
                while (!carreraFinalizada.get()) {
                    chapa.avanzar(rd);
                    try {
                        colaMensajes.put(chapa);
                        barrier.await();
                    } catch (InterruptedException | BrokenBarrierException e) {
                        throw new RuntimeException(e);
                    }
                }
                Thread.currentThread().interrupt();
            });

            jugador.start();
        }
    }

}
