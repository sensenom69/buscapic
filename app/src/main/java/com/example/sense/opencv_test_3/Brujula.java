package com.example.sense.opencv_test_3;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.TextView;

/**
 * Created by sense on 10/08/2016.
 */
public class Brujula implements SensorEventListener {
    private TextView textAngle;

    // guarda el angulo (grado) actual del compass
    private float currentDegree = 0f;

    // El sensor manager del dispositivo
    private SensorManager sensorManager;
    // Los dos sensores que son necesarios porque TYPE_ORINETATION esta deprecated
    private Sensor accelerometer;
    private Sensor magnetometer;
    private Sensor rotationmeter;

    // Los angulos del movimiento de la flecha que señala al norte
    float degree;
    // Guarda el valor del azimut
    float azimut;
    // Guarda los valores que cambián con las variaciones del sensor TYPE_ACCELEROMETER
    float[] mGravity;
    // Guarda los valores que cambián con las variaciones del sensor TYPE_MAGNETIC_FIELD
    float[] mGeomagnetic;
    //proves
    float[] rotationVector;
    float pich;
    float avgRead[] = { 0, 0, 0, 0, 0, 0, 0, 0, 0 };


    public void llansa(SensorManager mSensorManager, TextView txtAngle){
        textAngle = txtAngle;
        // Se inicializa los sensores del dispositivo android
        //sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager = mSensorManager;
        rotationmeter = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        mGravity = null;
        mGeomagnetic = null;
        rotationVector = null;
    }



    public Sensor getAccelerometer(){ return accelerometer;}

    public Sensor getMagnetometer(){return magnetometer;}

    public Sensor getRotationmeter(){return rotationmeter;}

    public SensorManager getSensorManager(){
        return sensorManager;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        // Se comprueba que tipo de sensor está activo en cada momento
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
            mGravity = lowPass( event.values.clone(), mGravity );
        if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
            mGeomagnetic = lowPass( event.values.clone(), mGeomagnetic );
        if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
            rotationVector = event.values.clone();



        if ((mGravity != null) && (mGeomagnetic != null)) {
            float rotationV[] = new float[16];
            float RI[] = new float[9];
            float I[] = new float[9];
            boolean success = SensorManager.getRotationMatrix(RI, I, mGravity, mGeomagnetic);

            if (success) {
                float orientation[] = new float[3];
                //SensorManager.getOrientation(RI, orientation);
                float[] outR = new float[9];
                SensorManager.remapCoordinateSystem(RI, SensorManager.AXIS_X,SensorManager.AXIS_Z, outR);
                SensorManager.getOrientation(outR, orientation);

                azimut = orientation[0] * (180 / (float) Math.PI);
                pich = orientation[1] * 180/ (float) Math.PI;
                //azimut = (float) Math.toDegrees(orientation[0]);

                avgRead[0] = avgRead[1];
                avgRead[1] = avgRead[2];
                avgRead[2] = avgRead[3];
                avgRead[3] = avgRead[4];
                avgRead[4] = avgRead[5];
                avgRead[5] = avgRead[6];
                avgRead[6] = avgRead[7];
                avgRead[7] = avgRead[8];
                avgRead[8] = orientation[0]; // orientation contains: azimuth, pitch and roll
                azimut = (avgRead[0] + avgRead[1] + avgRead[2] + avgRead[3]
                        + avgRead[4] + avgRead[5] + avgRead[6] + avgRead[7] + avgRead[8]) / 9;


                azimut = azimut * 360 / (2 * 3.14159f);
                if (azimut < 0 && azimut > -180)
                    azimut += 360;

            }


        }
        textAngle.setText("Angle: " + azimut);
        degree = azimut;

        // se crea la animacion de la rottacion (se revierte el giro en grados, negativo)

        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);
        // el tiempo durante el cual la animación se llevará a cabo
        ra.setDuration(1000);
        // establecer la animación después del final de la estado de reserva
        ra.setFillAfter(true);
        // Inicio de la animacion
        //imgCompass.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public double getAngle(){
        return degree;
    }

    /*
    * time smoothing constant for low-pass filter
    * 0 ≤ alpha ≤ 1 ; a smaller value basically means more smoothing
    * See: http://en.wikipedia.org/wiki/Low-pass_filter#Discrete-time_realization
    */
    static final float ALPHA = 0.1f;

    /**
     * @see ://en.wikipedia.org/wiki/Low-pass_filter#Algorithmic_implementation
     * @see ://developer.android.com/reference/android/hardware/SensorEvent.html#values
     */
    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}

