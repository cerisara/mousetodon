package fr.xtof54.mousetodon;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.content.Context;
import android.util.AttributeSet;
import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.view.View;

public class DetWebView extends WebView {
    WebView dwv=null;

    public DetWebView(Context context) {
        this(context, null);
    }

    public DetWebView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public static Bitmap loadBitmapFromView(View v) {
        Bitmap b = Bitmap.createBitmap( 100,100, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        // v.layout(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());
        v.draw(c);
        return b;
    }

    public DetWebView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        dwv=this;
        this.getSettings().setDomStorageEnabled(true);
        this.getSettings().setLoadsImagesAutomatically(true);
        this.setWebViewClient(new WebViewClient() {
            @Override 
            public void onPageFinished(WebView view, String url) { 
                final String xurl=""+url;
                System.out.println("webimgpage finished "+url);
                dwv.setDrawingCacheEnabled(true);
                dwv.buildDrawingCache();
                Thread tt = new Thread(new Runnable() {
                    public void run() {
                        try {
                            Thread.sleep(100);
                            MouseApp.main.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Bitmap img = loadBitmapFromView(dwv);
                                    DetIcons.newImg(xurl,img);
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                tt.start();
            } 
        });
        this.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if(progress == 100) {
                    System.out.println("WEBPROGRESS "+Integer.toString(progress));
                }
            }
        });        
    }

    @Override
    public void loadUrl(String url) {
        System.out.println("webview LAODURL "+url);
        super.loadUrl(url);
    }

}

