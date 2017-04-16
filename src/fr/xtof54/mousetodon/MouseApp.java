package fr.xtof54.mousetodon;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MouseApp extends Activity
{

    String instanceDomain = "octodon.social";
    SharedPreferences pref;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        pref = getSharedPreferences("MastodonApiExample", MODE_PRIVATE);
    }

    public void detconnect(View v) {
        login();
    }

    private String getQuery(List<Pair<String, String>> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;

        for (Pair<String, String> pair : params)
        {
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(pair.first, "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(pair.second, "UTF-8"));
        }

        return result.toString();
    }

    private void login() {
        String clientId = pref.getString(String.format("client_id_for_%s", instanceDomain), null);
        String clientSecret = pref.getString(String.format("client_secret_for_%s", instanceDomain), null);
        Log.d("LoginTask", "client id saved: " + clientId);
        Log.d("LoginTask", "client secret saved: " + clientSecret);
        if(clientId == null || clientSecret == null) {
            // Client registration
            Log.d("LoginTask", "Going to fetch new client id/secret");
            List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
            params.add(new Pair<String, String>("client_name", "Mousetodon"));
            params.add(new Pair<String, String>("redirect_uris", "http://localhost"));
            Object[] args = {instanceDomain, params, 0};
            new ConnectTask().execute(args);
        }
    }

    void endlogin(String s) {
        try {
            JSONObject json = new JSONObject(s);
            Log.d("afterLogin",json.toString());
            String clientId = json.getString("client_id");
            String clientSecret = json.getString("client_secret");
            SharedPreferences.Editor edit = pref.edit();
            edit.putString(String.format("client_id_for_%s", instanceDomain), clientId);
            edit.putString(String.format("client_secret_for_%s", instanceDomain), clientSecret);
            edit.commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    class ConnectTask extends AsyncTask<Object, Void, String> {
        int jid=-1;

        @Override
        protected String doInBackground(Object... args) {
            String instanceDomain=(String)args[0];
            List<Pair<String, String>> params = (List<Pair<String, String>>)args[1];
            jid=(Integer)args[2];
            String res=null;
            try {
                URL url = new URL(String.format("https://%s/api/v1/apps", instanceDomain));
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestMethod("POST");

                OutputStream os = urlConnection.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(getQuery(params));
                writer.flush();
                writer.close();
                os.close();

                urlConnection.connect();

                BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;

                while((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }

                br.close();
                res=sb.toString();
                urlConnection.disconnect();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return res;
        }

                /*
                // Create URL
                URL mastEndpoint = new URL("https://octodon.social/");

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) githubEndpoint.openConnection();
                */  

        @Override
        protected void onPostExecute(String response) {
            switch(jid) {
                case 0: endlogin(response);
            }
        }
    }
}
