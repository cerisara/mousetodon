package fr.xtof54.mousetodon;

import java.util.ArrayList;
import java.util.List;
import com.optimaize.langdetect.*;
import com.optimaize.langdetect.profiles.*;
import com.optimaize.langdetect.ngram.*;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.graphics.Bitmap;

public class DetToot {
    static LangDetect langdetect = null;
    static boolean langdetectpossible = true;
    String txt, lang=null;
    private Bitmap usericon=null;

    public DetToot(JSONObject json, boolean detectlang) {
        String texte = getText(json);
        try {
            if (!json.isNull("reblog")) {
                JSONObject reblog = json.getJSONObject("reblog");
                texte += "\n REBLOGED: "+getText(reblog);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        txt=texte;
        if (detectlang) detectlang();
    }

    public Bitmap getUserIcon() {
        return usericon;
    }

    String getText(JSONObject json) {
        if (json==null) return "";
        try {
            String aut="";
            if (!json.isNull("account")) {
                JSONObject acc = json.getJSONObject("account");
                aut = acc.getString("username")+": ";
                usericon = DetIcons.downloadImg(acc);
            }
            String txt=json.getString("content");
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

    public String getStr() {
        String s=txt;
        if (lang!=null) s+=" ("+lang+")";
        return s;
    }
}


