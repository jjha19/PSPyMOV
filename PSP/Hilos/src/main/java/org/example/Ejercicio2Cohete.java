package org.example;

public class Ejercicio2Cohete {
    public  static void main(String[] args) {
        Thread cuentaRegresiva = new Thread(
                () -> {
                    try {
                        System.out.println("Cuenta regresiva para el despegue del cohete:");
                        for (int i = 10; i >= 0; i--) {
                            System.out.println(i);
                            Thread.sleep(1000);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
        );
        System.out.println("Preparando el lanzamiento del cohete...");
        cuentaRegresiva.start();
        try {
            cuentaRegresiva.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("¡Despegue del cohete!");
    }
}
