package fr.xtof54.mousetodon;

import android.widget.ImageView;
import android.content.DialogInterface; 
import android.app.AlertDialog; 
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Button;
import java.util.ArrayList;
import android.graphics.Bitmap;

public class VisuToot {
	static AlertDialog dialog=null;

    public static void close() {
        if (dialog!=null) {
            dialog.dismiss();
            dialog=null;
        }
    }
    public static void show(int position) {
        Activity main = MouseApp.main;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.onetoot, null);

        ImageView imageView = (ImageView) dialogview.findViewById(R.id.autor);
        if (MouseApp.imgsinrow.size()>position) {
            Bitmap bMap = MouseApp.imgsinrow.get(position);
            imageView.setImageBitmap(bMap);
        }

        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("Toot details");
        //dialog.setMessage("Enter creds");
        dialog.setView(dialogview);
        dialog.show();
    }
}
