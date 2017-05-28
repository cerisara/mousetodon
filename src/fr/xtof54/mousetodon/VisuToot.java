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
import android.widget.ToggleButton;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import android.graphics.Bitmap;
import android.content.Intent;
import android.text.Html;

public class VisuToot {
	private static AlertDialog dialog=null;

    public static void close() {
        if (dialog!=null) {
            dialog.dismiss();
            dialog=null;
        }
    }
    public static void show(final DetToot toot) {
        Activity main = MouseApp.main;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.onetoot, null);

        ImageView imageView = (ImageView) dialogview.findViewById(R.id.autor);
        if (toot.getUserIcon()!=null) {
            Bitmap bMap = toot.getUserIcon();
            imageView.setImageBitmap(bMap);
        }
        TextView txtv = (TextView) dialogview.findViewById(R.id.texttoot);
        String ttxt = toot.getStr();
        if (ttxt!=null) txtv.setText(Html.fromHtml(ttxt));

        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("Toot ID: "+Integer.toString(toot.id));
        dialog.setView(dialogview);
        dialog.show();

        {
            ToggleButton fav = (ToggleButton)dialogview.findViewById(R.id.favbut);
            fav.setChecked(toot.boosted);
            ToggleButton follow = (ToggleButton)dialogview.findViewById(R.id.folbut);
            // TODO: where to get followers ? Store it on disk !
            if (toot.medias.size()>0) {
                ArrayAdapter adapt = new ArrayAdapter(MouseApp.main, R.layout.rowtext, toot.medias);
                if (adapt==null) MouseApp.main.message("ERROR: media list");
                else {
                    ListView list=(ListView)dialog.findViewById(R.id.medialist);
                    list.setAdapter(adapt);
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int medpos, long id) {
                            String surl = toot.medias.get(medpos);
                            if (!surl.startsWith("http")) MouseApp.main.message("not http: unsupported");
                            else {
                                Uri uri = Uri.parse(surl);
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                MouseApp.main.startActivity(intent);
                            }
                        }
                    });
                } 
            }
        }

    }
}
