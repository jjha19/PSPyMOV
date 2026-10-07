package org.example;

import java.util.concurrent.ArrayBlockingQueue;

public class Ejercicio6BlockingQueue {
    private ArrayBlockingQueue<Integer> queue;
    public Ejercicio6BlockingQueue(int size) {
        this.queue =  new ArrayBlockingQueue<>(size);
    }

    public void put(Integer item) {
        try {
            queue.put(item);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public int take(){
        try {
            return queue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    public static void main(String[] args) {
        Ejercicio6BlockingQueue buffer = new Ejercicio6BlockingQueue(10);
        for (int i = 0; i < 3; i++) {
            int idProductor = i;
            Thread productor = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    buffer.put(idProductor);
                    System.out.println("Producido: " + idProductor);
                }
            });
            productor.start();
        }

        for (int i = 0; i < 3; i++) {
            Thread consumidor = new Thread(() -> {
                for (int j = 0; j < 5; j++) {
                    int item = buffer.take();
                    System.out.println("Consumido: " + item);
                }
            });
            consumidor.start();
        }
    }
}
