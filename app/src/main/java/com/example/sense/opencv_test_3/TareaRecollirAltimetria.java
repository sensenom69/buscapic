package com.example.sense.opencv_test_3;

import android.app.Activity;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sense on 09/08/2016.
 */
public class TareaRecollirAltimetria extends AsyncTask<Double,Integer,ArrayList<Punt>> {
    private final String LOG_TAG = TareaRecollirAltimetria.class.getSimpleName();
    private ArrayList<Punt> llistaPunts = new ArrayList<>();
    private TextView textViewAltura;
    private TextView textViewNom;
    private int NUM_MIDES = 500;
    private Activity activity;

    public void setLlistaPunts(ArrayList<Punt> llistaPunts){
        this.llistaPunts.clear();
        this.llistaPunts = llistaPunts;
    }

    public void setContext(Activity activity){
        this.activity = activity;
    }

    public void setVistes(TextView textViewAltura, TextView textViewNom){
        this.textViewAltura = textViewAltura;
        this.textViewNom = textViewNom;
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
                    .appendQueryParameter(SAMPLES_PARAM, Integer.toString(NUM_MIDES))
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

        LlistaPunts llistaPuntsIntern = new LlistaPunts(llistaPunts);
        Punt puntMesAltVisible = llistaPuntsIntern.getPuntMesAltVisible();
        int posPuntMesAlt = llistaPuntsIntern.getPosPuntMesAltVisible();
        Double distanciaPeu = llistaPuntsIntern.getDistanciaPeu(NUM_MIDES,30000);
        if(puntMesAltVisible != null) {
            TextView textViewAltura = (TextView) activity.findViewById(R.id.txtAltura);
            textViewAltura.setText(Math.round(puntMesAltVisible.getAltura()) + "m");
            TextView textDistanciaPeu = (TextView) activity.findViewById(R.id.textDistanciaPeu);
            textDistanciaPeu.setText(Math.round(distanciaPeu*10)/10 + "m");
            TextView textDistancia = (TextView) activity.findViewById(R.id.textDistancia);
            textDistancia.setText((30000/NUM_MIDES)*posPuntMesAlt + "m");
            //El grafic
            //ArrayList<Entry> yVals = carregaDadesGrafic(llistaPunts);
            ArrayList<String> xVals = new ArrayList<String>();
            for(int i=0; i< llistaPunts.size() && i<posPuntMesAlt;i++)
                xVals.add((30000/NUM_MIDES)*i+"");
            LineChart chart = (LineChart) activity.findViewById(R.id.chart);
            chart.setVisibility(View.VISIBLE);
            //TODO: fer sols fins elpunt mes alt?
            List<Entry> entries = carregaDadesGrafic(llistaPunts,posPuntMesAlt);
            LineDataSet dataSet = new LineDataSet(entries, "Altura");
            dataSet.setColor(Color.BLUE);

            dataSet.setDrawCircles(false);
            chart.setBackgroundColor(Color.TRANSPARENT);
            chart.setDrawGridBackground(false);
            chart.getXAxis().setTextColor(Color.WHITE);
            chart.getAxisLeft().setTextColor(Color.WHITE);
            chart.getAxisRight().setTextColor(Color.WHITE);
            dataSet.setLineWidth(2.5f);
            ArrayList<LineDataSet> dataSets = new ArrayList<LineDataSet>();
            dataSets.add(dataSet);
            LineData lineData = new LineData();
            lineData.addDataSet(dataSet);
            chart.setData(lineData);
            chart.invalidate();


            // create a data object with the datasets

            LineData data = new LineData(xVals, dataSets);
            chart.setData(data);

            ///
            QueryNom queryNom = new QueryNom();
            queryNom.setVista(textViewNom);
            queryNom.execute(puntMesAltVisible.getLongitudUTM() + "", puntMesAltVisible.getLatitudUTM() + "", puntMesAltVisible.getHuso() + "");
        }
    }

    private ArrayList<Entry> carregaDadesGrafic(ArrayList<Punt> punts, int posPuntMesAlt) {
        ArrayList<Entry> entries = new ArrayList<Entry>();
        for (int i = 0;i< punts.size() && i< posPuntMesAlt;i++) {
            // turn your data into Entry objects
            entries.add(new Entry((int)llistaPunts.get(i).getAltura(), i));
        }
        return entries;
    }


    private ArrayList<Punt> getDades(String resposta) throws JSONException{
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
