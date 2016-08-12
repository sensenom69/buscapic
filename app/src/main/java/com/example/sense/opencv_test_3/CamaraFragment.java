package com.example.sense.opencv_test_3;

import android.Manifest;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
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

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sense on 11/08/2016.
 */
public class CamaraFragment extends Fragment implements CameraBridgeViewBase.CvCameraViewListener2 {
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

    // Used in Camera selection from menu (when implemented)
    //private boolean mIsJavaCamera = true;
    //private MenuItem mItemSwitchCamera = null;

    // These variables are used (at the moment) to fix camera orientation from 270degree to 0degree
    Mat mRgba;
    Mat mRgbaF;
    Mat mRgbaT;
    Mat mIntermediateMat;

    public CamaraFragment(){

    }


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(getActivity()) {
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

    /*
    public void MainActivity_show_camera() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }
    */
     //El Fragment ha sido creado
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    //Now, when the activity is created, display the OpenCV camera in the layout. show_camera.xml.
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater,container,savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_camera, container, false);
        Log.i(TAG, "called onCreate");
        //super.onCreate(savedInstanceState);

        getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);


        mOpenCvCameraView = (JavaCameraView) view.findViewById(R.id.show_camera_activity_java_surface_view);

        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
            mOpenCvCameraView.setMaxFrameSize(600, 650);
        }


        LocationManager mlocManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        mlocListener = new Localitzador();
        TextView textView = (TextView) view.findViewById(R.id.textCoordenades);
        //textView.setText("Esperant GPS");
        mlocListener.setVistes(textView);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return view;
        }
        mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, mlocListener);

        //imgCompass = (ImageView) findViewById(R.id.imgViewCompass);

        //bruixola
        brujula = new Brujula();
        sensorManager = (SensorManager) getActivity().getSystemService( getActivity().SENSOR_SERVICE);
        TextView textAngle = (TextView)view.findViewById(R.id.txtAngle);
        brujula.llansa(sensorManager,textAngle);


        //El boto flotant
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fab);
        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View view){

                    latitut = mlocListener.getLatitud();
                    longitud = mlocListener.getLongitud();
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
                    tareaRecollirAltimetria.setVistes((TextView) getActivity().findViewById(R.id.txtAltura),(TextView) getActivity().findViewById(R.id.txtNomPic));
                    tareaRecollirAltimetria.execute(latitut,longitud,latitudDesti,longitudDesti);
                    Toast.makeText(getActivity().getApplicationContext(), latitut+" "+longitud+" "+angle, Toast.LENGTH_LONG).show();

                }
            });
        }
        return view;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        sensorManager.unregisterListener(brujula);
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        // Se registra un listener para los sensores del accelerometer y el             magnetometer
        sensorManager.registerListener(brujula,brujula.getRotationmeter(),SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(brujula, brujula.getAccelerometer(), SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(brujula, brujula.getMagnetometer(), SensorManager.SENSOR_DELAY_UI);

        //esta part es de la vista
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, getActivity(), mLoaderCallback);
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
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        //En la RA el recuadre que dellimita el pic
        Point cantoDretaDalt;
        Point cantoEsquerreBaix;
        mRgba = inputFrame.rgba();
        Size sizeRgba = mRgba.size();
        Mat rgbaInnerWindow;

        int rows = (int) sizeRgba.height;
        int cols = (int) sizeRgba.width;

        int left = 0;
        int top = (rows/4)+25;

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
                cantoDretaDalt = new Point(70,height/2.6);
                cantoEsquerreBaix =  new Point(width-20,height-110);


                Imgproc.GaussianBlur(rgbaInnerWindow, imageBlurr, new Size(5,5), 45);
                Mat grayscaledMat = new Mat();
                Imgproc.cvtColor(rgbaInnerWindow, grayscaledMat, Imgproc.COLOR_BGR2GRAY, 4);
                Mat cannyOut = new Mat();
                Imgproc.Canny(grayscaledMat, cannyOut, 5, 25);
                List<MatOfPoint> contours = new ArrayList<>();
                Imgproc.findContours(cannyOut, contours, new Mat(), Imgproc.RETR_LIST,Imgproc.CHAIN_APPROX_NONE );
                List<Point> llistaMesAlts = new ArrayList<>();
                for(int i=0; i< contours.size();i++){
                    System.out.println(Imgproc.contourArea(contours.get(i)));
                    if (Imgproc.contourArea(contours.get(i)) > 0 ){
                        List<Point> listaPuntos = new ArrayList<>();
                        Converters.Mat_to_vector_Point(contours.get(i),listaPuntos);
                        int puntMesAlt = getPuntMesAlt(listaPuntos);
                        llistaMesAlts.add(listaPuntos.get(puntMesAlt));
                        Imgproc.rectangle(rgbaInnerWindow,new Point(listaPuntos.get(puntMesAlt).x+5,listaPuntos.get(puntMesAlt).y+5),new Point(listaPuntos.get(puntMesAlt).x,listaPuntos.get(puntMesAlt).y), new Scalar(255,0,0));
                    }
                }

                if(llistaMesAlts.size()>0) {
                    int puntMesAltTotal = getPuntMesAlt(llistaMesAlts);
                    llistaMesAlts.get(puntMesAltTotal);
                    Imgproc.rectangle(rgbaInnerWindow, new Point(llistaMesAlts.get(puntMesAltTotal).x + 5, llistaMesAlts.get(puntMesAltTotal).y + 5), new Point(llistaMesAlts.get(puntMesAltTotal).x, llistaMesAlts.get(puntMesAltTotal).y), new Scalar(0, 255, 0));
                    //if(temporitzarLlansament.voltesPasades(cantoDretaDalt,cantoEsquerreBaix,new Point(80,120)))

                    int progres = temporitzarLlansament.estaDins(cantoDretaDalt,cantoEsquerreBaix,llistaMesAlts.get(puntMesAltTotal));

                    if(progres>0) {
                        if (progres > 10) {
                            latitut = mlocListener.getLatitud();
                            longitud = mlocListener.getLongitud();
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
                            latitudDesti = getLatDesti(latitut, angle, distancia);
                            longitudDesti = getLongDesti(longitud, latitut, angle, distancia);
                            TareaRecollirAltimetria tareaRecollirAltimetria = new TareaRecollirAltimetria();
                            llistaPuntsRetornada.clear();
                            tareaRecollirAltimetria.setLlistaPunts(llistaPuntsRetornada);
                            tareaRecollirAltimetria.execute(latitut, longitud, latitudDesti, longitudDesti);
                        }
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
}
