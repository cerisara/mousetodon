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
import android.graphics.Bitmap;

import android.util.Pair;
import android.os.AsyncTask;
import android.util.Log;

import java.net.URL;
import java.net.URLEncoder;
import java.net.HttpURLConnection;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import java.net.MalformedURLException;

import android.webkit.WebView;
import android.webkit.WebViewClient;

public class Connect extends WebViewClient {
    String domain;

    public Connect() {
        super();
    }

    // c'est un webview client qui repond uniquement au WebView dedie au javascript
    @Override 
    public void onPageFinished(WebView view0, String url) { 
        System.out.println("webviewpage finished "+url);
        if (url.contains("javascript")) {
            System.out.println("webviewpage javascriptcall detected");
        } else {
            // c'est un GET ou POST vers l'API du server mastodon
            // je lance un appel JS pour recuperer le JSON en reponse
            MouseApp.javascriptCmd("javascript:window.INTERFACE.processContent(document.getElementsByTagName('html')[0].innerHTML);",null);
        }
    } 

    /*
  There are several ways to set content for WebView:
WebView webView = (WebView) findViewById(R.id.WebView);
// Ex 1: set URL address:
webView.loadUrl("https://www.android.com/");
// Ex 2: set .html from a raw folder:
webView.loadUrl("file:///Android_res/raw/some_file.HTML");
// Ex 3: set .html from an asset folder:
webView.loadUrl("file:///Android_asset/some_file.HTML");
// Ex 4: set html content as String:
String rawHTML = "<HTML>"+ "<body><h1>HTML content</h1></body>"+ "</HTML>";
webView.loadData(data, "text/HTML", "UTF-8");
*/

    /* celle-ci est appelee avant que la page ne se charge
     
    @Override 
    public void onPageStarted(WebView view0, String url, Bitmap favicon) { 
        final WebView view = view0;
        System.out.println("webviewpage started "+url);
        MouseApp.main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('html')[0].innerHTML);");
                // view.loadUrl("javascript:window.INTERFACE.processContent(document.content);"); 
            }
        });
        //view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);"); 
    } 
    */


    /* celle-ci n'est jamais appelee ??

    @Override 
    public boolean shouldOverrideUrlLoading(WebView view0, String url) { 
        final WebView view = view0;
        System.out.println("webviewpage urlloading "+url);
        MouseApp.main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('html')[0].innerHTML);");
            }
        });
        return false;
    } 
    */

    public void setInstance(String in) {
        domain=""+in;
    }

    public void registerApp(final NextAction next) {
        Log.d("Connect","register app");

        String surl = String.format("https://%s/api/v1/apps", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send('client_name=Mousetodon&scopes=read+write+follow&redirect_uris=urn:ietf:wg:oauth:2.0:oob'); ";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }

    public void userLogin(String clientid, String secret, String email, String pwd, final NextAction next) {
        Log.d("Connect","userlogin");

        String surl = String.format("https://%s/oauth/token", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send('client_id="+clientid+"&client_secret="+secret+"&grant_type=password&username="+email+"&password="+pwd+"&scope=read+write+follow'); ";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }

    public void getTL(final String tl, final NextAction next) {
        Log.d("Connect","getTL");
        String surl = String.format("https://%s/api/v1/"+tl, domain);
        if (MouseApp.main.maxid>=0) surl = String.format("https://%s/api/v1/"+tl+"?max_id="+Integer.toString(MouseApp.main.maxid), domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('GET', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null);";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }

    public void getOneStatus(int id, final NextAction next) {
        Log.d("Connect","getOneStatus");
        String surl = String.format("https://%s/api/v1/statuses/"+Integer.toString(id), domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('GET', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null);";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }

    public void sendToot(String s, NextAction next) {
        Log.d("Connect","sendToot");
        String surl = String.format("https://%s/api/v1/statuses", domain);
        String parms = "status="+s;
        if (MouseApp.curtootidx>=0 && MouseApp.main.toots.get(MouseApp.curtootidx).id>=0) {
            System.out.println("replying to toot "+Integer.toString(MouseApp.main.toots.get(MouseApp.curtootidx).id));
            parms += "&in_reply_to_id="+Integer.toString(MouseApp.main.toots.get(MouseApp.curtootidx).id);
        }
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send('"+parms+"'); ";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
 
        /*
         *  in_reply_to_id (optional): local ID of the status you want to reply to
            media_ids (optional): array of media IDs to attach to the status (maximum 4)
            sensitive (optional): set this to mark the media of the status as NSFW
            spoiler_text (optional): text to be shown as a warning before the actual content
            visibility (optional): either "direct", "private", "unlisted" or "public"
            */
    }
    public void boost(int id, NextAction next) {
        Log.d("Connect","Boost");
        String surl = String.format("https://%s/api/v1/statuses/"+Integer.toString(id)+"/favourite", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null); ";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }
    public void unboost(int id, NextAction next) {
        Log.d("Connect","UnBoost");
        String surl = String.format("https://%s/api/v1/statuses/"+Integer.toString(id)+"/unfavourite", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null); ";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
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
}


