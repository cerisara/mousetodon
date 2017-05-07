package fr.xtof54.mousetodon;

import java.util.ArrayList;

/**
    Manages all downloaded toots:
    - stores all toots on disk
    - download new toots in the background
    - detect "disconnected" periods without toots and download missing toots in the background with low priority

    This class ONLY downloads in the background: if the user wants to download toots only once, then it must not use this class.
*/
public class TootsManager {
    private static TootsManager singleton = null;
    // maintains 4 lists of toots (notifs, home, local, federated)
    private ArrayList<DetToot>[] tls = new ArrayList[4];
    private Thread downloader = null;
    private int tl4downloader=-1;

    public static TootsManager getTootsManager() {
        if (singleton==null) singleton=new TootsManager();
        return singleton;
    }
    private TootsManager() {
        for (int i=0;i<tls.length;i++) tls[i]=new ArrayList<DetToot>();
    }

    // the caller must set-up a loop+timer if he wants to update its displayed list
    public ArrayList<DetToot> getMostRecentToots(String instance, int tl) {
        tl4downloader=tl;
        if (downloader==null) downloadInBackground(instance);
        return tls[tl];
    }

    public static void stopAll() {
        if (singleton!=null) singleton.tl4downloader=-1;
        singleton=null;
    }

    private void downloadInBackground(String instance) {
        // set a timer to download newest toots regularly
        downloader = new Thread(new Runnable() {
            public void run() {
                try {
                    while (tl4downloader>=0) {
                        switch (tl4downloader) {
                            case 0: // notifications
                                MouseApp.main.getNotifs("notifications", new TootsListener() {
                                    public void gotNewToots(ArrayList<DetToot> newtoots) {
                                        tls[0].clear();
                                        tls[0].addAll(newtoots);
                                    }
                                });
                                break;
                            case 1: // home
                                MouseApp.main.getToots("timelines/home", new TootsListener() {
                                    public void gotNewToots(ArrayList<DetToot> newtoots) {
                                        tls[1].clear();
                                        tls[1].addAll(newtoots);
                                    }
                                });
                                break;
                            case 2:
                                break;
                            case 3: // federated
                                MouseApp.main.getToots("timelines/public", new TootsListener() {
                                    public void gotNewToots(ArrayList<DetToot> newtoots) {
                                        tls[3].clear();
                                        tls[3].addAll(newtoots);
                                    }
                                });
                                break;
                            default: break;
                        }
                        Thread.sleep(5000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                downloader=null;
            }
        });
        downloader.start();
    }
}

