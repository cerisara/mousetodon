package fr.xtof54.mousetodon;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import org.json.JSONException;
import org.json.JSONObject;

public class MouseApp extends Activity
{

    String instanceDomain = "octodon.social";
    Connect connect;

    String clientId=null, clientSecret=null;
    String useremail=null, userpwd=null;
    String atoken = null, rtoken = null;
    // static BasicCookieStore mycookiestore = new BasicCookieStore();

    SharedPreferences pref;
    private static String OAUTH_SCOPES = "read";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        pref = getSharedPreferences("MouseApp", MODE_PRIVATE);
        connect=new Connect(instanceDomain);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.quit:
			return true;
		case R.id.reset:
			resetClient();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}


    private void resetClient() {
        connect.registerApp(new NextAction() {
            public void run(String res) {
                try {
                    JSONObject json = new JSONObject(res);
                    Log.d("afterRegApp",json.toString());
                    String clientId = json.getString("client_id");
                    String clientSecret = json.getString("client_secret");
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString(String.format("client_id_for_%s", instanceDomain), clientId);
                    edit.putString(String.format("client_secret_for_%s", instanceDomain), clientSecret);
                    edit.commit();
                    userlogin();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void quit(View v) {
    }
    public void resetApp(View v) {
        resetClient();
    }

    public void detconnect(View v) {
        clientId = pref.getString(String.format("client_id_for_%s", instanceDomain), null);
        clientSecret = pref.getString(String.format("client_secret_for_%s", instanceDomain), null);
        Log.d("LoginTask", "client id saved: " + clientId);
        Log.d("LoginTask", "client secret saved: " + clientSecret);
        if(clientId == null || clientSecret == null) {
            resetClient();
        } else {
            userlogin();
        }
    }

    public void userok(View v) {
	    UserInput.userok(v);
    }
    public void userko(View v) {
	    UserInput.userko(v);
    }
    void askPwd() {
        UserInput.show(this, new NextAction() {
            public void run(String res) {
                String[] ss = res.split(" ");
                if (ss.length==2) {
                    useremail=ss[0];
                    userpwd=ss[1];
                    SharedPreferences.Editor edit = pref.edit();
                    edit.putString(String.format("user_for_%s", instanceDomain), useremail);
                    edit.putString(String.format("pswd_for_%s", instanceDomain), userpwd);
                    edit.commit();
                }
            }
        });
    }

    void userlogin() {
        useremail = pref.getString(String.format("user_for_%s", instanceDomain), null);
        userpwd   = pref.getString(String.format("pswd_for_%s", instanceDomain), null);
        if(useremail == null || userpwd == null) {
            askPwd();
        } else {
            connect.userLogin(clientId,clientSecret,useremail, userpwd, new NextAction() {
                public void run(String res) {
                    try {
                        JSONObject json = new JSONObject(res);
                        Log.d("afterLogin",json.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

}
