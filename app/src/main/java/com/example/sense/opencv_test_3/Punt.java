package com.example.sense.opencv_test_3;

import java.math.BigDecimal;

/**
 * Created by sense on 20/07/2016.
 */
public class Punt {
    private double latitud;
    private double longitud;
    private double altura;
    private double distancia;
    private double latitudUTM;
    private double longitudUTM;
    private int huso;


    public Punt(double latitud, double longitud, double altura){
        this.latitud = latitud;
        this.longitud = longitud;
        this.altura = altura;
        transformaUTM();
    }

    public double getLatitud(){
        return latitud;
    }

    public double getLongitud(){
        return longitud;
    }

    public double getLatitudUTM() { return latitudUTM;}

    public double getLongitudUTM(){ return longitudUTM;}

    public int getHuso(){ return huso;}

    public double getAltura(){
        return altura;
    }

    public double getDistancia(){return distancia;}

    public void setDistancia(double distancia){this.distancia = distancia;}

    public void transformaUTM(){

        BigDecimal a = new BigDecimal("6378137.0");
        BigDecimal b = new BigDecimal("6356752.314");

        BigDecimal e1 = a.pow(2);
        BigDecimal e2 = b.pow(2);
        BigDecimal e3= e1.subtract(e2);
        BigDecimal e4= a.subtract(b);

        double exp=Math.pow(Math.sqrt(e3.doubleValue())/b.doubleValue(),2);
        double c=Math.pow(a.doubleValue(),2)/b.doubleValue();

        double longitudRadianes = Math.toRadians(longitud);
        double latitudRadianes = Math.toRadians(latitud);
        huso=(int)Math.floor((longitud/6)+31);
        int meridianoHuso=huso*6-183;

        double lambda=longitudRadianes-Math.toRadians(meridianoHuso);
        double A=Math.cos(latitudRadianes)*Math.sin(lambda);

        BigDecimal e6= new BigDecimal(A);
        BigDecimal uno = new BigDecimal("1");
        BigDecimal e7= uno.subtract(e6);
        BigDecimal e8= uno.add(e6);
        double aux= e8.doubleValue()/e7.doubleValue();
        double xi=0.5*Math.log(aux);

        double ni=(c/(1+exp*Math.pow(Math.pow(Math.cos(latitudRadianes),2),0.5))*0.9996);
        double zeta=(exp/2)*Math.pow(xi,2)*Math.pow(Math.cos(latitudRadianes),2);
        double eta=Math.atan(Math.tan(latitudRadianes)/Math.cos(lambda))-latitudRadianes;
        double a1=Math.sin(2*latitudRadianes);
        double a2=a1*Math.pow(Math.cos(latitudRadianes),2);
        double j2=latitudRadianes+(a1/2);
        double j4=((3*j2)+a2)/4;
        double j6=(5*j4+a2*Math.pow(Math.cos(latitudRadianes),2))/3;
        double alfa=(0.75)*exp;
        double beta=(1.6666666)*Math.pow(alfa,2);
        double gamma=(1.2962962)*Math.pow(alfa,3);
        double bfi=0.9996*c*(latitudRadianes-(alfa*j2)+(beta*j4)-(gamma*j6));

        longitudUTM=(xi*ni*(1+zeta/3)*1.003+500000);//el 1.003 es per a ajustar el UTM
        latitudUTM=(eta*ni*1.0009*(1+zeta)+bfi);//el 1.0009 es per ajustar el UTM
    }
}
