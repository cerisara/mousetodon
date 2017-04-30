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
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MouseApp extends Activity {
    public static MouseApp main=null;
    public static String access_token=null;
    public static String tmpfiledir=null;
    private boolean detectlang = false, resetTL = true;

    public boolean appRegistered=false;
    public boolean userLogged=false;
    public int maxid=-1;
    public int lastTL=-1;

    ArrayList<DetToot> toots = new ArrayList<DetToot>();
    ArrayList<DetToot> savetoots = new ArrayList<DetToot>();
    String[] filterlangs = null;

    WebView wv;

    ArrayList<String> allinstances=new ArrayList<String>();
    int curAccount=0;
    String instanceDomain = "";
    Connect connect=null;

    String clientId=null, clientSecret=null;
    String useremail=null, userpwd=null;
    // static BasicCookieStore mycookiestore = new BasicCookieStore();

    SharedPreferences pref;
    private static String OAUTH_SCOPES = "read";
    CustomList adapter=null;
    public static ArrayList<Bitmap> imgsinrow = new ArrayList<Bitmap>();

    public static int curtootidx = -1;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        MouseApp.main=this;
        setContentView(R.layout.main);
        pref = getSharedPreferences("MouseApp", MODE_PRIVATE);
        ArrayList<String> tmp = new ArrayList<String>();
        adapter = new CustomList(MouseApp.main, tmp);
        ListView list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position>=toots.size()) getOlderToots();
                else {
                    curtootidx = position;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            VisuToot.show(curtootidx);
                        }
                    });
                }
            }
        });

        File d = getExternalCacheDir();
        File mouseappdir = new File(d, "mouseappdir");
        mouseappdir.mkdirs();
        tmpfiledir=mouseappdir.getAbsolutePath();
        Log.d("CACHEDIR",tmpfiledir);
        imgsinrow.clear();

        connect = new Connect();
        wv = (WebView)findViewById(R.id.web1);
        wv.getSettings().setJavaScriptEnabled(true);
        wv.setWebViewClient(connect);
        wv.addJavascriptInterface(new MyJavaScriptInterface(), "INTERFACE"); 

        serverStage0();
    }

    public void serverStage0() {
        // check if we know the instance
        String s = pref.getString("mouseapp_insts", null);
        if (s!=null) {
            String[] ss = s.split(" ");
            allinstances.clear();
            for (String x: ss) allinstances.add(x);
        }
        instanceDomain = pref.getString("mouseapp_inst0", null);
        if (instanceDomain==null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    UserInput.show(main, new NextAction() {
                        public void run(String res) {
                            String[] ss = res.split(" ");
                            if (ss.length>2) {
                                useremail=ss[0];
                                userpwd=ss[1];
                                instanceDomain=ss[2];
                                if (useremail.length()==0 || userpwd.length()==0 || instanceDomain.length()==0)
                                    message("All 3 fields are mandatory");
                                else {
                                    SharedPreferences.Editor edit = pref.edit();
                                    edit.putString("mouseapp_inst0", instanceDomain);
                                    MouseApp.main.allinstances.add(instanceDomain);
                                    String s = "";
                                    for (String iss: MouseApp.main.allinstances) s+=iss+" ";
                                    s=s.trim();
                                    edit.putString("mouseapp_insts", s);
                                    edit.putString(String.format("user_for_%s", instanceDomain), useremail);
                                    edit.putString(String.format("pswd_for_%s", instanceDomain), userpwd);
                                    edit.commit();
                                    serverStage1();
                                }
                            }
                        }
                    });
                }
            });
        } else {
            if (s==null) {
                // bugfix: to remove
                SharedPreferences.Editor edit = pref.edit();
                edit.putString("mouseapp_insts", instanceDomain);
                edit.commit();
                allinstances.add(instanceDomain);
            }
            serverStage1();
        }
    }
    public void serverStage1() {
        // check if the app is registered
        connect.setInstance(instanceDomain);
        clientId = pref.getString(String.format("client_id_for_%s", instanceDomain), null);
        clientSecret = pref.getString(String.format("client_secret_for_%s", instanceDomain), null);
        if (clientId==null||clientSecret==null) {
            appRegistered=false;
            startWaitingWindow("Trying to register client...");
            connect.registerApp(new NextAction() {
                public void run(String res) {
                    boolean goon=false;
                    try {
                        if (res==null) message("ERROR instance");
                        else {
                            JSONObject json = new JSONObject(res);
                            if (json!=null) {
                                Log.d("afterRegApp",json.toString());
                                String clientId = json.getString("client_id");
                                String clientSecret = json.getString("client_secret");
                                if (clientId!=null&&clientSecret!=null) {
                                    SharedPreferences.Editor edit = pref.edit();
                                    edit.putString(String.format("client_id_for_%s", instanceDomain), clientId);
                                    edit.putString(String.format("client_secret_for_%s", instanceDomain), clientSecret);
                                    edit.commit();
                                    MouseApp.main.appRegistered=true;
                                    message("App registered on "+instanceDomain);
                                    // everytime we register a new app, we automatically login the user
                                    goon=true;
                                } else message("Problem client registration");
                            } else message("Problem client json registration");
                        }
                    } catch (JSONException e) {
                        MouseApp.main.appRegistered=false;
                        message("error when registrating");
                        e.printStackTrace();
                    } finally {
                        stopWaitingWindow();
                    }
                    if (goon) serverStage2();
                }
            });
        } else {
            serverStage2();
        }
    }

    public void serverStage2() {
        // check login
        useremail = pref.getString(String.format("user_for_%s", instanceDomain), null);
        userpwd   = pref.getString(String.format("pswd_for_%s", instanceDomain), null);
        clientId = pref.getString(String.format("client_id_for_%s", instanceDomain), null);
        clientSecret = pref.getString(String.format("client_secret_for_%s", instanceDomain), null);
        if (clientId==null || clientSecret==null || connect==null || useremail==null || userpwd==null) message("ERROR no user creds");
        else {
            startWaitingWindow("Trying to login...");
            connect.userLogin(clientId,clientSecret,useremail, userpwd, new NextAction() {
                public void run(String res) {
                    if (res==null) {
                        message("Error at login ?");
                        stopWaitingWindow();
                        return;
                    }
                    try {
                        JSONObject json = new JSONObject(res);
                        Log.d("afterLogin",json.toString());
                        access_token = json.getString("access_token");
                        if (access_token!=null) {
                            System.out.println("AAAAAAAAAAAAccess "+access_token);
                            message("Login OK");
                            MouseApp.main.userLogged=true;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        message("error when login");
                        MouseApp.main.userLogged=false;
                    } finally {
                        stopWaitingWindow();
                    }
                }
            });
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save the user's current game state
        //
        // TODO
        //
        // savedInstanceState.putInt(STATE_SCORE, mCurrentScore);
        // savedInstanceState.putInt(STATE_LEVEL, mCurrentLevel);

        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        

        // Restore state members from saved instance
        // mCurrentScore = savedInstanceState.getInt(STATE_SCORE);
        // mCurrentLevel = savedInstanceState.getInt(STATE_LEVEL);
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
                    waitwin=ProgressDialog.show(MouseApp.main,"Connect "+instanceDomain+"...",s);
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
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                adapter.clear();
                imgsinrow.clear();
                for (DetToot t: toots) {
                    adapter.add(t.getStr());
                    imgsinrow.add(t.getUserIcon());
                }
                adapter.add("[Press to get older toots]");
                adapter.notifyDataSetChanged();
            }
        });
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
		case R.id.otherCreds:
            addAccount();
			return true;
		case R.id.delinst:
            delInstance();
			return true;
		case R.id.nolang:
            detectlang=!detectlang;
            if (detectlang) filterlang(); else filterlangs=null;
			return true;
		case R.id.reset:
			serverStage0();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    void delInstance() {
        if (instanceDomain==null) {
            message("ERROR: no instance ?");
            return;
        }
        if (pref==null) {
            message("ERROR: no prefs ?");
            return;
        }
        int i=-1;
        for (i=0;i<allinstances.size();i++) {
            if (allinstances.get(i).equals(instanceDomain)) break;
        }
        if (i<0) message("instance "+instanceDomain+" not found");
        else {
            allinstances.remove(i);
            SharedPreferences.Editor edit = pref.edit();
            edit.remove(String.format("user_for_%s", instanceDomain));
            edit.remove(String.format("pswd_for_%s", instanceDomain));
            edit.remove(String.format("client_id_for_%s", instanceDomain));
            edit.remove(String.format("client_secret_for_%s", instanceDomain));
            edit.remove(String.format("langs_for_%s", instanceDomain));
            edit.remove("mouseapp_inst0");
            {
                String s = "";
                for (String iss: MouseApp.main.allinstances) s+=iss+" ";
                s=s.trim();
                edit.putString("mouseapp_insts", s);
            }
            edit.commit();
            message("instance "+instanceDomain+" removed !");
            clientId=null; clientSecret=null;
            useremail=null; userpwd=null;
            filterlangs=null;
            instanceDomain="";
            nextAccount(null);
        }
    }

    void addAccount() {
        if (instanceDomain==null) {
            message("Must have a first instance");
            return;
        }
        SharedPreferences.Editor edit = pref.edit();
        edit.remove("mouseapp_inst0");
        edit.commit();
        curAccount++;
        serverStage0();
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

    public void getOlderToots() {
        for (int i=toots.size()-1;i>=0;i--)
            if (toots.get(i).id>=0) {
                maxid=toots.get(i).id-1;
                break;
            }
        if (connect!=null&&userLogged)
            switch(lastTL) {
                case 0: 
                    getToots("timelines/home"); break;
                case 1: 
                    getNotifs("notifications"); break;
                case 2: 
                    getToots("timelines/public"); break;
                default:
                    message("ERROR OLDER TOOTS");
            }
        else message("not connected");
    }

    private void checkInstance() {
        if (instanceDomain==null && allinstances.size()>0) {
            instanceDomain=allinstances.get(0);
            message("instance: "+instanceDomain);
        }
    }
    public void closeOnetoot(View v) {
        VisuToot.close();
        updateList();
    }
    public void boost(View v) {
        boost();
    }
    public void unboost(View v) {
        unboost();
    }
    public void replyhist(View v) {
        showReplyHistory();
    }
    public void publicTL(View v) {
        checkInstance();
        lastTL=2;
        maxid=-1;
        if (connect!=null&&userLogged)
            getToots("timelines/public");
        else message("not connected");
    }
    public void homeTL(View v) {
        checkInstance();
        lastTL=0;
        maxid=-1;
        if (connect!=null&&userLogged)
            getToots("timelines/home");
        else message("not connected");
    }
    public void noteTL(View v) {
        checkInstance();
        lastTL=1;
        maxid=-1;
        if (connect!=null&&userLogged)
            getNotifs("notifications");
        else message("not connected");
    }
    public void reply(View v) {
        writeToot(null);
    }
    public void writeToot(View v) {
        if (connect==null||!userLogged) {
            message("not connected");
            return;
        }
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                UserWritings.show(main, new NextAction() {
                    public void run(String res) {
                        if (res.length()>0) {
                            startWaitingWindow("Sending toot...");
                            connect.sendToot(res, new NextAction() {
                                public void run(String res) {
                                    stopWaitingWindow();
                                }
                            });
                        }
                    }
                });
            }
        });
    }
    public void quit(View v) {
    }
    public void nextAccount(View v) {
        if (++curAccount>=allinstances.size()) curAccount=0;
        SharedPreferences.Editor edit = pref.edit();
        if (allinstances.size()==0) edit.remove("mouseapp_inst0");
        else {
            edit.putString("mouseapp_inst0", allinstances.get(curAccount));
            message("instance: "+allinstances.get(curAccount));
        }
        edit.commit();
        serverStage0();
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

    void unboost() {
        if (curtootidx>=0 && toots.get(curtootidx).id>=0 && toots.get(curtootidx).boosted) {
            startWaitingWindow("Unboosting toot...");
            connect.unboost(toots.get(curtootidx).id, new NextAction() {
                public void run(String res) {
                    stopWaitingWindow();
                    // TODO check if error here
                    toots.get(curtootidx).boosted=false;
                }
            });
        } else {
            message("Cannot unboost: no toot id");
        }
    }

    void boost() {
        if (curtootidx>=0 && toots.get(curtootidx).id>=0 && !toots.get(curtootidx).boosted) {
            startWaitingWindow("Boosting toot...");
            connect.boost(toots.get(curtootidx).id, new NextAction() {
                public void run(String res) {
                    stopWaitingWindow();
                    // TODO check if error here
                    toots.get(curtootidx).boosted=true;
                }
            });
        } else {
            message("Cannot boost: no toot id");
        }
    }
    void getToots(final String tl) {
        startWaitingWindow("Getting toots... "+Integer.toString(maxid));
        connect.getTL(tl,new NextAction() {
            public void run(String res) {
                try {
                    JSONArray json = new JSONArray(res);
                    System.out.println("GOTTOOTS "+Integer.toString(json.length()));
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

    private DetToot downloadOneToot(final int id) {
        String res = connect.getSyncToot(id);
        if (res==null) {
            message("error getting toot");
            return null;
        }
        try {
            JSONObject o = new JSONObject(res);
            DetToot dt = new DetToot(o,false);
            return dt;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    void showReplyHistory() {
        if (curtootidx<0||curtootidx>=toots.size()) {
            message("no initial toot");
            return;
        }
        final DetToot tt = toots.get(curtootidx);
        if (tt.id<0) {
            message("no initial toot");
            return;
        }
        // y a-t-il des toots parents ?
        if (tt.parentid<0) {
            message("no parent toot");
            return;
        }
        Thread histthread = new Thread(new Runnable() {
            public void run() {
                int toot2download = tt.parentid;
                savetoots.clear();
                for (DetToot t : toots) savetoots.add(t);
                toots.clear();
                toots.add(tt);
                startWaitingWindow("Getting toot... "+Integer.toString(toot2download));
                for (int i=0;i<20;i++) {
                    DetToot ttt = downloadOneToot(toot2download);
                    if (i==0) stopWaitingWindow();
                    if (ttt==null) break;
                    toots.add(0,ttt);
                    if (i==0) VisuToot.close();
                    updateList();
                    toot2download = ttt.parentid;
                    if (toot2download<0) break;
                }
            }
        });
        histthread.start();
    }
    public NextAction jsnext=null;
}

class MyJavaScriptInterface {
    public MyJavaScriptInterface() {
    }

    @SuppressWarnings("unused")
    public void processContent(String aContent) {
        System.out.println("texttread "+aContent);
        if (aContent.startsWith("DETOK")) {
            aContent=aContent.substring(5).trim();
            if (aContent.startsWith("{\"error\":")) {
                MouseApp.main.message("error:"+aContent.substring(10));
            } else MouseApp.main.message("connect OK");
        } else if (aContent.startsWith("DETKO")) {
            MouseApp.main.message("error connect");
        }
        if (MouseApp.main.jsnext!=null) {
            NextAction a=MouseApp.main.jsnext;
            MouseApp.main.jsnext=null;
            a.run(aContent);
        }
    }
}

