package au.com.CarDVR.Roadvision.Viewer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import au.com.CarDVR.Roadvision.CameraCommand;
import au.com.CarDVR.Roadvision.CustomDialog;
import au.com.CarDVR.Roadvision.MainActivity;
import au.com.CarDVR.Roadvision.R;

public class StreamPlayerActivity extends Activity {
    public final static String TAG = "VLC/VideoPlayerActivity";

    private SurfaceView mSurface;
    private SurfaceHolder mSurfaceHolder;
    private RelativeLayout mSurfaceFrame;

    private boolean mRecording;
    private TextView curdate;
    private Button cameraRecordButton;
    private ImageView snapshotButton;
    private ImageView recordButton;
    private ImageView preview_function_btn;
    private TextView mRecordTxt;
    private TextView mFunctionTxt;
    private ImageButton btn_back;
    private LinearLayout rela_preview_bottom;
    private RelativeLayout rela_preview_title;
    //    private ImageButton mSoundControlButtonDisable,mSoundControlButton;
    private AudioManager audioManager;
    private static Date mCameraTime;
    private static long mCameraUptimeMills;
    private String mTime;
    public static String mRecordStatus = "";
    public static String mRecordmode = "";
    private boolean mRecordthread = false;

    private int mB = -1;
    private int mlocalB = 0;
    private static final int MSG_SUCCESS = 1;
    private static final int MSG_FAIL = 2;
    private static final int MSG_DESTORY = 3;
    private static final int MSG_PLYDISSMISS = 4;


    private static final int SURFACE_BEST_FIT = 0;
    private static final int SURFACE_FIT_HORIZONTAL = 1;
    private static final int SURFACE_FIT_VERTICAL = 2;
    private static final int SURFACE_FILL = 3;
    private static final int SURFACE_16_9 = 4;
    private static final int SURFACE_4_3 = 5;
    private static final int SURFACE_ORIGINAL = 6;
    private int mCurrentSize = SURFACE_BEST_FIT;
    /**
     * For uninterrupted switching between audio and video mode
     */
    private boolean mEndReached;

    // size of the video
    private int mVideoHeight;
    private int mVideoWidth;
    private int mVideoVisibleHeight;
    private int mVideoVisibleWidth;
    private int mSarNum;
    private int mSarDen;

    private boolean mPlaying;
    ProgressDialog mProgressDialog = null;
    private static final int MSG_VISIABLE_RECORD_BTN = 1;
    private static final int MSG_INVISIABLE_RECORD_BTN = 2;
    private static final int MSG_REFRESH_MUTE_STATE = 3;
    private static final int MSG_SNAPSHOTONCLICK = 4;
    private static final int MSG_SINTERVAlCLICK = 5;
    private static final int MSG_changoShotlCLICK = 6;

    private static final int button_fresh_delaytime = 500; //ms
    private static final String KEY_MEDIA_URL = "mediaUrl";
    private static final String MuteOn = "MuteOn";
    private static final String MuteOff = "MuteOff";
    private TimeThread timestampthread;
    private int countTime = 0;
    private String mMuteStatus = MuteOn;
    private boolean isVISIBLE = true;
    private boolean isINVISIABLE = true;
    private TextView tv_playVideoError;
    //add by John 2015.10.27
    Button btn_changemedia;
    private Boolean isMideaURL = false;//默认是前路视频，第一次点击换后路
    private static int mCameraId = 0;//默认是前路视频，第一次点击换后路
    private Boolean isfirstOpenView = false;
    private Boolean isVisiibleBottom = true;//显示隐藏底部布局
    private Boolean isfunction = true;//默认function功能是录像
    private int version;
    private Boolean ShotStatus = false;//镜头状态，默认没有后路
    private VideoViewer mVideoView;
    private static final int MSG_VIDEO_RECORD = 1;
    private static final int MSG_VIDEO_STOP = 2;
    private StreamLogThread streamlogthread;
    int suface_initWidth = 0, suface_initHeight = 0; //第一次进入时sufaceView尺寸

    /*用来记录handle延时发送的时间，防止延时发送产生重复的指令*/
    Timer timer = new Timer();
    Context context = StreamPlayerActivity.this;

    /*限制抓拍按钮点击时间间隔*/
    public void isSnapshotButtonsinterval() {
        countTime = 0;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mHandlerUI.sendEmptyMessage(MSG_SNAPSHOTONCLICK);
                mHandlerUI.sendEmptyMessage(MSG_changoShotlCLICK);
            }
        };
        timer.schedule(task, 1000 * 6);
    }

    public void isVISIBLEs() {
        countTime = 0;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                isVISIBLE = true;
            }
        };
        timer.schedule(task, 100 * 5);
    }

    public void isINVISIABLEs() {
        countTime = 0;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                isINVISIABLE = true;
            }
        };
        timer.schedule(task, 100 * 6);
    }

    public void isChangShotButtonsinterval() {
        countTime = 0;
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                mHandlerUI.sendEmptyMessage(MSG_changoShotlCLICK);
            }
        };
        timer.schedule(task, 1000 * 5);
    }

    private Handler mHandlerUI = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_VISIABLE_RECORD_BTN:
                    if (isVISIBLE) {
                        isVISIBLE = false;
                        cameraRecordButton.setVisibility(View.VISIBLE);
                        mHandlerUI.sendEmptyMessageDelayed(MSG_INVISIABLE_RECORD_BTN, button_fresh_delaytime);
                        isVISIBLEs();
                    }
                    break;
                case MSG_INVISIABLE_RECORD_BTN:
                    if (isINVISIABLE) {
                        cameraRecordButton.setVisibility(View.INVISIBLE);
                        mHandlerUI.sendEmptyMessageDelayed(MSG_VISIABLE_RECORD_BTN, button_fresh_delaytime);
                        isINVISIABLEs();
                    }
                    break;
                case MSG_REFRESH_MUTE_STATE:
                    Log.i("muteo", "muteo=" + mMuteStatus);
                    if (mMuteStatus.equals(MuteOff)) {
                        Log.i("muteo", "muteo=" + 1);
//                        mSoundControlButton.setVisibility(View.VISIBLE);
//                        mSoundControlButtonDisable.setVisibility(View.GONE);
                    } else if (mMuteStatus.equals(MuteOn)) {
                        Log.i("muteo", "muteo=" + 2);
//                        mSoundControlButton.setVisibility(View.GONE);
//                        mSoundControlButtonDisable.setVisibility(View.VISIBLE);
                    } else {
                        Log.i("muteo", "muteo=" + 3);
//                        mSoundControlButton.setVisibility(View.VISIBLE);
//                        mSoundControlButtonDisable.setVisibility(View.GONE);
                    }
                    break;
                case MSG_changoShotlCLICK:
                    btn_changemedia.setEnabled(true);
                    break;
                case MSG_SNAPSHOTONCLICK:
                    preview_function_btn.setEnabled(true);
                    break;
                default:
                    break;
            }
        }
    };


    public String checkTime(int i) {
        mTime = Integer.toString(i);
        if (i < 10) {
            mTime = "0" + mTime;
        }
        return mTime;
    }


    //yining

    /**
     * 获取设备时间
     */
    private class GetTimeStamp extends AsyncTask<URL, Integer, String> {

        protected void onPreExecute() {
            setWaitingState(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... params) {
            URL url = CameraCommand.commandTimeStampUrl();
            if (url != null) {
                return CameraCommand.sendRequest(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Activity activity = StreamPlayerActivity.this;
//Log.d(TAG, "TimeStamp property "+result) ;
            if (result != null) {
                int year = 2015;
                int month = 1;
                int day = 1;
                int hour = 1;
                int minute = 1;
                int second = 1;
                String[] lines;
                String[] lines_temp = result.split("Camera.Preview.MJPEG.TimeStamp.year=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    year = Integer.valueOf(lines[0]);
                }
                lines_temp = result.split("Camera.Preview.MJPEG.TimeStamp.month=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    month = Integer.valueOf(lines[0]);
                }
                lines_temp = result.split("Camera.Preview.MJPEG.TimeStamp.day=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    day = Integer.valueOf(lines[0]);
                }
                lines_temp = result.split("Camera.Preview.MJPEG.TimeStamp.hour=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    hour = Integer.valueOf(lines[0]);
                }
                lines_temp = result.split("Camera.Preview.MJPEG.TimeStamp.minute=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    minute = Integer.valueOf(lines[0]);
                }
                lines_temp = result.split("Camera.Preview.MJPEG.TimeStamp.second=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    second = Integer.valueOf(lines[0]);
                }
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
                try {
                    String cameraUptimeStr = String.format("%04d-%02d-%02d %02d:%02d:%02d",
                            year, month, day, hour, minute, second);
                    mCameraTime = format.parse(cameraUptimeStr);
                    Log.i("GetTimeStamp", cameraUptimeStr);
                    Log.i("GetTimeStamp", result);
                } catch (ParseException e) {
// TODO Auto-generated catch block
                    e.printStackTrace();
                }
                mCameraUptimeMills = SystemClock.uptimeMillis();
            } else if (activity != null) {
//                Toast.makeText(activity, activity.getResources().getString(R.string.message_fail_get_info),
//                        Toast.LENGTH_LONG).show() ;
            }
            setWaitingState(false);
            setInputEnabled(true);
            super.onPostExecute(result);
        }
    }

    private class GetRecordStatus extends AsyncTask<URL, Integer, String> {
        @Override
        protected void onPreExecute() {
            setWaitingState(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... params) {
            URL url = CameraCommand.commandRecordStatusUrl();
            if (url != null) {
                return CameraCommand.sendRequest(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Activity activity = StreamPlayerActivity.this;
//Log.d(TAG, "TimeStamp property "+result) ;
            if (result != null) {
                String[] lines;
                String[] lines_temp = result.split("Camera.Preview.MJPEG.status.record=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    if (lines != null)
                        mRecordStatus = lines[0];
                    Log.d(TAG, "mMuteStatus=" + mRecordmode);
                }
                lines_temp = result.split("Camera.Preview.MJPEG.status.mode=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    if (lines != null)
                        mRecordmode = lines[0];
                    Log.d(TAG, "mMuteStatus=" + mRecordmode);
                }
                lines_temp = result.split("Camera.Preview.MJPEG.status.mute=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    if (lines != null)
                        mMuteStatus = lines[0];
                    Log.d(TAG, "mMuteStatus=" + mMuteStatus);
                }

                mHandlerUI.sendEmptyMessage(MSG_REFRESH_MUTE_STATE);
                mRecordStatusHandler.sendMessage(mRecordStatusHandler.obtainMessage());
            } else if (activity != null) {
                Toast.makeText(activity, activity.getResources().getString(R.string.message_fail_get_info),
                        Toast.LENGTH_LONG).show();
            }
            setWaitingState(false);
            setInputEnabled(true);
            super.onPostExecute(result);
        }
    }

    private class CameraVideoRecord extends AsyncTask<URL, Integer, String> {
        @Override
        protected void onPreExecute() {
            setWaitingState(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... params) {
            if (version <= 2) {
                URL url = CameraCommand.commandCameraRecordUrl();
                if (url != null) {
                    return CameraCommand.sendRequest(url);
                }
            } else {
                if (mRecordmode.equals("Videomode")) {
                    if (mRecordStatus.equals("Recording")) {
                        URL url = CameraCommand.commandCameraStopRecordUrl();

                        if (url != null) {
                            return CameraCommand.sendRequest(url);
                        }
                    } else {
                        URL url = CameraCommand.commandCameraStartRecordUrl();
                        if (url != null) {
                            return CameraCommand.sendRequest(url);
                        }
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Activity activity = StreamPlayerActivity.this;
            Log.d(TAG, "Video record response:" + result);
            if (result != null && result.contains("OK")) {
                if (mRecordmode.equals("Videomode")) {
                    if (mRecordStatus.equals("Recording")) {
                        mRecordStatus = "Standby";
                        Toast.makeText(activity,
                                activity.getResources().getString(R.string.msg_stoprecord),
                                Toast.LENGTH_SHORT).show();
                    } else {
                        mRecordStatus = "Recording";
                        Toast.makeText(activity,
                                activity.getResources().getString(R.string.msg_recording),
                                Toast.LENGTH_SHORT).show();
                    }
                    mRecordStatusHandler.sendMessage(mRecordStatusHandler.obtainMessage());
                }
            } else if (result != null && result.contains("716")) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.SDCaedWarning),
                        Toast.LENGTH_SHORT).show();
            } else if (activity != null) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.message_command_failed),
                        Toast.LENGTH_SHORT).show();
            }
            setWaitingState(false);
            setInputEnabled(true);
            super.onPostExecute(result);
        }
    }

    /**
     * 高清抓拍
     */
    private class CameraSnapShot extends AsyncTask<URL, Integer, String> {
        @Override
        protected void onPreExecute() {
            setWaitingState(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... params) {
            URL url = CameraCommand.commandCameraSnapshotUrl();
            if (url != null) {
                return CameraCommand.sendRequest(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Activity activity = StreamPlayerActivity.this;
            Log.d(TAG, "snapshot response:" + result);
            if (result != null && result.contains("OK")) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.message_command_camer),
                        Toast.LENGTH_SHORT).show();
                String[] lines;
                if (result.indexOf("CameraLastPicName") == -1)
                    return;
                String[] lines_temp = result.split("CameraLastPicName=");
                try {
                    lines = lines_temp[1].split("\\|");
                    String newStr = lines[0].replace("\\", "/");
                    String s = newStr.replace(":", "");
                    if (s.indexOf("JPG") != -1) {
                    } else
                        return;

                    Log.i(TAG, "onPostExecute: 地址s" + s);
                    URL url = null;
                    url = new URL("http://" + CameraCommand.getCameraIp() + "/" + s);
                    new DownloadTask().execute(url);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Log.i(TAG, "onPostExecute: " + e);
                    return;
                }
            } else if (result != null && result.contains("716")) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.SDCaedWarning),
                        Toast.LENGTH_SHORT).show();
            } else if (activity != null) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.message_command_error),
                        Toast.LENGTH_SHORT).show();
            }
            setWaitingState(false);
            setInputEnabled(true);
            super.onPostExecute(result);
        }
    }

    private List<View> mViewList = new LinkedList<View>();

    private void setInputEnabled(boolean enabled) {
        for (View view : mViewList) {
            view.setEnabled(enabled);
        }
    }

    private boolean mWaitingState = false;
    private boolean mWaitingVisible = false;

    private void setWaitingState(boolean waiting) {
        if (mWaitingState != waiting) {
            mWaitingState = waiting;
            setWaitingIndicator(mWaitingState, mWaitingVisible);
        }
    }

    private void setWaitingIndicator(boolean waiting, boolean visible) {
        if (!visible)
            return;
        setInputEnabled(!waiting);
        Activity activity = StreamPlayerActivity.this;
        if (activity != null) {
            activity.setProgressBarIndeterminate(true);
            activity.setProgressBarIndeterminateVisibility(waiting);
        }
    }

    private void clearWaitingIndicator() {
        mWaitingVisible = false;
        setWaitingIndicator(false, true);
    }

    private void restoreWaitingIndicator() {
        mWaitingVisible = true;
        setWaitingIndicator(mWaitingState, true);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("mRecordStatus", mRecordStatus);
        outState.putString("muteStatus", mMuteStatus);
        outState.putString("", "");
        super.onSaveInstanceState(outState);
    }

    public Handler mCameraStatusHandler = new Handler() {
        public void handleMessage(Message msg) {
            int msgid = msg.getData().getInt("msg");
            switch (msgid) {
                case MSG_VIDEO_RECORD:
                    cameraRecordButton.setText(R.string.label_camera_stop_record);
                    break;
                case MSG_VIDEO_STOP:
                    cameraRecordButton.setText(R.string.label_camera_record);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_player);
        MainActivity.setUpdateRecordStatusFlag(true);
        version = new MainActivity().M_VERSION;
        Log.i(TAG, "版本号=" + version);
        new GetTimeStamp().execute();
        new GetRecordStatus().execute();
        if (savedInstanceState == null) {
            mRecordthread = true;

// GET CAMERA STATE
        } else {
            mRecordStatusHandler.sendMessage(mRecordStatusHandler.obtainMessage());
        }
        //获取intent传过来的URL
        //  String liurl=null;

        final IntentFilter filter = new IntentFilter();
        filter.addAction(VLCApplication.SLEEP_INTENT);
        context.registerReceiver(mReceiver, filter);

        mVideoView = (VideoViewer) findViewById(R.id.player_surface);

        mVideoView.setHandler(mCameraStatusHandler);
        mRecording = false;

        timestampthread = new TimeThread();
        timestampthread.start();
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//// TODO Auto-generated method stub
//                new GSetRandomValues().execute();
//            }
//        }).start();

        cameraRecordButton = (Button) findViewById(R.id.cameraRecordButton);

        btn_back = (ImageButton) findViewById(R.id.btn_back);
        snapshotButton = (ImageView) findViewById(R.id.snapshotButton);
        recordButton = (ImageView) findViewById(R.id.recordButton);
        preview_function_btn = (ImageView) findViewById(R.id.preview_function_btn);
        mRecordTxt = (TextView) findViewById(R.id.record_txt);
        mFunctionTxt = (TextView) findViewById(R.id.function_text);
        curdate = (TextView) findViewById(R.id.TimeStampLabel);
        mSurface = (SurfaceView) findViewById(R.id.player_surface);
        mSurfaceFrame = (RelativeLayout) findViewById(R.id.player_surface_frame);
        tv_playVideoError = (TextView) findViewById(R.id.tv_playVideoError);
        rela_preview_bottom = (LinearLayout) findViewById(R.id.rela_preview_bottom);
        rela_preview_title = (RelativeLayout) findViewById(R.id.rela_preview_title);
        btn_changemedia = (Button) findViewById(R.id.btn_changemedia);


        if (version > 2) {
            btn_changemedia.setVisibility(View.VISIBLE);
        }
        if (MainActivity.ShotStatus) {
            ShotStatus = true;
            Log.i(TAG, "yes" + new MainActivity().ShotStatus);
            btn_changemedia.setBackgroundResource(R.drawable.play_btn_simplewipe_yes_bg);
        } else {
//            btn_changemedia.setBackgroundResource(R.drawable.play_btn_simplewipe_no_bg);
            btn_changemedia.setVisibility(View.GONE);
            Log.i(TAG, "no" + new MainActivity().ShotStatus);
            ShotStatus = false;
        }
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        String chroma = pref.getString("chroma_format", "");
        mSurfaceHolder = mSurface.getHolder();
        if (chroma.equals("YV12")) {
            mSurfaceHolder.setFormat(ImageFormat.YV12);
        } else if (chroma.equals("RV16")) {
            mSurfaceHolder.setFormat(PixelFormat.RGB_565);
            PixelFormat info = new PixelFormat();
            PixelFormat.getPixelFormatInfo(PixelFormat.RGB_565, info);
        } else {
            mSurfaceHolder.setFormat(PixelFormat.RGBX_8888);
            PixelFormat info = new PixelFormat();
            PixelFormat.getPixelFormatInfo(PixelFormat.RGBX_8888, info);
        }

        mSurfaceFrame.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (!isVisiibleBottom) {
                        layoutanimationGONE();
                        isVisiibleBottom = true;
                        Log.i(TAG, "点击surfface1");
                    } else {
                        layoutanimationVISIBLE();
                        isVisiibleBottom = false;
                        Log.i(TAG, "点击surfface2");
                    }
                }
            }
        });
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StreamPlayerActivity.this.finish();
            }
        });
        snapshotButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                isfunction = false;
                snapshotButton.setEnabled(false);
                recordButton.setEnabled(true);
                mFunctionTxt.setText(R.string.exposalmodel);

            }
        });
        preview_function_btn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isfunction) {
                    new CameraVideoRecord().execute();
                } else {
                    new CameraSnapShot().execute();
                }
                preview_function_btn.setEnabled(false);
                btn_changemedia.setEnabled(false);
                new CountDownTimer(1000 * 3, 1000) {

                    @Override
                    public void onTick(long millisUntilFinished) {

                    }

                    @Override
                    public void onFinish() {
                        preview_function_btn.setEnabled(true);
                        btn_changemedia.setEnabled(true);
                    }
                }.start();

//                isSnapshotButtonsinterval();
            }
        });
        btn_changemedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i("moop", "btn_changemedia");
                btn_changemedia.setEnabled(false);
                isChangShotButtonsinterval();
                if (ShotStatus) {
                    new GetWifiInfo().execute();
                    showwattingDialog();
                    new CountDownTimer(5 * 1000, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                        }

                        @Override
                        public void onFinish() {
                            dismissdialog();
                        }
                    }.start();
                }
            }
        });
        recordButton.setEnabled(false);
        recordButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                recordButton.setEnabled(false);
                snapshotButton.setEnabled(true);
                mFunctionTxt.setText(R.string.videorecording);
                isfunction = true;
            }
        });
//        }) ;
        cameraRecordButton.setVisibility(View.VISIBLE);
        StreamPlayerActivity.this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i("moop", "onResume");
        playLiveStream();
        mSurface.setKeepScreenOn(true);
    }

    @Override
    protected void onStart() {
        Log.i("moop", "onStart");
        super.onStart();
    }

    @Override
    public void onPause() {
        Log.i("moop", "onPause");

        super.onPause();
        stop();
        mSurface.setKeepScreenOn(false);
    }

    @Override
    public void onStop() {
        Log.i("moop", "onResume");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.i("moop", "onDestroy");
        super.onDestroy();
        context.unregisterReceiver(mReceiver);
        if (streamlogthread != null)
            streamlogthread.stopPlay();
        timestampthread.stopPlay();
        mRecordthread = false;
    }

    private void stop() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
        if (mPlaying == true) {
            mPlaying = false;
            mVideoView.releasePlayer();

        }
    }

    private class StreamLogThread extends Thread {
        boolean streamPlaying = true;
        FileWriter fw = null;
        BufferedWriter bw;

        public void run() {
            Log.e(TAG, "TimeThread start");
            try {
                try {
                    fw = new FileWriter(MainActivity.getAppDir() + "/output.txt", false);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                bw = new BufferedWriter(fw);
                bw.write("Hello, Sih! Hello, Android!");
                bw.newLine();
                //bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            while (streamPlaying) {
                try {
                    Thread.sleep(33);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //Log.e(TAG,"lost="+mVideoView.getLostP());
                try {
                    bw.write("time:" + SystemClock.uptimeMillis() + ",   lost frame:" + mVideoView.getLostP());
                    bw.newLine();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            try {
                if (bw != null)
                    bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void stopPlay() {
            streamPlaying = false;
        }
    }

    //add by John 2015.11.3
    public void showwattingDialog() {
        Log.i("moop", "showwattingDialog");

        Activity activity = StreamPlayerActivity.this;
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setCancelable(false);
        }
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View v = layoutInflater.inflate(R.layout.loading_dialog, null);
        TextView t = (TextView) v.findViewById(R.id.title_txt);
        t.setText(R.string.connecting_to_camera);
        mProgressDialog.show();
        mProgressDialog.setContentView(v);
    }

    private void dismissdialog() {
        Log.i("moop", "dismissdialog");
        if (mProgressDialog != null) {
            Log.i("moop", "mProgressDialog");
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private class GetWifiInfo extends AsyncTask<URL, Integer, String> {
        @Override
        protected void onPreExecute() {
            setWaitingState(true);
            super.onPreExecute();
            if (mPlaying == true) {
                stop();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        @Override
        protected String doInBackground(URL... params) {
            URL url = null;
            if (mCameraId == 1)
                url = CameraCommand.commandTestRear();
            else if (mCameraId == 0)
                url = CameraCommand.commandTestFront();
            if (url != null) {
                return CameraCommand.sendRequest(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {

            Activity activity = StreamPlayerActivity.this;
            String err = result.substring(0, 3);
            if (result != null && err.equals("709") != true) {
                if (mCameraId == 1) {
                    mCameraId = 0;
                } else if (mCameraId == 0) {
                    mCameraId = 1;
                }
                if (mPlaying == true) {
                    mPlaying = false;
                    mVideoView.releasePlayer();
                }
                playLiveStream();
                if (activity != null)
                    Toast.makeText(activity,
                            activity.getResources().getString(R.string.message_command_succeed),
                            Toast.LENGTH_SHORT).show();
//                showChangShotwattingDialog();
//                mHandler_ui.sendEmptyMessageDelayed(MSG_DISSCHANGSHOT, 5000);
            } else if (activity != null) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.message_command_failed),
                        Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(result);
        }
    }

    public void play(int connectionDelay) {
        Log.i("moop", "play---" + MainActivity.StreamPlayUrl);
        if (context != null) {
//mProgressDialog.setTitle("Connecting to Camera") ;
            showwattingDialog();
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    Log.i("moop", "751");
                    if (mPlaying == false && mRecordmode.equals("Videomode")) {
                        mPlaying = true;
                        mVideoView.createPlayer(MainActivity.StreamPlayUrl, true, 600);
                        mEndReached = false;
                    }
                    if (mProgressDialog != null && mProgressDialog.isShowing()) {
//                        if (!isfirstOpenView){
                        mHandler_ui.sendEmptyMessageDelayed(MSG_PLYDISSMISS, 2000);
//                        }
                    }
                }
            }, connectionDelay);
        }
    }

    private void playLiveStream() {
        Log.i("moop", "playLiveStream");

        play(MainActivity.sConnectionDelay);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equalsIgnoreCase(VLCApplication.SLEEP_INTENT)) {
                StreamPlayerActivity.this.finish();
            }
        }
    };

    public static int px2dip(Context context, float pxValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (pxValue / scale + 0.5f);
    }


    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        Log.i("moop", "onConfigurationChanged");
        if (suface_initWidth == 0) {
            suface_initWidth = mVideoView.getWidth();
            suface_initHeight = mVideoView.getHeight();
            Log.i(TAG, "宽=" + mVideoView.getWidth() + "高=" + mVideoView.getHeight());
        }
//        setSurfaceSize(mVideoWidth, mVideoHeight, mVideoVisibleWidth, mVideoVisibleHeight, mSarNum, mSarDen);
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            playvideo_linrar.setVisibility(View.GONE);
            rela_preview_bottom.setVisibility(View.GONE);
            rela_preview_title.setVisibility(View.GONE);
            isVisiibleBottom = true;
//            mHandler_ui.sendEmptyMessage(15);
            mVideoView.setSize(StreamPlayerActivity.this.getWindow().getDecorView().getHeight(), StreamPlayerActivity.this.getWindow().getDecorView().getWidth());
            mVideoView.setSize(ScreenUtils.getScreenWidth(this), ScreenUtils.getScreenHeight(this));
        } else {
            rela_preview_bottom.setVisibility(View.VISIBLE);
            rela_preview_title.setVisibility(View.VISIBLE);
            isVisiibleBottom = false;
//            mHandler_ui.sendEmptyMessage(15);
            mVideoView.setSize(suface_initWidth, (int) (suface_initHeight*0.56));
        }
        super.onConfigurationChanged(newConfig);
    }


    private Handler mHandler_ui = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_PLYDISSMISS:
                    Log.i("moop", "Streamplay--827");
                    dismissdialog();
                    break;
                case MSG_SUCCESS:
                    break;
                case MSG_FAIL:
                    mProgressDialog.dismiss();
                    CustomDialog alertDialog = new CustomDialog.Builder(context)
                            .setTitle(getResources().getString(R.string.verify))
                            .setMessage(getResources().getString(R.string.verify_error))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
// TODO Auto-generated method stub
                                    arg0.dismiss();
                                    StreamPlayerActivity.this.finish();
                                    return;
                                }
                            }).create();

                    alertDialog.show();
                    break;
                case MSG_DESTORY:
                    if (StreamPlayerActivity.this != null)
                        StreamPlayerActivity.this.finish();
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private static final int SURFACE_SIZE = 1;


    /**
     * Handle libvlc asynchronous events
     */


    private void endReached() {
        Log.i("moop", "endReached");

/* Exit player when reach the end */
        mEndReached = true;
//getActivity().onBackPressed() ;
    }

    private void encounteredError() {
        Log.i("moop", "encounteredError");

        if (false) {
            new MediaUrlDialog(context, MainActivity.StreamPlayUrl, new MediaUrlDialog.MediaUrlDialogHandler() {
                @Override
                public void onCancel() {
// TODO Auto-generated method stub
                }
            }).show();
        }
        mHandler_ui.sendEmptyMessage(MSG_PLYDISSMISS);
        mHandlerUI.removeMessages(MSG_INVISIABLE_RECORD_BTN);
        mHandlerUI.removeMessages(MSG_VISIABLE_RECORD_BTN);
        tv_playVideoError.setVisibility(View.VISIBLE);
        mSurface.setVisibility(View.GONE);
        cameraRecordButton.setVisibility(View.GONE);
        curdate.setVisibility(View.GONE);
        recordButton.setEnabled(false);
        snapshotButton.setEnabled(false);
//		playLiveStream();
    }

    private void playing() {
        Log.i("moop", "playing");
        if (isfirstOpenView) {
            mHandler_ui.sendEmptyMessage(MSG_PLYDISSMISS);
            if (mRecordStatus.equals("Recording")) {
                mHandlerUI.sendEmptyMessage(MSG_INVISIABLE_RECORD_BTN);
                mHandlerUI.sendEmptyMessage(MSG_VISIABLE_RECORD_BTN);
            }
        }

        isfirstOpenView = true;
        tv_playVideoError.setVisibility(View.GONE);
        mSurface.setVisibility(View.VISIBLE);
        cameraRecordButton.setVisibility(View.VISIBLE);

        curdate.setVisibility(View.VISIBLE);
    }

    private void handleVout(Message msg) {
        if (msg.getData().getInt("data") == 0 && mEndReached) {
            Log.i(TAG, "Video track lost");
            stop();
            playLiveStream();
        }
    }


    private Handler mTimeHandler = new Handler() {
        public void handleMessage(Message msg) {

            long timeElapsed = SystemClock.uptimeMillis() - mCameraUptimeMills;
            Date currentTime = new Date(mCameraTime.getTime() + timeElapsed);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.US);
            String currentTimeStr = sdf.format(currentTime);
            DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            Time localTime = new Time("Asia/Hong_Kong");
            localTime.setToNow();
            long second = 0;
            try {
                Date d1 = df.parse(localTime.format("%Y/%m/%d %H:%M:%S"));
                Date d2 = df.parse(currentTimeStr);
                long diff = d2.getTime() - d1.getTime();
                second = Math.abs(diff / 1000);
            } catch (ParseException e) {
                Log.i("moop", "Exception" + e);
                e.printStackTrace();
            }
            if (second > 60) {
                Log.i("moop", "时间同步");
                URL url = CameraCommand.commandCameraTimeSettingsUrl();
                CameraCommand cameraCommand = new CameraCommand();
                if (url != null) {
                    new CameraCommand.SendRequest().execute(url);
                    new GetTimeStamp().execute();
                }
            }
            curdate.setText(currentTimeStr);
            super.handleMessage(msg);
        }
    };

    public Handler mRecordStatusHandler = new Handler() {
        public void handleMessage(Message msg) {

            if (mRecordStatus.equals("Recording")) {
                mHandlerUI.sendEmptyMessage(MSG_INVISIABLE_RECORD_BTN);
                mRecordTxt.setText(R.string.recording);
            } else {
                mHandlerUI.removeMessages(MSG_INVISIABLE_RECORD_BTN);
                mHandlerUI.removeMessages(MSG_VISIABLE_RECORD_BTN);
                cameraRecordButton.setVisibility(View.VISIBLE);
                mRecordTxt.setText(R.string.label_app_record);
            }
            if (mRecordmode.equals("NotVideomode")) {
                CustomDialog alertDialog = new CustomDialog.Builder(context)
                        .setTitle("Mode Error!")
                        .setMessage("Not At Video Mode")
                        .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
// TODO Auto-generated method stub
                                arg0.dismiss();
                            }
                        }).create();
                alertDialog.show();
            }
            super.handleMessage(msg);
        }
    };

    private class TimeThread extends Thread {
        boolean mPlaying = true;

        public void run() {
            Log.i("moop", "TimeThread");
            while (mPlaying) {
                try {
                    Thread.sleep(1000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (mCameraTime == null) {
                    continue;
                }
//if(mRecordthread)
                mTimeHandler.sendMessage(mTimeHandler.obtainMessage());
            }
        }

        public void stopPlay() {
            mPlaying = false;
        }
    }


    private void layoutanimationGONE() {
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(1, 0);
        alphaAnimation.setDuration(500);
        animationSet.addAnimation(alphaAnimation);
        rela_preview_bottom.startAnimation(animationSet);
//        rela_preview_title.startAnimation(animationSet);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rela_preview_bottom.setVisibility(View.GONE);
                rela_preview_title.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private void layoutanimationVISIBLE() {
        AnimationSet animationSet = new AnimationSet(true);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
        alphaAnimation.setDuration(500);
        animationSet.addAnimation(alphaAnimation);
        rela_preview_bottom.startAnimation(animationSet);
//        rela_preview_title.startAnimation(animationSet);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rela_preview_bottom.setVisibility(View.VISIBLE);
//                rela_preview_title.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
    }

    private class DownloadTask extends AsyncTask<URL, Long, Boolean> {
        String mFileName;
        String mIp;
        boolean mCancelled;

        @Override
        protected Boolean doInBackground(URL... urls) {
            File file = null;
            Log.i(TAG, "DownloadTask doInBackground " + urls[0]);
            try {
                mIp = urls[0].getHost();
                HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(30000);
                urlConnection.setReadTimeout(30000);
                urlConnection.setUseCaches(false);
                urlConnection.setDoInput(true);
                urlConnection.connect();

                int responseCode = urlConnection.getResponseCode();
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    return null;
                }
                InputStream inputStream = urlConnection.getInputStream();

                mFileName = urls[0].getFile().substring(urls[0].getFile().lastIndexOf(File.separator) + 1);

                File appDir = MainActivity.getAppDir();
                file = new File(appDir, mFileName);

                Log.i(TAG, "DownloadTask 下载文件存储地址：" + appDir.getPath() + File.separator + mFileName);

                if (file.exists()) {
                    file.delete();
                }

                file.createNewFile();

                FileOutputStream fileOutput = new FileOutputStream(file);

                byte[] buffer = new byte[1024 * 1000];
                int bufferLength = 0;
                try {
                    Log.i(TAG, "begin download");
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        publishProgress(Long.valueOf(urlConnection.getContentLength()), file.length());
                        fileOutput.write(buffer, 0, bufferLength);
                        if (mCancelled) {
                            urlConnection.disconnect();
                            Log.i(TAG, "disconnect 执行完毕");

                            try {
                                inputStream.close();
                                fileOutput.close();
//                                sleepHandler.sendEmptyMessageDelayed(2, 1000 * 0);
                            } catch (IOException exio) {
                                Log.i(TAG, "下载失败 IOException1");
                            }
                            file.delete();
                            /*end delete file when cancel download files*/
                            break;
                        }
                    }
                } finally {
                    if (!mCancelled) {
                        try {
                            fileOutput.close();

                        } catch (IOException exio) {
                            Log.i(TAG, "下载失败 IOException2");
                        }
                    }
                }
                if (mCancelled && file.exists()) {
                    Log.i(TAG, "end leak mem");
                    file.delete();
                    return false;
                }
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri uri = Uri.fromFile(file);
                intent.setData(uri);
                VLCApplication.getAppContext().sendBroadcast(intent);
                return true;

            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.i(TAG, "下载失败 IOException3");
                file.delete();
                e.printStackTrace();
                return false;
            }

        }
    }


}
