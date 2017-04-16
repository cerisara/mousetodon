package fr.xtof54.mousetodon;

import java.util.ArrayList;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import android.util.Pair;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;

public class Connect {
    String domain;

    public Connect(String instance) {
        domain=instance;
    }

    public void registerApp(NextAction next) {
        Log.d("Connect","register app");
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("client_name", "Mousetodon"));
        params.add(new Pair<String, String>("redirect_uris", "urn:ietf:wg:oauth:2.0:oob"));
        params.add(new Pair<String, String>("scopes", "read write follow"));
        String surl = String.format("https://%s/api/v1/apps", domain);
        Object[] args = {surl, params, next};
        new ConnectTask().execute(args);
    }

    public void userLogin(String clientid, String secret, String email, String pwd, NextAction next) {
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("client_id", clientid));
        params.add(new Pair<String, String>("client_secret", secret));
        params.add(new Pair<String, String>("grant_type", "password"));
        params.add(new Pair<String, String>("username", email));
        params.add(new Pair<String, String>("password", pwd));
        String surl = String.format("https://%s/oauth/token", domain);
        Object[] args = {surl, params, next};
        new ConnectTask().execute(args);
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


    class ConnectTask extends AsyncTask<Object, Void, String> {
        NextAction next=null;
        @Override
        protected String doInBackground(Object... args) {
            String surl=(String)args[0];
            List<Pair<String, String>> params = (List<Pair<String, String>>)args[1];
            next=(NextAction)args[2];
            String res=null;
            try {
                URL url = new URL(surl);
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

        @Override
        protected void onPostExecute(String response) {
            if (next!=null) {next.run(response);}
        }
    }

}

