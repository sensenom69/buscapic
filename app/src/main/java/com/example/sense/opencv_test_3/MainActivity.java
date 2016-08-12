package com.example.sense.opencv_test_3;

import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.CameraBridgeViewBase;

import java.util.ArrayList;

//TODO: fer un progressBar
//TODO: llevar el show_Camera de la v21
//TODO: llevaqr el cartell del nom quan es llansa una nova query
//TODO: quan  es fa una query de nom , preparar el que no existixca el nom
//TODO: manejar el error quan no pilla conexio internet o falla la pagina de google

public class MainActivity extends AppCompatActivity {
    //Per al gps i el temporitzador
    Localitzador mlocListener;
    //El temporitzador
    private TemporitzarLlansament temporitzarLlansament =  new TemporitzarLlansament();
    //En la RA el punt mes alt dins la visió.
    //private Point puntMesAlt = new Point(0,0);


    //Les dades per a tirar la linea
    private double latitut;
    private double longitud;
    private double angle;
    //el desti en km
    private double latitudDesti;
    private double longitudDesti;
    private double distancia;
    //private double altitud;
    private ArrayList<Punt> llistaPuntsRetornada = new ArrayList<>();
    //La brujula
    private SensorManager sensorManager;
    private Brujula brujula;



    // Views donde se cargaran los elementos del XML

   // private ImageView imgCompass;
    ////Fins aci la brujula
    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;


    /*
    public void MainActivity_show_camera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    */
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_camera);

       CamaraFragment camaraFragment = (CamaraFragment)getSupportFragmentManager().findFragmentById(R.id.CamaraFragment) ;
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);
        /*
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
    }






    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}




