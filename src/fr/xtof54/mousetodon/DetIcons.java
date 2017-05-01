package fr.xtof54.mousetodon;

import org.json.JSONObject;
import java.io.InputStream;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class DetIcons {
    public static HashMap<String,Bitmap> avatars = new HashMap<String,Bitmap>();
    public static Bitmap[] waitimg=new Bitmap[1];

    public static void setImage(String nom, Bitmap img) {
        System.out.println("SETTTTIMMMMGG "+nom+" "+avatars.get(nom));
        if (avatars.get(nom)!=null) {
            System.out.println("AVATARERROR "+nom);
        }
        avatars.put(nom,img);
        MouseApp.main.updateList();
    }

    private static LinkedBlockingQueue<Object[]> todownload = new LinkedBlockingQueue<Object[]>();
    private static Thread downloader = null;

    public static void downloadImg(DetToot toot, JSONObject account) {
        try {
            if (downloader==null) {
                downloader = new Thread(new Runnable() {
                    public void run() {
                        try {
                            for (;;) {
                                Object[] tmp = todownload.take();
                                if (tmp==null) break;
                                DetToot t = (DetToot)tmp[0];
                                String u = (String)tmp[1];
                                MouseApp.imgurl(u);
                                // attends que l'image soit completement chargee
                                for (;;) {
                                    if (waitimg[0]!=null) break;
                                    Thread.sleep(100);
                                }
                                System.out.println("IMMMMMM found");
                                t.setIcon(waitimg[0]);
                                setImage(u,waitimg[0]);
                                synchronized(waitimg) {
                                    waitimg[0]=null;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                downloader.start();
            }
            // TODO: charger les icons sur disque
            String avatar = account.getString("avatar");
            if (avatar!=null && avatar.startsWith("http")) {
                Bitmap img = avatars.get(avatar);
                if (img!=null) {
                    toot.setIcon(img);
                } else {
                    Object[] tmp = {toot,avatar};
                    todownload.put(tmp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
