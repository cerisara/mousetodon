package fr.xtof54.mousetodon;

import java.io.File;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;

public class TootsPool {
    private File poolfile=null;
    ArrayList<DetToot> pool = new ArrayList<DetToot>();

    private static File getOutputPoolFile(){

        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MouseApp.main.getApplicationContext().getPackageName()
                + "/TootFiles");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String mImageName="tootspool.gz";
        File mediaFile = new File(mediaStorageDir.getPath() + File.separator + mImageName);
        return mediaFile;
    }

    public void loadPool() {
        poolfile = getOutputPoolFile();
        if (poolfile.exists()) {
            try {
                BufferedReader f = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(poolfile)), "UTF-8"));
                for (;;) {
                    String s = f.readLine();
                    if (s==null) break;
                    DetToot toot = DetToot.getToot(s,true);
                    pool.add(toot);
                    // pas besoin de verifier d'eventuels duplicata, car on n'enregistre dans le fichier que le minimum
                }
                f.close();
            } catch (Exception e ) {
                e.printStackTrace();
            }
        }
    }    
}

