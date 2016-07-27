package com.example.sense.opencv_test_3;

import org.opencv.core.Point;

/**
 * Created by sense on 27/07/2016.
 */
public class TemporitzarLlansament  {
    int contador;
    public TemporitzarLlansament(){
        contador = 0;
    }

    public int setContador(){
        contador = 0;
        return 0;
    }

    public int addContador(){
        contador++;
        return contador;
    }

    public int estaDins(Point dretaDalt, Point esquerreBaix, Point punt){
        if(punt.x < esquerreBaix.x && punt.x > dretaDalt.x && punt.y < esquerreBaix.y && punt.y > dretaDalt.y) {
            return addContador();
        }
        else {
            return setContador();
        }
    }

    public boolean voltesPasades(Point dretaDalt, Point esquerreBaix, Point punt){
        int contadorAux = estaDins(dretaDalt,esquerreBaix,punt);
        if(contadorAux>10){
            return true;
        }
        return false;
    }

    public int getContador(){
        return contador;
    }
}
