package com.example.sense.opencv_test_3;

import java.util.ArrayList;

/**
 * Created by sense on 20/07/2016.
 */
public class LlistaPunts {
    private ArrayList<Punt> llistaPunts;
    private int posPuntMesAlt;
    private int posPuntMesAltVisible;

    public LlistaPunts(ArrayList<Punt> llistaPunts){
        this.llistaPunts = llistaPunts;
        posPuntMesAlt = llistaPunts.size();
    }

    public void resetLlistaPunts(){
        llistaPunts.clear();
        posPuntMesAltVisible = 1000000;
    }

    public ArrayList<Punt> getLlistaPunts(){
        return llistaPunts;
    }

    public void addPunt(Punt punt){
        llistaPunts.add(punt);
    }

    public int getPosPuntMesAlt(){
        return posPuntMesAlt;
    }

    public Punt getPuntMesAlt(){
        return llistaPunts.get(posPuntMesAlt);
    }

    public int calculaPuntMesAlt(){
        int pos=0;
        double alturaMax=0;
        for(int i=0; i< llistaPunts.size() && i < posPuntMesAlt; i++){
            if (llistaPunts.get(i).getAltura()>alturaMax) {
                alturaMax = llistaPunts.get(i).getAltura();
                pos = i;
            }
        }
        posPuntMesAlt = pos;
        return pos;
    }

    private double getDistanciaPunt(Punt orige, Punt desti){
        double radiTerra = 6371;//en kilómetros
        double incrementLat = Math.toRadians(desti.getLatitud() - orige.getLatitud());
        double incrementLng = Math.toRadians(desti.getLongitud() - orige.getLongitud());
        double sinIncrementLat = Math.sin(incrementLat / 2);
        double sinIncrementLng = Math.sin(incrementLng / 2);
        double va1 = Math.pow(sinIncrementLat, 2) + Math.pow(sinIncrementLng, 2) * Math.cos(Math.toRadians(orige.getLatitud())) * Math.cos(Math.toRadians(desti.getLatitud()));
        double va2 = 2 * Math.atan2(Math.sqrt(va1), Math.sqrt(1 - va1));
        return (radiTerra * va2)*1000;

    }

    private double getAngleVisio(Punt orige,Punt desti){
        //li reste la altura del usuari
        double altura = desti.getAltura()-orige.getAltura();
        double distancia = getDistanciaPunt(orige,desti);
        return Math.toDegrees(Math.atan(altura/distancia));
    }

    //Calcule el punt ems alt visible de forma recursiva
    public int calculaPuntMesAltVisible(int pos){
        double angle =  getAngleVisio(llistaPunts.get(0),llistaPunts.get(pos));
        double alturaPuntOrige = llistaPunts.get(0).getAltura()+1.7;
        for(int i= pos-1; i > 0 ;i--){
            double distancia = getDistanciaPunt(llistaPunts.get(0),llistaPunts.get(i));
            double alturaVisual = distancia*(1/Math.tan(Math.toRadians(90-angle)))+alturaPuntOrige;
            double alturaPunt = llistaPunts.get(i).getAltura();
            if(alturaPunt > alturaVisual){
                pos = calculaPuntMesAltVisible(i);
                return pos;
            }
        }
        return pos;
    }

    public int getPosPuntMesAltVisible(){
        int pos = calculaPuntMesAlt();
        pos = calculaPuntMesAltVisible(pos);
        posPuntMesAltVisible = pos;
        return pos;
    }

    public Punt getPuntMesAltVisible(){
        return llistaPunts.get(getPosPuntMesAltVisible());
    }
}
