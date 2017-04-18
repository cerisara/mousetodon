package fr.xtof54.mostodon;

import java.util.ArrayList;
import java.util.List;
import com.optimaize.langdetect.*;
import com.optimaize.langdetect.profiles.*;
import com.optimaize.langdetect.ngram.*;

public class Toot {
    static LangDetect langdetect = null;
    static boolean langdetectpossible = true;
    String txt, lang;

    public void detectlang() {
        if (!langdetectpossible) return;
        if (langdetect==null) {
            langdetect = new LangDetect();
            if (langdetect==null) langdetectpossible=false;
        }
        if (langdetectpossible) {
            lang = langdetect.detect(txt);
        }
    }
}

class LangDetect {
    LanguageDetector languageDetector;

    public LangDetect() {
        try {
            List<LanguageProfile> languageProfiles = new LanguageProfileReader().readAll();
            languageDetector = LanguageDetectorBuilder.create(NgramExtractors.standard())
                .withProfiles(languageProfiles)
                .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    } 

    public String detect(String text) {
        List<DetectedLanguage> res = languageDetector.getProbabilities(text);
        String bestl="";
        float bestp=-1;
        for (DetectedLanguage l : res) {
            String[] ll=l.toString().replace('[',' ').replace(']',' ').replace(':',' ').split(" ");
            String la = ll[1];
            float p = Float.parseFloat(ll[2]);
            if (p>bestp) {
                bestp=p;
                bestl=la;
            }
        }
        return bestl;
    }
}



