package org.Problemas;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.locks.LockSupport;
import java.util.concurrent.locks.ReentrantLock;

public class Problema2_BancoVirtual {

    private static final double SALDO_INICIAL = 10_000.00;
    private static final int CLIENTES = 50;
    private static final int OPERACIONES_POR_CLIENTE = 10;
    private static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    
    interface CuentaBancaria {
        boolean retirar(double cantidad);
        void ingresar(double cantidad);
        double consultarSaldo();
        List<String> obtenerHistorial();
    }

    



    private static void validacionAntifraude() {
        LockSupport.parkNanos(2_000_000); 
    }

    private static double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }

    private static String registro(String tipo, double cantidad, boolean ok, double saldo) {
        return String.format(Locale.US, "[%s] %-7s %8.2f€ %-6s saldo=%.2f€",
                LocalTime.now().format(HORA), tipo, cantidad, ok ? "OK" : "FALLO", saldo);
    }

    
    
    
    static class CuentaConReentrantLock implements CuentaBancaria {
        private final ReentrantLock lock = new ReentrantLock();
        private double saldo;
        private final List<String> historial = new ArrayList<>();

        CuentaConReentrantLock(double saldoInicial) {
            this.saldo = saldoInicial;
        }

        @Override
        public boolean retirar(double cantidad) {
            lock.lock();
            try {
                validacionAntifraude();
                boolean ok = saldo >= cantidad;
                if (ok) {
                    saldo = redondear(saldo - cantidad);
                }
                historial.add(registro("RETIRO", cantidad, ok, saldo));
                return ok;
            } finally {
                lock.unlock(); 
            }
        }

        @Override
        public void ingresar(double cantidad) {
            lock.lock();
            try {
                validacionAntifraude();
                saldo = redondear(saldo + cantidad);
                historial.add(registro("INGRESO", cantidad, true, saldo));
            } finally {
                lock.unlock();
            }
        }

        @Override
        public double consultarSaldo() {
            lock.lock();
            try {
                return saldo;
            } finally {
                lock.unlock();
            }
        }

        @Override
        public List<String> obtenerHistorial() {
            lock.lock();
            try {
                return new ArrayList<>(historial);
            } finally {
                lock.unlock();
            }
        }
    }

    
    
    
    static class CuentaSynchronized implements CuentaBancaria {
        private double saldo;
        private final List<String> historial = new ArrayList<>();

        CuentaSynchronized(double saldoInicial) {
            this.saldo = saldoInicial;
        }

        @Override
        public synchronized boolean retirar(double cantidad) {
            validacionAntifraude();
            boolean ok = saldo >= cantidad;
            if (ok) {
                saldo = redondear(saldo - cantidad);
            }
            historial.add(registro("RETIRO", cantidad, ok, saldo));
            return ok;
        }

        @Override
        public synchronized void ingresar(double cantidad) {
            validacionAntifraude();
            saldo = redondear(saldo + cantidad);
            historial.add(registro("INGRESO", cantidad, true, saldo));
        }

        @Override
        public synchronized double consultarSaldo() {
            return saldo;
        }

        @Override
        public synchronized List<String> obtenerHistorial() {
            return new ArrayList<>(historial);
        }
    }

    static class CuentaVolatile implements CuentaBancaria {
        private volatile double saldo;
        
        
        private final List<String> historial = Collections.synchronizedList(new ArrayList<>());

        CuentaVolatile(double saldoInicial) {
            this.saldo = saldoInicial;
        }

        @Override
        public boolean retirar(double cantidad) {
            double actual = saldo;                
            boolean ok = actual >= cantidad;      
            if (ok) {
                validacionAntifraude();           
                saldo = redondear(actual - cantidad); 
            }
            historial.add(registro("RETIRO", cantidad, ok, saldo));
            return ok;
        }

        @Override
        public void ingresar(double cantidad) {
            double actual = saldo;                
            validacionAntifraude();               
            saldo = redondear(actual + cantidad); 
            historial.add(registro("INGRESO", cantidad, true, saldo));
        }

        @Override
        public double consultarSaldo() {
            return saldo;
        }

        @Override
        public List<String> obtenerHistorial() {
            return new ArrayList<>(historial);
        }
    }

    private static class Resultado {
        final AtomicInteger exitosas = new AtomicInteger();
        final AtomicInteger fallidas = new AtomicInteger();
        final DoubleAdder ingresado = new DoubleAdder(); 
        final DoubleAdder retirado = new DoubleAdder();  
    }

    private static void ejecutarPrueba(String titulo, CuentaBancaria cuenta, boolean esperaCorrecta)
            throws InterruptedException {
        System.out.println("--- " + titulo + " ---");
        Resultado res = new Resultado();

        List<Thread> clientes = new ArrayList<>();
        for (int c = 1; c <= CLIENTES; c++) {
            clientes.add(new Thread(() -> {
                ThreadLocalRandom azar = ThreadLocalRandom.current();
                try {
                    for (int i = 0; i < OPERACIONES_POR_CLIENTE; i++) {
                        Thread.sleep(azar.nextInt(100, 301)); 
                        if (azar.nextInt(100) < 60) {         
                            double cantidad = redondear(azar.nextDouble(1, 100));
                            if (cuenta.retirar(cantidad)) {
                                res.exitosas.incrementAndGet();
                                res.retirado.add(cantidad);
                            } else {
                                res.fallidas.incrementAndGet();
                            }
                        } else {                              
                            double cantidad = redondear(azar.nextDouble(1, 50));
                            cuenta.ingresar(cantidad);
                            res.exitosas.incrementAndGet();
                            res.ingresado.add(cantidad);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "Cliente-" + c));
        }

        long inicio = System.nanoTime();
        clientes.forEach(Thread::start);
        for (Thread t : clientes) {
            t.join();
        }
        double segundos = (System.nanoTime() - inicio) / 1_000_000_000.0;

        int total = CLIENTES * OPERACIONES_POR_CLIENTE;
        double saldoFinal = cuenta.consultarSaldo();
        
        double saldoEsperado = SALDO_INICIAL + res.ingresado.sum() - res.retirado.sum();
        double diferencia = redondear(saldoFinal - saldoEsperado);
        boolean coherente = Math.abs(diferencia) < 0.01 && saldoFinal >= 0;

        System.out.printf(Locale.US, "Saldo final: %.2f€ (esperado según operaciones: %.2f€)%n",
                saldoFinal, saldoEsperado);
        System.out.println("Operaciones exitosas: " + res.exitosas + "/" + total);
        System.out.println("Operaciones fallidas: " + res.fallidas + " (fondos insuficientes)");
        System.out.println("Registros en el historial: " + cuenta.obtenerHistorial().size() + "/" + total);
        System.out.printf(Locale.US, "Tiempo total: %.1fs %s%n", segundos, coherente ? "✅" : "❌ INCORRECTO");
        if (!coherente) {
            System.out.printf(Locale.US, "Operaciones perdidas detectadas! Descuadre de %.2f€%s%n",
                    diferencia, saldoFinal < 0 ? " y SALDO NEGATIVO" : "");
        }
        if (esperaCorrecta && !coherente) {
            System.out.println("¡Atención! Esta implementación debería ser correcta.");
        }
        System.out.println();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== BANCO VIRTUAL ===");
        System.out.printf(Locale.US, "Saldo inicial: %.2f€%n", SALDO_INICIAL);
        System.out.println(CLIENTES + " clientes realizando " + (CLIENTES * OPERACIONES_POR_CLIENTE)
                + " operaciones totales...\n");

        ejecutarPrueba("CON REENTRANTLOCK", new CuentaConReentrantLock(SALDO_INICIAL), true);
        ejecutarPrueba("CON SYNCHRONIZED", new CuentaSynchronized(SALDO_INICIAL), true);
        ejecutarPrueba("CON VOLATILE", new CuentaVolatile(SALDO_INICIAL), false);
    }
}
