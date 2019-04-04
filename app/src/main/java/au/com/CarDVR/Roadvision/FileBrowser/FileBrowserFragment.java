package au.com.CarDVR.Roadvision.FileBrowser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import au.com.CarDVR.Roadvision.CameraCommand;
import au.com.CarDVR.Roadvision.CustomDialog;
import au.com.CarDVR.Roadvision.ImagebrowseActivity;
import au.com.CarDVR.Roadvision.MainActivity;
import au.com.CarDVR.Roadvision.R;
import au.com.CarDVR.Roadvision.FileBrowser.Model.FileNode;
import au.com.CarDVR.Roadvision.FileBrowser.Model.FileNode.Format;
import au.com.CarDVR.Roadvision.RTPullListView;
import au.com.CarDVR.Roadvision.StartRecord;
import ijkplay.OriginPlayerActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class FileBrowserFragment extends Fragment {

    public final static String TAG = "FileBrowserFragmens";
    public static final String DEFAULT_PATH = "/cgi-bin/Config.cgi";
    public static final String DEFAULT_DIR = "DCIM";
    public static String mRecordStatus = "";
    public static String mRecordmode = "";
    private ProgressDialog mProgDlg;
    private int mTotalFile;
    ProgressDialog mProgressDialog = null;
    static ProgressDialog mProgressDialog2 = null;
    private boolean mCancelDelete;
    private static final int MSG_SHOWDIALOG = 1;
    private static final int MSG_DISMISSDIALOG = 2;
    private static final int MSG_GETFILELIST = 3;
    private static final int MSG_FRONTVIDEONOMORE = 4;
    private static final int MSG_BACKVIDEONOMORE = 5;
    private static final int MSG_FRONTPHOTONOMORE = 6;
    private static final int MSG_BACKPHOTONOMORE = 10;
    private static final int MSG_SOSFRONTVIDEONOMORE = 7;
    private static final int MSG_SOSBACKVIDEONOMORE = 8;
    private static final int MSG_CONNECTERROREXIT = 9;//连接失败退出Dialog
    private static final int G_TRYMAXTIMES = 5;
    private static final int LOAD_MORE_SUCCESS = 3;//点击加载更多成功
    private static final int LOAD_NEW_INFO = 5;    //加载新信息
    private static final int MSG_CLEARSELECT = 1;        //清除listviewitem选中状态
    private static final int G_TRYTIME = 5000;  //5s
    private int mTryTimes = G_TRYMAXTIMES;
    private FileNode.Format m_fileformat = FileNode.Format.all;
    private String m_dir = "dir";
    private String m_reardir = "reardir";
    private String m_DCIM = "DCIM";
    private String m_Event = "Event";
    private String m_Photo = "Photo";
    RelativeLayout backVideofooterView;
    RelativeLayout frontVideofooterView;
    RelativeLayout frontPhotofooterView;
    RelativeLayout backPhotofooterView;
    RelativeLayout frontSOSfooterView;
    RelativeLayout backSOSfooterView;
    private static final int FILEREADBLOCK_SIZE = 1024 * 1000;
    private static final int BUTTON_NONE = 0;
    private static final int BUTTON_DOWNLOAD = 1;
    private static final int BUTTON_OPEN = 2;
    private int mCurrentButton = BUTTON_NONE;
    private ProgressBar moreProgressBar;
    int deleteflag;
    boolean deleteOver = false;
    Activity activitytoals;

    private RTPullListView frontVideoListView, backVideoListView, frontPhotoListView, backPhotoListView, frontSOSListView, backSOSListView;
    public FileBrowser rowsers = null;
    private boolean isstatusVideo; //判断是照片还是视频加载更多
    private boolean isstatusPhoto = false; //判断是照片还是视频加载更多

    private boolean isfirstPhoto = true;//判断第一次点击照片选项
    private boolean isfirstViedeo = true;//判断第一次点击视频选项
    private boolean isfirstBackViedeo = true;//判断第一次点击后路视频选项
    private boolean isfirstonePhoto = true;//判断第一次点击照片选项
    private boolean isfirstonebackPhoto = true;//判断第一次点击后照片选项
    private boolean isfirstSOSfrontViedeo = true;//判断第一次点击SOS前视频选项
    private boolean isfirstSOSbackViedeo = true;//判断第一次点击SOS后视频选项
    //add by John 2015.11.3
    public boolean isdeletesos = false;
    public boolean isdeletSOStoast = false;
    public boolean isdeleteSOSfileforthree = true;//判断第三个版本SOS文件删除
    private int VERSION = 0;
    URL threeviedourl;//第三版本URl地址
    URL threebackviedourl;//第三版本URl地址
    URL threephotourl;//第三版本URl地址试试
    URL threesosfrontvideo;
    static Timer timer = new Timer();
    //add john 2015.12.19
    private static boolean isDownloadFile = false;// 判断是否正在执行下载任务
    static boolean isDownload = true;
    private static boolean isTounch = true;
    private SideIndexGestureListener mSideIndexGestureListener;
    private int version;
    Toast mToast;


    private void sortfile() {

        fileVideoFrontList.clear();
        fileVideoBackList.clear();

        filePhotoFrontList.clear();
        filePhotoBackList.clear();

        fileSOSBackList.clear();
        fileSOSFrontList.clear();

        if (stackVideoFrontList.size() > 0) {
//			for(int i=len-1;i>=0;i--)
            for (int i = 0; i < stackVideoFrontList.size(); i++) {
                fileVideoFrontList.add(stackVideoFrontList.get(i));
            }
        }

        if (stackVideoBackList.size() > 0) {
            for (int i = 0; i < stackVideoBackList.size(); i++) {
                fileVideoBackList.add(stackVideoBackList.get(i));
            }
        }
        if (stackPhotoFrontList.size() > 0) {
//			for(int i=len_pic-1;i>=0;i--)
            for (int i = 0; i < stackPhotoFrontList.size(); i++) {
                filePhotoFrontList.add(stackPhotoFrontList.get(i));
            }
        }
        if (stackPhotoBackList.size() > 0) {
//			for(int i=len_pic-1;i>=0;i--)
            for (int i = 0; i < stackPhotoBackList.size(); i++) {
                filePhotoBackList.add(stackPhotoBackList.get(i));
            }
        }
        if (stackSOSBackList.size() > 0) {
//			for(int i=len_pic-1;i>=0;i--)
            for (int i = 0; i < stackSOSBackList.size(); i++) {
                fileSOSBackList.add(stackSOSBackList.get(i));
            }
        }
        if (stackSOSFrontList.size() > 0) {
//			for(int i=len_pic-1;i>=0;i--)
            for (int i = 0; i < stackSOSFrontList.size(); i++) {
                fileSOSFrontList.add(stackSOSFrontList.get(i));
            }
        }


    }

    private class GetRecordStatus extends AsyncTask<URL, Integer, String> {
        @Override
        protected void onPreExecute() {
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
            if (result != null) {
                String[] lines;
                String[] lines_temp = result.split("Camera.Preview.MJPEG.status.record=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    if (lines != null)
                        mRecordStatus = lines[0];
                }
                lines_temp = result.split("Camera.Preview.MJPEG.status.mode=");
                if (null != lines_temp && 1 < lines_temp.length) {
                    lines = lines_temp[1].split(System.getProperty("line.separator"));
                    if (lines != null)
                        mRecordmode = lines[0];
                }
            } else {
                mRecordmode = "";
                mRecordStatus = "";
            }
            if (mRecordmode.equals("Videomode")) {
                if (mRecordStatus.equals("Recording")) {
                    mRecordStatusHandler.sendMessage(mRecordStatusHandler.obtainMessage());
                } else {
                    if (BUTTON_DOWNLOAD == mCurrentButton) {
                        Log.i(TAG, "下载文件");
                        downloadFile(getActivity(), mIp);
                    } else if (BUTTON_OPEN == mCurrentButton) {
                        Log.i(TAG, "打开文件");
                        ///modify by eric
                        if (sSelectedFiles.size() < 0) {
                            return;
                        }
                        FileNode fileNode = sSelectedFiles.get(0);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        if (fileNode.mFormat == Format.mov
                                || fileNode.mFormat == Format.mp4
                                || fileNode.mFormat == Format.avi) {
                            intent.setDataAndType(Uri.parse("http://" + mIp + fileNode.mName), "video/3gp");
//                            startActivity(intent);
                            Intent intent1 = new Intent(getContext(), OriginPlayerActivity.class);
                            intent.putExtra("URL", "http://" + mIp + fileNode.mName);
                            startActivity(intent1);

                        } else if (fileNode.mFormat == Format.jpeg) {
                            // CarDV WiFi Support Video container is 3GP (.MOV)
                            // For HTTP File Streaming
                            intent.setDataAndType(Uri.parse("http://" + mIp + fileNode.mName), "image/jpeg");
                            startActivity(intent);
                        }
                        myHandler.sendEmptyMessage(MSG_CLEARSELECT);

                    }
                }
            }
            super.onPostExecute(result);
        }
    }

    /*请求视频数据（单独）*/
    private class VideoDownFileListTask extends AsyncTask<FileBrowser, Integer, FileBrowser> {
        @Override
        protected void onPreExecute() {
            if (isfirstOpen) {
                setWaitingState(true);
                fileVideoFrontList.clear();
                stackVideoFrontList.clear();
                filePhotoFrontList.clear();
                stackPhotoFrontList.clear();
                filePhotoBackList.clear();
                stackPhotoBackList.clear();
                fileVideoBackList.clear();
                stackVideoBackList.clear();
                stackSOSFrontList.clear();
                fileSOSFrontList.clear();
                stackSOSBackList.clear();
                fileSOSBackList.clear();
                notifyDataAdapter();
            }
            super.onPreExecute();
        }

        @Override
        protected FileBrowser doInBackground(FileBrowser... browsers) {
            if (isfirstViedeo) {
                browsers[0].retrieveFileList(m_DCIM, m_fileformat, true, stackVideoFrontList.size(), m_dir);
                Log.d("moop", "mDirectory=" + mDirectory);
                isfirstViedeo = false;
            } else
                browsers[0].retrieveFileList(m_DCIM, m_fileformat, false, stackVideoFrontList.size(), m_dir);
            return browsers[0];
        }

        @Override
        protected void onPostExecute(FileBrowser result) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }

            if (activity != null) {
                List<FileNode> fileNodeList = result.getFileList();
                Log.d(TAG, "视频请求返-文件数" + fileNodeList.size());
                if (!result.mIsError) {
                    Log.d(TAG, "onPostExecute() !IsError");
                    mTryTimes = G_TRYMAXTIMES;
                } else if (fileNodeList.size() > 0) {
                    Log.d(TAG, "onPostExecute() size()>0");
                    mTryTimes = G_TRYMAXTIMES;
                } else {
                    //try again!
                    Log.d(TAG, "error! try again");
                    mHandler.sendEmptyMessage(MSG_CONNECTERROREXIT);
//					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_GETFILELIST),G_TRYTIME);
                }
                for (int i = 0; i < fileNodeList.size(); i++) {
                    stackVideoFrontList.add(fileNodeList.get(i));
                }

                sortfile();
                videoFrontAdapter.notifyDataSetChanged();
                if (!result.isCompleted() && fileNodeList.size() > 0) {
                    videoFrontAdapter.notifyDataSetChanged();
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    moreProgressBar.setVisibility(View.GONE);
                } else {
                    moreProgressBar.setVisibility(View.GONE);
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    mHandler.sendEmptyMessage(MSG_FRONTVIDEONOMORE);
                    setWaitingState(false);
                }
            }
        }
    }

    /*请求照片数据（前路）*/
    private class PhotoDownFileListTask extends AsyncTask<FileBrowser, Integer, FileBrowser> {
        @Override
        protected FileBrowser doInBackground(FileBrowser... browsers) {
            if (isfirstonePhoto) {
                isfirstonePhoto = false;
                browsers[0].retrieveFileList(m_Photo, m_fileformat, true, stackPhotoFrontList.size(), m_dir);
            } else
                browsers[0].retrieveFileList(m_Photo, m_fileformat, false, stackPhotoFrontList.size(), m_dir);
            return browsers[0];
        }

        @Override
        protected void onPostExecute(FileBrowser result) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            if (activity != null) {
                List<FileNode> fileNodeList = result.getFileList();
                Log.d(TAG, "照片请求返文件数：" + fileNodeList.size());
                for (int i = 0; i < fileNodeList.size(); i++) {
                    stackPhotoFrontList.add(fileNodeList.get(i));
                }
                sortfile();
                photoFrontAdapter.notifyDataSetChanged();
                if (!result.isCompleted() && fileNodeList.size() > 0) {
                    photoFrontAdapter.notifyDataSetChanged();
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    moreProgressBar.setVisibility(View.GONE);
                } else {
                    moreProgressBar.setVisibility(View.GONE);
                    mHandler.sendEmptyMessage(MSG_FRONTPHOTONOMORE);
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    setWaitingState(false);
                }
            }
        }
    }

    /*请求照片数据（后路）*/
    private class PhotoBackDownFileListTask extends AsyncTask<FileBrowser, Integer, FileBrowser> {
        @Override
        protected FileBrowser doInBackground(FileBrowser... browsers) {
            if (isfirstonebackPhoto) {
                isfirstonebackPhoto = false;
                browsers[0].retrieveFileList(m_Photo, m_fileformat, true, stackPhotoBackList.size(), m_reardir);
            } else
                browsers[0].retrieveFileList(m_Photo, m_fileformat, false, stackPhotoBackList.size(), m_reardir);
            return browsers[0];
        }

        @Override
        protected void onPostExecute(FileBrowser result) {
            Activity activity = getActivity();
            if (activity == null)
                return;

            if (activity != null) {
                List<FileNode> fileNodeList = result.getFileList();
                Log.d(TAG, "后照片请求返文件数：" + fileNodeList.size());
                for (int i = 0; i < fileNodeList.size(); i++) {
                    stackPhotoBackList.add(fileNodeList.get(i));
                }
                sortfile();
                photoBackAdapter.notifyDataSetChanged();
                if (!result.isCompleted() && fileNodeList.size() > 0) {
                    photoBackAdapter.notifyDataSetChanged();
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    moreProgressBar.setVisibility(View.GONE);
                } else {
                    moreProgressBar.setVisibility(View.GONE);
                    mHandler.sendEmptyMessage(MSG_BACKPHOTONOMORE);
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    setWaitingState(false);
                }
            }
        }
    }

    /*请求后路摄像头视频文件（单独）*/
    private class BackViedoDownFileListTask extends AsyncTask<FileBrowser, Integer, FileBrowser> {
        @Override
        protected FileBrowser doInBackground(FileBrowser... browsers) {
            if (isfirstBackViedeo) {
                browsers[0].retrieveFileList(m_DCIM, m_fileformat, true, stackVideoBackList.size(), m_reardir);
                isfirstBackViedeo = false;
            } else
                browsers[0].retrieveFileList(m_DCIM, m_fileformat, false, stackVideoBackList.size(), m_reardir);
            return browsers[0];
        }

        @Override
        protected void onPostExecute(FileBrowser result) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            if (activity != null) {
                List<FileNode> fileNodeList = result.getFileList();
                for (int i = 0; i < fileNodeList.size(); i++) {
                    stackVideoBackList.add(fileNodeList.get(i));
                }
                Log.d(TAG, "后路视频请求返文件个数：" + stackVideoBackList.size());
                sortfile();
                videoBackAdapter.notifyDataSetChanged();
                if (!result.isCompleted() && fileNodeList.size() > 0) {
                    videoBackAdapter.notifyDataSetChanged();
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    moreProgressBar.setVisibility(View.GONE);
                } else {
                    moreProgressBar.setVisibility(View.GONE);
                    mHandler.sendEmptyMessage(MSG_BACKVIDEONOMORE);
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    setWaitingState(false);
                }
            }
        }
    }

    /*请求SOS前路摄像头视频文件（前路）*/
    private class SOSFrontViedoDownFileListTask extends AsyncTask<FileBrowser, Integer, FileBrowser> {
        @Override
        protected FileBrowser doInBackground(FileBrowser... browsers) {
            if (isfirstSOSfrontViedeo) {
                browsers[0].retrieveFileList(m_Event, m_fileformat, true, fileSOSFrontList.size(), m_dir);
                isfirstSOSfrontViedeo = false;
            } else
                browsers[0].retrieveFileList(m_Event, m_fileformat, false, fileSOSFrontList.size(), m_dir);
            return browsers[0];
        }

        @Override
        protected void onPostExecute(FileBrowser result1) {
            Activity activity = getActivity();

            if (activity == null) {
                return;
            }

            if (activity != null) {
                List<FileNode> fileNodeList = result1.getFileList();
                for (int i = 0; i < fileNodeList.size(); i++) {
                    Log.d(TAG, "5SOS视频返回文件数：" + result1.getFileList().size());
                    stackSOSFrontList.add(fileNodeList.get(i));
                }
                sortfile();
                sosFrontAdapter.notifyDataSetChanged();
                if (!result1.isCompleted() && fileNodeList.size() > 0) {
                    sosFrontAdapter.notifyDataSetChanged();
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    moreProgressBar.setVisibility(View.GONE);
                } else {
                    moreProgressBar.setVisibility(View.GONE);
                    mHandler.sendEmptyMessage(MSG_SOSFRONTVIDEONOMORE);
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    setWaitingState(false);
                }
            }
        }
    }

    /*请求SOS后路摄像头视频文件（后路）*/
    private class SOSBackViedoDownFileListTask extends AsyncTask<FileBrowser, Integer, FileBrowser> {
        @Override
        protected FileBrowser doInBackground(FileBrowser... browsers) {
            if (isfirstSOSbackViedeo) {
                browsers[0].retrieveFileList(m_Event, m_fileformat, true, fileSOSBackList.size(), m_reardir);
                isfirstSOSbackViedeo = false;
            } else
                browsers[0].retrieveFileList(m_Event, m_fileformat, false, fileSOSBackList.size(), m_reardir);
            return browsers[0];
        }

        @Override
        protected void onPostExecute(FileBrowser result) {
            Activity activity = getActivity();
            if (activity == null) {
                return;
            }
            if (activity != null) {
                List<FileNode> fileNodeList = result.getFileList();
                for (int i = 0; i < fileNodeList.size(); i++) {
                    stackSOSBackList.add(fileNodeList.get(i));
                }
                sortfile();
                sosFrontAdapter.notifyDataSetChanged();
                if (!result.isCompleted() && fileNodeList.size() > 0) {
                    Log.d(TAG, "SOS后路视频返回文件数：" + fileSOSBackList.size());
                    sosBackAdapter.notifyDataSetChanged();
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    moreProgressBar.setVisibility(View.GONE);
                } else {
                    moreProgressBar.setVisibility(View.GONE);
                    mHandler.sendEmptyMessage(MSG_SOSBACKVIDEONOMORE);
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    setWaitingState(false);
                }
            }
        }
    }

    /*第一次请求Sd卡文件列表*/
    private class DownloadFileListTask extends AsyncTask<FileBrowser, Integer, FileBrowser> {
        private boolean error;

        @Override
        protected void onPreExecute() {
            setWaitingState(true);
            filePhotoFrontList.clear();
            fileVideoFrontList.clear();
            stackPhotoFrontList.clear();
            stackVideoFrontList.clear();
            fileSOSFrontList.clear();
            notifyDataAdapter();
            sSelectedFiles.clear();

            super.onPreExecute();
        }

        @Override
        protected FileBrowser doInBackground(FileBrowser... browsers) {
//            browsers[0].retrieveFileList(mDirectory, m_fileformat, true,-1) ;
            Log.d("moop", "mDirectory=" + mDirectory);

            error = browsers[0].mIsError;
            return browsers[0];
        }

        @Override
        protected void onPostExecute(FileBrowser result) {
            Activity activity = getActivity();
            if (activity != null) {
                List<FileNode> fileList = result.getFileList();
                if (!result.mIsError) {
                    Log.d(TAG, "onPostExecute() !IsError");
                    mTryTimes = G_TRYMAXTIMES;
                } else if (fileList.size() > 0) {
                    Log.d(TAG, "onPostExecute() size()>0");
                    mTryTimes = G_TRYMAXTIMES;
                } else {
                    //try again!
                    Log.d(TAG, "error! try again");
                    mHandler.sendEmptyMessage(MSG_CONNECTERROREXIT);
//					mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_GETFILELIST),G_TRYTIME);
                }
                Log.d(TAG, "第一次请求视频文件数：" + fileList.size());
                for (int i = 0; i < fileList.size(); i++) {
                    if (fileList.get(i).mFormat == FileNode.Format.avi
                            || fileList.get(i).mFormat == FileNode.Format.mov
                            || fileList.get(i).mFormat == FileNode.Format.mp4) {
                        stackVideoFrontList.add(fileList.get(i));
                    } else if (fileList.get(i).mFormat == FileNode.Format.jpeg) {
                        stackPhotoFrontList.add(fileList.get(i));
                    }
                }
                sortfile();
                rowsers = result;
                if (!result.isCompleted() && fileVideoFrontList.size() < 6) {
//                    new ContiunedDownloadTask().execute(result);
                    mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
                    Log.i(TAG, "第一次，视频不足六个，继续请求");
                }
                if (!result.isCompleted() && fileList.size() > 0) {
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                } else {
                    mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
                    setWaitingState(false);
                }
                videoFrontAdapter.notifyDataSetChanged();
            }
        }
    }

    private String mIp;
    private String mPath;
    private String mDirectory;

    private static final String KEY_IP = "ip";
    private static final String KEY_PATH = "path";
    private static final String KEY_DIRECTORY = "directory";

    private static ArrayList<FileNode> fileVideoFrontList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> fileVideoBackList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> filePhotoFrontList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> filePhotoBackList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> fileSOSFrontList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> fileSOSBackList = new ArrayList<FileNode>();

    private static ArrayList<FileNode> stackVideoFrontList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> stackVideoBackList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> stackPhotoFrontList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> stackPhotoBackList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> stackSOSFrontList = new ArrayList<FileNode>();
    private static ArrayList<FileNode> stackSOSBackList = new ArrayList<FileNode>();

    private static List<FileNode> sSelectedFiles = new LinkedList<FileNode>();

    private static FileListVideoAdapter videoFrontAdapter;
    private static FileListVideoAdapter videoBackAdapter;
    private static FileListJpgAdapter photoFrontAdapter;
    private static FileListJpgAdapter photoBackAdapter;
    private static FileListVideoAdapter sosFrontAdapter;
    private static FileListVideoAdapter sosBackAdapter;
    //private TextView mFileListTitle ;
    private LinearLayout mSaveButton;
    private LinearLayout mDeleteButton;
    private LinearLayout mOpenButton;
    private String mFileBrowser;
    private String mReading;
    private String mItems;
    private LinearLayout btn_photo;
    private LinearLayout btn_video;
    private LinearLayout btn_sosvideo;
    private LinearLayout btn_frontvideo;
    private LinearLayout btn_backvideo;
    private LinearLayout btn_frontsos;
    private LinearLayout btn_backsos;
    private LinearLayout btn_frontphoto;
    private LinearLayout btn_backphoto;
    private LinearLayout fbvideo_layout;
    private LinearLayout fbphoto_layout;
    private LinearLayout fbsos_layout;
    private Boolean isvideo = true;	/*video is default*/
    private boolean isfirstOpen = true;
    private static String gDownloadStr;
    private static String gpleasewait;
    private static String gcancel;
    public int videolistsize = 0, jpglistsize = 0;
    CustomDialog stoprecordalertDialog;
    ///added by eric for show stop recording dialog

    @SuppressLint("HandlerLeak")
    public Handler mRecordStatusHandler = new Handler() {
        public void handleMessage(Message msg) {

            if (mRecordmode.equals("Videomode")) {
                if (mRecordStatus.equals("Recording")) {
                    stoprecordalertDialog = new CustomDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.trip))
                            .setMessage(R.string.sd_browser_stoprecord)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    // TODO Auto-generated method stub
                                    arg0.dismiss();
                                    new StartRecord().stopRecord();

                                    if (BUTTON_DOWNLOAD == mCurrentButton) {
                                        downloadFile(getActivity(), mIp);
                                    } else if (BUTTON_OPEN == mCurrentButton) {
                                        ///modify by eric
                                        FileNode fileNode = sSelectedFiles.get(0);
                                        if (fileNode.mFormat == Format.mov
                                                || fileNode.mFormat == Format.mp4
                                                || fileNode.mFormat == Format.avi) {
//                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            // CarDV WiFi Support Video container is 3GP (.MOV)
                                            // For HTTP File Streaming
//                                            intent.setDataAndType(Uri.parse("http://" + mIp + fileNode.mName), "video/3gp");
//                                            startActivity(intent);
                                            Intent intent1 = new Intent(getActivity(), OriginPlayerActivity.class);
                                            intent1.putExtra("URL", "http://" + mIp + fileNode.mName);
                                            startActivity(intent1);
                                            // For HTML5 Video Streaming
//									String filename = fileNode.mName.replaceAll("/", "\\$");
//									Intent browserIntent = new Intent(Intent.ACTION_VIEW,
//												Uri.parse("http://"+mIp+"/cgi-bin/Config.cgi?action=play&property=" + filename));
//									startActivity(browserIntent);
//									mSaveButton.setEnabled(false) ;
//									mDeleteButton.setEnabled(false) ;
//									mOpenButton.setEnabled(false) ;
                                        } else if (fileNode.mFormat == Format.jpeg) {
                                            Intent intent = new Intent(getActivity(), ImagebrowseActivity.class);
                                            intent.putExtra("imageUrl", "http://" + mIp + fileNode.mName);
                                            // CarDV WiFi Support Video container is 3GP (.MOV)
                                            // For HTTP File Streaming
//                                            intent.setDataAndType(Uri.parse("http://" + mIp + fileNode.mName), "image/jpeg");
                                            startActivity(intent);

                                        }
                                    }
                                }
                            })
                            .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//							myHandler.sendEmptyMessage(MSG_CLEARSELECT);
                                    dialog.dismiss();
                                    //disconnectWifi();///added by eric for disconnect wifi
                                    //finish();
                                }
                            }).create();
                    stoprecordalertDialog.show();
                    super.handleMessage(msg);
                    return;
                }

            }
            //finish();
            super.handleMessage(msg);
        }
    };

    public static FileBrowserFragment newInstance(String ip, String url, String directory) {

        FileBrowserFragment fragment = new FileBrowserFragment();

        Bundle args = new Bundle();

        if (ip != null)
            args.putString(KEY_IP, ip);

        if (url != null)
            args.putString(KEY_PATH, url);

        if (directory != null)
            args.putString(KEY_DIRECTORY, directory);

        fragment.setArguments(args);

        return fragment;
    }

    private static int sNotificationCount = 0;
    private static DownloadTask sDownloadTask = null;

    /*文件下载*/
    private static class DownloadTask extends AsyncTask<URL, Long, Boolean> {

        String mFileName;
        Context mContext;
        WifiLock mWifiLock;
        String mIp;
        boolean mCancelled;
        PowerManager.WakeLock mWakeLock;
        TextView cont_schedule;


        private ProgressDialog mProgressDialog;

        DownloadTask(Context context) {

            mContext = context;
        }

        @Override
        protected void onPreExecute() {

            Log.i(TAG, "DownloadTask onPreExecute");

            WifiManager wm = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
            mWifiLock = wm.createWifiLock(WifiManager.WIFI_MODE_FULL, "DownloadTask");
            mWifiLock.acquire();

            PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DownloadTask");
            mWakeLock.acquire();

            mCancelled = false;
            showProgress(mContext);

            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(URL... urls) {
            File file = null;
            Log.i(TAG, "DownloadTask doInBackground " + urls[0]);

            try {
                mIp = urls[0].getHost();

                HttpURLConnection urlConnection = (HttpURLConnection) urls[0].openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(8000);
                urlConnection.setReadTimeout(8000);
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

                byte[] buffer = new byte[FILEREADBLOCK_SIZE];
                int bufferLength = 0;
                try {
                    Log.i(TAG, "begin download");
                    while ((bufferLength = inputStream.read(buffer)) > 0) {
                        publishProgress(Long.valueOf(urlConnection.getContentLength()), file.length());
                        fileOutput.write(buffer, 0, bufferLength);

                        if (mCancelled) {

                            urlConnection.disconnect();
                            Log.i(TAG, "disconnect 执行完毕");
//                            Intent i = mContext.getPackageManager()
//                                    .getLaunchIntentForPackage(mContext.getPackageName());
//                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                            mContext.startActivity(i);
                            /*begin delete file when cancel download files*/
                            try {
                                inputStream.close();
                                fileOutput.close();
                            } catch (IOException exio) {
                                Log.i(TAG, "下载失败 IOException1");
                            }
                            file.delete();
                            /*end delete file when cancel download files*/
                            break;
                        }
                    }
                } finally {
                    //Log.i("DownloadTask", "doInBackground close START") ;
                    //inputStream.close() ;
                    //fileOutput.close() ;
                    //Log.i("DownloadTask", "doInBackground disconnect START") ;
                    //urlConnection.disconnect() ;
                    //Log.i("DownloadTask", "doInBackground disconnect FINISHED") ;
                    /*begin leak mem*/
                    if (!mCancelled) {
                        try {
                            fileOutput.close();
                        } catch (IOException exio) {
                            Log.i(TAG, "下载失败 IOException2");
                        }
                    }
                    /*end leak mem*/
                }
                if (mCancelled && file.exists()) {
                    Log.i(TAG, "end leak mem");
                    file.delete();
                    return false;
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.i(TAG, "下载失败 IOException3");
                file.delete();
                e.printStackTrace();
                return false;
            }
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            mContext.sendBroadcast(intent);
            return true;
        }

        @Override
        protected void onProgressUpdate(Long... values) {

            if (mProgressDialog != null) {
                int max = values[0].intValue();
                int progress = values[1].intValue();
                String unit = "Bytes";
                mProgressDialog.setTitle(gDownloadStr + mFileName);

                if (max > 1024) {
                    max /= 1024;
                    progress /= 1024;
                    unit = "KB";
                }

                if (max > 1024) {
                    max /= 1024;
                    progress /= 1024;
                    unit = "MB";
                }
                mProgressDialog.setMax(max);
                mProgressDialog.setProgress(progress);
                Log.i("moop", "progress=" + progress + "max=" + max);
                ratioCalculation(progress, max);
                mProgressDialog.setProgressNumberFormat("%1d/%2d " + unit);
            }
            super.onProgressUpdate(values);
        }

        int ratioy = 0;

        public void ratioCalculation(int pro, int m) {
            int x = (pro * 100) / m;
            if (x != ratioy) {
                ratioy = x;
                cont_schedule.setText(ratioy + "%");
                Log.i("mop1", "y=" + ratioy);
            }
        }

        private void cancelDownload() {
            mCancelled = true;
            isDownloadFile = false;
            for (FileNode fileNode : sSelectedFiles) {
                fileNode.mSelected = false;
            }
//            sDownloadTask.cancel(true);
            sSelectedFiles.clear();
            notifyDataAdapter();
        }

        @Override
        protected void onCancelled() {

            Log.i(TAG, "onCancelled");

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            sDownloadTask = null;
            mWakeLock.release();
            mWifiLock.release();
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Boolean result) {

            Log.i(TAG, "onPostExecute " + mFileName + " " + (mCancelled ? "CANCELLED"
                    : result ? "SUCCESS" : "FAIL"));

            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
            sDownloadTask = null;

            mWakeLock.release();
            mWifiLock.release();

            if (mContext != null) {
                String ext = mFileName.substring(mFileName.lastIndexOf(".") + 1).toLowerCase(Locale.US);
                int nIcon;
                if (ext.equalsIgnoreCase("jpg")) {
                    nIcon = R.drawable.type_photo;
                } else {
                    nIcon = R.drawable.type_video;
                }
                if (!result) {
                    if (!mCancelled) {
                        Notification notification = new Notification.Builder(mContext)
                                .setContentTitle(mFileName).setSmallIcon(nIcon)
                                .setContentText("Download Failed").getNotification();

                        NotificationManager notificationManager = (NotificationManager) mContext
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                        notification.flags |= Notification.FLAG_AUTO_CANCEL;

                        notificationManager.notify(sNotificationCount++, notification);
                    } else {
                        Notification notification = new Notification.Builder(mContext)
                                .setContentTitle(mFileName).setSmallIcon(nIcon)
                                .setContentText("Download Cancelled").getNotification();

                        NotificationManager notificationManager = (NotificationManager) mContext
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                        notification.flags |= Notification.FLAG_AUTO_CANCEL;

                        notificationManager.notify(sNotificationCount++, notification);
                    }
                } else {

                    Uri uri = Uri.parse("file://" + MainActivity.sAppDir + File.separator + mFileName);

                    String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);

                    Log.i("MIME", ext + "  ==>  " + mimeType);

                    Log.i("Path", uri.toString());

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(uri, mimeType);

                    PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent, 0);
                    Notification notification = new Notification.Builder(mContext).setContentTitle(mFileName)
                            .setSmallIcon(nIcon).setContentText("Download Completed")
                            .setContentIntent(pIntent).getNotification();

                    NotificationManager notificationManager = (NotificationManager) mContext
                            .getSystemService(Context.NOTIFICATION_SERVICE);

                    notification.flags |= Notification.FLAG_AUTO_CANCEL;

                    notificationManager.notify(sNotificationCount++, notification);
                }
            }
            if (!mCancelled)
                downloadFile(mContext, mIp);

            super.onPostExecute(result);
        }

        private void showWattingDialog() {
            if (mProgressDialog2 == null) {
                mProgressDialog2 = new ProgressDialog(mContext);
                mProgressDialog2.setCancelable(true);
            }
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View v = layoutInflater.inflate(R.layout.loading_dialog, null);
            TextView t = (TextView) v.findViewById(R.id.title_txt);

            t.setText(R.string.calloffdown_tip);
            mProgressDialog2.show();
            mProgressDialog2.setCancelable(false);
            mProgressDialog2.setContentView(v);
        }

        void showProgress(Context context) {
            mContext = context;
            if (mProgressDialog != null && mProgressDialog.isShowing()) {
                mProgressDialog.dismiss();
            }
            LayoutInflater layoutInflater = LayoutInflater.from(mContext);
            View v1 = layoutInflater.inflate(R.layout.loading_dialog, null);
            TextView cont_txt = (TextView) v1.findViewById(R.id.content_txt);
            cont_schedule = (TextView) v1.findViewById(R.id.loadingschedule);
            cont_schedule.setVisibility(View.VISIBLE);
            cont_txt.setText(R.string.downloading);
            ((TextView) v1.findViewById(R.id.title_txt)).setText(R.string.pleasewait);
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setTitle(R.string.downloading);
            mProgressDialog.setMessage(gpleasewait);
            mProgressDialog.setCancelable(false);
//            mProgressDialog.setMax(100) ;
//            mProgressDialog.setProgress(0) ;
//            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL) ;
            mProgressDialog.show();
            mProgressDialog.setContentView(v1);
            mProgressDialog.
                    setOnKeyListener(new DialogInterface.OnKeyListener() {
                        @Override
                        public boolean onKey(final DialogInterface dialog, int keyCode, KeyEvent event) {
                            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                                CustomDialog alertDialog = new CustomDialog.Builder(mContext)
                                        .setTitle(R.string.trip)
                                        .setMessage(R.string.cleanDownload)
                                        .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogs, int which) {
                                                cancelDownload();
                                                dialogs.dismiss();
                                                dialog.dismiss();
                                            }
                                        })
                                        .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogs, int which) {
                                                dialogs.dismiss();
                                            }
                                        }).create();
                                alertDialog.setCancelable(false);
                                alertDialog.show();
                            }
                            return false;
                        }
                    });
        }

        void hideProgress() {
            if (mProgressDialog != null) {
                mProgressDialog.dismiss();
                mProgressDialog = null;
            }
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isDownload = true;


        mIp = getArguments().getString(KEY_IP);

        if (mIp == null) {
            WifiManager wifiManager = (WifiManager) getActivity().getApplicationContext().getSystemService(Context.WIFI_SERVICE);

            DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();

            if (dhcpInfo != null && dhcpInfo.gateway != 0) {

                mIp = MainActivity.intToIp(dhcpInfo.gateway);
            }
        }
        gDownloadStr = getString(R.string.downloading);
        gpleasewait = getString(R.string.pleasewait);
        gcancel = getString(R.string.label_cancel);
        mPath = getArguments().getString(KEY_PATH);

        if (mPath == null) {

            mPath = DEFAULT_PATH;
        }

        mDirectory = getArguments().getString(KEY_DIRECTORY);

        if (mDirectory == null) {

            mDirectory = DEFAULT_DIR;
        }
    }

    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return availableBlocks * blockSize;
    }

    public static void notifyDataAdapter() {
        videoFrontAdapter.notifyDataSetChanged();
        photoFrontAdapter.notifyDataSetChanged();
        photoBackAdapter.notifyDataSetChanged();
        videoBackAdapter.notifyDataSetChanged();
        sosFrontAdapter.notifyDataSetChanged();
        sosBackAdapter.notifyDataSetChanged();
    }


    public void deleteFile() {
            switch (deleteflag) {
                case 0:
                    fileVideoBackList.clear();
                    stackVideoBackList.clear();
                    new BackViedoDownFileListTask().execute(new FileBrowser(threebackviedourl,
                            FileBrowser.COUNT_MAX));
                    break;
                case 1:
                    fileVideoFrontList.clear();
                    stackVideoFrontList.clear();
                    new VideoDownFileListTask().execute(new FileBrowser(threeviedourl,
                            FileBrowser.COUNT_MAX));
                    break;
                case 2:
                    fileSOSBackList.clear();
                    stackSOSBackList.clear();
                    new SOSBackViedoDownFileListTask().execute(new FileBrowser(threesosfrontvideo,
                            FileBrowser.COUNT_MAX));
                    break;
                case 3:
                    fileSOSFrontList.clear();
                    stackSOSFrontList.clear();
                    new SOSFrontViedoDownFileListTask().execute(new FileBrowser(threesosfrontvideo,
                            FileBrowser.COUNT_MAX));
                    break;
                case 4:
                    filePhotoBackList.clear();
                    stackPhotoBackList.clear();
                    new PhotoBackDownFileListTask().execute(new FileBrowser(threebackviedourl,
                            FileBrowser.COUNT_MAX));
                    break;
                case 5:
                    filePhotoFrontList.clear();
                    stackPhotoFrontList.clear();
                    new PhotoDownFileListTask().execute(new FileBrowser(threephotourl,
                            FileBrowser.COUNT_MAX));
                    break;
            }
//        if (!btn_frontvideo.isEnabled()) {
//            Log.i(TAG, "deleteFile: 删除后路视频");
//
//        } else if (!btn_backvideo.isEnabled()) {
//            Log.i(TAG, "deleteFile: 删除前路视频");
//
//        } else if (!btn_frontsos.isEnabled()) {
//            Log.i(TAG, "deleteFile: 删除后路SOS视频");
//
//        } else if (!btn_backsos.isEnabled()) {
//            Log.i(TAG, "deleteFile: 删除前路SOS视频");
//
//        } else if (!btn_backsos.isEnabled()) {
//            Log.i(TAG, "deleteFile: 删除后路照片");
//
//        } else if (!btn_frontphoto.isEnabled()) {
//            Log.i(TAG, "deleteFile: 删除前路照片");
//        }
    }

    /*视频储存到本地*/
    private static void downloadFile(final Context context, String ip) {
        isDownloadFile = true;
        if (sSelectedFiles.size() == 0) {
            isDownloadFile = false;
            isTounch = false;
            Log.i(TAG, "下载完成  sSelectedFiles.size()=" + sSelectedFiles.size());
            clsSelect();

            return;
        } else if (getAvailableInternalMemorySize() < sSelectedFiles.get(0).mSize) {
            CustomDialog alertDialog = new CustomDialog.Builder(context)
                    .setTitle(R.string.trip)
                    .setMessage(R.string.memorysizedeficiency)
                    .setCancelable(false)
                    .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int arg1) {
                            dialog.dismiss();
                        }
                    }).create();
            alertDialog.show();
            return;
        }
        Log.i(TAG, "sSelectedFiles.size()=" + sSelectedFiles.size());
        FileNode fileNode = sSelectedFiles.remove(0);
        fileNode.mSelected = false;
        notifyDataAdapter();
        final String filename = fileNode.mName.substring(fileNode.mName.lastIndexOf("/") + 1);
        final String urlString = "http://" + ip + fileNode.mName;

        File appDir = MainActivity.getAppDir();
        final File file = new File(appDir, filename);
        if (file.exists()) {
            CustomDialog alertDialog = new CustomDialog.Builder(context)
                    .setTitle(filename)
                    .setMessage(R.string.message_overwrite_file)
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            file.delete();
                            try {
                                sDownloadTask = new DownloadTask(context);
                                sDownloadTask.execute(new URL(urlString));
                            } catch (MalformedURLException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            dialog.dismiss();
                        }
                    })
                    .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create();
            alertDialog.setCancelable(false);
            alertDialog.show();
        } else {
            try {
                sDownloadTask = new DownloadTask(context);
                sDownloadTask.execute(new URL(urlString));
            } catch (MalformedURLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    private class CameraDeleteFile extends AsyncTask<URL, Integer, String> {
        @Override
        protected void onPreExecute() {
            setWaitingState(true);
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(URL... params) {
            FileNode fileNode = sSelectedFiles.get(0);
            ///added by eric
            URL url = CameraCommand.commandSetdeletesinglefileUrl(fileNode.mName);
//            if (fileNode.mName.contains("SOS")) {
//                mProgDlg.dismiss();
//                if (isdeletSOStoast)
            isdeletesos = true;
//                isdeletSOStoast = false;
//				mProgDlg.setMessage("Can not delete " + fileNode.mName);
//                return "next";
//            }
            if (url != null) {
                return CameraCommand.sendRequest(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            Activity activity = getActivity();
            FileNode fileNode = sSelectedFiles.remove(0);
            String time = fileNode.mTime;

            Log.d(TAG, "delete file response:" + result);

            if (result != null && result.equals("709\n?") != true) {
                if (result.equals("next")) {
//					mFileListJPGAdapter.notifyDataSetChanged() ;
                    mProgDlg.setMessage("Can not delete " + fileNode.mName);
                    fileNode.mSelected = false;
                } else {

                    if (fileNode.mName.indexOf("SOS") != -1) {
                        Log.i(TAG, "onPostExecute: 删除的是SOS文件");

                        for (int i = 0; i < fileSOSFrontList.size(); i++) {
                            if (i < fileSOSBackList.size()) {
                                if (time.equals(fileSOSFrontList.get(i).mTime)) {
                                    fileSOSBackList.remove(i);
                                }
                            }
                            if (time.equals(fileSOSFrontList.get(i).mTime)) {
                                fileSOSFrontList.remove(i);
                            }
                        }
                        fileSOSFrontList.remove(fileNode);
                        fileSOSBackList.remove(fileNode);
                        stackSOSFrontList.remove(fileNode);
                        stackSOSBackList.remove(fileNode);

                    } else if (fileNode.mName.indexOf("IMG") != -1) {
                        Log.i(TAG, "onPostExecute: 删除的是照片文件");
                        for (int i = 0; i < filePhotoFrontList.size(); i++) {
                            if (i < filePhotoBackList.size()) {
                                if (time.equals(filePhotoBackList.get(i).mTime)) {
                                    filePhotoBackList.remove(i);
                                }
                            }
                            if (time.equals(filePhotoFrontList.get(i).mTime)) {
                                filePhotoFrontList.remove(i);
                            }
                        }
                        filePhotoFrontList.remove(fileNode);
                        filePhotoBackList.remove(fileNode);
                        stackPhotoFrontList.remove(fileNode);
                        stackPhotoBackList.remove(fileNode);
                    } else {
                        for (int i = 0; i < fileVideoFrontList.size(); i++) {
                            if (i < fileVideoBackList.size()) {
                                if (time.equals(fileVideoBackList.get(i).mTime)) {
                                    Log.i(TAG, "onPostExecute: 后路删除成功");
                                    fileVideoBackList.remove(i);
                                }
                            }
                            if (time.equals(fileVideoFrontList.get(i).mTime)) {
                                Log.i(TAG, "onPostExecute: 前路删除成功");

                                fileVideoFrontList.remove(i);
                            }
                        }
                        Log.i(TAG, "onPostExecute: 删除的是普通视频文件");
                        fileVideoFrontList.remove(fileNode);
                        fileVideoBackList.remove(fileNode);
                        stackVideoFrontList.remove(fileNode);
                        stackVideoBackList.remove(fileNode);
                    }

                    mProgDlg.setMessage("Please wait, deleteing " + fileNode.mName);
                    mProgDlg.setProgress(mTotalFile - sSelectedFiles.size());

                }
                if (sSelectedFiles.size() > 0 && !mCancelDelete) {
//					if (!fileNode.mName.contains("SOS")){
                    new CameraDeleteFile().execute();
//					}
                } else {
                    deleteFile();
                    notifyDataAdapter();
                    if (mProgDlg != null) {
                        mProgDlg.dismiss();
                        mProgDlg = null;
                    }
                    //mFileListTitle.setText(mFileBrowser + " : " + mDirectory + " (" + sFileList.size()
                    //		+ " " + mItems + ")") ;
                    setWaitingState(false);
                }
            } else if (activity != null) {
                Toast.makeText(activity,
                        activity.getResources().getString(R.string.message_command_failed),
                        Toast.LENGTH_SHORT).show();
                setWaitingState(false);
            }
            super.onPostExecute(result);
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if (stoprecordalertDialog != null) {
            mHandler.sendEmptyMessage(MSG_DISMISSDIALOG);
            stoprecordalertDialog.dismiss();
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mHandler.removeMessages(MSG_GETFILELIST);
        sSelectedFiles.clear();
        super.onDestroy();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView");
        View view = inflater.inflate(R.layout.browser, container, false);
        videoFrontAdapter = new FileListVideoAdapter(inflater, fileVideoFrontList);
        videoBackAdapter = new FileListVideoAdapter(inflater, fileVideoBackList);
        sosFrontAdapter = new FileListVideoAdapter(inflater, fileSOSFrontList);
        sosBackAdapter = new FileListVideoAdapter(inflater, fileSOSBackList);
        photoFrontAdapter = new FileListJpgAdapter(inflater, filePhotoFrontList);
        photoBackAdapter = new FileListJpgAdapter(inflater, filePhotoBackList);

        mSaveButton = (LinearLayout) view.findViewById(R.id.browserDownloadButton);
        mDeleteButton = (LinearLayout) view.findViewById(R.id.browserDeleteButton);
        mOpenButton = (LinearLayout) view.findViewById(R.id.browserOpenButton);

        btn_frontvideo = (LinearLayout) view.findViewById(R.id.btn_frontvideo);
        btn_backvideo = (LinearLayout) view.findViewById(R.id.btn_backvideo);
        btn_frontsos = (LinearLayout) view.findViewById(R.id.btn_frontsosvideo);
        btn_backsos = (LinearLayout) view.findViewById(R.id.btn_backsosvideo);
        btn_frontphoto = (LinearLayout) view.findViewById(R.id.btn_frontphoto);
        btn_backphoto = (LinearLayout) view.findViewById(R.id.btn_backphoto);


        btn_photo = (LinearLayout) view.findViewById(R.id.btn_photo);
        btn_video = (LinearLayout) view.findViewById(R.id.btn_video);
        btn_sosvideo = (LinearLayout) view.findViewById(R.id.btn_sosfile);

        fbvideo_layout = (LinearLayout) view.findViewById(R.id.fb_layout);
        fbsos_layout = (LinearLayout) view.findViewById(R.id.fbsos_layout);
        fbphoto_layout = (LinearLayout) view.findViewById(R.id.fbphoto_layout);

        frontVideoListView = (RTPullListView) view.findViewById(R.id.browserList);
        backVideoListView = (RTPullListView) view.findViewById(R.id.browserList_backvideo);
        frontPhotoListView = (RTPullListView) view.findViewById(R.id.browserList_jpg);
        backPhotoListView = (RTPullListView) view.findViewById(R.id.browserListback_jpg);
        frontSOSListView = (RTPullListView) view.findViewById(R.id.browserList_sosfrontvideo);
        backSOSListView = (RTPullListView) view.findViewById(R.id.browserList_sosbackvideo);
        //mFileListAdapter.registerDataSetObserver(mDataSetobserver);
        mFileBrowser = getActivity().getResources().getString(R.string.label_file_browser);
        mReading = getActivity().getResources().getString(R.string.label_reading);
        mItems = getActivity().getResources().getString(R.string.label_items);


        activitytoals = getActivity();
        try {
            threeviedourl = new URL("http://" + mIp + mPath);
            threebackviedourl = new URL("http://" + mIp + mPath);
            threephotourl = new URL("http://" + mIp + mPath);
            threesosfrontvideo = new URL("http://" + mIp + mPath);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        /*判断当前版本信息*/
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        btn_backvideo.setVisibility(View.VISIBLE);
        fbvideo_layout.setVisibility(View.VISIBLE);
        btn_sosvideo.setVisibility(View.VISIBLE);

        if (!MainActivity.ShotStatus) {
//            btn_changemedia.setBackgroundResource(R.drawable.play_btn_simplewipe_no_bg);;
            fbsos_layout.setVisibility(View.GONE);
            fbvideo_layout.setVisibility(View.GONE);
            fbphoto_layout.setVisibility(View.GONE);
        }

        mSaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ///downloadFile(getActivity(), mIp) ;///modify by eric for show stop record dialog first
                mCurrentButton = BUTTON_DOWNLOAD;
                if (sSelectedFiles.size() < 0) {
                    return;
                }
                if (sSelectedFiles.size() > 0) {
                    FileNode filenode = sSelectedFiles.get(0);
                    if (getAvailableInternalMemorySize() < filenode.mSize) {
//					if (false){
                        CustomDialog alertDialog = new CustomDialog.Builder(getActivity())
                                .setTitle(R.string.trip)
                                .setMessage(R.string.memorysizedeficiency)
                                .setCancelable(false)
                                .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int arg1) {
                                        dialog.dismiss();
//										MainActivity.addFragment(FileBrowserFragment.this, new LocalFileBrowserFragment()) ;
                                    }
                                }).create();
                        alertDialog.show();
                    } else {
                        new GetRecordStatus().execute();
                    }
                } else {
                    toastUtil(getResources().getString(R.string.pleaseSelectFile));
                }

            }
        });

        mDeleteButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mTotalFile = sSelectedFiles.size();
//                if (!isdeleteSOSfileforthree) {
//                    toastUtil(getResources().getString(R.string.unabledeleteSOSFile));
//                    return;
//                }
                if (mTotalFile > 0) {
                    mProgDlg = new ProgressDialog(getActivity());
                    mProgDlg.setCancelable(false);
                    mProgDlg.setMax(mTotalFile);
                    mProgDlg.setProgress(0);
                    mProgDlg.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    mProgDlg.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.label_cancel),
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mCancelDelete = true;
                                }
                            });
                    mProgDlg.setTitle(getResources().getString(R.string.deletefileincamera));
                    mProgDlg.setMessage(getResources().getString(R.string.pleasewait));
                    mCancelDelete = false;
                    mProgDlg.show();
                    isdeletSOStoast = true;
                    new CameraDeleteFile().execute();
                    deleteOver = false;
                } else {
                    toastUtil(getResources().getString(R.string.pleaseSelectFile));
                }
            }
        });

        mOpenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentButton = BUTTON_OPEN;
                if (sSelectedFiles.size() == 1) {
                    new GetRecordStatus().execute();
                } else {
                    toastUtil(getString(R.string.erroroneopenfile));
//                    CustomDialog alertDialog = new CustomDialog.Builder(getActivity())
//                            .setTitle(getResources().getString(R.string.trip))
//                            .setMessage(getResources().getString(R.string.erroroneopenfile))
//                            .setCancelable(false)
//                            .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface arg0, int arg1) {
//                                    arg0.dismiss();
//                                    return;
//                                }
//                            }).create();
//                    alertDialog.show();
                }

            }
        });

        btn_video.setEnabled(false);
        btn_video.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.ShotStatus) {
                    fbvideo_layout.setVisibility(View.VISIBLE);
                    fbsos_layout.setVisibility(View.GONE);
                    fbphoto_layout.setVisibility(View.GONE);
                }

                btn_video.setEnabled(false);
                btn_sosvideo.setEnabled(true);
                btn_photo.setEnabled(true);
                btn_frontvideo.setEnabled(false);
                btn_backvideo.setEnabled(true);
                isdeleteSOSfileforthree = true;
                myHandler.sendEmptyMessage(MSG_CLEARSELECT);

                frontVideoListView.setVisibility(View.VISIBLE);
                backVideoListView.setVisibility(View.GONE);
                frontPhotoListView.setVisibility(View.GONE);
                backPhotoListView.setVisibility(View.GONE);
                frontSOSListView.setVisibility(View.GONE);
                backSOSListView.setVisibility(View.GONE);
                deleteflag = 0;

            }
        });
        btn_sosvideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.ShotStatus) {
                    fbvideo_layout.setVisibility(View.GONE);
                    fbsos_layout.setVisibility(View.VISIBLE);
                    fbphoto_layout.setVisibility(View.GONE);
                }
                frontVideoListView.setVisibility(View.GONE);
                backVideoListView.setVisibility(View.GONE);
                frontPhotoListView.setVisibility(View.GONE);
                backPhotoListView.setVisibility(View.GONE);
                frontSOSListView.setVisibility(View.VISIBLE);
                backSOSListView.setVisibility(View.GONE);
                myHandler.sendEmptyMessage(MSG_CLEARSELECT);
                btn_sosvideo.setEnabled(false);
                isdeleteSOSfileforthree = false;


                btn_frontsos.setEnabled(false);
                btn_backsos.setEnabled(true);
                btn_video.setEnabled(true);
                btn_photo.setEnabled(true);
                if (isfirstSOSfrontViedeo) {
                    mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
                    new SOSFrontViedoDownFileListTask().execute(new FileBrowser(threesosfrontvideo,
                            FileBrowser.COUNT_MAX));
                }
                frontSOSListView.setAdapter(sosFrontAdapter);
                sosFrontAdapter.notifyDataSetChanged();
                deleteflag = 2;

            }
        });
        btn_photo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.ShotStatus) {
                    fbvideo_layout.setVisibility(View.GONE);
                    fbsos_layout.setVisibility(View.GONE);
                    fbphoto_layout.setVisibility(View.VISIBLE);
                }
                frontVideoListView.setVisibility(View.GONE);
                backVideoListView.setVisibility(View.GONE);
                frontPhotoListView.setVisibility(View.VISIBLE);
                backPhotoListView.setVisibility(View.GONE);
                frontSOSListView.setVisibility(View.GONE);
                backSOSListView.setVisibility(View.GONE);

                myHandler.sendEmptyMessage(MSG_CLEARSELECT);
                isvideo = false;
                isdeleteSOSfileforthree = true;
                btn_photo.setEnabled(false);
                btn_sosvideo.setEnabled(true);
                btn_video.setEnabled(true);
                btn_frontphoto.setEnabled(false);
                btn_backphoto.setEnabled(true);
                deleteflag = 4;

                if (isfirstPhoto) {
                    isfirstPhoto = false;
                    isstatusPhoto = true;
                    mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
                    new PhotoDownFileListTask().execute(new FileBrowser(threephotourl,
                            FileBrowser.COUNT_MAX));
                }
                frontPhotoListView.setAdapter(photoFrontAdapter);
                photoFrontAdapter.notifyDataSetChanged();
            }
        });

        btn_frontvideo.setEnabled(false);
        btn_frontvideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                frontVideoListView.setVisibility(View.VISIBLE);
                backVideoListView.setVisibility(View.GONE);
                frontPhotoListView.setVisibility(View.GONE);
                backPhotoListView.setVisibility(View.GONE);
                frontSOSListView.setVisibility(View.GONE);
                backSOSListView.setVisibility(View.GONE);
                isvideo = true;
                btn_frontvideo.setEnabled(false);
                btn_backvideo.setEnabled(true);
                myHandler.sendEmptyMessage(MSG_CLEARSELECT);
                deleteflag = 0;

                //m_fileformat = FileNode.Format.all;
//                frontVideoListView.setAdapter(videoFrontAdapter);
                videoFrontAdapter.notifyDataSetChanged();
            }
        });
        btn_backvideo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                frontVideoListView.setVisibility(View.GONE);
                backVideoListView.setVisibility(View.VISIBLE);
                frontPhotoListView.setVisibility(View.GONE);
                backPhotoListView.setVisibility(View.GONE);
                frontSOSListView.setVisibility(View.GONE);
                backSOSListView.setVisibility(View.GONE);
                btn_frontvideo.setEnabled(true);
                btn_backvideo.setEnabled(false);

                deleteflag = 1;
                myHandler.sendEmptyMessage(MSG_CLEARSELECT);
                if (isfirstBackViedeo) {
                    mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
                    new BackViedoDownFileListTask().execute(new FileBrowser(threebackviedourl,
                            FileBrowser.COUNT_MAX));

                }
                backVideoListView.setAdapter(videoBackAdapter);
                videoBackAdapter.notifyDataSetChanged();
            }
        });
        btn_frontsos.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                frontVideoListView.setVisibility(View.GONE);
                backVideoListView.setVisibility(View.GONE);
                frontPhotoListView.setVisibility(View.GONE);
                backPhotoListView.setVisibility(View.GONE);
                frontSOSListView.setVisibility(View.VISIBLE);
                backSOSListView.setVisibility(View.GONE);

                btn_frontsos.setEnabled(false);
                btn_backsos.setEnabled(true);

                deleteflag = 2;
                myHandler.sendEmptyMessage(MSG_CLEARSELECT);
//                frontSOSListView.setAdapter(sosFrontAdapter);
                sosFrontAdapter.notifyDataSetChanged();
            }
        });
        btn_backsos.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                frontVideoListView.setVisibility(View.GONE);
                backVideoListView.setVisibility(View.GONE);
                frontPhotoListView.setVisibility(View.GONE);
                backPhotoListView.setVisibility(View.GONE);
                frontSOSListView.setVisibility(View.GONE);
                backSOSListView.setVisibility(View.VISIBLE);

                btn_frontsos.setEnabled(true);
                btn_backsos.setEnabled(false);

                deleteflag = 3;
                myHandler.sendEmptyMessage(MSG_CLEARSELECT);
                if (isfirstSOSbackViedeo) {
                    mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
                    new SOSBackViedoDownFileListTask().execute(new FileBrowser(threebackviedourl,
                            FileBrowser.COUNT_MAX));
                }
                backSOSListView.setAdapter(sosBackAdapter);
                sosBackAdapter.notifyDataSetChanged();
            }
        });
        btn_frontphoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                frontVideoListView.setVisibility(View.GONE);
                backVideoListView.setVisibility(View.GONE);
                frontPhotoListView.setVisibility(View.VISIBLE);
                backPhotoListView.setVisibility(View.GONE);
                frontSOSListView.setVisibility(View.GONE);
                backSOSListView.setVisibility(View.GONE);

                btn_frontphoto.setEnabled(false);
                btn_backphoto.setEnabled(true);

                myHandler.sendEmptyMessage(MSG_CLEARSELECT);
                deleteflag = 4;
                frontPhotoListView.setAdapter(photoFrontAdapter);
                photoFrontAdapter.notifyDataSetChanged();
            }
        });
        btn_backphoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                frontVideoListView.setVisibility(View.GONE);
                backVideoListView.setVisibility(View.GONE);
                frontPhotoListView.setVisibility(View.GONE);
                backPhotoListView.setVisibility(View.VISIBLE);
                frontSOSListView.setVisibility(View.GONE);
                backSOSListView.setVisibility(View.GONE);

                btn_frontphoto.setEnabled(true);
                btn_backphoto.setEnabled(false);

                deleteflag = 5;
                myHandler.sendEmptyMessage(MSG_CLEARSELECT);
                if (isfirstonebackPhoto) {
                    mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
                    new PhotoBackDownFileListTask().execute(new FileBrowser(threebackviedourl,
                            FileBrowser.COUNT_MAX));

                }
                backPhotoListView.setAdapter(photoBackAdapter);
                photoBackAdapter.notifyDataSetChanged();
            }
        });


        initFrontVideoListview();
        initBackVideoListview();
        initFrontJPGListView();
        initBackJPGListView();
        initFrontSOSListView();
        initBackSOSListView();

        frontVideoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        backVideoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        frontPhotoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        backPhotoListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        frontSOSListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        backSOSListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        if (isvideo) {
            frontVideoListView.setAdapter(videoFrontAdapter);
        } else {
            frontPhotoListView.setAdapter(photoFrontAdapter);
        }

        backVideoListView.setAdapter(videoBackAdapter);
        frontVideoListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewTag viewTag = (ViewTag) view.getTag();
//                view.setBackgroundResource(R.color.listview_item_bg);
                if (viewTag != null) {
                    FileNode file = viewTag.mFileNode;
                    CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.fileListCheckBox);
                    checkBox.setChecked(!checkBox.isChecked());
                    file.mSelected = checkBox.isChecked();
                    if (file.mSelected)
                        sSelectedFiles.add(file);
                    else
                        sSelectedFiles.remove(file);

                }
            }
        });
        frontPhotoListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewTag viewTag = (ViewTag) view.getTag();
                if (viewTag != null) {
                    FileNode file = viewTag.mFileNode;
                    CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.fileListCheckBox);
                    checkBox.setChecked(!checkBox.isChecked());
                    file.mSelected = checkBox.isChecked();
                    if (file.mSelected) {
                        sSelectedFiles.add(file);
                    } else {
                        sSelectedFiles.remove(file);
                    }

                }
            }
        });
        backPhotoListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewTag viewTag = (ViewTag) view.getTag();
                if (viewTag != null) {
                    FileNode file = viewTag.mFileNode;
                    CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.fileListCheckBox);
                    checkBox.setChecked(!checkBox.isChecked());
                    file.mSelected = checkBox.isChecked();
                    if (file.mSelected) {
                        sSelectedFiles.add(file);
                    } else {
                        sSelectedFiles.remove(file);
                    }

                }
            }
        });
        backVideoListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewTag viewTag = (ViewTag) view.getTag();
                if (viewTag != null) {
                    FileNode file = viewTag.mFileNode;
                    CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.fileListCheckBox);
                    checkBox.setChecked(!checkBox.isChecked());
                    file.mSelected = checkBox.isChecked();
                    if (file.mSelected)
                        sSelectedFiles.add(file);
                    else
                        sSelectedFiles.remove(file);

                }
            }
        });
        frontSOSListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewTag viewTag = (ViewTag) view.getTag();
                if (viewTag != null) {
                    FileNode file = viewTag.mFileNode;
                    CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.fileListCheckBox);
                    checkBox.setChecked(!checkBox.isChecked());
                    file.mSelected = checkBox.isChecked();
                    if (file.mSelected)
                        sSelectedFiles.add(file);
                    else
                        sSelectedFiles.remove(file);

                }
            }
        });
        backSOSListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ViewTag viewTag = (ViewTag) view.getTag();
                if (viewTag != null) {
                    FileNode file = viewTag.mFileNode;
                    CheckedTextView checkBox = (CheckedTextView) view.findViewById(R.id.fileListCheckBox);
                    checkBox.setChecked(!checkBox.isChecked());
                    file.mSelected = checkBox.isChecked();
                    if (file.mSelected)
                        sSelectedFiles.add(file);
                    else
                        sSelectedFiles.remove(file);

                }
            }
        });
        return view;
    }

    TextView backvideoNomre;

    private void initBackVideoListview() {
        Activity activity = getActivity();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.list_footview, null);

        backVideofooterView = (RelativeLayout) view.findViewById(R.id.list_footview);
        backvideoNomre = (TextView) view.findViewById(R.id.text_view);
        moreProgressBar = (ProgressBar) view.findViewById(R.id.footer_progress);
        backVideoListView.addFooterView(backVideofooterView);
        backVideofooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                moreProgressBar.setVisibility(View.VISIBLE);
                new BackViedoDownFileListTask().execute(new FileBrowser(threebackviedourl,
                        FileBrowser.COUNT_MAX));
                mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
            }
        });
    }

    TextView frontVideoNomore;

    public void initFrontVideoListview() {
        //添加listview底部获取更多按钮（可自定义）
        Activity activity = getActivity();
        LayoutInflater inflater = LayoutInflater.from(activity);
        View view = inflater.inflate(R.layout.list_footview, null);

        frontVideofooterView = (RelativeLayout) view.findViewById(R.id.list_footview);
        frontVideoNomore = (TextView) view.findViewById(R.id.text_view);
        moreProgressBar = (ProgressBar) view.findViewById(R.id.footer_progress);
        frontVideoListView.addFooterView(frontVideofooterView);
        //获取跟多监听器
        frontVideofooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isstatusVideo = true;
                moreProgressBar.setVisibility(View.VISIBLE);
                new VideoDownFileListTask().execute(new FileBrowser(threeviedourl,
                        FileBrowser.COUNT_MAX));

                mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
//				frontVideoListView.setSelection(0);
                //myHandler.sendEmptyMessage(LOAD_MORE_SUCCESS);
            }
        });
    }

    TextView frontphotoNomore;

    private void initFrontJPGListView() {

        LayoutInflater inflater;
        View vieww;
        //添加listview底部获取更多按钮（可自定义）
        Activity activity = getActivity();
        inflater = LayoutInflater.from(activity);
        vieww = inflater.inflate(R.layout.list_footview, null);
        frontPhotofooterView = (RelativeLayout) vieww.findViewById(R.id.list_footview);
        frontphotoNomore = (TextView) vieww.findViewById(R.id.text_view);
        moreProgressBar = (ProgressBar) vieww.findViewById(R.id.footer_progress);
        frontPhotoListView.addFooterView(frontPhotofooterView);
        //获取更多监听器
        frontPhotofooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new PhotoDownFileListTask().execute(new FileBrowser(threephotourl,
                        FileBrowser.COUNT_MAX));
                mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
            }
        });
    }

    TextView tv_backphotonomore;

    private void initBackJPGListView() {


        LayoutInflater inflater;
        View vieww;
        //添加listview底部获取更多按钮（可自定义）
        Activity activity = getActivity();
        inflater = LayoutInflater.from(activity);
        vieww = inflater.inflate(R.layout.list_footview, null);
        backPhotofooterView = (RelativeLayout) vieww.findViewById(R.id.list_footview);
        tv_backphotonomore = (TextView) vieww.findViewById(R.id.text_view);
        moreProgressBar = (ProgressBar) vieww.findViewById(R.id.footer_progress);
        backPhotoListView.addFooterView(backPhotofooterView);
        //获取更多监听器
        backPhotofooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new PhotoBackDownFileListTask().execute(new FileBrowser(threephotourl,
                        FileBrowser.COUNT_MAX));
                mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
            }
        });
    }

    TextView tv_sosfrontvideonomore;

    private void initFrontSOSListView() {

        LayoutInflater inflater;
        View vieww;
        //添加listview底部获取更多按钮（可自定义）
        Activity activity = getActivity();
        inflater = LayoutInflater.from(activity);
        vieww = inflater.inflate(R.layout.list_footview, null);
        frontSOSfooterView = (RelativeLayout) vieww.findViewById(R.id.list_footview);
        tv_sosfrontvideonomore = (TextView) vieww.findViewById(R.id.text_view);
        moreProgressBar = (ProgressBar) vieww.findViewById(R.id.footer_progress);
        frontSOSListView.addFooterView(frontSOSfooterView);
        //获取更多监听器
        frontSOSfooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new SOSFrontViedoDownFileListTask().execute(new FileBrowser(threesosfrontvideo,
                        FileBrowser.COUNT_MAX));
                mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
            }
        });
    }

    TextView tv_sosbackvideonomore;

    private void initBackSOSListView() {
        LayoutInflater inflater;
        View vieww;
        //添加listview底部获取更多按钮（可自定义）
        Activity activity = getActivity();
        inflater = LayoutInflater.from(activity);
        vieww = inflater.inflate(R.layout.list_footview, null);
        backSOSfooterView = (RelativeLayout) vieww.findViewById(R.id.list_footview);
        tv_sosbackvideonomore = (TextView) vieww.findViewById(R.id.text_view);
        moreProgressBar = (ProgressBar) vieww.findViewById(R.id.footer_progress);
        backSOSListView.addFooterView(backSOSfooterView);
        //获取更多监听器
        backSOSfooterView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new SOSBackViedoDownFileListTask().execute(new FileBrowser(threesosfrontvideo,
                        FileBrowser.COUNT_MAX));
                mHandler.sendEmptyMessage(MSG_SHOWDIALOG);
            }
        });
    }

    private static void clsSelect() {
        if (isDownloadFile)
            return;

        if (sSelectedFiles.size() > 0) {
            FileNode fileNode = sSelectedFiles.remove(0);
            fileNode.mSelected = false;
            notifyDataAdapter();
        }
        if (sSelectedFiles.size() > 0) {
            clsSelect();
        }
    }

    //点击加载更多listview结果处理
    private Handler myHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case LOAD_MORE_SUCCESS:
                    if (!rowsers.mIsError) {
                        frontVideoListView.setSelectionfoot();
                        moreProgressBar.setVisibility(View.GONE);
                    }
                    break;
                case LOAD_NEW_INFO:
                    videoFrontAdapter.notifyDataSetChanged();
//					frontVideoListView.setSelection(0);
                    frontVideoListView.onRefreshComplete();
                    break;
                case MSG_CLEARSELECT:
                    clsSelect();
                default:
                    break;
            }
        }

    };

    private boolean mWaitingState = false;
    private boolean mWaitingVisible = false;

    private void showWattingDialog() {
        Activity activity = getActivity();
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(activity);
            mProgressDialog.setCancelable(true);
        }
        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View v = layoutInflater.inflate(R.layout.loading_dialog, null);
        TextView t = (TextView) v.findViewById(R.id.title_txt);
        t.setText(R.string.sdfile_loading);
        mProgressDialog.show();
        mProgressDialog.setCancelable(false);
        mProgressDialog.setContentView(v);
    }

    private void dismissdialog() {
        if (mProgressDialog != null) {
            mProgressDialog.dismiss();
            mProgressDialog = null;
        }
    }

    private void setWaitingState(boolean waiting) {

        frontVideoListView.setClickable(!waiting);

        if (mWaitingState != waiting) {

            mWaitingState = waiting;
            setWaitingIndicator(mWaitingState, mWaitingVisible);
        }
    }

    private void setWaitingIndicator(boolean waiting, boolean visible) {

        if (!visible)
            return;

        Activity activity = getActivity();

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

    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_FRONTVIDEONOMORE:
                    frontVideoNomore.setText(getResources().getString(R.string.nomore));
                    frontVideofooterView.setEnabled(false);
                    break;
                case MSG_BACKVIDEONOMORE:
                    backvideoNomre.setText(getResources().getString(R.string.nomore));
                    backVideofooterView.setEnabled(false);
                    break;
                case MSG_FRONTPHOTONOMORE:
                    frontphotoNomore.setText(getResources().getString(R.string.nomore));
                    frontPhotofooterView.setEnabled(false);
                    break;
                case MSG_BACKPHOTONOMORE:
                    tv_backphotonomore.setText(getResources().getString(R.string.nomore));
                    backPhotofooterView.setEnabled(false);
                    break;
                case MSG_SOSFRONTVIDEONOMORE:
                    tv_sosfrontvideonomore.setText(getResources().getString(R.string.nomore));
                    frontSOSfooterView.setEnabled(false);
                    break;
                case MSG_SOSBACKVIDEONOMORE:
                    tv_sosbackvideonomore.setText(getResources().getString(R.string.nomore));
                    backSOSfooterView.setEnabled(false);
                    break;
                case MSG_SHOWDIALOG:
                    showWattingDialog();
                    break;
                case MSG_DISMISSDIALOG:
                    dismissdialog();
                    break;
                case MSG_GETFILELIST:
                    try {
                        Log.d(TAG, "MSG_GETFILELIST mTryTimes=" + mTryTimes);
                        mTryTimes--;
                        if (mTryTimes > 0) {
                            new DownloadFileListTask().execute(new FileBrowser(new URL("http://" + mIp + mPath),
                                    FileBrowser.COUNT_MAX));
                        } else {
                            Log.d(TAG, "MSG_GETFILELIST dismiss dialog");
                            dismissdialog();
                        }
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    break;
                case MSG_CONNECTERROREXIT:
                    final CustomDialog alertDialog = new CustomDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.connecterror))
                            .setMessage(R.string.verify_error)
                            .setCancelable(false)
                            .setPositiveButton(R.string.label_ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
// TODO Auto-generated method stub
                                    getFragmentManager().popBackStack();
                                    arg0.dismiss();
                                    return;
                                }
                            }).create();
                    alertDialog.show();
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    public void toastUtil(String msg) {
        if (mToast != null) {
            mToast.cancel();
        }
        mToast = Toast.makeText(getActivity(), msg, Toast.LENGTH_SHORT);
        mToast.show();
    }

    @Override
    public void onResume() {

        Log.d(TAG, "  onResume");
        restoreWaitingIndicator();
        version =  MainActivity.M_VERSION;
        if (sDownloadTask != null) {
            sDownloadTask.showProgress(getActivity());
        }
        if (isfirstOpen) {
            mTryTimes = G_TRYMAXTIMES;
            new VideoDownFileListTask().execute(new FileBrowser(threeviedourl,
                    FileBrowser.COUNT_MAX));
            Log.d(TAG, "  第三个版本请求");
            showWattingDialog();
            isfirstOpen = false;
        }
        MainActivity.setUpdateRecordStatusFlag(false);
        ///end

        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause");
        clearWaitingIndicator();
        if (sDownloadTask != null) {
            sDownloadTask.hideProgress();
        }
        ////begin added by eric for update record status
        if (BUTTON_OPEN == mCurrentButton || BUTTON_DOWNLOAD == mCurrentButton) {
            MainActivity.setUpdateRecordStatusFlag(true);
        }
        myHandler.sendEmptyMessage(MSG_CLEARSELECT);

        super.onPause();
    }

    @Override
    public void onStop() {
        Log.i(TAG, "sDownloadTask=" + sDownloadTask);
        Log.i(TAG, "isDownloadFile=" + isDownloadFile);
        Log.i(TAG, "onStop");
        super.onStop();
    }


}
