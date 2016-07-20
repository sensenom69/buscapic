package com.example.sense.opencv_test_3;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by sense on 20/07/2016.
 */
public class TareaRecollirAltimetria extends AsyncTask<Double,Double,ArrayList<Punt>> {
    private final String LOG_TAG = TareaRecollirAltimetria.class.getSimpleName();
    private ArrayList<Punt> llistaPunts = new ArrayList<>();

    public void setLlistaPunts(ArrayList<Punt> llistaPunts){
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
            return getDades(respostaJsonStr);
        }catch (JSONException e){
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Punt> result) {
        if (result != null) {
            llistaPunts = result;
        }
    }

    private ArrayList<Punt> getDades(String resposta) throws JSONException{
        //ArrayList<Punt> llistaPunts = new ArrayList<>();
        JSONObject alturesJson = new JSONObject(resposta);
        JSONArray alturesArray = alturesJson.getJSONArray("results");

        for(int i = 0; i<alturesArray.length();i++){
            //El objecte del punt
            JSONObject dadesPunt  = alturesArray.getJSONObject(i);
            double altura = dadesPunt.getDouble("elevation");
            //TODO aci casca

            JSONObject dadesLocalitzacioPunt  = dadesPunt.getJSONObject("location");

            double latitud = dadesLocalitzacioPunt.getDouble("lat");

            double longitud = dadesLocalitzacioPunt.getDouble("lng");
            llistaPunts.add(new Punt(latitud, longitud, altura));
        }
        return null;
    }

    public ArrayList<Punt> getLlistaPunts(){
        return llistaPunts;
    }
}
