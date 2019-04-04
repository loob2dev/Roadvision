package au.com.CarDVR.Roadvision;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.TextView;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;

import au.com.CarDVR.Roadvision.Control.NetworkConfigurationsFragment;
import au.com.CarDVR.Roadvision.FileBrowser.Model.SettingModel;
import au.com.CarDVR.Roadvision.Viewer.Rec_RadarSetting_Adapter;

public class SettingFragment extends Fragment {
    ArrayList<SettingModel> settingModelArrayList1 = new ArrayList<SettingModel>();
    RecyclerView rec_setting;
    Rec_RadarSetting_Adapter recAdapter;
    ArrayList<String> arrayList;
    CustomDialog versionDialog;
    int flag = 0;
    private ProgressDialog mProgressDialog;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//		new GetParkCarValues().execute();
        settingModelArrayList1 = MainActivity.settingModelArrayList;
        new GetCameraSettingStatus().execute();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.setting_fragment, container, false);
        rec_setting = (RecyclerView) view.findViewById(R.id.reclist_radar);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recAdapter = new Rec_RadarSetting_Adapter(getActivity(), settingModelArrayList1);
        rec_setting.setLayoutManager(layoutManager);
        rec_setting.setAdapter(recAdapter);
        mProgressDialog = new ProgressDialog(getActivity());
//        mProgressDialog.setCancelable(false);
        recAdapter.setOnItemClickLitener(new Rec_RadarSetting_Adapter.OnItemClickLitener() {
            @Override
            public void onItemClick(View view, int position) {
                if (!"yes".equals(settingModelArrayList1.get(position).getmSwitch()) &&
                        !getString(R.string.FW_Version).equals(settingModelArrayList1.get(position).getTitle()) &&
                        !getString(R.string.label_formatsdcard).equals(settingModelArrayList1.get(position).getTitle()) &&
                        !getString(R.string.label_factoryreset).equals(settingModelArrayList1.get(position).getTitle()) &&
                        !getString(R.string.label_network_configurations).equals(settingModelArrayList1.get(position).getTitle())) {
                    Log.i("moop", "其他");
                    arrayList = new ArrayList<String>();
                    for (int i = 0; i < settingModelArrayList1.get(position).getTitles().size(); i++) {
                        arrayList.add(settingModelArrayList1.get(position).getTitles().get(i).getArg());
                    }
                    actionSheetDialogNoTitle(position, arrayList);
                }
                if (getString(R.string.FW_Version).equals(settingModelArrayList1.get(position).getTitle())) {
                    if (flag == 5) {
                        File file = new File(MainActivity.getAppDir(), MainActivity.SettingXmlName);
                        if (file.exists()) {
                            file.delete();
                            Log.i("moop", "点击五次删除配置项");
                        }
                    }
                    flag++;
                } else {
                    flag = 0;
                }
                if (getString(R.string.label_formatsdcard).equals(settingModelArrayList1.get(position).getTitle())) {
                    Log.i("moop", "格式化SD卡");
                    versionDialog = new CustomDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.trip))
                            .setMessage(getResources().getString(R.string.format_attaction))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int arg1) {
                                    dialog.dismiss();
//                                    mProgressDialog.show();
                                    showProgress(getString(R.string.label_formatsdcard));
                                    URL url = CameraCommand.commandformatsdcardSettingsUrl();
                                    if (url == null)
                                        return;
                                    new CameraCommand.SendRequest().execute(url);

                                    new CountDownTimer(10 * 1000, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                        }
                                        @Override
                                        public void onFinish() {
                                            mProgressDialog.dismiss();
                                            MainActivity.backToPreFragment();
                                        }
                                    }.start();
                                    return;
                                }
                            }).setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
                    versionDialog.setCancelable(false);
                    if (!versionDialog.isShowing())
                        versionDialog.show();
                } else if (getString(R.string.label_factoryreset).equals(settingModelArrayList1.get(position).getTitle())) {
                    Log.i("moop", "恢复出厂设置");
                    versionDialog = new CustomDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.trip))
                            .setMessage(getResources().getString(R.string.label_factoryresettrip))
                            .setCancelable(false)
                            .setPositiveButton(getResources().getString(R.string.label_ok), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int arg1) {
                                    dialog.dismiss();
//                                    mProgressDialog.show();
                                    showProgress(getString(R.string.label_factoryreset));
                                    URL url = CameraCommand.commandfactorySettingsUrl();
                                    if (url == null)
                                        return;
                                    new CameraCommand.SendRequest().execute(url);

                                    new CountDownTimer(10 * 1000, 1000) {
                                        @Override
                                        public void onTick(long millisUntilFinished) {
                                        }
                                        @Override
                                        public void onFinish() {
                                            mProgressDialog.dismiss();
                                            MainActivity.backToPreFragment();
                                        }
                                    }.start();
                                    return;
                                }
                            }).setNegativeButton(R.string.label_cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            }).create();
                    versionDialog.setCancelable(false);
                    if (!versionDialog.isShowing())
                        versionDialog.show();
                } else if (getString(R.string.label_network_configurations).equals(settingModelArrayList1.get(position).getTitle())) {
                    MainActivity.addFragment(SettingFragment.this, new NetworkConfigurationsFragment());
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {

            }

            @Override
            public void onClickBox(CheckBox checkBox, SettingModel settingModel, int position) {
                if (checkBox.isChecked()) {
                    settingSendRequest(settingModelArrayList1.get(position).getmSet(), "0");
                    settingModelArrayList1.get(position).setmDefault("0");
                } else {
                    settingSendRequest(settingModelArrayList1.get(position).getmSet(), "1");
                    settingModelArrayList1.get(position).setmDefault("1");
                }

            }
        });
        return view;
    }

    private void actionSheetDialogNoTitle(final int index, ArrayList<String> stringItems) {
        final ActionSheetDialog dialog = new ActionSheetDialog(getActivity(), stringItems, null);
        dialog.isTitleShow(false).show();
        dialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, View view, int position, long id) {
                dialog.dismiss();
                settingModelArrayList1.get(index).setmDefault(settingModelArrayList1.get(index).getTitles().get(position).getArg());
                settingSendRequest(settingModelArrayList1.get(index).getmSet(), settingModelArrayList1.get(index).getTitles().get(position).getId());
                recAdapter.notifyDataSetChanged();
            }
        });
    }

    private class CamerRowSettingsSendRequest extends CameraCommand.SendRequest {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
        }
    }

    /**
     * 发送指令集
     */
    public void settingSendRequest(String name, String value) {
        URL url = CameraCommand.commandSendSet(name, value);
        if (url != null) {
            new CamerRowSettingsSendRequest().execute(url);
        }
    }

    public void setDefault(String[] args) {

        for (int i = 0; i < settingModelArrayList1.size(); i++) {
            if (settingModelArrayList1.get(i) == null) {
                settingModelArrayList1.remove(i);
                i--;
            }
            for (int j = 0; j < args.length; j++) {
                String[] items;
                items = args[j].split("=");
                if (items[0].equals(settingModelArrayList1.get(i).getmGet())) {
                    Log.i("miop", "setDefault: " + items[0] + "=" + settingModelArrayList1.get(i).getmGet());
                    if ("Camera.Menu.FWversion".equals(settingModelArrayList1.get(i).getmGet()))
                        try {
                            settingModelArrayList1.get(i).setmDefault(items[1] + "/" + getActivity().getPackageManager().getPackageInfo(
                                    getActivity().getPackageName(), 0).versionName);
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                        }
                    if (settingModelArrayList1.get(i).getTitles() != null)
                        for (int x = 0; x < settingModelArrayList1.get(i).getTitles().size(); x++) {
                            if (items[1].equals(settingModelArrayList1.get(i).getTitles().get(x).getId()))
                                if ("yes".equals(settingModelArrayList1.get(i).getmSwitch()))
                                    settingModelArrayList1.get(i).setmDefault(settingModelArrayList1.get(i).getTitles().get(x).getId());
                                else
                                    settingModelArrayList1.get(i).setmDefault(settingModelArrayList1.get(i).getTitles().get(x).getArg());
                        }
                }
            }
        }
    }


    private class GetCameraSettingStatus extends AsyncTask<URL, Integer, String> {
        @Override
        protected String doInBackground(URL... params) {
            URL url = CameraCommand.commandGetMenuSettingsValuesUrl();
            if (url != null) {
                return CameraCommand.sendRequest(url);
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                String[] lines_temp = result.split(System.getProperty("line.separator"));//录音状态
                setDefault(lines_temp);
            }
            recAdapter.notifyDataSetChanged();
        }
    }

    void showProgress(String tip) {
        TextView cont_schedule;
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View v1 = layoutInflater.inflate(R.layout.loading_dialog, null);
        TextView cont_txt = (TextView) v1.findViewById(R.id.content_txt);
        cont_schedule = (TextView) v1.findViewById(R.id.loadingschedule);
        cont_schedule.setVisibility(View.GONE);
        cont_txt.setText(R.string.pleasewait);
        ((TextView) v1.findViewById(R.id.title_txt)).setText(tip);
        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setTitle(R.string.trip);
        mProgressDialog.setMessage(getString(R.string.pleasewait));
        mProgressDialog.setCancelable(false);
//            mProgressDialog.setProgress(0) ;
        mProgressDialog.show();
        mProgressDialog.setContentView(v1);
    }
}
