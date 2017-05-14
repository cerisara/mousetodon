package fr.xtof54.mousetodon;

import android.net.Uri;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ImageView;
import android.content.DialogInterface; 
import android.app.AlertDialog; 
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Button;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.content.Intent;
import java.io.File;
import android.os.Environment;

public class ExtraMenu {
	static AlertDialog dialog=null;

    public static void close() {
        if (dialog!=null) {
            dialog.dismiss();
            dialog=null;
        }
    }

    private static void clearCache() {
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + MouseApp.main.getApplicationContext().getPackageName()
                + "/Files");
        if (! mediaStorageDir.exists()) return;
        File[] ls = mediaStorageDir.listFiles();
        for (File f: ls) {
            f.delete();
        }
        MouseApp.main.message("files deleted");
    }

    public static void show() {
        Activity main = MouseApp.main;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.extramenu, null);

        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("Extra actions");
        dialog.setView(dialogview);
        dialog.show();

        ArrayList<String> actions = new ArrayList<String>();
        actions.add("clear cache");
        actions.add("add account");
        if (MouseApp.main.tts==null) actions.add("start TTS");
        else actions.add("stop TTS");

        ArrayAdapter adapt = new ArrayAdapter(MouseApp.main, R.layout.rowmenu, actions);
        if (adapt==null) MouseApp.main.message("ERROR extra actions");
        else {
            ListView list=(ListView)dialog.findViewById(R.id.actionlist);
            list.setAdapter(adapt);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                    ExtraMenu.close();
                    switch (pos) {
                        case 0: clearCache(); break;
                        case 1: MouseApp.main.addAccount(); break;
                        case 2: TTS.startorstop(); break;
                    }
                }
            });
        } 
    }
}
