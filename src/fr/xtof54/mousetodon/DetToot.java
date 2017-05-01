package fr.xtof54.mousetodon;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.graphics.Bitmap;
import java.util.HashMap;

public class DetToot {
    static LangDetect langdetect = null;
    static boolean langdetectpossible = true;
    String txt, lang=null;
    public int id=-1, parentid=-1;
    public boolean boosted = false;
    private Bitmap usericon=null;
    private String usericonurl=null;
    public String date="";
    ArrayList<String> medias = new ArrayList<String>();

    private static HashMap<Integer,DetToot> alltoots = new HashMap<Integer,DetToot>();
    public static void checkImages() {
        for (DetToot t : alltoots.values()) {
            if (t!=null && t.usericon==null && t.usericonurl!=null) {
                Bitmap img = DetIcons.avatars.get(t.usericonurl);
                t.setIcon(img);
            }
        }
    }

    public DetToot(JSONObject json, boolean detectlang) {
        // MUST get extra info before text because of media list
        getExtraInfos(json);
        String texte = getText(json,true);
        try {
            if (!json.isNull("reblog")) {
                JSONObject reblog = json.getJSONObject("reblog");
                texte += "\n REBLOGED: "+getText(reblog,false);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        txt=texte;
        if (detectlang) detectlang();

        if (id>=0) alltoots.put(id,this);
        else System.out.println("ERROR TOOT NOID "+json);
    }

    public Bitmap getUserIcon() {
        return usericon;
    }

    private void getExtraInfos(JSONObject json) {
        medias.clear();
        if (json!=null) {
            try {
                id = json.getInt("id");

                if (!json.isNull("created_at")) date=json.getString("created_at");

                if (!json.isNull("in_reply_to_id")) parentid=json.getInt("in_reply_to_id");

                if (json.isNull("favourited")) boosted=false;
                else boosted = json.getBoolean("favourited");

                JSONArray meds = json.getJSONArray("media_attachments");
                if (meds!=null) {
                    for (int i=0;i<meds.length();i++) {
                        JSONObject mm = meds.getJSONObject(i);
                        if (!mm.isNull("url")) medias.add(mm.getString("url"));
                        else if (!mm.isNull("remote_url")) medias.add(mm.getString("remote_url"));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void setIcon(Bitmap img) {
        usericon=img;
    }

    public String getText(JSONObject json, boolean withIcon) {
        if (json==null) return "";
        try {
            String aut="";
            if (!json.isNull("account")) {
                JSONObject acc = json.getJSONObject("account");
                aut = acc.getString("username")+": ";
                String avatar = acc.getString("avatar");
                if (avatar!=null && avatar.startsWith("http")) {
                    usericonurl=avatar;
                    if (withIcon) DetIcons.downloadImg(this,avatar,aut);
                }
            }
            String txt=json.getString("content");
            // look for URLs
            String[] words = txt.split(" ");
            for (String w : words)
                if (w.startsWith("href=")) medias.add(w.substring(6,w.length()-1));
            return aut+txt.trim();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
    public DetToot(String t) {
        txt=""+t;
    }

    public String detectlang() {
        if (!langdetectpossible) return null;
        if (langdetect==null) {
            langdetect = new LangDetect();
            if (langdetect==null) langdetectpossible=false;
        }
        if (langdetectpossible) {
            lang = langdetect.detect(txt);
            return lang;
        }
        return null;
    }

    /* The method that is called to show the toot in the ListView
     * */
    public String getStr() {
        String s=""+txt;
        if (lang!=null) s+=" ("+lang+")";

        String attrs="";
        if (boosted) attrs+="♡";
        if (medias.size()>0) attrs+="♭";
        if (parentid>=0) attrs+="↑";

        if (attrs.length()>0) attrs+=" ";
        if (date.length()>0) s=attrs+date+" "+s;
        return s;
    }
}


