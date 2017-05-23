package fr.xtof54.mousetodon;

import android.app.Activity;
import java.net.URLEncoder;
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
import android.widget.ToggleButton;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.concurrent.LinkedBlockingQueue;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemSelectedListener;

public class MouseApp extends Activity {
    public static MouseApp main=null;
    public static String access_token=null;
    public static String tmpfiledir=null;
    public boolean detectlang = false, resetTL = true;

    public boolean appRegistered=false;
    public boolean userLogged=false;
    public int lastTL=-1;

    private static LinkedBlockingQueue<Object[]> jstodownload = new LinkedBlockingQueue<Object[]>();
    private static final Object[] finfin = {null};
    private static Thread jsdownloader = null;
    static String[] waitres = {null};

    public TTS tts = null;
    private boolean delinstance = false;

    ArrayList<DetToot> toots = new ArrayList<DetToot>();
    ArrayList<DetToot> savetoots = new ArrayList<DetToot>();
    String[] filterlangs = null;

    private DetWebView wvimg;
    private WebView wvjs;

    private TootsManager tootsmgr=null;
    private boolean downloadInBackground=true;
    private String tags="";

    ArrayList<String> allinstances=new ArrayList<String>();
    String instanceDomain = "";
    Connect connect=null;

    String clientId=null, clientSecret=null;
    String useremail=null, userpwd=null;
    // static BasicCookieStore mycookiestore = new BasicCookieStore();

    SharedPreferences pref;
    private static String OAUTH_SCOPES = "read";
    CustomList adapter=null;
    public static ArrayList<Bitmap> imgsinrow = new ArrayList<Bitmap>();

    public static DetToot tootselected = null;

    @Override
    public void onPause() {super.onPause(); stopAll();}
    @Override
    public void onStop() {super.onStop(); stopAll();}
    @Override
    public void onDestroy() {super.onDestroy(); stopAll();}
    @Override
    public void onStart() {super.onStart(); jstodownload.clear(); DetIcons.init();}
    @Override
    public void onRestart() {super.onRestart(); jstodownload.clear(); DetIcons.init();}
    @Override
    public void onResume() {
        super.onResume();
        jstodownload.clear();
        DetIcons.init();
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MouseApp.main=this;
        jstodownload.clear(); DetIcons.init();
        setContentView(R.layout.main);
        pref = getSharedPreferences("MouseApp", MODE_PRIVATE);
        ArrayList<String> tmp = new ArrayList<String>();
        adapter = new CustomList(MouseApp.main, tmp);
        adapter.imgsinrow = imgsinrow;
        ListView list=(ListView)findViewById(R.id.list);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position>=toots.size()) getOlderToots();
                else {
                    tootselected=toots.get(position);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (tts!=null) tts.silence();
                            VisuToot.show(tootselected);
                        }
                    });
                }
            }
        });

        {
            final NDSpinner spinner = (NDSpinner)findViewById(R.id.spinner);
            String[] items = new String[]{"TL","Notifs", "Home", "Local", "Feder", "Search"};
            ArrayAdapter<String> spinadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
            spinner.setAdapter(spinadapter);
            spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                    if (instanceDomain==null && position>0) message("Login first !");
                    else {
                        switch(position) {
                            case 0: break;
                            case 1: noteTL(null);
                                break;
                            case 2: homeTL(null);
                                break;
                            case 3: localTL(null);
                                break;
                            case 4: publicTL(null);
                                break;
                            case 5: tagTL();
                                break;
                        }
                    }
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }

        setInstanceSpinner();

        File d = getExternalCacheDir();
        File mouseappdir = new File(d, "mouseappdir");
        mouseappdir.mkdirs();
        tmpfiledir=mouseappdir.getAbsolutePath();
        Log.d("CACHEDIR",tmpfiledir);
        imgsinrow.clear();

        connect = new Connect();
        wvimg = (DetWebView)findViewById(R.id.webimg);
        wvjs = (WebView)findViewById(R.id.webjs);
        wvjs.getSettings().setJavaScriptEnabled(true);
        wvjs.setWebViewClient(connect);
        wvjs.addJavascriptInterface(new MyJavaScriptInterface(), "INTERFACE"); 

    }
    private void setInstanceSpinner() {
        String s = pref.getString("mouseapp_insts", null);
        if (s!=null) {
            String[] ss = s.split(" ");
            allinstances.clear();
            for (String x: ss) allinstances.add(x);
        }
        instanceDomain = null;

        final NDSpinner spinner = (NDSpinner)findViewById(R.id.spinneracc);
        ArrayList<String> items = new ArrayList<String>();
        items.add("No inst");
        for (String x: allinstances) {
            if (x.length()>10) items.add(x.substring(0,10));
            else items.add(x);
        }
        items.add("New Acc");
        ArrayAdapter<String> spinadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        spinner.setAdapter(spinadapter);
        spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                jstodownload.clear();
                NDSpinner spin = (NDSpinner)findViewById(R.id.spinner);
                spin.setSelection(0,false);
                if (position==allinstances.size()+1) {
                    if (!delinstance) {
                        addAccount();
                    } else instanceDomain=null;
                } else if (position==0) {
                    instanceDomain=null;
                } else {
                    instanceDomain = allinstances.get(position-1);
                    if (!delinstance) serverStage1();
                    else {
                        delInstance2();
                        delinstance=false;
                        spinner.setSelection(0,false);
                    }
                }
                delinstance=false;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    public void serverStage1() {
        if (instanceDomain==null) return; // must never happen !!
        setTitle("MouseApp "+instanceDomain);
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
                            // autoDownload();
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

    public void stopAll() {
        DetIcons.stopAll();
        TootsManager.stopAll();
        tootsmgr=null;
        jstodownload.clear();
        try {
            jstodownload.put(finfin);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
        start downloading toots automatically in the background
    */
    public void autoDownload() {
        tootsmgr = TootsManager.getTootsManager();
        Thread displayer = new Thread(new Runnable() {
            public void run() {
                try {
                    while (tootsmgr!=null) {
                        if (downloadInBackground) {
                            switch(lastTL) {
                                case 0: // home
                                    {
                                        ArrayList<DetToot> newtts = tootsmgr.getMostRecentToots(instanceDomain,1);
                                        toots.clear(); toots.addAll(newtts); updateList();
                                    }
                                    break;
                                case 1: // notifs
                                    {
                                        ArrayList<DetToot> newtts = tootsmgr.getMostRecentToots(instanceDomain,0);
                                        toots.clear(); toots.addAll(newtts); updateList();
                                    }
                                    break;
                                case 2: // public
                                    {
                                        ArrayList<DetToot> newtts = tootsmgr.getMostRecentToots(instanceDomain,3);
                                        toots.clear(); toots.addAll(newtts); updateList();
                                    }
                                    break;
                                case 3: // local
                                    {
                                        ArrayList<DetToot> newtts = tootsmgr.getMostRecentToots(instanceDomain,2);
                                        toots.clear(); toots.addAll(newtts); updateList();
                                    }
                                    break;
                                case 4: // tag
                                    {
                                        ArrayList<DetToot> newtts = tootsmgr.getMostRecentToots(instanceDomain,4);
                                        toots.clear(); toots.addAll(newtts); updateList();
                                    }
                                    break;
                                default: break;
                            }
                        }
                        Thread.sleep(3000);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        lastTL=1; // commence par afficher les notifs
        displayer.start();
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
        System.out.println("UIUUUUUUUUUUUUUULLLL");
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
		case R.id.delinst:
            delInstance();
			return true;
		case R.id.nolang:
            detectlang=!detectlang;
            if (detectlang) filterlang(); else filterlangs=null;
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

    void delInstance() {
        delinstance=true;
        message("select an instance in the drop-down list");
    }
    void delInstance2() {
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
            instanceDomain=null;
            setInstanceSpinner();
        }
    }

    void addAccount() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // show the window where the user must enter his credentials
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
                                MouseApp.main.allinstances.add(instanceDomain);
                                String s = "";
                                for (String iss: MouseApp.main.allinstances) s+=iss+" ";
                                s=s.trim();
                                edit.putString("mouseapp_insts", s);
                                edit.putString(String.format("user_for_%s", instanceDomain), useremail);
                                edit.putString(String.format("pswd_for_%s", instanceDomain), userpwd);
                                edit.commit();
                                setInstanceSpinner();
                                serverStage1();
                            }
                        }
                    }
                });
            }
        });
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
        int maxid=-1;
        for (int i=toots.size()-1;i>=0;i--)
            if (toots.get(i).id>=0) {
                maxid=toots.get(i).id-1;
                break;
            }
        if (connect!=null&&userLogged&&maxid>=0)
            switch(lastTL) {
                case 0: 
                    getToots("timelines/home?&max_id="+maxid); break;
                case 1: 
                    getNotifs("notifications?&max_id="+maxid); break;
                case 2: 
                    getToots("timelines/public?&max_id="+maxid); break;
                case 3: 
                    getToots("timelines/public?local=1&max_id="+maxid); break;
                case 4: 
                    getToots("timelines/tag/"+tags+"?max_id="+maxid); break;
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

    // main buttons
    public void publicTL(View v) {
        checkInstance();
        jstodownload.clear();
        lastTL=2;
        {
            if (connect!=null&&userLogged) getToots("timelines/public");
            else message("not connected");
        }
    }
    public void tagTL() {
        checkInstance();
        jstodownload.clear();
        lastTL=4;
        UserWritings.show4tags(MouseApp.main, "Search tag ?", new NextAction() {
            public void run(String s) {
                try {
                    tags = URLEncoder.encode(s, "UTF-8");
                    if (connect!=null&&userLogged) getToots("timelines/tag/"+tags);
                    else message("not connected");
                } catch (Exception e) {
                    message("Error encoding");
                }
            }
        });
    }
    public void localTL(View v) {
        checkInstance();
        jstodownload.clear();
        lastTL=3;
        {
            if (connect!=null&&userLogged) getToots("timelines/public?local=1");
            else message("not connected");
        }
    }
    public void homeTL(View v) {
        checkInstance();
        jstodownload.clear();
        lastTL=0;
        {
            if (connect!=null&&userLogged) getToots("timelines/home");
            else message("not connected");
        }
    }
    public void noteTL(View v) {
        checkInstance();
        jstodownload.clear();
        lastTL=1;
        {
            if (connect!=null&&userLogged) getNotifs("notifications");
            else message("not connected");
        }
    }
    public void extramenu(View v) {
        ExtraMenu.show();
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

    public void reply(View v) {
        if (connect==null||!userLogged) {
            message("not connected");
            return;
        }
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                UserWritings.show(main, true, new NextAction() {
                    public void run(String res) {
                        if (res.length()>0) {
                            if (res.length()>4&&res.startsWith("_d_i")) {
                                startWaitingWindow("Sending toot discretely...");
                                connect.replyToot(res, tootselected, new NextAction() {
                                    public void run(String res) {
                                        stopWaitingWindow();
                                    }
                                });
                            } else {
                                startWaitingWindow("Sending toot publicly...");
                                connect.replyToot(res, tootselected, new NextAction() {
                                    public void run(String res) {
                                        stopWaitingWindow();
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }
    public void writeToot(View v) {
        if (connect==null||!userLogged) {
            message("not connected");
            return;
        }
        if (instanceDomain==null) {message("login first !"); return;}
        tootselected=null;
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
                UserWritings.show(main, false, new NextAction() {
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
    public void openToot() {
	UserWritings.show4tags(MouseApp.main, "Toot ID ?", new NextAction() {
            public void run(String tootid) {
		connect.getTL("statuses/"+tootid,new NextAction() {
		    public void run(String res) {
			try {
				JSONObject o = new JSONObject(res);
				DetToot dt = new DetToot(o,detectlang);
			    tootselected=dt;
			    runOnUiThread(new Runnable() {
				@Override
				public void run() {
				    if (tts!=null) tts.silence();
				    VisuToot.show(tootselected);
				}
			    });
			} catch (Exception e) {
				e.printStackTrace();
			}
		    });
		}
            }
        });
    }
    public void openUser() {
	message("not implemented yet");
    }

    public void share(View v) {
        if (tootselected!=null && tootselected.tooturl!=null) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboard.setText(tootselected.tooturl);
            // ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE); 
            // ClipData clip = ClipData.newPlainText("MouseApp toot", tootselected.tooturl);
            // clipboard.setPrimaryClip(clip);
            message("Toot URL is now in clipboard !");
        } else {
            message("Cannot reblog: no toot id");
        }
    }
    public void quit(View v) {
    }
    public void reblog(View v) {
        if (tootselected!=null) {
            startWaitingWindow("Rebloging toot...");
            connect.reblog(tootselected.id, new NextAction() {
                public void run(String res) {
                    stopWaitingWindow();
                    // TODO check if error here
                    message("Message reblogged !");
                }
            });
        } else {
            message("Cannot reblog: no toot id");
        }
    }
    public void mute(final View v) {
        final NextAction dumbaction = new NextAction() {
            public void run(String res) {}
        };
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToggleButton tb = (ToggleButton)v;
                if (tb.isChecked()) {
                    connect.muteUser(VisuUser.curuserid, dumbaction);
                    message("muting user "+VisuUser.curuserid);
                } else {
                    connect.unmuteUser(VisuUser.curuserid, dumbaction);
                    message("unmuting user "+VisuUser.curuserid);
                }
            }
        });
    }


    public void block(final View v) {
        final NextAction dumbaction = new NextAction() {
            public void run(String res) {}
        };
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToggleButton tb = (ToggleButton)v;
                if (tb.isChecked()) {
                    connect.blockUser(VisuUser.curuserid, dumbaction);
                    message("blocking user "+VisuUser.curuserid);
                } else {
                    connect.unblockUser(VisuUser.curuserid, dumbaction);
                    message("unblocking user "+VisuUser.curuserid);
                }
            }
        });
    }

    public void follow(final View v) {
        final NextAction dumbaction = new NextAction() {
            public void run(String res) {}
        };
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToggleButton tb = (ToggleButton)v;
                if (tb.isChecked()) {
                    connect.followUser(VisuUser.curuserid, dumbaction);
                    message("following user "+VisuUser.curuserid);
                } else {
                    connect.unfollowUser(VisuUser.curuserid, dumbaction);
                    message("unfollowing user "+VisuUser.curuserid);
                }
            }
        });
    }
    public void userinfos(View v) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                VisuToot.close();
                System.out.println("tootsel "+tootselected.autid);
                VisuUser.show(tootselected);
            }
        });
    }
    private NextAction torun = null;

    void getNotifs(final String tl) {
        startWaitingWindow("Getting notifs...");
        getNotifs(tl, new TootsListener() {
            boolean isFirstCall = true;
            // attention: avec getNotifs, cette fonction est appelee plusieurs fois !
            public void gotNewToots(ArrayList<DetToot> newtoots) {
                if (true||isFirstCall) {
                    stopWaitingWindow();
                    isFirstCall=false;
                }
                toots.clear();
                toots.addAll(newtoots);
                updateList();
            }
        });
    }
 
    void getNotifs(final String tl, final TootsListener action2) {
        connect.getTL(tl,new NextAction() {
            public void run(String res) {
                try {
                    final ArrayList<DetToot> restoots = new ArrayList<DetToot>();
                    JSONArray json = new JSONArray(res);

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
                            idx.add(restoots.size());
                            ids.add(id);
                            restoots.add(dt);
                        } else if (typ.equals("follow")) {
                            if (!o.isNull("account")) {
                                DetToot dt = new DetToot("followed by: ");
                                dt.getExtraInfos(o);
                                dt.txt+=dt.username;
                                restoots.add(dt);
                            } else {
                                DetToot dt = new DetToot("unhandled type: "+typ);
                                restoots.add(dt);
                            }
                        } else if (typ.equals("favourite")) {
                            if (!o.isNull("account")) {
                                DetToot dt = new DetToot("favourite by: ");
                                dt.getExtraInfos(o);
                                dt.txt+=dt.username+"\n"+dt.getText(o.getJSONObject("status"),false);
                                restoots.add(dt);
                            } else {
                                DetToot dt = new DetToot("unhandled type: "+typ);
                                restoots.add(dt);
                            }
                        } else if (typ.equals("reblog")) {
                            if (!o.isNull("account")) {
                                JSONObject acc = o.getJSONObject("account");
                                String aut = acc.getString("username")+": ";
                                DetToot dt = new DetToot("reblog by: "+aut);
                                dt.txt+=dt.getText(o.getJSONObject("status"),false);
                                restoots.add(dt);
                            } else {
                                DetToot dt = new DetToot("unhandled type: "+typ);
                                restoots.add(dt);
                            }
                        } else {
                            DetToot dt = new DetToot("unhandled type: "+typ);
                            restoots.add(dt);
                        }
                    }
                    if (ids.size()==0) {
                        action2.gotNewToots(restoots);
                    }
                    // now download all statutes
                    for (int i=0;i<ids.size();i++) {
                        final int ii = idx.get(i);
                        connect.getOneStatus(ids.get(i), new NextAction() {
                            public void run(String res) {
                                try {
                                    JSONObject o = new JSONObject(res);
                                    restoots.set(ii,new DetToot(o,detectlang));
                                    action2.gotNewToots(restoots);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void unboost() {
        if (tootselected!=null && tootselected.boosted) {
            startWaitingWindow("Unboosting toot...");
            connect.unboost(tootselected.id, new NextAction() {
                public void run(String res) {
                    stopWaitingWindow();
                    // TODO check if error here
                    tootselected.boosted=false;
                }
            });
        } else {
            message("Cannot unboost: no toot id");
        }
    }

    void boost() {
        if (tootselected!=null && !tootselected.boosted) {
            startWaitingWindow("Boosting toot...");
            connect.boost(tootselected.id, new NextAction() {
                public void run(String res) {
                    stopWaitingWindow();
                    // TODO check if error here
                    tootselected.boosted=true;
                }
            });
        } else {
            message("Cannot boost: no toot id");
        }
    }
    void getToots(final String tl) {
        startWaitingWindow("Getting toots... ");
        getToots(tl, new TootsListener() {
            public void gotNewToots(ArrayList<DetToot> newtoots) {
                stopWaitingWindow();
                if (resetTL) toots.clear();
                toots.addAll(newtoots);
                updateList();
                if (tts!=null) tts.list2read(toots);
            }
        });
    }
    public void getToots(final String tl, final TootsListener action2) {
        connect.getTL(tl,new NextAction() {
            public void run(String res) {
                try {
                    ArrayList<DetToot> restoots = new ArrayList<DetToot>();
                    JSONArray json = new JSONArray(res);
                    System.out.println("GOTTOOTS "+Integer.toString(json.length()));
                    for (int i=0;i<json.length();i++) {
                        JSONObject o = (JSONObject)json.get(i);
                        DetToot dt = new DetToot(o,detectlang);
                        if (dt.lang==null || filterlangs==null) restoots.add(dt);
                        else {
                            for (String s: filterlangs) {
                                if (s.equals(dt.lang)) {
                                    restoots.add(dt);
                                    break;
                                }
                            }
                        }
                    }
                    action2.gotNewToots(restoots);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    void showReplyHistory() {
        if (tootselected==null) {
            message("no initial toot");
            return;
        }
        if (tootselected.id<0) {
            message("no initial toot");
            return;
        }
        // y a-t-il des toots parents ?
        if (tootselected.parentid<0) {
            message("no parent toot");
            return;
        }
        VisuHistory.show();
        VisuHistory.addToot(tootselected);
        recursHist(tootselected.parentid, 20);
        VisuToot.close();
    }
    private void recursHist(final int toot, final int curtoot) {
        if (curtoot<=0) return;
        connect.getOneStatus(toot,new NextAction() {
            public void run(String res) {
                try {
                    JSONObject o = new JSONObject(res);
                    DetToot dt = new DetToot(o,false);
                    VisuHistory.addToot(dt);
                    int toot2download = dt.parentid;
                    if (toot2download>=0) recursHist(toot2download, curtoot-1);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void imgurl(final String url) {
        main.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.wvimg.loadUrl(url);
            }
        });
    }

    // empile les commandes javascript a executer + cree un thread qui les execute et attend qu'elles aient fini
    public static void javascriptCmd(final String url, final NextAction jsnext) {
        try {
            if (jsdownloader==null) {
                jsdownloader = new Thread(new Runnable() {
                    public void run() {
                        try {
                            for (;;) {
                                Object[] tmp = jstodownload.take();
                                if (tmp==finfin) break;
                                final String u = (String)tmp[0];
                                NextAction na = null;
                                if (tmp[1]!=null) na = (NextAction)tmp[1];
                                System.out.println("JSSS url "+u);
                                main.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        main.wvjs.loadUrl(u);
                                    }
                                });
                                // attends que l'url soit completement chargee
                                for (;;) {
                                    if (waitres[0]!=null) break;
                                    Thread.sleep(100);
                                }
                                System.out.println("URLLLLL found "+waitres[0]);
                                if (na!=null) na.run(waitres[0]);
                                synchronized(waitres) {
                                    waitres[0]=null;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        jsdownloader=null;
                    }
                });
                jsdownloader.start();
            }
            Object[] tmp = {url,jsnext};
            jstodownload.put(tmp);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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
            } else if (aContent.indexOf("re sorry, but something went wrong")>=0) {
                MouseApp.main.message("error!");
            } else MouseApp.main.message("connect OK");
        } else if (aContent.startsWith("DETKO")) {
            MouseApp.main.message("error connect");
        }
        synchronized(MouseApp.waitres) {
            MouseApp.waitres[0]=aContent;
        }
    }
}

