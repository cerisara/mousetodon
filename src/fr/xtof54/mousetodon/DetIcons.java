package fr.xtof54.mousetodon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.io.File;
import java.io.FileOutputStream;

public class DetIcons {
    public static HashMap<String,Bitmap> avatars = new HashMap<String,Bitmap>();
    private static Bitmap[] waitimg={null};
    private static LinkedBlockingQueue<Object[]> todownload = new LinkedBlockingQueue<Object[]>();
    private static Thread downloader = null;

    private static File getOutputMediaFile(String url){

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MouseApp.main.getApplicationContext().getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String mImageName=url.replace('/','_').replace(':','_')+".png";
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public static void newImg(String url, Bitmap img) {
        {
            try {
                File pictureFile = getOutputMediaFile(url);
                System.out.println("SAVEIMG "+pictureFile.getAbsolutePath());
                FileOutputStream fos = new FileOutputStream(pictureFile);
                img.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Bitmap oldimg = avatars.get(url);
        if (oldimg==null) {
            avatars.put(url,img);
        }
        DetToot.checkImages();
        MouseApp.main.updateList();
        synchronized(waitimg) {
            waitimg[0]=img;
        }
    }

    public static void downloadImg(DetToot toot, String avatar, String username) {
        try {
            if (downloader==null) {
                downloader = new Thread(new Runnable() {
                    public void run() {
                        try {
                            for (;;) {
                                Object[] tmp = todownload.take();
                                if (tmp==null) break;
                                String u = (String)tmp[1];
                                String uname = (String)tmp[2];
                                // inutile de downloader si deja fait
                                if (DetIcons.avatars.get(u)==null) {
                                    MouseApp.imgurl(u);
                                    // attends que l'image soit completement chargee
                                    for (;;) {
                                        if (waitimg[0]!=null) break;
                                        Thread.sleep(100);
                                    }
                                    System.out.println("IMMMMMM found");

                                    // MouseApp.syncShow(waitimg[0],uname);

                                    synchronized(waitimg) {
                                        waitimg[0]=null;
                                    }
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
            Bitmap img = avatars.get(avatar);
            if (img!=null) {
                toot.setIcon(img);
            } else {
                // check if the icon is on disk
                File pictureFile = getOutputMediaFile(avatar);
                if (pictureFile.exists()) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    img = BitmapFactory.decodeFile(pictureFile.getAbsolutePath(), options);
                    toot.setIcon(img);
                } else {
                    Object[] tmp = {toot,avatar,username};
                    todownload.put(tmp);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
