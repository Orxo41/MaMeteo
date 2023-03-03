package com.example.projetandroid;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.BreakIterator;

public class MainActivity extends AppCompatActivity {

    Button b1 = null;
    EditText text = null;
    TextView tResultat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        text = (EditText) findViewById(R.id.editVille);
        tResultat = (TextView) findViewById(R.id.tResultat);
    }

    public void go(View v){
        String ville = text.getText().toString();
        RequestTask rt = new RequestTask();
        rt.execute(ville);
    }

    private class RequestTask extends AsyncTask<String, Void, String> {
        // Le corps de la tâche asynchrone (exécuté en tâche de fond)
        //  lance la requète
        protected String doInBackground(String... ville) {
            String response = requete(ville[0]);
            return response;
        }
        private String requete(String ville) {
            String response = "";
            try {
                HttpURLConnection connection = null;
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q="+
                        URLEncoder.encode(ville,"utf8")+",fr&units=metric&lang=fr&appid=ea3fd47b79737d51d2e5684dcd45ed17");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                InputStreamReader inputStreamReader = new
                        InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String ligne = bufferedReader.readLine() ;
                while (ligne!= null){
                    response+=ligne;
                    ligne = bufferedReader.readLine();
                }
            } catch (UnsupportedEncodingException e) {
                response = "problème d'encodage";
            } catch (MalformedURLException e) {
                response = "problème d'URL ";
            } catch (IOException e) {
                response = "problème de connexion ";
            } catch (Exception e) {
                e.printStackTrace();
            }
            return response;
        }
        private String decodeJSON(JSONObject jso) throws Exception {
            String response = "";
            int jsocod = jso.getInt("cod");
            if (jsocod == 200) { // ...
                JSONObject jsocoord = jso.getJSONObject("coord");
                JSONObject jsomain = jso.getJSONObject("main");
                JSONArray jsoweather = jso.getJSONArray("weather");
                response = "\n Voici le temps à " + jso.getString("name");
                response += "(lon=" + jsocoord.getString("lon");
                response += ",lat=" + jsocoord.getString("lat") + ")";;
                response +="\n\n \t Température = " + jsomain.getString("temp");
                response += "\n\n \t Le temps = ";
                for (int i = 0; i < jsoweather.length(); i++)
                    response += jsoweather.getJSONObject(i).getString("description")+ " ";
            } else { // ...
                response ="\n Code erreur retourné par le serveur :";
                response += "\n\n \t Code = " + jsocod;
                response += "\n\n \t Message : " + jso.getString("message");
            }
            return response;
        }
        // Méthode appelée lorsque la tâche de fond sera terminée
        //  Affiche le résultat
        protected void onPostExecute(String result) {
            JSONObject toDecode = null;
            try {
                toDecode = new JSONObject(result);
                tResultat.setText(decodeJSON(toDecode));
            } catch (Exception e) {
                tResultat.setText("error parsing JSON");
            }
        }
    }


}