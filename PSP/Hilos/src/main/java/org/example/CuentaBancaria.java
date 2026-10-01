package org.example;

public class CuentaBancaria {
    private int saldo = 0;
    private int saldoSync = 0;

    public void depositar(int cantidad) {
        saldo += cantidad;
    }

    public void despoitar2(int cantidad) {
        synchronized (this) {
            saldo += cantidad;
        }
    }

    public synchronized void depositarSync(int cantidad) {
        saldoSync += cantidad;
    }

    public int getSaldo() {
        return saldo;
    }

    public int getSaldoSync() {
        return saldoSync;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public static void main(String[] args) {
        CuentaBancaria cuenta = new CuentaBancaria();
        for (int i = 0; i < 100; i++) {
            Thread hilo = new Thread(
                    () -> {
                        cuenta.depositar(1);
                    }
            );
            hilo.start();
        }
        System.out.println("Saldo final sin sincronización: " + cuenta.getSaldo());

        for (int i = 0; i < 100; i++) {
            Thread hilo = new Thread(
                    () -> {
                        cuenta.depositarSync(1);
                    }
            );
            hilo.start();
        }
        try {
            Thread.sleep(1000); // Esperar a que todos los hilos terminen
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Saldo final con sincronización: " + cuenta.getSaldoSync());
        cuenta.setSaldo(0);

        for (int i = 0; i < 100; i++) {
            Thread hilo = new Thread(
                    () -> {
                        cuenta.despoitar2(1);
                    }
            );
            hilo.start();

        }

try {
            Thread.sleep(1000); // Esperar a que todos los hilos terminen
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Saldo final con sincronización bloque synchronized: " + cuenta.getSaldo());

    }
}
