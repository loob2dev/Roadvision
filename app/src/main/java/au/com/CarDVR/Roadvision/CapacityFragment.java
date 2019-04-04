package au.com.CarDVR.Roadvision;


import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Fragment ;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.net.URL;



public class CapacityFragment extends Fragment {
    int newbadblock=0;//新增坏块
    int sdcardlifetime=0;//剩余寿命
    int sdcapacit;//SD总容量
    int surpluscapacity; //SD卡剩余容量
    TextView tv_surpluscapacity,tv_sdcapacity,tv_sdcardlifetime,tv_newbadblock,tv_surplus;
    ImageView iv_surpluscapacity,iv_sdcardlifetime,iv_newbadblock;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_capacity, container, false) ;
        tv_surpluscapacity= (TextView) view.findViewById(R.id.tv_surpluscapacity);
        tv_sdcapacity= (TextView) view.findViewById(R.id.tv_sdcapacity);
        tv_sdcardlifetime= (TextView) view.findViewById(R.id.tv_sdcardlifetime);
        tv_newbadblock= (TextView) view.findViewById(R.id.tv_newbadblock);
        tv_surplus= (TextView) view.findViewById(R.id.tv_surplus);
        iv_surpluscapacity= (ImageView) view.findViewById(R.id.iv_surpluscapacity);
        iv_sdcardlifetime= (ImageView) view.findViewById(R.id.iv_sdcardlifetime);
        iv_newbadblock= (ImageView) view.findViewById(R.id.iv_newbadblock);
        new GetSDcardLifeTimeRecorStatus().execute();
        return view;
    }
    private class GetSDcardLifeTimeRecorStatus extends AsyncTask<URL, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute() ;
        }
        @Override
        protected String doInBackground(URL... params) {
            URL url=CameraCommand.commandSDcardLifeTime();
            if (url!=null){
                return CameraCommand.sendRequest(url);
            }
            return null;
        }
        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        protected void onPostExecute(String result) {
            if (result!=null){
                String[] lines;
                String[] lines_temp =result.split("Camera.Menu.SDCardStatus=");
                String[] value;
                int sp,ss,sh;
                Log.i("CapacityFragment", result);
                try {


                if (null!=lines_temp && 1<lines_temp.length)
                {
                    lines = lines_temp[1].split(System.getProperty("line.separator")) ;
                    if (lines!=null&&!lines[0].equals(0)){
                        value=lines[0].split(",");
//                        newbadblock=Integer.parseInt(value[0]);
//                        sdcardlifetime=Integer.parseInt(value[1]);
                        if ("123456789".equals(value[1])){
                            return;
                        }
                        sdcapacit=Integer.parseInt(value[0]);

                        surpluscapacity=Integer.parseInt(value[1]);

                        tv_sdcapacity.setText(sdcapacit+"G");
                        tv_newbadblock.setText(newbadblock*10+"%");
                        if (surpluscapacity<1024){
                            tv_surpluscapacity.setText(surpluscapacity + "M");
                        }else {
                            tv_surpluscapacity.setText(""+surpluscapacity/1024+"G");
                        }
                        sp=100-(surpluscapacity*100)/(sdcapacit*1024);
                        tv_sdcardlifetime.setText(sp+"%");
                        ss=sdcardlifetime/10;
                        sh=newbadblock*10;
                        if (sp <= 10) {
                            iv_surpluscapacity.setBackground(getResources().getDrawable(R.drawable.y0));
                        } else if (sp <= 50) {
                            iv_surpluscapacity.setBackground(getResources().getDrawable(R.drawable.y25));
                        } else if (sp <= 75) {
                            iv_surpluscapacity.setBackground(getResources().getDrawable(R.drawable.y50));
                        } else if (sp <= 85) {
                            iv_surpluscapacity.setBackground(getResources().getDrawable(R.drawable.y75));
                        } else {
                            iv_surpluscapacity.setBackground(getResources().getDrawable(R.drawable.y100));
                        }
                        tv_surplus.setText(sp+"%");
                        tv_newbadblock.setText(sh+"%");

                        if (ss<=25){
                            iv_sdcardlifetime.setBackground(getResources().getDrawable(R.drawable.x100));
                        }else if (ss<=50){
                            iv_sdcardlifetime.setBackground(getResources().getDrawable(R.drawable.x75));
                        }else if (ss<=75){
                            iv_sdcardlifetime.setBackground(getResources().getDrawable(R.drawable.x50));
                        }else if (ss<=95){
                            iv_sdcardlifetime.setBackground(getResources().getDrawable(R.drawable.x0));
                            CustomDialog alertDialog = new CustomDialog.Builder(getActivity())
                                    .setTitle(getResources().getString(R.string.trip))
                                    .setMessage(R.string.errorsd)
                                    .setPositiveButton(R.string.label_ok,new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface arg0, int arg1) {
                                            // TODO Auto-generated method stub
                                            arg0.dismiss();
                                        }
                                    }).create();
                            alertDialog.show() ;
                        }
                        else if (ss>=100){

                            tv_sdcardlifetime.setText(0+"%");
                            iv_sdcardlifetime.setBackground(getResources().getDrawable(R.drawable.x0));
                        }
                        if (ss==0){
                            tv_sdcardlifetime.setText(0+"%");
                            iv_sdcardlifetime.setBackground(getResources().getDrawable(R.drawable.x0));
                        }else {
                            tv_sdcardlifetime.setText(100-ss+"%");
                        }

                        if (sh<=25){
                            iv_newbadblock.setBackground(getResources().getDrawable(R.drawable.x0));
                        }else if (sh<=50){
                            iv_newbadblock.setBackground(getResources().getDrawable(R.drawable.x25));
                        }else if (sh<=75){
                            iv_newbadblock.setBackground(getResources().getDrawable(R.drawable.x50));
                        }else if(sh<95){
                            iv_newbadblock.setBackground(getResources().getDrawable(R.drawable.x75));
                        }else {
                            iv_newbadblock.setBackground(getResources().getDrawable(R.drawable.x100));
                        }
                        Log.i("moop", "shijian=" + sdcardlifetime);
                    }
                }
                }catch (Exception e){

                }
            }
            super.onPostExecute(result) ;
        }
    }
}
