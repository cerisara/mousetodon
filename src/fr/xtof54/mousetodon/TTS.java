package fr.xtof54.mousetodon;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import java.util.ArrayList;

public class TTS implements OnInitListener {
    private TextToSpeech tts=null;

    @Override
    public void onInit(int status) {
        System.out.println("TTS INIT "+status);
    }

    public TTS() {
        tts = new TextToSpeech(MouseApp.main, this);
    }

    public void talk(String s) {
        tts.speak(s, TextToSpeech.QUEUE_ADD, null);
    }

    public static void startorstop() {
        if (MouseApp.main.tts==null) startit();
        else stopit();
        MouseApp.main.detectlang=true;
    }

    static void startit() {
        MouseApp.main.tts = new TTS();
    }
    static void stopit() {
        MouseApp.main.tts.tts.shutdown();
        MouseApp.main.tts = null;
    }

    public void silence() {
        tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
    }

    public void list2read(ArrayList<DetToot> toots) {
        int n=0;
        for (DetToot t: toots) {
            if (t.lang=="en") {
                String s = t.getOnlyStr();
                System.out.println("TTS......... "+s);
                String u = t.getOnlyUser();
                talk("User "+u+".");
                tts.playSilence(1000,TextToSpeech.QUEUE_ADD,null);
                talk(s);
                tts.playSilence(1000,TextToSpeech.QUEUE_ADD,null);
            }
            n++;
        }
    }
}

