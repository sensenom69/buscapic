package com.example.sense.opencv_test_3;

/**
 * Created by sense on 20/07/2016.
 */
public class Punt {
    private double latitud;
    private double longitud;
    private double altura;


    public Punt(double latitud, double longitud, double altura){
        this.latitud = latitud;
        this.longitud = longitud;
        this.altura = altura;
    }

    public double getLatitud(){
        return latitud;
    }

    public double getLongitud(){
        return longitud;
    }

    public double getAltura(){
        return altura;
    }
}
