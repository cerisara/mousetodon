package fr.xtof54.mousetodon;

import android.content.DialogInterface; 
import android.app.AlertDialog; 
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Button;
import java.util.ArrayList;

public class UserWritings {
	static AlertDialog dialog;
	static NextAction next;
    public static void show(Activity main, final NextAction next) {
	    LangInput.next=next;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.tootinput, null);


        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("Please write your toot");
        dialog.setView(dialogview);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        TextView txt = (TextView)dialogview.findViewById(R.id.toots);
                        String u = txt.getText().toString(); 
                        next.run(u);
                    }
                });
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,"Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        next.run("");
                    }
                });
        dialog.show();
    }
}
