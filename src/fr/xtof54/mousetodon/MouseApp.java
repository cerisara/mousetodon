package fr.xtof54.mousetodon;

import android.app.Activity;
import android.app.ProgressDialog;
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
    private boolean detectlang = false, resetTL = true;

    ArrayList<DetToot> toots = new ArrayList<DetToot>();
    String[] filterlangs = null;

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
                        // TODO
                    }
                });

        File d = getExternalCacheDir();
        File mouseappdir = new File(d, "mouseappdir");
        mouseappdir.mkdirs();
        tmpfiledir=mouseappdir.getAbsolutePath();
        Log.d("CACHEDIR",tmpfiledir);

        detconnect(null);
    }

    void message(final String s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                Toast.makeText(MouseApp.this, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
    private ProgressDialog waitwin = null;
    void startWaitingWindow(final String s) {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                if (waitwin==null) {
                    waitwin=ProgressDialog.show(MouseApp.main,"Network operation...",s);
                }
            }
        });
    }
    void stopWaitingWindow() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                if (waitwin!=null) {
                    waitwin.dismiss();
                    waitwin=null;
                }
            }
        });
    }

    void updateList() {
        adapter.clear();
        for (DetToot t: toots) {
            adapter.add(t.getStr());
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
		case R.id.nolang:
            detectlang=!detectlang;
            if (detectlang) filterlang(); else filterlangs=null;
			return true;
		case R.id.reset:
			resetClient();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    void filterlang() {
        String langs = pref.getString(String.format("langs_for_%s", instanceDomain), null);
        if (langs==null) langs="";
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                LangInput.show(main, new NextAction() {
                    public void run(String res) {
                        String[] ss = res.split(" ");
                        if (ss.length>0) {
                            SharedPreferences.Editor edit = pref.edit();
                            edit.putString(String.format("langs_for_%s", instanceDomain), res);
                            edit.commit();
                            filterlangs = ss;
                        }
                    }
                });
            }
        });

    }

    private void resetClient() {
        startWaitingWindow("Trying to connect...");
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
                    stopWaitingWindow();
                    userlogin();
                } catch (JSONException e) {
                    stopWaitingWindow();
                    message("error when connecting");
                    e.printStackTrace();
                }
            }
        });
    }

    public void publicTL(View v) {
        getToots("timelines/public");
    }
    public void homeTL(View v) {
        getToots("timelines/home");
    }
    public void noteTL(View v) {
        getNotifs("notifications");
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
            startWaitingWindow("Trying to login...");
            connect.userLogin(clientId,clientSecret,useremail, userpwd, new NextAction() {
                public void run(String res) {
                    try {
                        JSONObject json = new JSONObject(res);
                        Log.d("afterLogin",json.toString());
                        access_token = json.getString("access_token");
                        if (access_token!=null) message("Login OK");
                    } catch (JSONException e) {
                        e.printStackTrace();
                        message("error when login");
                    } finally {
                        stopWaitingWindow();
                    }
                }
            });
        }
    }

    private NextAction torun = null;

    void getStatus(final int id) {
        connect.getTL("statuses/"+Integer.toString(id),new NextAction() {
            public void run(String res) {
                try {
                    if (torun!=null) {
                        torun.run(res);
                        torun=null;
                    } else {
                        JSONObject json = new JSONObject(res);
                        // TODO
                    }
                } catch (Exception e) {
                }
            }
        });
    }

    ArrayList<String> getStatuses(ArrayList<Integer> ids) {
        ArrayList<String> lres = new ArrayList<String>();
        for (int id: ids) {
            String res = connect.blockGetTL("statuses/"+Integer.toString(id));
            lres.add(res);
        }
        return lres;
    }


    void getNotifs(final String tl) {
        startWaitingWindow("Getting notifs...");
        connect.getTL(tl,new NextAction() {
            public void run(String res) {
                try {
                    JSONArray json = new JSONArray(res);
                    if (resetTL) toots.clear();

                    // just pre-download list of notifications
                    ArrayList<Integer> idx = new ArrayList<Integer>();
                    final ArrayList<Integer> ids = new ArrayList<Integer>();
                    for (int i=0;i<json.length();i++) {
                        JSONObject o = (JSONObject)json.get(i);
                        String typ = o.getString("type");
                        if (typ.equals("mention")) {
                            JSONObject toot =o.getJSONObject("status");
                            int id = toot.getInt("id");
                            DetToot dt = new DetToot("mention: "+Integer.toString(id));
                            idx.add(toots.size());
                            ids.add(id);
                            toots.add(dt);
                        } else if (typ.equals("follow")) {
                            if (!o.isNull("account")) {
                                JSONObject acc = o.getJSONObject("account");
                                String aut = acc.getString("username")+": ";
                                DetToot dt = new DetToot("followed by: "+aut);
                                toots.add(dt);
                            } else {
                                DetToot dt = new DetToot("unhandled type: "+typ);
                                toots.add(dt);
                            }
                        } else if (typ.equals("favourite")) {
                            if (!o.isNull("account")) {
                                JSONObject acc = o.getJSONObject("account");
                                String aut = acc.getString("username")+": ";
                                DetToot dt = new DetToot("favourite by: "+aut);
                                dt.txt+=dt.getText(o.getJSONObject("status"));
                                toots.add(dt);
                            } else {
                                DetToot dt = new DetToot("unhandled type: "+typ);
                                toots.add(dt);
                            }
                        } else if (typ.equals("reblog")) {
                            if (!o.isNull("account")) {
                                JSONObject acc = o.getJSONObject("account");
                                String aut = acc.getString("username")+": ";
                                DetToot dt = new DetToot("reblog by: "+aut);
                                dt.txt+=dt.getText(o.getJSONObject("status"));
                                toots.add(dt);
                            } else {
                                DetToot dt = new DetToot("unhandled type: "+typ);
                                toots.add(dt);
                            }
                        } else {
                            DetToot dt = new DetToot("unhandled type: "+typ);
                            toots.add(dt);
                        }
                    }

                    // now download all statutes
                    ArrayList<String> stats = getStatuses(ids);
                    for (int i=0;i<stats.size();i++) {
                        JSONObject o = new JSONObject(stats.get(i));
                        toots.set(idx.get(i),new DetToot(o,detectlang));
                    }
                    updateList();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stopWaitingWindow();
                }
            }
        });
    }

    void getToots(final String tl) {
        startWaitingWindow("Getting toots...");
        connect.getTL(tl,new NextAction() {
            public void run(String res) {
                try {
                    JSONArray json = new JSONArray(res);
                    if (resetTL) toots.clear();
                    for (int i=0;i<json.length();i++) {
                        JSONObject o = (JSONObject)json.get(i);
                        DetToot dt = new DetToot(o,detectlang);
                        if (dt.lang==null || filterlangs==null) toots.add(dt);
                        else {
                            for (String s: filterlangs) {
                                if (s.equals(dt.lang)) {
                                    toots.add(dt);
                                    break;
                                }
                            }
                        }
                    }
                    updateList();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    stopWaitingWindow();
                }
            }
        });
    }
}
