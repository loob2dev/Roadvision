package au.com.CarDVR.Roadvision;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImagebrowseActivity extends Activity {
    String url;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_imagebrowse);
        imageView = (ImageView) findViewById(R.id.sdPhoto);
        Intent intent = getIntent();
        url= intent.getStringExtra("imageUrl");
        Glide.with(this).load(url).asBitmap().into(imageView);
    }
}
