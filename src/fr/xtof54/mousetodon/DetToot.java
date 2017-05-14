package fr.xtof54.mousetodon;

import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import android.graphics.Bitmap;
import java.util.HashMap;
import java.util.Date;

public class DetToot {
    static LangDetect langdetect = null;
    static boolean langdetectpossible = true;
    String txt, lang=null;
    public int id=-1, parentid=-1;
    public boolean boosted = false;
    private Bitmap usericon=null;
    private String usericonurl=null;
    public String username=null;
    public int autid=-1;
    public String date=""; // formatted in ISO 8601
    ArrayList<String> medias = new ArrayList<String>();
    public String tooturl = "";

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

    public void getExtraInfos(JSONObject json) {
        medias.clear();
        if (json!=null) {
            try {
                if (!json.isNull("id")) id = json.getInt("id");

                if (!json.isNull("account")) {
                    JSONObject acc = json.getJSONObject("account");
                    username = acc.getString("acct");
                    if (!acc.isNull("id")) autid=acc.getInt("id");
                    String avatar = acc.getString("avatar");
                    if (avatar!=null && avatar.startsWith("http")) usericonurl=avatar;
                }

                if (!json.isNull("created_at")) date=json.getString("created_at");

                if (!json.isNull("url")) tooturl=json.getString("url");

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
            if (withIcon&&usericonurl!=null) DetIcons.downloadImg(this,usericonurl,username);
            String txt=json.getString("content");
            // look for URLs
            String[] words = txt.split(" ");
            for (String w : words)
                if (w.startsWith("href=")) {
                    int nextquote = w.indexOf('"',6);
                    if (nextquote<0) nextquote=w.length();
                    medias.add(w.substring(6,nextquote));
                }
            return txt.trim();
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

        String attrs="";
        if (boosted) attrs+="♡";
        if (medias.size()>0) attrs+="♭";
        if (parentid>=0) attrs+="↑";

        if (username!=null) attrs+=username;
        if (date.length()>0) {
            Calendar c = ISO8601.toCalendar(date);
            TimeZone tz = TimeZone.getDefault();
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            formatter.setCalendar(c);
            formatter.setTimeZone(tz); // convert the date to the current user
            String datestr = formatter.format(c.getTime());
            attrs+=" <font color='#EE0000'>"+datestr+"</font>";
        }
        if (lang!=null) attrs+=" ("+lang+")";
        if (attrs.length()>0) return "<p>"+attrs+"</p>"+txt.trim();
        else return txt.trim();
    }
}

/**
 * Helper class for handling a most common subset of ISO 8601 strings
 * (in the following format: "2008-03-01T13:00:00+01:00"). It supports
 * parsing the "Z" timezone, but many other less-used features are
 * missing.
 */
class ISO8601 {
    /** Transform Calendar to ISO 8601 string. */
    public static String fromCalendar(final Calendar calendar) {
        Date date = calendar.getTime();
        String formatted = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").format(date);
        return formatted.substring(0, 22) + ":" + formatted.substring(22);
    }

    /** Get current date and time formatted as ISO 8601 string. */
    public static String now() {
        return fromCalendar(GregorianCalendar.getInstance());
    }

    /** Transform ISO 8601 string to Calendar. */
    public static Calendar toCalendar(final String iso8601string) {
        Calendar calendar = GregorianCalendar.getInstance();
        // String s = iso8601string.replace("Z", "+00:00");
        try {
            // s = s.substring(0, 22) + s.substring(23);  // to get rid of the ":"
            SimpleDateFormat datef = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            datef.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = datef.parse(iso8601string);
            calendar.setTime(date);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return calendar;
    }
}

