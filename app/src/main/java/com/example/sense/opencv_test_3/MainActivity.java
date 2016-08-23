package com.example.sense.opencv_test_3;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

//TODO: fer un progressBar
//TODO: llevar el show_Camera de la v21
//TODO: llevaqr el cartell del nom quan es llansa una nova query
//TODO: quan  es fa una query de nom , preparar el que no existixca el nom
//TODO: manejar el error quan no pilla conexio internet o falla la pagina de google

public class MainActivity extends AppCompatActivity {

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_camera);

    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();

    }

    @Override
    public void onStop()
    {
        super.onStop();

    }

    public void onDestroy() {
        super.onDestroy();

    }

}




