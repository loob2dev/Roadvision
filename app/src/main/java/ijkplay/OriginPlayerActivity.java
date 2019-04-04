package ijkplay;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import au.com.CarDVR.Roadvision.R;
import ijkplay.listener.OnShowThumbnailListener;
import ijkplay.utils.MediaUtils;
import ijkplay.widget.PlayStateParams;
import ijkplay.widget.PlayerView;


/**
 * ========================================
 * <p/>
 * 版 权：深圳市晶网科技控股有限公司 版权所有 （C） 2015
 * <p/>
 * 作 者：陈冠明
 * <p/>
 * 个人网站：http://www.dou361.com
 * <p/>
 * 版 本：1.0
 * <p/>
 * 创建日期：2015/11/18 9:40
 * <p/>
 * 描 述：半屏界面
 * <p/>
 * <p/>
 * 修订历史：
 * <p/>
 * ========================================
 */
public class OriginPlayerActivity extends Activity {

    private PlayerView player;
    private Context mContext;
    private PowerManager.WakeLock wakeLock;
    LinearLayout root_layout;
    String url;
    boolean hideTopButtonCrolPanl=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = this;
        setContentView(R.layout.activity_h);
        root_layout = (LinearLayout) findViewById(R.id.root_layout);

        url = getIntent().getStringExtra("URL");
        /**常亮*/
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "liveTAG");
        wakeLock.acquire();
//         url = "http://120.77.13.85:8888/getfile?location=zxkc/test1.mp4";
        player = new PlayerView(this)
                .setTitle("")
                .setScaleType(PlayStateParams.fitparent)
                .hideMenu(true)
                .setOnlyFullScreen(true)
                .setForbidHideControlPanl(true)
                .hideFullscreen(true)
                .hideRotation(true)
                .hideSteam(true)
                .showThumbnail(new OnShowThumbnailListener() {
                    @Override
                    public void onShowThumbnail(ImageView ivThumbnail) {
//                        Glide.with(mContext)
//                                .load("http://pic2.nipic.com/20090413/406638_125424003_2.jpg")
//                                .placeholder(R.color.cl_default)
//                                .error(R.color.cl_error)
//                                .into(ivThumbnail);
                    }
                })
                .setPlaySource(url)
                .startPlay();
        player.hideCenterPlayer(true);
        root_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hideTopButtonCrolPanl) {
                    hideTopButtonCrolPanl = false;

                }else {
                    hideTopButtonCrolPanl = true;
                }
                player.hideControlPanl(hideTopButtonCrolPanl);

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onPause();
        }
        MediaUtils.muteAudioFocus(mContext, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.onResume();
        }
        MediaUtils.muteAudioFocus(mContext, false);
        if (wakeLock != null) {
            wakeLock.acquire();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.onDestroy();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (player != null) {
            player.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public void onBackPressed() {
        if (player != null && player.onBackPressed()) {
            return;
        }
        super.onBackPressed();
        if (wakeLock != null) {
            wakeLock.release();
        }
    }

}
