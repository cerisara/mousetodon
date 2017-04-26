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
        params.add(new Pair<String, String>("scopes", "read write follow"));
        params.add(new Pair<String, String>("redirect_uris", "urn:ietf:wg:oauth:2.0:oob"));
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
        params.add(new Pair<String, String>("scope", "read write follow"));
        String surl = String.format("https://%s/oauth/token", domain);
        Object[] args = {surl, params, next};
        new ConnectTask().execute(args);
    }

    public void sendToot(String s, NextAction next) {
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("status", s));
        if (MouseApp.curtootidx>=0 && MouseApp.main.toots.get(MouseApp.curtootidx).id>=0) {
            System.out.println("replying to toot "+Integer.toString(MouseApp.main.toots.get(MouseApp.curtootidx).id));
            params.add(new Pair<String, String>("in_reply_to_id", Integer.toString(MouseApp.main.toots.get(MouseApp.curtootidx).id)));
        }
        /*
         *  in_reply_to_id (optional): local ID of the status you want to reply to
            media_ids (optional): array of media IDs to attach to the status (maximum 4)
            sensitive (optional): set this to mark the media of the status as NSFW
            spoiler_text (optional): text to be shown as a warning before the actual content
            visibility (optional): either "direct", "private", "unlisted" or "public"
            */
        String surl = String.format("https://%s/api/v1/statuses", domain);
        Object[] args = {surl, params, next};
        new PostTask().execute(args);
    }
    public void boost(int id, NextAction next) {
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        String surl = String.format("https://%s/api/v1/statuses/"+Integer.toString(id)+"/favourite", domain);
        Object[] args = {surl, params, next};
        new PostTask().execute(args);
    }
    public void unboost(int id, NextAction next) {
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        String surl = String.format("https://%s/api/v1/statuses/"+Integer.toString(id)+"/unfavourite", domain);
        Object[] args = {surl, params, next};
        new PostTask().execute(args);
    }
    public void getTL(final String tl, NextAction next) {
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        String surl = String.format("https://%s/api/v1/"+tl, domain);
        if (MouseApp.main.maxid>=0) surl = String.format("https://%s/api/v1/"+tl+"?max_id="+Integer.toString(MouseApp.main.maxid), domain);
        Object[] args = {surl, params, next};
        new GetTask().execute(args);
    }
    public String getSyncToot(final int id) {
        String surl = String.format("https://%s/api/v1/statuses/"+Integer.toString(id), domain);
        try {
            URL url = new URL(surl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer "+MouseApp.access_token);
            urlConnection.connect();
            int rep = urlConnection.getResponseCode();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;

            while((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
            String res=sb.toString();
            urlConnection.disconnect();
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public String blockGetTL(final String tl) {
        List<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        String surl = String.format("https://%s/api/v1/"+tl, domain);
        String res=null;
        try {
            URL url = new URL(surl);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization", "Bearer "+MouseApp.access_token);
            urlConnection.connect();

            int rep = urlConnection.getResponseCode();
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

    class GetTask extends AsyncTask<Object, Void, String> {
        NextAction next=null;
        @Override
        protected String doInBackground(Object... args) {
            String surl=(String)args[0];
            next=(NextAction)args[2];
            String res=null;
            try {
                URL url = new URL(surl);
                System.out.println("COCOCOCOCCOOOOOOOOOOOOOOOOOOOOOOOOOO "+surl);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestProperty("Authorization", "Bearer "+MouseApp.access_token);
                urlConnection.connect();

                int rep = urlConnection.getResponseCode();
                System.out.println("ZOCOCOCOCCOOOOOOOOOOOOOOOOOOOOOOOOOO "+Integer.toString(rep));

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
                String ssq = getQuery(params);
                System.out.println("DEBUGSSSSSSS "+ssq);
                writer.write(ssq);
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

    class PostTask extends AsyncTask<Object, Void, String> {
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
                urlConnection.setRequestProperty("Authorization", "Bearer "+MouseApp.access_token);
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

