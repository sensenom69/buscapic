package com.example.sense.opencv_test_3;

import java.util.ArrayList;

/**
 * Created by sense on 20/07/2016.
 */
public class LlistaPunts {
    private ArrayList<Punt> llistaPunts;
    private int posPuntMesAlt;
    public LlistaPunts(ArrayList<Punt> llistaPunts){
        this.llistaPunts = llistaPunts;
        posPuntMesAlt = llistaPunts.size();
    }

    public ArrayList<Punt> getLlistaPunts(){
        return llistaPunts;
    }

    public void addPunt(Punt punt){
        llistaPunts.add(punt);
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
}
