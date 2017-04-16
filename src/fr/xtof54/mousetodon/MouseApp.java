package fr.xtof54.mousetodon;

import fr.xtof54.mousetodon.entity.AppCredentials;
import fr.xtof54.mousetodon.entity.Status;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import java.nio.charset.Charset;

import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;
import android.content.SharedPreferences;
import android.util.Log;
import android.util.Pair;
import android.content.Intent;
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
import android.net.Uri;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

public class MouseApp extends Activity
{

    String instanceDomain = "octodon.social";
    String clientId=null, clientSecret=null;
    String atoken = null, rtoken = null;
    static BasicCookieStore mycookiestore = new BasicCookieStore();

    SharedPreferences pref;
    private static String OAUTH_SCOPES = "read";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        pref = getSharedPreferences("MouseApp", MODE_PRIVATE);
    }

    public void detconnect(View v) {
        login();
    }

    /*
    private String getOauthRedirectUri() {
        final String oauth_scheme = "oauth2redirect";
        String scheme = getString(oauth_scheme);
        String host = getString(R.string.oauth_redirect_host);
        return scheme + "://" + host + "/";
    }
    */

    private MastodonAPI getApiFor(String domain) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://" + domain)
                .client(OkHttpUtils.getCompatibleClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit.create(MastodonAPI.class);
    }

    void userlogin() {
        MastodonAPI mapi = getApiFor(instanceDomain);
        Log.d("userlogin","got API");
        /*
        Call<AppCredentials> appcreds = mapi.authenticateApp("Mousetodon","http://localhost",OAUTH_SCOPES,"http://localhost");
        Callback<AppCredentials> callback = new Callback<AppCredentials>() {
                @Override
                public void onResponse(Call<AppCredentials> call, Response<AppCredentials> response) {
                    if (!response.isSuccessful()) {
                        Log.e("appcreds", "App authentication failed. " + response.message());
                        return;
                    }
                    AppCredentials credentials = response.body();
                    clientId = credentials.clientId;
                    clientSecret = credentials.clientSecret;
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString(domain + "/client_id", clientId);
                    editor.putString(domain + "/client_secret", clientSecret);
                    editor.apply();
                    redirectUserToAuthorizeAndLogin(editText);
                }

                @Override
                public void onFailure(Call<AppCredentials> call, Throwable t) {
                    editText.setError(getString(R.string.error_failed_app_registration));
                    t.printStackTrace();
                }
            };
        appcreds.enqueue(callback);
        */

        // method 1 de OAUTH: redirige vers le site mastodon qui verifie le mot de passe
        // redirectUserToAuthorizeAndLogin();
        // method 2 de OAUTH: user/password verifie par l'app
        // tryOauth();
        // method 3: redirect mais on recupere l'access token
        tryOauth2();


        Callback<List<Status>> callback = new Callback<List<Status>>() {
                @Override
                public void onResponse(Call<List<Status>> call, Response<List<Status>> response) {
                    if (!response.isSuccessful()) {
                        Log.e("appcreds", "App authentication failed. " + response.message());
                        return;
                    }
                    List<Status> res = response.body();
                    Log.d("TOOTS",Integer.toString(res.size()));
                }

                @Override
                public void onFailure(Call<List<Status>> call, Throwable t) {
                    t.printStackTrace();
                }
            };
 
        Call<List<Status>> toots = mapi.homeTimeline("10","0",10);
        toots.enqueue(callback);
    }

    /**
     * Chain together the key-value pairs into a query string, for either appending to a URL or
     * as the content of an HTTP request.
     */
    private static String toQueryString(Map<String, String> parameters) {
        StringBuilder s = new StringBuilder();
        String between = "";
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            s.append(between);
            s.append(Uri.encode(entry.getKey()));
            s.append("=");
            s.append(Uri.encode(entry.getValue()));
            between = "&";
        }
        return s.toString();
    }

    void tryOauth2() {
        HttpContext httpctxt = new BasicHttpContext();
        HttpClient httpclient = null;

        httpctxt.setAttribute(ClientContext.COOKIE_STORE, mycookiestore);
        if (httpclient == null) {
            HttpParams httpparms = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpparms, 6000);
            HttpConnectionParams.setSoTimeout(httpparms, 6000);
            httpclient = new DefaultHttpClient(httpparms);
        }
 
        try {
            //String redirectUri = getOauthRedirectUri();
            String redirectUri = "http://localhost";
            // redirectUri = "urn:ietf:wg:oauth:2.0:oob";
            
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("client_id", clientId));
            formparams.add(new BasicNameValuePair("client_secret", clientSecret));
            formparams.add(new BasicNameValuePair("redirect_uri", redirectUri));
            formparams.add(new BasicNameValuePair("grant_type", "authorization_code"));
            formparams.add(new BasicNameValuePair("response_type", "code"));
            formparams.add(new BasicNameValuePair("scope", "read"));
            UrlEncodedFormEntity entity;
            entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            String cmd = "https://"+instanceDomain+"/oauth/authorize";
            Log.d("REDIR",cmd);
     
            HttpPost httppost = new HttpPost(cmd);
            httppost.setEntity(entity);


        // Do not do this in production!!!
        HttpsURLConnection.setDefaultHostnameVerifier( new HostnameVerifier(){
            public boolean verify(String string,SSLSession ssls) {
                return true;
            }
        });


            HttpResponse response = httpclient.execute(httppost, httpctxt);

            // retrieve the access token from the response
            BufferedReader fin = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.forName("UTF-8")));
            for (; ; ) {
                String s = fin.readLine();
                if (s == null) break;
                System.out.println("ogslog " + s);
                s = s.trim();
                if (s.indexOf("error") >= 0) {
                    Log.d("OAUTHLOG","ERROR login OGS " + s);
                } else {
                    // retrieve the token
                    int i = s.indexOf("access_token");
                    if (i < 0) {
                        Log.d("OAUTHLOG","ERROR login OGS " + s);
                    } else {
                        int j = s.indexOf(':', i);
                        i = s.indexOf('"', j) + 1;
                        j = s.indexOf('"', i);
                        atoken = s.substring(i, j);
                        Log.d("ATOKEN",atoken);
                        i = s.indexOf("refresh_token");
                        if (i >= 0) {
                            j = s.indexOf(':', i);
                            i = s.indexOf('"', j) + 1;
                            j = s.indexOf('"', i);
                            rtoken = s.substring(i, j);
                            Log.d("RTOKEN",rtoken);
                        } else rtoken = null;
                    }
                }
            }
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    void tryOauth() {
        HttpContext httpctxt = new BasicHttpContext();
        HttpClient httpclient = null;

        httpctxt.setAttribute(ClientContext.COOKIE_STORE, mycookiestore);
        if (httpclient == null) {
            HttpParams httpparms = new BasicHttpParams();
            HttpConnectionParams.setConnectionTimeout(httpparms, 6000);
            HttpConnectionParams.setSoTimeout(httpparms, 6000);
            httpclient = new DefaultHttpClient(httpparms);
        }

        try {
            List<NameValuePair> formparams = new ArrayList<NameValuePair>();
            formparams.add(new BasicNameValuePair("client_id", clientId));
            formparams.add(new BasicNameValuePair("client_secret", clientSecret));
            formparams.add(new BasicNameValuePair("grant_type", "password"));
            formparams.add(new BasicNameValuePair("username", user));
            formparams.add(new BasicNameValuePair("password", pwd));
            UrlEncodedFormEntity entity;
            entity = new UrlEncodedFormEntity(formparams, "UTF-8");
            String cmd = "https://instances."+instanceDomain+"/oauth/token";
            HttpPost httppost = new HttpPost(cmd);
            httppost.setEntity(entity);
            HttpResponse response = httpclient.execute(httppost, httpctxt);

            // retrieve the access token from the response
            BufferedReader fin = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), Charset.forName("UTF-8")));
            for (; ; ) {
                String s = fin.readLine();
                if (s == null) break;
                System.out.println("ogslog " + s);
                s = s.trim();
                if (s.indexOf("error") >= 0) {
                    Log.d("OAUTHLOG","ERROR login OGS " + s);
                } else {
                    // retrieve the token
                    int i = s.indexOf("access_token");
                    if (i < 0) {
                        Log.d("OAUTHLOG","ERROR login OGS " + s);
                    } else {
                        int j = s.indexOf(':', i);
                        i = s.indexOf('"', j) + 1;
                        j = s.indexOf('"', i);
                        atoken = s.substring(i, j);
                        Log.d("ATOKEN",atoken);
                        i = s.indexOf("refresh_token");
                        if (i >= 0) {
                            j = s.indexOf(':', i);
                            i = s.indexOf('"', j) + 1;
                            j = s.indexOf('"', i);
                            rtoken = s.substring(i, j);
                            Log.d("RTOKEN",rtoken);
                        } else rtoken = null;
                    }
                }
            }
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void redirectUserToAuthorizeAndLogin() {
        /* To authorize this app and log in it's necessary to redirect to the domain given,
         * activity_login there, and the server will redirect back to the app with its response. */
        String endpoint = MastodonAPI.ENDPOINT_AUTHORIZE;
        //String redirectUri = getOauthRedirectUri();
        String redirectUri = "http://localhost";
        redirectUri = "urn:ietf:wg:oauth:2.0:oob";
        Map<String, String> parameters = new HashMap<String,String>();
        parameters.put("client_id", clientId);
        parameters.put("redirect_uri", redirectUri);
        parameters.put("response_type", "code");
        parameters.put("scope", OAUTH_SCOPES);
        String url = "https://" + instanceDomain + endpoint + "?" + toQueryString(parameters);
        Log.d("REDIR",url);
        Intent viewIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        if (viewIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(viewIntent);
        } else {
            Log.d("UserLogin","no_web_browser_found");
        }
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
        clientId = pref.getString(String.format("client_id_for_%s", instanceDomain), null);
        clientSecret = pref.getString(String.format("client_secret_for_%s", instanceDomain), null);
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
        } else {
            userlogin();
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
            userlogin();
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
