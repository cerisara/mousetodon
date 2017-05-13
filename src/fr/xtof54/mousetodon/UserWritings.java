package fr.xtof54.mousetodon;

import android.content.DialogInterface; 
import android.app.AlertDialog; 
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Button;
import java.util.ArrayList;
import android.text.TextWatcher;
import android.text.Editable;

public class UserWritings {
	static AlertDialog dialog;
	static NextAction next;

    public static void show4tags(Activity main, final NextAction next) {
	    LangInput.next=next;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.tootinput, null);
        final TextView txt = (TextView)dialogview.findViewById(R.id.toots);

        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("Search tag ?");
        dialog.setView(dialogview);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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

    public static void show(Activity main, final NextAction next) {
	    LangInput.next=next;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.tootinput, null);
        final TextView txt = (TextView)dialogview.findViewById(R.id.toots);

        if (MouseApp.main.tootselected!=null) txt.setText("@"+MouseApp.main.tootselected.username+" ");

        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                int nchars = s.length();
                dialog.setTitle("char length: "+Integer.toString(nchars));
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
        });

        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("Please write your toot");
        dialog.setView(dialogview);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
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
