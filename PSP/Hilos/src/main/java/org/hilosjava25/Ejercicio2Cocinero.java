package org.hilosjava25;

public class Ejercicio2Cocinero {
    static void main() {
        Thread cocinero = new Thread(
                () -> {
                    System.out.println("Cocinero preparando el plato - " + Thread.currentThread().getState());
                    try {
                        for (int i = 1; i <= 10; i++) {
                            System.out.println("El cocinero lleva cocinando " + i + " segundos");
                            Thread.sleep(1000);
                            System.out.println("El cocinero sigue cocinando - " + Thread.currentThread().getState());
                        }

                    }catch (InterruptedException e){
                        System.out.println("El cocinero ha cancleado el plato - " + Thread.currentThread().getState());
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
        );

        System.out.println("El cocinero Está listo para cocinar - " + cocinero.getState());
        cocinero.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        cocinero.interrupt();
        System.out.println("El cocinero ha terminado de cocinar - " + cocinero.getState());
    }
}
