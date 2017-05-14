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

public class VisuUser {
	private static AlertDialog dialog=null;
    public static int curuserid=-1;
    private static ArrayList<DetToot> tootlist = new ArrayList<DetToot>();

    public static void close() {
        if (dialog!=null) {
            dialog.dismiss();
            dialog=null;
        }
    }
    public static void show(final DetToot user) {
        curuserid = user.autid;
        Activity main = MouseApp.main;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.oneuser, null);

        ArrayList<String> oo = new ArrayList<String>();
        for (int i=0;i<20;i++) oo.add("EEEE");
        final CustomList adapt = new CustomList(MouseApp.main, oo);
        tootlist.clear();

        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("User "+user.username);
        dialog.setView(dialogview);
        dialog.show();

        ListView list=(ListView)dialog.findViewById(R.id.usertootlist);
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
                            VisuUser.close();
                            VisuToot.show(MouseApp.main.tootselected);
                        }
                    });
                }
            }
        });

        /*
         WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.MATCH_PARENT;
        */
        //dialog.getWindow().setAttributes(lp);

        {
            ToggleButton follow = (ToggleButton)dialogview.findViewById(R.id.folbut);
            // TODO: where to get followers ? Store it on disk !


            MouseApp.main.startWaitingWindow("downloading user toots");
            MouseApp.main.connect.getUserToots(curuserid, new NextAction() {
                public void run(final String res) {
                    MouseApp.main.stopWaitingWindow();
                    MouseApp.main.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                JSONArray json = new JSONArray(res);
                                System.out.println("USERTOOOOOOOOOOOO "+Integer.toString(json.length()));
                                adapt.clear();
                                tootlist.clear();
                                for (int i=0;i<json.length();i++) {
                                    JSONObject o = (JSONObject)json.get(i);
                                    DetToot dt = new DetToot(o,false);
                                    adapt.add(dt.getStr());
                                    tootlist.add(dt);
                                    System.out.println("USERTLLLLLLLO "+dt.getStr());
                                }
                                adapt.notifyDataSetChanged();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            });
        }
    }
}
