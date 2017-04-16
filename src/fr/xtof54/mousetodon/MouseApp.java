package fr.xtof54.mousetodon;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
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

import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.model.OAuthRequest;
import com.github.scribejava.core.model.Response;
import com.github.scribejava.core.model.Verb;
import com.github.scribejava.core.oauth.OAuth20Service;

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


    class LoginTask extends AsyncTask<String, Void, OAuth20Service> {
        // Dialog authDialog;

        @Override
        protected OAuth20Service doInBackground(String... urls) {
            String clientId = pref.getString(String.format("client_id_for_%s", instanceDomain), null);
            String clientSecret = pref.getString(String.format("client_secret_for_%s", instanceDomain), null);
            Log.d("LoginTask", "client id saved: " + clientId);
            Log.d("LoginTask", "client secret saved: " + clientSecret);
            if(clientId == null || clientSecret == null) {
                // Client registration
                Log.d("LoginTask", "Going to fetch new client id/secret");

                try {
                    URL url = new URL(String.format("https://%s/api/v1/apps", instanceDomain));
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                    urlConnection.setRequestMethod("POST");

                    List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
                    params.add(new Pair<String, String>("client_name", "Mastodon API Example"));
                    params.add(new Pair<String, String>("redirect_uris", "http://localhost"));

                    OutputStream os = urlConnection.getOutputStream();
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                    writer.write(getQuery(params));
                    writer.flush();
                    writer.close();
                    os.close();

                    urlConnection.connect();

                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;

                        while((line = br.readLine()) != null) {
                            sb.append(line + "\n");
                        }

                        br.close();
                        JSONObject json = new JSONObject(sb.toString());
                        clientId = json.getString("client_id");
                        clientSecret = json.getString("client_secret");

                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString(String.format("client_id_for_%s", instanceDomain), clientId);
                        edit.putString(String.format("client_secret_for_%s", instanceDomain), clientSecret);
                        edit.commit();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } finally {
                        urlConnection.disconnect();
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

                /*
                // Create URL
                URL mastEndpoint = new URL("https://octodon.social/");

                // Create connection
                HttpsURLConnection myConnection = (HttpsURLConnection) githubEndpoint.openConnection();
                */  

            return new ServiceBuilder()
                    .apiKey(clientId)
                    .apiSecret(clientSecret)
                    .callback("http://localhost")
                    .build(MastodonApi.instance(instanceDomain));

        }
    }
}
