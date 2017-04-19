package fr.xtof54.mousetodon;

import java.util.ArrayList;
import java.util.List;
import com.optimaize.langdetect.*;
import com.optimaize.langdetect.profiles.*;
import com.optimaize.langdetect.ngram.*;

public class DetToot {
    static LangDetect langdetect = null;
    static boolean langdetectpossible = true;
    String txt, lang=null;

    public DetToot(String t) {
        txt=t;
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


