package Hilos;

import lombok.Data;

import java.util.Random;
@Data
public class Chapita {
    private int id;
    private int casillasAvanzadas;

    public Chapita(int id){
        this.id = id;
        casillasAvanzadas = 0;
    }

    public void avanzar(Random r){
        this.casillasAvanzadas = casillasAvanzadas + r.nextInt(20);
    }
}
