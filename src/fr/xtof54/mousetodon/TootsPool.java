package fr.xtof54.mousetodon;

import org.json.JSONObject;
import java.util.concurrent.ArrayBlockingQueue;
import java.io.File;
import android.os.Environment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.HashMap;

/**
    this class is a bit redundant with TootsManager, but the latter was built to automatically
    download the newest toots from several timelines in the background, without control from the user,
    which appeared to not be a very good idea. (and it's deprecated in fact)

    This class rather stores past toots on disk, when asked by the user, and eventually goes back in time
    until the beginning of the instance.
*/
public class TootsPool {
    public static boolean stopdownload = false;
    private File poolfile=null;
    HashMap<String,TreeSet<DetToot>> inst2pool = new HashMap();
    // garde une liste des IDs que on a tente de downloader (ca evite de redownloader ceux qui ne sont pas downloadables)
    HashMap<String,TreeSet<Integer>> alreadyDownloaded = new HashMap();

    // file that contains the compressed past toots from all instances
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

    // loads the toots stored locally. should be called at every creation of this object, to avoid downloading already stored toots
    public void loadPool() {
        poolfile = getOutputPoolFile();
        if (poolfile.exists()) {
            try {
                BufferedReader f = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(poolfile)), "UTF-8"));
                for (;;) {
                    String s = f.readLine();
                    if (s==null) break;
                    String inst = f.readLine().trim();
                    DetToot toot = DetToot.getToot(inst,s,true);
                    TreeSet<DetToot> sortedset = inst2pool.get(inst);
                    if (sortedset==null) {
                        sortedset = new TreeSet<DetToot>();
                        inst2pool.put(inst,sortedset);
                    }
                    sortedset.add(toot);
                    TreeSet<Integer> knownids = alreadyDownloaded.get(inst);
                    if (knownids==null) {
                        knownids = new TreeSet<Integer>();
                        alreadyDownloaded.put(inst,knownids);
                    }
                    knownids.add(toot.id);
                    // pas besoin de verifier d'eventuels duplicata, car on n'enregistre dans le fichier que le minimum
                    // mais les toots du fichier ne sont pas forcement ordonnes, et peuvent venir de plusieurs instances
                }
                f.close();
            } catch (Exception e ) {
                e.printStackTrace();
            }
        }
    }
    public void savePool() {
        // TODO
    }

    // after downloading the latest timeline, we can call this method to add these toots into the pool
    public void addToots(String inst, ArrayList<DetToot> toots) {
        TreeSet<DetToot> sortedset = inst2pool.get(inst);
        if (sortedset==null) {
            sortedset = new TreeSet<DetToot>();
            inst2pool.put(inst,sortedset);
        }
        sortedset.addAll(toots);
        TreeSet<Integer> knownids = alreadyDownloaded.get(inst);
        if (knownids==null) {
            knownids = new TreeSet<Integer>();
            alreadyDownloaded.put(inst,knownids);
        }
        for (DetToot tt: toots) knownids.add(tt.id);
    }

    // while the user is reading toots, it's good to call this method in the background to download the previous ones
    // WARNING: must be called with the same current instance in MouseApp: cannot download from another instance !
    // TODO: stop downloading from MouseApp when changing instance
    private static Thread downloadt = null;
    public void downloadFrom(final String inst, final int highestid) {
        // a chaque fois qu'on l'appelle, on verifie d'abord que l'ancien est arrete
        while (downloadt!=null) {
            stopdownload=true;
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        downloadt = new Thread(new Runnable() {
            public void run() {
                stopdownload=false;
                TreeSet<DetToot> sortedset = inst2pool.get(inst);
                if (sortedset==null) {
                    sortedset = new TreeSet<DetToot>();
                    inst2pool.put(inst,sortedset);
                }
                TreeSet<Integer> knownids = alreadyDownloaded.get(inst);
                if (knownids==null) {
                    knownids = new TreeSet<Integer>();
                    alreadyDownloaded.put(inst,knownids);
                }
                for (int id=highestid-1;id>0;id--) {
                    if (stopdownload) break;
                    long curt = System.currentTimeMillis();
                    if (curt-lastUserActionTime<5000) {
                        id++; continue;
                        // ne download rien si le user est en train de manpulr
                    }
                    if (!knownids.contains(id)) {
                        DetToot tt = downloadOne(id);
                        knownids.add(id);
                        if (tt!=null) sortedset.add(tt);
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                downloadt=null;
            }
        });
        downloadt.start();
    }
    private static String curdownloadurl = "FREFVSVFGFSGR4HYOOOR";
    private static long lastUserActionTime = 0;
    private DetToot downloadOne(final int tootid) {
        final ArrayBlockingQueue<DetToot> tres = new ArrayBlockingQueue<DetToot>(1);
        curdownloadurl = "statuses/"+tootid;
        MouseApp.main.connect.getTL("statuses/"+tootid,new NextAction() {
            public void run(String res) {
                try {
                    JSONObject o = new JSONObject(res);
                    DetToot dt = new DetToot(MouseApp.main.instanceDomain,o,true);
                    tres.put(dt);
                } catch (Exception e) {
                    try {
                        tres.put(null);
                    } catch (Exception ee) {
                        ee.printStackTrace();
                    }
                }
            }
        });
        try {
            DetToot tt = tres.take();
            return tt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void userDownloadAction(String url) {
        if (url.indexOf(curdownloadurl)>=0) {
            // c'est bon, c'est notre download
            return;
        }
        lastUserActionTime = System.currentTimeMillis();
    }
}

