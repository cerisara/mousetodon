package fr.xtof54.mousetodon;

import android.net.Uri;
import org.json.JSONObject;
import org.json.JSONArray;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ImageView;
import android.content.DialogInterface; 
import android.app.AlertDialog; 
import android.view.View;
import android.view.WindowManager;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Button;
import android.widget.ToggleButton;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.content.Intent;

public class VisuHistory {
	private static AlertDialog dialog=null;
    private static CustomList adapt=null;
    private static boolean isfirst=true;
    private static ArrayList<DetToot> tootlist = new ArrayList<DetToot>();

    public static void close() {
        if (dialog!=null) {
            dialog.dismiss();
            dialog=null;
        }
    }
    public static void show() {
        isfirst=true;
        Activity main = MouseApp.main;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.history, null);

        ArrayList<String> oo = new ArrayList<String>();
        for (int i=0;i<20;i++) oo.add("please wait...");
        adapt = new CustomList(MouseApp.main, oo);
        tootlist.clear();

        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("User infos");
        dialog.setView(dialogview);
        dialog.show();

        ListView list=(ListView)dialog.findViewById(R.id.histtootlist);
        System.out.println("LLLLLLLIST "+list);
        list.setAdapter(adapt);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                {
                    MouseApp.main.tootselected=tootlist.get(position);
                    MouseApp.main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            VisuHistory.close();
                            VisuToot.show(MouseApp.main.tootselected);
                        }
                    });
                }
            }
        });
    }
    public static void addToot(final DetToot dt) {
        System.out.println("ADDTOOOT "+dt.getStr());
        // TODO: put the img at the right place
        MouseApp.main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (isfirst) {
                    adapt.initempty();
                    isfirst=false;
                }
                adapt.add(dt.getStr());
                tootlist.add(dt);
                adapt.imgsinrow.add(dt.getUserIcon());
                adapt.notifyDataSetChanged();
            }
        });
    }
}
