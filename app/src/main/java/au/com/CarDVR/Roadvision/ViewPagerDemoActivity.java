package au.com.CarDVR.Roadvision;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ViewPagerDemoActivity extends Activity  {

    private ViewPager vp;
    private ViewPagerAdapter vpAdapter;
    private List<View> views;
    private ImageButton m_imgBtn;
    private SharedPreferences preferences;
    public static ViewPagerDemoActivity instance = null;

    private int currentIndex;
    /** Called when the activity is first created. */
    @SuppressWarnings("deprecation")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_disn);
            final Intent intent = new Intent(ViewPagerDemoActivity.this, MainActivity.class);
            Timer timer = new Timer();
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    startActivity(intent);
                    finish();
                }
            };
            timer.schedule(task, 1000 * 1);
            return;
    }
}
