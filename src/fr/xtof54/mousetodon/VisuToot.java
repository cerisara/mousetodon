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

public class VisuToot {
	static AlertDialog dialog=null;

    public static void close() {
        MouseApp.curtootidx=-1;
        if (dialog!=null) {
            dialog.dismiss();
            dialog=null;
        }
    }
    public static void show(final int position) {
        Activity main = MouseApp.main;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.onetoot, null);

        ImageView imageView = (ImageView) dialogview.findViewById(R.id.autor);
        if (MouseApp.imgsinrow.size()>position) {
            Bitmap bMap = MouseApp.imgsinrow.get(position);
            imageView.setImageBitmap(bMap);
        }
        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("Toot medias & details");
        dialog.setView(dialogview);
        dialog.show();

        if (MouseApp.main.toots.size()>position) {
            if (MouseApp.main.toots.get(position).medias.size()>0) {
                ArrayAdapter adapt = new ArrayAdapter(MouseApp.main, R.layout.rowtext, MouseApp.main.toots.get(position).medias);
                if (adapt==null) MouseApp.main.message("ERROR: media list");
                else {
                    ListView list=(ListView)dialog.findViewById(R.id.medialist);
                    list.setAdapter(adapt);
                    list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int medpos, long id) {
                            String surl = MouseApp.main.toots.get(position).medias.get(medpos);
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
