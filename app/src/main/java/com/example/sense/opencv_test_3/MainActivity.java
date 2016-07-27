package com.example.sense.opencv_test_3;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

//TODO: per que quan llance varies vegades no canvia laltura?

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2 {
    //Per al gps i el temporitzador
    MyLocationListener mlocListener;
    //El temporitzador
    private TemporitzarLlansament temporitzarLlansament =  new TemporitzarLlansament();
    //En la RA el punt mes alt dins la visió.
    private Point puntMesAlt = new Point(0,0);
    //En la RA el recuadre que dellimita el pic
    private Point cantoDretaDalt;
    private Point cantoEsquerreBaix;

    //Les dades per a tirar la linea
    private double latitut;
    private double longitud;
    private double angle;
    //el desti en km
    private double latitudDesti;
    private double longitudDesti;
    private double distancia;
    private double altitud;
    private ArrayList<Punt> llistaPuntsRetornada = new ArrayList<>();
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
    Mat mIntermediateMat;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        //Now, lets call OpenCV manager to help our app communicate with android phone to make OpenCV work
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();//This variable acts as a bridge between camera and OpenCV library.
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
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


        //bruixola
        brujula = new MyBrujula();
        brujula.llansa();
        sensorManager = brujula.getmSensorManager();
        //////



        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.show_camera);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.show_camera_activity_java_surface_view);

        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);

        mOpenCvCameraView.setCvCameraViewListener(this);

        mOpenCvCameraView.setMaxFrameSize(600, 650);

        LocationManager mlocManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new MyLocationListener();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, (LocationListener) mlocListener);
        TextView textView = (TextView) findViewById(R.id.view_text);
        textView.setText("Esperant GPS");
        //imgCompass = (ImageView) findViewById(R.id.imgViewCompass);
        txtAngle = (TextView) findViewById(R.id.txtAngle);
        //El boto flotant
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                latitut = mlocListener.latitud;
                longitud = mlocListener.longitud;
                angle = brujula.getAngle();
                //Laboratori
                //latitut = 39.070622;
                //longitud = -0.269588;
                //angle = 177.5;//moduver desde laboratori
                //Casa
                latitut = 39.068727;
                longitud = -0.291360;
                //angle = 162;//moduver desde casa
                //angle = 179;//penyalba
                //angle=50;//Creus
                distancia = 30;
                latitudDesti = getLatDesti(latitut,angle,distancia);
                longitudDesti = getLongDesti(longitud,latitut,angle,distancia);
                TareaRecollirAltimetria tareaRecollirAltimetria = new TareaRecollirAltimetria();
                llistaPuntsRetornada.clear();
                tareaRecollirAltimetria.setLlistaPunts(llistaPuntsRetornada);
                tareaRecollirAltimetria.execute(latitut,longitud,latitudDesti,longitudDesti);
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
        sensorManager.registerListener(brujula,brujula.rotationmeter,SensorManager.SENSOR_DELAY_UI);
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
        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
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
        Size sizeRgba = mRgba.size();
        Mat rgbaInnerWindow;

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = 0;
        int top = rows/4;

        int width = cols * 3 / 8;
        int height = rows * 3 / 7;

        switch (mOpenCvCameraView.getDisplay().getRotation()) {
            case Surface.ROTATION_0: // Vertical portrait
                /*
                Core.transpose(mRgba, mRgbaT);
                Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);
                Core.flip(mRgbaF, mRgba, 1);
                */
                rgbaInnerWindow = mRgba.submat(top, top + height, left, left + width);
                Mat imageBlurr = mRgba.submat(top, top + height, left, left + width);

                //cantoDretaDalt = new Point(70,height/2.5);
                //cantoEsquerreBaix =  new Point(width-20,height-110);
                cantoDretaDalt = new Point(70,height/2.5);
                cantoEsquerreBaix =  new Point(width-20,height-110);


                Imgproc.GaussianBlur(rgbaInnerWindow, imageBlurr, new Size(5,5), 45);
                Mat grayscaledMat = new Mat();
                Imgproc.cvtColor(rgbaInnerWindow, grayscaledMat, Imgproc.COLOR_BGR2GRAY, 4);
                Mat cannyOut = new Mat();
                Imgproc.Canny(grayscaledMat, cannyOut, 5, 25);
                List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
                Imgproc.findContours(cannyOut, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_NONE );
                List<Point> llistaMesAlts = new ArrayList<>();
                for(int i=0; i< contours.size();i++){
                    System.out.println(Imgproc.contourArea(contours.get(i)));
                    if (Imgproc.contourArea(contours.get(i)) > 0 ){
                        List<Point> listaPuntos = new ArrayList<Point>();
                        Converters.Mat_to_vector_Point(contours.get(i),listaPuntos);
                        int puntMesAlt = getPuntMesAlt(listaPuntos);
                        llistaMesAlts.add(listaPuntos.get(puntMesAlt));
                        Imgproc.rectangle(rgbaInnerWindow,new Point(listaPuntos.get(puntMesAlt).x+5,listaPuntos.get(puntMesAlt).y+5),new Point(listaPuntos.get(puntMesAlt).x,listaPuntos.get(puntMesAlt).y), new Scalar(255,0,0));
                    }
                }

                if(llistaMesAlts.size()>0) {
                    int puntMesAltTotal = getPuntMesAlt(llistaMesAlts);
                    puntMesAlt = llistaMesAlts.get(puntMesAltTotal);
                    Imgproc.rectangle(rgbaInnerWindow, new Point(llistaMesAlts.get(puntMesAltTotal).x + 5, llistaMesAlts.get(puntMesAltTotal).y + 5), new Point(llistaMesAlts.get(puntMesAltTotal).x, llistaMesAlts.get(puntMesAltTotal).y), new Scalar(0, 255, 0));
                    //if(temporitzarLlansament.voltesPasades(cantoDretaDalt,cantoEsquerreBaix,new Point(80,120)))
                    if(temporitzarLlansament.estaDins(cantoDretaDalt,cantoEsquerreBaix,llistaMesAlts.get(puntMesAltTotal))>10){
                        latitut = mlocListener.latitud;
                        longitud = mlocListener.longitud;
                        angle = brujula.getAngle();
                        //Laboratori
                        //latitut = 39.070622;
                        //longitud = -0.269588;
                        //angle = 177.5;//moduver desde laboratori
                        //Casa
                        latitut = 39.068727;
                        longitud = -0.291360;
                        //angle = 162;//moduver desde casa
                        //angle = 179;//penyalba
                        //angle=50;//Creus
                        distancia = 30;
                        latitudDesti = getLatDesti(latitut,angle,distancia);
                        longitudDesti = getLongDesti(longitud,latitut,angle,distancia);
                        TareaRecollirAltimetria tareaRecollirAltimetria = new TareaRecollirAltimetria();
                        llistaPuntsRetornada.clear();
                        tareaRecollirAltimetria.setLlistaPunts(llistaPuntsRetornada);
                        tareaRecollirAltimetria.execute(latitut,longitud,latitudDesti,longitudDesti);
                    }

                }

                Imgproc.rectangle(rgbaInnerWindow, cantoEsquerreBaix,cantoDretaDalt, new Scalar(255,0,0));


                Core.transpose(mRgba, mRgbaT);
                Imgproc.resize(mRgbaT, mRgbaF, mRgbaF.size(), 0,0, 0);

                Core.flip(mRgbaF, mRgba, 1);
                imageBlurr.release();
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



    public int getPuntMesAlt(List<Point> llistaPunts){
        int primerPunt = 0;
        double auxY=100000;
        for(int i=0;i<llistaPunts.size();i++){
            if(llistaPunts.get(i).x < auxY){
                auxY = llistaPunts.get(i).x;
                primerPunt = i;
            }
        }
        return primerPunt;
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

    public double getLatDesti(double lat, double graus, double distancia){

        double latitudRadi =Math.toRadians(lat);
        double grausRadians = Math.toRadians(graus);
        int radiTerra = 6371;

        return  Math.toDegrees(Math.asin(Math.sin(latitudRadi)*Math.cos(distancia/radiTerra) + Math.cos(latitudRadi)*Math.sin(distancia/radiTerra)*Math.cos(grausRadians)));

    }

    public double getLongDesti(double longi, double lat, double graus, double distancia){
        /*
        double longitudRadi =longi;
        double grausRadians = graus;
        double coseno = Math.cos(grausRadians);
        double dist = (360*distancia)/(2*3.141516*6371000);
        double resultatEnGrau = longitudRadi + coseno * dist;
        */
        int radiTerra = 6371;
        double lati =  getLatDesti(lat,graus,distancia);
        double longitud =Math.toRadians(longi) + Math.atan2(Math.sin(Math.toRadians(angle))*Math.sin(distancia/radiTerra)*Math.cos(Math.toRadians(lat)), Math.cos(distancia/radiTerra)-Math.sin(Math.toRadians(lat))*Math.sin(Math.toRadians(lati)));
        longitud = (Math.toDegrees(longitud)+540)%360-180;
        return longitud;
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
            longitud = loc.getLongitude();
            altitud = loc.getAltitude();
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


        public void llansa(){
            // Se inicializa los sensores del dispositivo android
            mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
            rotationmeter = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
            accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

            mGravity = null;
            mGeomagnetic = null;
            rotationVector = null;
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
                txtAngle.setText("Angle: " + azimut);

            }

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

    public class TareaRecollirAltimetria extends AsyncTask<Double,Double,ArrayList<Punt>> {
        private final String LOG_TAG = TareaRecollirAltimetria.class.getSimpleName();
        private ArrayList<Punt> llistaPunts = new ArrayList<>();

        public void setLlistaPunts(ArrayList<Punt> llistaPunts){
            this.llistaPunts.clear();
            this.llistaPunts = llistaPunts;
        }
        @Override
        protected ArrayList<Punt> doInBackground(Double... params) {
            //Verifiquem tenim parametres
            if (params.length == 0) {
                return null;
            }

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // La resposta anira aci
            String respostaJsonStr = null;

            int numMides = 400;

            try {
                // Construim la URI per a la demanda de altimetria

                final String QUERY_BASE_URL =
                        "https://maps.googleapis.com/maps/api/elevation/json?";
                final String QUERY_PARAM = "path";
                final String SAMPLES_PARAM = "samples";
                final String KEY_PARAM = "key";
                //TODO: montar la query amb els parametres que arriben

                Uri builtUri = Uri.parse(QUERY_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, Double.toString(params[0])+","+Double.toString(params[1])+"|"+Double.toString(params[2])+","+Double.toString(params[3]))
                        .appendQueryParameter(SAMPLES_PARAM, Integer.toString(numMides))
                        .appendQueryParameter(KEY_PARAM, "AIzaSyDMbRA1zZeqZav72o3KINxncj3A8xEb3F8")
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                respostaJsonStr = buffer.toString();
                Log.v(LOG_TAG, "Resposta string: " + respostaJsonStr);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            try{
                ArrayList<Punt> retorno = getDades(respostaJsonStr);
                return retorno;
            }catch (JSONException e){
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Punt> result) {
            super.onPostExecute(result);

            LlistaPunts llistaPunts = new LlistaPunts(llistaPuntsRetornada);
            //llistaPunts.calculaPuntMesAlt();
            //Punt puntMesAlt = llistaPunts.getPuntMesAlt();
            Punt puntMesAltVisible = llistaPunts.getPuntMesAltVisible();
            TextView text = (TextView) findViewById(R.id.txtAltura);

            text.setText(Math.round(puntMesAltVisible.getAltura())+"m");
            Toast.makeText(getApplicationContext(),"GPS Activado",Toast.LENGTH_LONG);
        }

        private ArrayList<Punt> getDades(String resposta) throws JSONException{
            //ArrayList<Punt> llistaPunts = new ArrayList<>();
            JSONObject alturesJson = new JSONObject(resposta);
            JSONArray alturesArray = alturesJson.getJSONArray("results");

            for(int i = 0; i<alturesArray.length();i++){
                //El objecte del punt
                JSONObject dadesPunt  = alturesArray.getJSONObject(i);
                double altura = dadesPunt.getDouble("elevation");

                JSONObject dadesLocalitzacioPunt  = dadesPunt.getJSONObject("location");

                double latitud = dadesLocalitzacioPunt.getDouble("lat");

                double longitud = dadesLocalitzacioPunt.getDouble("lng");
                llistaPunts.add(new Punt(latitud, longitud, altura));
            }
            return null;
        }
    }
}




