package au.com.CarDVR.Roadvision;

import android.media.Image;
import android.os.Bundle;

import au.com.CarDVR.Roadvision.FileBrowser.FileBrowserFragment;
import au.com.CarDVR.Roadvision.FileBrowser.BrowserSettingFragment;
import au.com.CarDVR.Roadvision.FileBrowser.LocalFileBrowserFragment;
import au.com.CarDVR.Roadvision.Viewer.StreamPlayerActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("HandlerLeak")
public class FunctionListFragment extends Fragment {
    private static final String TAG = "FunctionListFragment";
    private int mB = -1;
    private int mlocalB = 0;
    private static final int MSG_SUCCESS = 1;
    private static final int MSG_FAIL = 2;
    private static final int MSG_DESTORY = 3;
    private static final int MSG_NOSDCARD = 4;
    ImageView img_func_round;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SUCCESS:
                    break;
                case MSG_FAIL:
                    CustomDialog alertDialog = new CustomDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.verify))
                            .setMessage(R.string.verify_error)
                            .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // TODO Auto-generated method stub
                                    arg0.dismiss();
                                    Activity act = getActivity();
                                    if (act != null)
                                        act.finish();
                                }
                            }).create();

                    alertDialog.show();
                    mHandler.sendEmptyMessageDelayed(MSG_DESTORY, 5000);
                    break;
                case MSG_DESTORY:
                    Activity act = getActivity();
                    if (act != null)
                        act.finish();
                    break;
                case MSG_NOSDCARD:
                    Toast toast = new Toast(getActivity());

                    toast = Toast.makeText(getActivity(),
                            getActivity().getResources().getString(R.string.SDCaedWarning), Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.BOTTOM, 0, 20);
                    toast.show();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.function_list, container, false);
        img_func_round = (ImageView) view.findViewById(R.id.func_round);
        img_func_round.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

//								if(android.os.Build.VERSION.SDK_INT > 10 ){
//									//3.0以上打开设置界面，也可以直接用ACTION_WIRELESS_SETTINGS打开到wifi界面
//									startActivity(new Intent(android.provider.Settings.ACTION_SETTINGS));
//								} else {
//									startActivity(new Intent(android.provider.Settings.ACTION_WIFI_SETTINGS));
//								}
            }
        });


        ImageView control = (ImageView) view.findViewById(R.id.settings_btn);
        control.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                        CustomDialog alertDialog = new CustomDialog.Builder(getActivity())
                        .setTitle(getResources().getString(R.string.trip))
                        .setMessage(getResources().getString(R.string.main_setting_warnning))
                        .setCancelable(false)
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                new StartRecord().stopRecord();
                                MainActivity.addFragment(FunctionListFragment.this, new SettingFragment());
                                arg0.dismiss();

                                return;
                            }
                        }).setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
                alertDialog.show()
                ;
            }
        });
        ImageView preview = (ImageView) view.findViewById(R.id.video_btn);
        preview.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Button_main", "ONCLICK");
                boolean engineerMode = ((MainActivity) getActivity()).mEngineerMode;
                WifiManager wifiManager = (WifiManager)
                        getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
                if (dhcpInfo == null || dhcpInfo.gateway == 0) {
                    AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
                    alertDialog.setTitle(getResources().getString(R.string.dialog_DHCP_error));
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL,
                            getResources().getString(R.string.label_ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                    return;
                }
                if (engineerMode) {
//					MainActivity.addFragment(FunctionListFragment.this, new ViewerSettingFragment()) ;
                } else {
//					new GetRTPS_AV1().execute();
//                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
//                    String liurl = null;
//                    liurl = pref.getString("liveStreamUrl", "");
                    Intent intent = new Intent(getActivity(), StreamPlayerActivity.class);
//                    intent.putExtra("KEY_MEDIA_URL", liurl);
                    startActivity(intent);
                }
            }
//		mToast.show();

        });
        ImageView browser = (ImageView) view.findViewById(R.id.dv_btn);
        browser.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean engineerMode = ((MainActivity) getActivity()).mEngineerMode;
                if (engineerMode) {
                    MainActivity.addFragment(FunctionListFragment.this, new BrowserSettingFragment());
                } else {
                    Fragment fragment = FileBrowserFragment.newInstance(null, null, null);

                    MainActivity.addFragment(FunctionListFragment.this, fragment);
                }
            }
        });


        ImageView localAlbum = (ImageView) view.findViewById(R.id.local_btn);

        localAlbum.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                MainActivity.addFragment(FunctionListFragment.this, new LocalFileBrowserFragment());
            }
        });

        RelativeLayout help_btn = (RelativeLayout) view.findViewById(R.id.help);
        help_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                MainActivity.addFragment(FunctionListFragment.this, new HelpFramgment());
            }
        });
        /*存储状态*/
        ImageView capacity_btn = (ImageView) view.findViewById(R.id.capacity_btn);
        capacity_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                MainActivity.addFragment(FunctionListFragment.this, new CapacityFragment());

            }
        });
        ImageView tv_privace= (ImageView) view.findViewById(R.id.tv_privace);
        tv_privace.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.addFragment(FunctionListFragment.this,new PrivacyPlokicyFragment());
            }
        });
        return view;
    }
    //added by john



    @Override
    public void onResume() {
        new StartRecord().startRecord();


        super.onResume();
    }

    @Override
    public void onPause() {
        MainActivity.setUpdateRecordStatusFlag(false);

        super.onPause();
    }
}
