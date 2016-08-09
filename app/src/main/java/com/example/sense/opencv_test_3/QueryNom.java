package com.example.sense.opencv_test_3;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sense on 28/07/2016.
 */
public class QueryNom extends AsyncTask<String, Void,String> {
    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    private final String LOG_TAG = QueryNom.class.getSimpleName();
    private TextView nom;

    public void setVista(TextView nom){
        this.nom = nom;
    }

    @Override
    protected String doInBackground(String... params) {
        //Verifiquem tenim parametres
        if (params.length == 0) {
            return null;
        }
        // These two need to be declared outside the try/catch
        // so that they can be closed in the finally block.
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // La resposta anira aci
        String resposta = null;
        try{
            String QUERY_BASE_URL = "https://www.ign.es/ign/layoutIn/geodesiaVerticesGeodesicosBusqueda.do?tipoBusqueda=5&formato=UTM&huso="+params[2]+"&tipoRango=cuadrado&Xa="+params[0]+"&Ya="+params[1]+"&radio=2&regente_coor=";

            Uri builtUri = Uri.parse(QUERY_BASE_URL).buildUpon().build();

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
            resposta = buffer.toString();
            resposta = resposta.substring(resposta.indexOf("filaNormal"),resposta.indexOf("<!-- fin -->"));
            resposta = resposta.substring(resposta.indexOf("_blank\">")+"_blank\">".length(),resposta.indexOf("</a>"));
            Log.v(LOG_TAG, "Resposta string: " + resposta);
        }catch (Exception e){
            Log.e(LOG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attemping
            // to parse it.
            return null;
        }finally {
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

        return resposta;
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        if(result!=null)
            nom.setText(result);
    }
}
