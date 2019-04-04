package au.com.CarDVR.Roadvision;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentManager.BackStackEntry;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.SubMenu;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;

import org.xmlpull.v1.XmlPullParser;

import au.com.CarDVR.Roadvision.FileBrowser.Model.SettingItemModel;
import au.com.CarDVR.Roadvision.FileBrowser.Model.SettingModel;

public class MainActivity extends Activity {
    private static Activity s_Activity;
    public static final String g_version_check = "checkversion";
    private static final int MSG_STARTRECORD = 1;
    private static final int MSG_WORINGSDCARD_SHOWDIALOG = 2;
    private static final int DELAY_TIME = 60 * 3000;   //60s modify by eric
    private static final int STATUS_RECORD_BACKKEY = 1;
    private static final int STATUS_RECORD_PAUSE = 2;
    private static final int STATUS_REOCORD_NOMAL = 0;
    private int mRecordSenderStatus = STATUS_REOCORD_NOMAL;
    private static final String TAG = "ipviewer";
    public static final int G_UNKNOWN_VERSION = 0;
    public static String G_UNKNOWN_URL = "";
    public static final int TOUNCHTIME = 86400;//最后一次触摸开始计算，自动开启录像
    public static final int DOUWNTIME = 86400;//下载完成之后，自动开启录像的时间
    public static final int GETSDSTATUSTIME = 15000;//设置轮询SD卡状态的时间
    private WifiInfo mWifiInfo;
    public static int M_VERSION = 3;//全局版本号
    public static String M_FWVERSION = null;//全局版本号
    private WifiManager mWifiManager;
    public static Boolean ShotStatus = true;//镜头状态，默认没有后路

    CustomDialog versionDialog;

    public static final String SettingXmlName = "settingXml.xml";
    public static boolean mNeedUpdateRecordStatus = false;
    public static boolean XMLFILEEXISTS = false;
    public static ArrayList<SettingModel> settingModelArrayList = new ArrayList<SettingModel>();
    public static String StreamPlayUrl = "";
    public static String ChangUI = "";


    public static void setUpdateRecordStatusFlag(boolean recStatusFlag) {
        mNeedUpdateRecordStatus = recStatusFlag;
    }

    ///end

    public static String intToIp(int addr) {

        return ((addr & 0xFF) + "." + ((addr >>>= 8) & 0xFF) + "." + ((addr >>>= 8) & 0xFF) + "." + ((addr >>>= 8) & 0xFF));
    }

    public static String sAppName = "";

    public static String sAppDir = "";

    public static File getAppDir() {
        File appDir = new File(sAppDir);
        if (!appDir.exists()) {
            try {
                appDir.mkdirs();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return appDir;
    }

    public static void addFragment(Fragment originalFragment, Fragment newFragment) {

        FragmentManager fragmentManager = originalFragment.getActivity().getFragmentManager();

        if (fragmentManager.getBackStackEntryCount() > 0) {

            FragmentManager.BackStackEntry backEntry = fragmentManager.getBackStackEntryAt(fragmentManager
                    .getBackStackEntryCount() - 1);

            if (backEntry != null && backEntry.getName().equals(newFragment.getClass().getName()))
                return;
        }

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction
                //.setCustomAnimations(R.anim.right_in, R.anim.right_out, R.anim.left_in, R.anim.left_out)
                .replace(R.id.mainMainFragmentLayout, newFragment)
                .addToBackStack(newFragment.getClass().getName()).commit();
        fragmentManager.executePendingTransactions();
    }

    public static void backToFristFragment(Activity activity) {
        FragmentManager fragmentManager = activity.getFragmentManager();
        if (fragmentManager.getBackStackEntryCount() == 0)
            return;

        BackStackEntry rootEntry = fragmentManager.getBackStackEntryAt(0);

        if (rootEntry == null)
            return;

        int rootFragment = rootEntry.getId();
        fragmentManager.popBackStack(rootFragment, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

    public static void backToPreFragment() {
        if (s_Activity == null) {
            return;
        }
        FragmentManager fragmentManager = s_Activity.getFragmentManager();
        int stack_len = fragmentManager.getBackStackEntryCount();
        if (stack_len == 0)
            return;

        BackStackEntry preEntry = fragmentManager.getBackStackEntryAt(stack_len - 1);

        if (preEntry == null)
            return;
        Log.d(TAG, "preEntry.getName()" + preEntry.getName());
        if (preEntry.getName().contains("StreamPlayerFragment")) {
            s_Activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        int rootFragment = preEntry.getId();
        fragmentManager.popBackStack(rootFragment, FragmentManager.POP_BACK_STACK_INCLUSIVE);

    }

    public boolean mEngineerMode = false;
    private static Locale sDefaultLocale;

    private static Locale sSelectedLocale;

    static {
        sDefaultLocale = Locale.getDefault();
    }

    public static Locale getDefaultLocale() {

        return sDefaultLocale;
    }

    public static void setAppLocale(Locale locale) {

        Locale.setDefault(locale);
        sSelectedLocale = locale;
    }

    public static Locale getAppLocale() {

        return sSelectedLocale == null ? sDefaultLocale : sSelectedLocale;
    }

    private CustomDialog mdialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i("Fragment Activity", "ON CREATE " + savedInstanceState);
        if (ViewPagerDemoActivity.instance != null) {
            ViewPagerDemoActivity.instance.finish();
        }

//        new GetShotStatus().execute();

        System.setProperty("http.keepAlive", "false");
        super.onCreate(savedInstanceState);
        mWifiManager = (WifiManager) this.getApplicationContext()
                .getSystemService(Context.WIFI_SERVICE);
        mWifiInfo = mWifiManager.getConnectionInfo();
        requestWindowFeature(Window.FEATURE_NO_TITLE);  //added by eric
        if (sSelectedLocale == null) {

            sSelectedLocale = sDefaultLocale;
        }
        Locale.setDefault(Locale.ENGLISH);
        Configuration config = new Configuration();
        config.locale = Locale.ENGLISH;
        getResources().updateConfiguration(config, null);

        sAppName = getResources().getString(R.string.app_name);

        Locale.setDefault(sSelectedLocale);
        config = new Configuration();
        config.locale = sSelectedLocale;
        getResources().updateConfiguration(config, null);


        sAppDir = getFilesDir() + File.separator + sAppName;

        // setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) ;
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        setTitle(getResources().getString(R.string.app_name));
        //getActionBar().setDisplayHomeAsUpEnabled(false) ;

        setProgressBarIndeterminateVisibility(false);

        if (savedInstanceState == null) {
            Log.i("moop", "wifi1");
            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(
                    Context.WIFI_SERVICE);

            Log.i("Wifi Info", wifiManager.getConnectionInfo().toString());

            if (wifiManager.isWifiEnabled() && wifiManager.getConnectionInfo().getNetworkId() != -1) {
                Log.i("moop", "wifi2");

                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Fragment fragment = new FunctionListFragment();
                fragmentTransaction.add(R.id.mainMainFragmentLayout, fragment);
                fragmentTransaction.commit();

            } else {
                Log.i("moop", "wifi3");

                LayoutInflater layoutInflater = LayoutInflater.from(this);
                View myDialogView = layoutInflater.inflate(R.layout.dialog_setting_connet, null);
                LinearLayout btn_open_wifi = (LinearLayout) myDialogView.findViewById(R.id.open_wifi);
                btn_open_wifi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        // TODO Auto-generated method stub
                        if (android.os.Build.VERSION.SDK_INT > 10) {
                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                        } else {
                            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                        }
                    }
                });
                if (mdialog != null) {
                    Log.i("moop", "wifi4");

                    mdialog.dismiss();
                }
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Fragment fragment_bg = new ConnectConfigBg();
                fragmentTransaction.add(R.id.mainMainFragmentLayout, fragment_bg);
                fragmentTransaction.commit();

                mdialog = new CustomDialog.Builder(this)
                        .setTitle(R.string.dialog_no_connection_message_title)
                        .setContentView(myDialogView)
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int arg1) {
                                // TODO Auto-generated method stub
                                dialog.dismiss();

                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                                Fragment fragment = new FunctionListFragment();
                                fragmentTransaction.replace(R.id.mainMainFragmentLayout, fragment);
                                //fragmentTransaction.add(R.id.mainMainFragmentLayout, fragment) ;
                                fragmentTransaction.commit();
                            }
                        })
                        .create();
                mdialog.setCancelable(false);

                mdialog.show();

            }
        }
        s_Activity = this;
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

//        String v = pref.getString("liveStreamUrl", G_UNKNOWN_URL);
        isSettingXml();

    }

    public void isSettingXml() {
        File file = new File(getAppDir(), SettingXmlName);
        file.getPath();
//        FileWriter fw = new FileWriter(getAppDir() + "/"+SettingXmlName);

        Log.i(TAG, "isSettingXml: " + getAppDir() + "----------" + file.getPath() + "---" + file.exists());
        if ("".equals(SharedPreferencesUtils.getParam(MainActivity.this, "liveStreamUrl", ""))) {
            if (file.exists())
                file.delete();
        }
        if (file.exists()) {
            try {
                settingModelArrayList.clear();
                InputStream inputStream = new FileInputStream(file);
                SettingModel s0 = new SettingModel();
                s0.setTitle(getString(R.string.label_network_configurations));
                s0.setmGet("");
                s0.setmDefault("");
                settingModelArrayList.add(s0);
                settingModelArrayList.addAll(praseXML(inputStream));
                SettingModel s1 = new SettingModel();
                s1.setTitle(getString(R.string.FW_Version));
                s1.setmGet("Camera.Menu.FWversion");
                s1.setmSwitch("no");
                settingModelArrayList.add(s1);
                SettingModel s2 = new SettingModel();
                s2.setTitle(getString(R.string.label_formatsdcard));
                s2.setmGet("");
                settingModelArrayList.add(s2);
                SettingModel s3 = new SettingModel();
                s3.setTitle(getString(R.string.label_factoryreset));
                s3.setmGet("");
                settingModelArrayList.add(s3);
                XMLFILEEXISTS = true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
//            mTimerHandler.sendEmptyMessage(MSG_WORINGSDCARD_SHOWDIALOG);
            new GetXml().execute();
        }
    }


    private class GetXml extends AsyncTask<URL, Integer, String> {

        @Override
        protected String doInBackground(URL... params) {
            URL url = null;
            try {
                url = new URL("http://" + CameraCommand.getCameraIp() + "/cdv/cammenu.xml");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return CameraCommand.sendRequest(url);
        }

        @Override
        protected void onPostExecute(String response) {
            Log.i("moop", "XML：" + response);
            super.onPostExecute(response);
            if (response != null) {

                InputStream inputStream = null;
                try {
                    inputStream = new ByteArrayInputStream(response.getBytes("UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                inputStream.toString();
                try {
                    settingModelArrayList.clear();
                    SettingModel s0 = new SettingModel();
                    s0.setTitle(getString(R.string.label_network_configurations));
                    s0.setmGet("");
                    s0.setmDefault("");
                    settingModelArrayList.add(s0);
                    settingModelArrayList.addAll(praseXML(inputStream));
                    SettingModel s1 = new SettingModel();
                    s1.setTitle(getString(R.string.FW_Version));
                    s1.setmGet("Camera.Menu.FWversion");
                    s1.setmSwitch("no");
                    settingModelArrayList.add(s1);
                    SettingModel s2 = new SettingModel();
                    s2.setTitle(getString(R.string.label_formatsdcard));
                    s2.setmGet("");
                    settingModelArrayList.add(s2);
                    SettingModel s3 = new SettingModel();
                    s3.setTitle(getString(R.string.label_factoryreset));
                    s3.setmGet("");
                    settingModelArrayList.add(s3);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    FileWriter fw = new FileWriter(getAppDir() + "/" + SettingXmlName);
                    fw.flush();
                    fw.write(response);
                    fw.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }


    SubMenu mLanguageSubMenu;
    String[] mLanguageNames;

    Locale[] mLocales;


    public static int sConnectionDelay = 1000;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        mLanguageNames = new String[]{getResources().getString(R.string.label_default),
                "English",
                getResources().getString(R.string.label_language_TChinese),
                getResources().getString(R.string.label_language_SChinese)};

        mLocales = new Locale[]{MainActivity.getDefaultLocale(), Locale.ENGLISH,
                Locale.TRADITIONAL_CHINESE, Locale.SIMPLIFIED_CHINESE};

        //getMenuInflater().inflate(R.menu.main, menu) ;

        mLanguageSubMenu = menu.addSubMenu(0, 0, 0, getResources().getString(R.string.label_language));

        int i = 0;
        for (String language : mLanguageNames) {

            MenuItem item = mLanguageSubMenu.add(0, i++, 0, language);
            item.setCheckable(true);

        }
        return true;
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) && getFragmentManager().getBackStackEntryCount() == 0) {
            CustomDialog alertDialog = new CustomDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.trip))
                    .setMessage(getResources().getString(R.string.onKeyDown))
                    .setCancelable(false)
                    .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            mRecordSenderStatus = STATUS_RECORD_BACKKEY;
                            new StartRecord().startRecord();
                            finish();
                            Log.d("moop", "onKeyDown");
                            //disconnectWifi();///added by eric for disconnect wifi
                            Log.d(TAG, "onDestoryonDestory");///added by eric for disconnect wifi
                            arg0.dismiss();
                        }
                    }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        } else if (keyCode == KeyEvent.KEYCODE_BACK && getFragmentManager().getBackStackEntryCount() > 0) {
            backToPreFragment();
            Log.d(TAG, "KeyEvent.KEYCODE_BACK");///added by eric for disconnect wifi
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (mLanguageSubMenu != null) {

            int size = mLanguageSubMenu.size();

            for (int i = 0; i < size; i++) {
                MenuItem item = mLanguageSubMenu.getItem(i);

                if (i > 0 && getAppLocale().equals(mLocales[i])) {
                    item.setChecked(true);
                } else {
                    item.setChecked(false);
                }

                item.setOnMenuItemClickListener(new OnMenuItemClickListener() {

                    @Override
                    public boolean onMenuItemClick(MenuItem checkedItem) {

                        int size = mLanguageSubMenu.size();

                        for (int i = 0; i < size; i++) {
                            MenuItem item = mLanguageSubMenu.getItem(i);
                            if (checkedItem == item && item.isChecked() == false) {
                                item.setChecked(true);

                                setAppLocale(mLocales[i]);

                                Intent intent = getIntent();
                                finish();
                                startActivity(intent);
                            } else {
                                item.setChecked(false);
                            }
                        }
                        return true;
                    }
                });
            }
        }


        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                backToFristFragment(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }


    private Handler mTimerHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_STARTRECORD:
                    mRecordSenderStatus = STATUS_RECORD_PAUSE;
                    Log.i("moop", "MSG_STARTRECORD");
                    break;
                case MSG_WORINGSDCARD_SHOWDIALOG:
                    Log.d("moop", " MSG_WORINGSDCARD_SHOWDIALOG");
                    if (versionDialog == null) {
                        versionDialog = new CustomDialog.Builder(MainActivity.this)
                                .setTitle(getResources().getString(R.string.trip))
                                .setMessage(getResources().getString(R.string.verify_error))
                                .setCancelable(false)
                                .setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int arg1) {
// TODO Auto-generated method stub
//									new CameraFWVersion().execute();
                                        new GetXml().execute();

                                        dialog.dismiss();
                                        return;
                                    }
                                }).setNegativeButton(R.string.setting_wifi, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                        if (android.os.Build.VERSION.SDK_INT > 10) {
                                            //3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
                                            startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
                                        } else {
                                            startActivity(new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
                                        }
                                    }
                                }).create();
                    }
                    versionDialog.setCancelable(false);
                    if (!versionDialog.isShowing())
                        versionDialog.show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onStart() {
        Log.d(TAG, "on start");
        super.onStart();
    }

    @Override
    public void onResume() {
        new StartRecord().startRecord();
        if (!XMLFILEEXISTS)
            isSettingXml();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        mTimerHandler.removeMessages(MSG_STARTRECORD);
        super.onDestroy();
    }

    private int getNetworkId() {
        return (mWifiInfo == null) ? 0 : mWifiInfo.getNetworkId();
    }


    public ArrayList<SettingModel> praseXML(InputStream inputStream) throws Exception {
        ArrayList<SettingModel> files = null;
        SettingModel settingModel = null;
        SettingItemModel settingitem = null;
        XmlPullParser parser = Xml.newPullParser();
        ArrayList<SettingItemModel> titlelist = null;
        parser.setInput(inputStream, "UTF-8");
        int event = parser.getEventType();
        //不等于结束事件，循环读取XML文件并封装成对象

        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_DOCUMENT:
                    files = new ArrayList<SettingModel>();
                    break;
                case XmlPullParser.START_TAG:
                    Log.i(TAG, "praseXML: " + parser.getName());

                    if ("menu".equals(parser.getName())) {
                        settingModel = new SettingModel();
                        titlelist = new ArrayList<SettingItemModel>();
                        if (parser.getAttributeCount() > 0) {
                            settingModel.setTitle(parser.getAttributeValue(0));
                            settingModel.setmGet(parser.getAttributeValue(1));
                            settingModel.setmSet(parser.getAttributeValue(2));
                            settingModel.setmSwitch(parser.getAttributeValue(3));
                            Log.i(TAG, "praseXML: " + parser.getAttributeName(0));
                        }
                    } else if ("item".equals(parser.getName())) {
                        settingitem = new SettingItemModel();
                        if (parser.getAttributeCount() > 0)
                            settingitem.setId(parser.getAttributeValue(0));
                        event = parser.next();
                        settingitem.setArg(parser.getText());
//                        Log.i(TAG, "praseXML:item ----" + parser.getText());
                    } else if ("custmenu".equals(parser.getName())) {
                        settingModel = new SettingModel();
                        if (parser.getAttributeCount() > 0) {
                            settingModel.setTitle(parser.getAttributeValue(0));
                            settingModel.setmSwitch(parser.getAttributeValue(1));
                            Log.i(TAG, "praseXML:custmenu " + parser.getAttributeCount() + "----" + parser.getAttributeValue(0));
//                        settingModel.setTitle(parser.getAttributeValue(0));
                        }

                    } else if ("isdualcamera".equals(parser.getName())) {
                        if (parser.getAttributeCount() > 0) {
                            ShotStatus = "1".equals(parser.getAttributeValue(1));
                            Log.i(TAG, "praseXML:----- "+parser.getAttributeValue(1));
                        }
                    } else if (parser.getName().equals("streamingaddr")) {
                        if (parser.getAttributeCount() > 0) {
                            Log.i(TAG, "视频预览地址 " + parser.getAttributeValue(1));
                            StreamPlayUrl = (String) SharedPreferencesUtils.getParam(MainActivity.this, "liveStreamUrl", "");
                            if ("".equals(StreamPlayUrl)) {
                                Log.i(TAG, "预览地址为空");
                                SharedPreferencesUtils.setParam(MainActivity.this, "liveStreamUrl", parser.getAttributeValue(1).replaceAll("ip", CameraCommand.getCameraIp()));
                                StreamPlayUrl = parser.getAttributeValue(1).replaceAll("ip", CameraCommand.getCameraIp());
                                Log.i(TAG, "保存预览地址为=" + StreamPlayUrl);
                            } else {
                                Log.i(TAG, "预览地址为=" + StreamPlayUrl);
                            }
                        }
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("item".equals(parser.getName())) {
                        titlelist.add(settingitem);
                        settingitem = null;
                    }
                    if ("menu".equals(parser.getName())) {
                        settingModel.setTitles(titlelist);
                        files.add(settingModel);
                        settingModel = null;
                        titlelist = null;

                    } else if ("camera".equals(parser.getName())) {
                        files.add(settingModel);
                        settingModel = null;
                    }
                    break;
            }
            event = parser.next();
        }

        return files;
    }
}
