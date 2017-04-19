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
import org.json.JSONArray;
import java.io.File;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.FileWriter;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

public class MouseApp extends Activity
{
    public static MouseApp main=null;
    public static String access_token=null;
    public static String tmpfiledir=null;

    ArrayList<DetToot> toots = new ArrayList<DetToot>();

    String instanceDomain = "octodon.social";
    Connect connect;

    String clientId=null, clientSecret=null;
    String useremail=null, userpwd=null;
    String atoken = null, rtoken = null;
    // static BasicCookieStore mycookiestore = new BasicCookieStore();

    SharedPreferences pref;
    private static String OAUTH_SCOPES = "read";
    CustomList adapter=null;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MouseApp.main=this;
        setContentView(R.layout.main);
        pref = getSharedPreferences("MouseApp", MODE_PRIVATE);
        connect=new Connect(instanceDomain);
        {
            ArrayList<String> tmp = new ArrayList<String>();
            for (int i=0;i<10;i++) {
                tmp.add("Emtpy");
            }
            adapter = new CustomList(MouseApp.main, tmp);
        }
        ListView list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Toast.makeText(MouseApp.this, "You Clicked at " +Integer.toString(position), Toast.LENGTH_SHORT).show();
                        toots.get(position).detectlang();
                        Toast.makeText(MouseApp.this, "Lang: " +toots.get(position).lang, Toast.LENGTH_SHORT).show();
                    }
                });

        File d = getExternalCacheDir();
        File mouseappdir = new File(d, "mouseappdir");
        mouseappdir.mkdirs();
        tmpfiledir=mouseappdir.getAbsolutePath();
        Log.d("CACHEDIR",tmpfiledir);
    }

    void updateList() {
        adapter.clear();
        for (DetToot t: toots) {
            adapter.add(t.txt);
        }
        adapter.notifyDataSetChanged();
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

    public void publicTL(View v) {
        getToots();
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

    void askPwd() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                UserInput.show(main, new NextAction() {
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
                        access_token = json.getString("access_token");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    void getToots() {
         connect.getFederatedTL(new NextAction() {
            public void run(String res) {
                try {
                    PrintWriter fout = new PrintWriter(new FileWriter(tmpfiledir+"/example.html"));
                    fout.println(res);
                    fout.close();

                    // JSONObject json = new JSONObject(res);
                    Log.d("afterPublicTL",res);
                    JSONArray json = new JSONArray(res);
                    toots.clear();
                    for (int i=0;i<json.length();i++) {
                        JSONObject o = (JSONObject)json.get(i);
                        String txt=o.getString("content");
                        toots.add(new DetToot(txt));
                    }
                    updateList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
