package au.com.CarDVR.Roadvision.Viewer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;


import java.util.ArrayList;

import au.com.CarDVR.Roadvision.FileBrowser.Model.SettingModel;
import au.com.CarDVR.Roadvision.R;


/**
 * Created by bzmoop on 2016/6/15 0015.
 */
public class Rec_RadarSetting_Adapter extends RecyclerView.Adapter<Rec_RadarSetting_Adapter.MyViewHolder> {

    //    Map<ArrayList<String> ,ArrayList<String>> item_map= new HashMap<ArrayList<String>, ArrayList<String>>();
    ArrayList<SettingModel> settingModels = new ArrayList<SettingModel>();


    private Context mContext;
    private LayoutInflater inflater;

    private OnItemClickLitener mOnItemClickLitener;

    public interface OnItemClickLitener {
        void onItemClick(View view, int position);

        void onItemLongClick(View view, int position);

        void onClickBox(CheckBox checkBox, SettingModel settingModel, int position);
    }

    public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener) {
        this.mOnItemClickLitener = mOnItemClickLitener;
    }


    //    public Rec_RadarSetting_Adapter(Context context,  Map<ArrayList<String> ,ArrayList<String>> datas){
    public Rec_RadarSetting_Adapter(Context context, ArrayList<SettingModel> settingModels) {

        this.mContext = context;
        this.settingModels = settingModels;
        inflater = LayoutInflater.from(mContext);
    }

    @Override
    public int getItemCount() {
        return settingModels.size();
    }

    @Override
    public void onBindViewHolder(final MyViewHolder myViewHolder, final int position) {
        if (settingModels.get(position) != null) {
            if ("yes".equals(settingModels.get(position).getmSwitch())) {
                myViewHolder.item_checkbox.setVisibility(View.VISIBLE);
                myViewHolder.tv_arg.setVisibility(View.GONE);
                myViewHolder.item_checkbox.setChecked( "0".equals( settingModels.get(position).getmDefault()));
            } else {
                myViewHolder.tv_arg.setText(settingModels.get(position).getmDefault());
                myViewHolder.item_checkbox.setVisibility(View.GONE);
                myViewHolder.tv_arg.setVisibility(View.VISIBLE);
            }
            myViewHolder.tv_name.setText(settingModels.get(position).getTitle());

            // 如果设置了回调，则设置点击事件

            if (mOnItemClickLitener != null) {
                myViewHolder.root_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickLitener.onItemClick(myViewHolder.itemView, position);
                    }
                });
                myViewHolder.root_layout.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mOnItemClickLitener.onItemLongClick(myViewHolder.itemView, position);
                        return true;
                    }

                });
                myViewHolder.item_checkbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mOnItemClickLitener.onClickBox(myViewHolder.item_checkbox, settingModels.get(position), position);
                    }
                });
            }
        }
    }


    //重写onCreateViewHolder方法，返回一个自定义的ViewHolder
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = inflater.inflate(R.layout.rec_radarsetting_item, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView tv_name, tv_arg;
        CheckBox item_checkbox;
        LinearLayout root_layout;

        public MyViewHolder(View view) {
            super(view);
            tv_name = (TextView) view.findViewById(R.id.tv_itemname);
            tv_arg = (TextView) view.findViewById(R.id.tv_itemarg);
            item_checkbox = (CheckBox) view.findViewById(R.id.item_checkbox);
            root_layout = (LinearLayout) view.findViewById(R.id.root_layout);
        }

    }

}
