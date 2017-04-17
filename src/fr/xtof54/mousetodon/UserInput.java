package fr.xtof54.mousetodon;

import android.content.DialogInterface; 
import android.app.Dialog; 
import android.view.View;
import android.widget.TextView;
import android.app.Activity;

public class UserInput {
	static Dialog dialog;
	static NextAction next;
    public static void show(Activity main, final NextAction next) {
	    UserInput.next=next;
        dialog = new Dialog(main);
        dialog.setContentView(R.layout.userinput);
        dialog.setTitle("Enter user creds");
        dialog.show();
    }
    public static void userok(View v) {
                            TextView txt = (TextView)dialog.findViewById(R.id.login);
                            String u = txt.getText().toString(); 
                            txt = (TextView)dialog.findViewById(R.id.pwd);
                            String p = txt.getText().toString();
                            dialog.cancel();
                            next.run(u+" "+p);
                        }
    public static void userko(View v) {
                            dialog.cancel();
                            next.run("");
                        }
}
