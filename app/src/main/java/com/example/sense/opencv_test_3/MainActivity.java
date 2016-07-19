package com.example.sense.opencv_test_3;

import android.content.Context;
import android.content.Loader;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Surface;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationRequest;

import org.opencv.android.JavaCameraView;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2{
    //Les dades per a tirar la linea
    private double latitut;
    private double longitud;
    private double angle;
    //La brujula
    private SensorManager sensorManager;
    private MyBrujula brujula;
    // Views donde se cargaran los elementos del XML
    private TextView txtAngle;
    private ImageView imgCompass;
    ////Fins aci la brujula
    // Used for logging success or failure messages
    private static final String TAG = "OCVSample::Activity";

    // Loads camera view of OpenCV for us to use. This lets us see using OpenCV
    private CameraBridgeViewBase mOpenCvCameraView;

    // Used in Camera selection from menu (when implemented)
    private boolean mIsJavaCamera = true;
    private MenuItem mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        //Now, lets call OpenCV manager to help our app communicate with android phone to make OpenCV work
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();//This variable acts as a bridge between camera and OpenCV library.
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };//Well, once OpenCV library is loaded, you may want to perform some actions. For example, displaying a success or failure message.

    public void MainActivity_show_camera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    /*
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }
    */
    //Now, when the activity is created, display the OpenCV camera in the layout. show_camera.xml.
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        brujula = new MyBrujula();
        brujula.llansa();

        sensorManager = brujula.getmSensorManager();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.show_camera);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mOpenCvCameraView.setMaxFrameSize(600, 3000);

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        final MyLocationListener mlocListener = new MyLocationListener();
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0,(LocationListener) mlocListener);
        TextView textView = (TextView) findViewById(R.id.view_text);
        textView.setText("Funcionant");
        imgCompass = (ImageView) findViewById(R.id.imgViewCompass);
        txtAngle = (TextView) findViewById(R.id.txtAngle);
        //El boto flotant
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                latitut = mlocListener.latitud;
                longitud = mlocListener.longitud;
                angle = brujula.getAngle();
                Toast.makeText(getApplicationContext(), latitut+" "+longitud+" "+angle, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener((SensorEventListener) brujula);
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Se registra un listener para los sensores del accelerometer y el             magnetometer
        sensorManager.registerListener(brujula, brujula.accelerometer, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(brujula, brujula.magnetometer, SensorManager.SENSOR_DELAY_UI);

        //esta part es de la vista
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    //Receive Image Data when the camera preview starts on your screen
    public void onCameraViewStarted(int width, int height) {

        //mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgba = new Mat(height, width, CvType.CV_8UC4);
        mRgbaF = new Mat(height, width, CvType.CV_8UC4);
        mRgbaT = new Mat(width, width, CvType.CV_8UC4);
    }

    //Destroy image data when you stop camera preview on your phone screen
    public void onCameraViewStopped() {
        mRgba.release();
    }

    //Now, this one is interesting! OpenCV orients the camera to left by 90 degrees.
    // So if the app is in portrait more, camera will be in -90 or 270 degrees orientation.
    // We fix that in the next and the most important function. There you go!
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        switch (mOpenCvCameraView.getDisplay().getRotation()) {
            case Surface.ROTATION_0: // Vertical portrait
                Core.transpose(mRgba, mRgbaT);
                Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
                Core.flip(mRgbaF, mRgba, 1);
                break;
            case Surface.ROTATION_90: // 90° anti-clockwise
                break;
            case Surface.ROTATION_180: // Vertical anti-portrait
                Core.transpose(mRgba, mRgbaT);
                Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
                Core.flip(mRgbaF, mRgba, 0);
                break;
            case Surface.ROTATION_270: // 90° clockwise
                Imgproc.resize(mRgba, mRgbaF, mRgbaF.size(), 0,0, 0);
                Core.flip(mRgbaF, mRgba, -1);
                break;
            default:
        }
        return mRgba;
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

    public class MyLocationListener implements LocationListener {
        public double latitud = 0;
        public double longitud = 0;
        public double bearing = 0;

        @Override
        public void onLocationChanged(Location loc) {

            // Este mŽtodo se ejecuta cada vez que el GPS recibe nuevas coordenadas
            // debido a la detecci—n de un cambio de ubicacion
            latitud = loc.getLatitude();
            longitud =loc.getLongitude();
            bearing = loc.getBearing();
            String Text = "Mi ubicaci—n actual es: " + "\n Lat = "
                    + loc.getLatitude() + "\n Long = " + loc.getLongitude()+""
                    +"\n bearing = "+loc.getBearing();
            TextView text = (TextView) findViewById(R.id.view_text);
            text.setText(Text);

            Toast.makeText(getApplicationContext(),Text,Toast.LENGTH_LONG);

        }


        @Override
        public void onProviderDisabled(String provider) {
            // Este mŽtodo se ejecuta cuando el GPS es desactivado
            TextView text = (TextView) findViewById(R.id.view_text);
            text.setText("GPS Desactivado");
            Toast.makeText(getApplicationContext(),"GPS Desactivado",Toast.LENGTH_LONG);
        }

        @Override
        public void onProviderEnabled(String provider) {
            // Este mŽtodo se ejecuta cuando el GPS es activado
            TextView text = (TextView) findViewById(R.id.view_text);
            text.setText("GPS Activado");
            Toast.makeText(getApplicationContext(),"GPS Activado",Toast.LENGTH_LONG);
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

    public class MyBrujula implements SensorEventListener{


        // guarda el angulo (grado) actual del compass
        private float currentDegree = 0f;

        // El sensor manager del dispositivo
        private SensorManager mSensorManager;
        // Los dos sensores que son necesarios porque TYPE_ORINETATION esta deprecated
        private Sensor accelerometer;
        private Sensor magnetometer;

        // Los angulos del movimiento de la flecha que señala al norte
        float degree;
        // Guarda el valor del azimut
        float azimut;
        // Guarda los valores que cambián con las variaciones del sensor TYPE_ACCELEROMETER
        float[] mGravity;
        // Guarda los valores que cambián con las variaciones del sensor TYPE_MAGNETIC_FIELD
        float[] mGeomagnetic;
        //proves
        float[] matriu = new float[9];

        public void llansa(){
            // Se inicializa los sensores del dispositivo android
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            mGravity = null;
            mGeomagnetic = null;
        }

        public SensorManager getmSensorManager(){
            return mSensorManager;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            // Se comprueba que tipo de sensor está activo en cada momento
            if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                    mGravity = lowPass( event.values.clone(), mGravity );
            if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                    mGeomagnetic = lowPass( event.values.clone(), mGeomagnetic );



            if ((mGravity != null) && (mGeomagnetic != null)) {
                float RotationMatrix[] = new float[16];
                float RI[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(RI, null, mGravity, mGeomagnetic);

                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(RI, orientation);
                    //azimut = orientation[0] * (180 / (float) Math.PI);
                    azimut = (float) Math.toDegrees(orientation[0]);
                }
            }
            degree = azimut;
            txtAngle.setText("Angle: " + Math.round((degree)) + " degrees");
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
            imgCompass.startAnimation(ra);
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

}




