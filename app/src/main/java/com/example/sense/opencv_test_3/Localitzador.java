package com.example.sense.opencv_test_3;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Created by sense on 11/08/2016.
 */
public class Localitzador implements LocationListener {
    private double latitud = 0;
    private double longitud = 0;
    private double altitud = 0;
    private TextView textCoordenades;


    public void setVistes(TextView textCoordenades){
        this.textCoordenades = textCoordenades;
    }

    public double getLatitud(){return latitud;}

    public double getLongitud(){return longitud;}

    public double getAltitud(){return altitud;}

    @Override
    public void onLocationChanged(Location loc) {

        // Este mŽtodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
        // debido a la detecci—n de un cambio de ubicacion

        latitud = loc.getLatitude();
        longitud = loc.getLongitude();
        altitud = loc.getAltitude();
        String Text = "Lat: "+loc.getLatitude() + "\n Long : " + loc.getLongitude();
        textCoordenades.setText(Text);

        //Toast.makeText(getApplicationContext(),Text,Toast.LENGTH_LONG);

    }


    @Override
    public void onProviderDisabled(String provider) {
        // Este mŽtodo se ejecuta cuando el GPS es desactivado
        textCoordenades.setText("GPS Desactivado");
        //Toast.makeText(getApplicationContext(),"GPS Desactivado",Toast.LENGTH_LONG);
    }

    @Override
    public void onProviderEnabled(String provider) {
        // Este mŽtodo se ejecuta cuando el GPS es activado
        textCoordenades.setText("GPS Activado");
        //Toast.makeText(getApplicationContext(),"GPS Activado",Toast.LENGTH_LONG);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Este mŽtodo se ejecuta cada vez que se detecta un cambio en el
        // status del proveedor de localizaci—n (GPS)
        // Los diferentes Status son:
        // OUT_OF_SERVICE -> Si el proveedor esta fuera de servicio
        // TEMPORARILY_UNAVAILABLE -> Temp˜ralmente no disponible pero se
        // espera que este disponible en breve
        // AVAILABLE -> Disponible
    }
}
