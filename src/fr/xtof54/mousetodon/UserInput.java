package fr.xtof54.mousetodon;

import android.content.DialogInterface; 
import android.app.Dialog; 


public class UserInput extends Dialog {
    public static void show(final NextAction next) {
        final Dialog dialog = new Dialog(getActivity());
        dialog.setContentView(R.layout.userinput);
        dialog.setTitle("Enter user creds");
        dialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            TextView txt = (TextView)dialog.findViewById(R.id.login);
                            String u = txt.getText().toString(); 
                            txt = (TextView)dialog.findViewById(R.id.pwd);
                            String p = txt.getText().toString();
                            dialog.cancel();
                            next.run(u+" "+p);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            next.run("");
                        }
                    });
        dialog.show();
    }
}
