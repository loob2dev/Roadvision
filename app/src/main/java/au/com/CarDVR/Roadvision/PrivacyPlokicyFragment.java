package au.com.CarDVR.Roadvision;

import android.app.Fragment;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;


public class PrivacyPlokicyFragment extends Fragment  {
    ImageView image_help;
    private static final String TAG = "HelpFramgment";

    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();

    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;

    // 第一个按下的手指的点
    private PointF startPoint = new PointF();
    // 两个按下的手指的触摸点的中点
    private PointF midPoint = new PointF();
    // 初始的两个手指按下的触摸点的距离
    private float oriDis = 1f;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.privacy_fragment, container, false);
//        Glide.with(this).load(R.mipmap.help_view_bg).asBitmap().into(image_help);
//        image_help.setOnTouchListener(this);
        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}