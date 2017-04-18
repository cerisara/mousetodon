package fr.xtof54.mousetodon;

import android.content.DialogInterface; 
import android.app.AlertDialog; 
import android.view.View;
import android.view.LayoutInflater;
import android.widget.TextView;
import android.app.Activity;
import android.widget.Button;
import java.util.ArrayList;

public class UserInput {
	static AlertDialog dialog;
	static NextAction next;
    public static void show(Activity main, final NextAction next) {
	    UserInput.next=next;

        LayoutInflater inflater = LayoutInflater.from(main);
        final View dialogview = inflater.inflate(R.layout.userinput, null);


        dialog = new AlertDialog.Builder(main).create();
        dialog.setTitle("Enter creds");
        //dialog.setMessage("Enter creds");
        dialog.setView(dialogview);
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,"OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        TextView txt = (TextView)dialogview.findViewById(R.id.login);
                        String u = txt.getText().toString(); 
                        txt = (TextView)dialogview.findViewById(R.id.pwd);
                        String p = txt.getText().toString();
                        next.run(u+" "+p);
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
