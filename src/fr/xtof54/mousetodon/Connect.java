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

    public void getUserToots(final int userid, final NextAction next) {
        Log.d("Connect","getUserToots "+Integer.toString(userid));
        String surl = String.format("https://%s/api/v1/accounts/"+Integer.toString(userid)+"/statuses", domain);
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

    public void muteUser(final int userid, final NextAction next) {
        Log.d("Connect","muteUser "+Integer.toString(userid));
        String surl = String.format("https://%s/api/v1/accounts/"+Integer.toString(userid)+"/mute", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null);";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }
    public void unmuteUser(final int userid, final NextAction next) {
        Log.d("Connect","unmuteUser "+Integer.toString(userid));
        String surl = String.format("https://%s/api/v1/accounts/"+Integer.toString(userid)+"/unmute", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null);";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }


    public void blockUser(final int userid, final NextAction next) {
        Log.d("Connect","blockUser "+Integer.toString(userid));
        String surl = String.format("https://%s/api/v1/accounts/"+Integer.toString(userid)+"/block", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null);";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }
    public void unblockUser(final int userid, final NextAction next) {
        Log.d("Connect","unblockUser "+Integer.toString(userid));
        String surl = String.format("https://%s/api/v1/accounts/"+Integer.toString(userid)+"/unblock", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null);";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }


    public void followUser(final int userid, final NextAction next) {
        Log.d("Connect","followUser "+Integer.toString(userid));
        String surl = String.format("https://%s/api/v1/accounts/"+Integer.toString(userid)+"/follow", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null);";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }
    public void unfollowUser(final int userid, final NextAction next) {
        Log.d("Connect","unfollowUser "+Integer.toString(userid));
        String surl = String.format("https://%s/api/v1/accounts/"+Integer.toString(userid)+"/unfollow", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
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

    public void replyToot(String s, DetToot origintoot, NextAction next) {
        Log.d("Connect","sendToot");
        String surl = String.format("https://%s/api/v1/statuses", domain);
        try {
            String parms;
            if (s.startsWith("_d_i")) {
                parms = "status="+URLEncoder.encode(s.substring(4), "UTF-8");
                if (origintoot!=null && origintoot.id>=0) {
                    System.out.println("replying to toot "+Integer.toString(origintoot.id));
                    parms += "&in_reply_to_id="+Integer.toString(origintoot.id);
                }
            } else {
                parms = "status="+URLEncoder.encode(s, "UTF-8");
                if (origintoot!=null && origintoot.id>=0) {
                    System.out.println("replying to toot "+Integer.toString(origintoot.id));
                    // don't use any more the reply link, because it prevents posting the toot on the public timeline.
                    // so rather insert a link to the previous toot if there's enough space
                    String ss = s+"\n"+"Replied to: "+origintoot.tooturl;
                    if (ss.length()<500) parms="status="+URLEncoder.encode(ss, "UTF-8");
                }
            }
            final String js = "var xhr = new XMLHttpRequest(); "+
                "xhr.open('POST', '"+surl+"', true); "+
                "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded'); "+
                "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
                "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
                "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
                "xhr.addEventListener('error', deterror); "+
                "xhr.send('"+parms+"'); ";
            System.out.println("SENDJS "+js);
            MouseApp.javascriptCmd("javascript:"+js,next);
        } catch (Exception e) {
            e.printStackTrace();
            MouseApp.main.message("ERROR encoding");
            next.run("");
        }
    }

    public void sendToot(String s, NextAction next) {
        Log.d("Connect","sendToot");
        String surl = String.format("https://%s/api/v1/statuses", domain);
        try {
            String parms = "status="+URLEncoder.encode(s, "UTF-8");
            final String js = "var xhr = new XMLHttpRequest(); "+
                "xhr.open('POST', '"+surl+"', true); "+
                "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded'); "+
                "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
                "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
                "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
                "xhr.addEventListener('error', deterror); "+
                "xhr.send('"+parms+"'); ";
            System.out.println("SENDJS "+js);
            MouseApp.javascriptCmd("javascript:"+js,next);
        } catch (Exception e) {
            e.printStackTrace();
            MouseApp.main.message("ERROR encoding");
            next.run("");
        }
 
        /*
         *  in_reply_to_id (optional): local ID of the status you want to reply to
            media_ids (optional): array of media IDs to attach to the status (maximum 4)
            sensitive (optional): set this to mark the media of the status as NSFW
            spoiler_text (optional): text to be shown as a warning before the actual content
            visibility (optional): either "direct", "private", "unlisted" or "public"
            */
    }
    public void reblog(int id, NextAction next) {
        Log.d("Connect","Reblog");
        String surl = String.format("https://%s/api/v1/statuses/"+Integer.toString(id)+"/reblog", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded'); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null); ";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }

    public void boost(int id, NextAction next) {
        Log.d("Connect","Boost");
        String surl = String.format("https://%s/api/v1/statuses/"+Integer.toString(id)+"/favourite", domain);
        final String js = "var xhr = new XMLHttpRequest(); "+
            "xhr.open('POST', '"+surl+"', true); "+
            "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded'); "+
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
            "xhr.setRequestHeader('Content-type', 'application/x-www-form-urlencoded'); "+
            "xhr.setRequestHeader('Authorization', 'Bearer "+MouseApp.access_token+"'); "+
            "xhr.onload = function () { window.INTERFACE.processContent('DETOK '+this.responseText); }; "+
            "function deterror(evt) { window.INTERFACE.processContent('DETKO'); }; "+
            "xhr.addEventListener('error', deterror); "+
            "xhr.send(null); ";
        System.out.println("SENDJS "+js);
        MouseApp.javascriptCmd("javascript:"+js,next);
    }
 
}


