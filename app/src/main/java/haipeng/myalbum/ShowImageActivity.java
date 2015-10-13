package haipeng.myalbum;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import java.lang.ref.SoftReference;

public class ShowImageActivity extends AppCompatActivity {

    private ImageView mImageView;
    private Intent intent;
    private String imagePath;
    private Bitmap mBitmap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        mImageView = (ImageView) findViewById(R.id.showImage);
        intent = getIntent();
        imagePath = intent.getStringExtra("imagePath");
        mBitmap = BitmapFactory.decodeFile(imagePath);
        mImageView.setImageBitmap(mBitmap);

    }

    @Override
    protected void onStop() {
        Log.i("tag","ShowImageActivity life = onStop");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if(mBitmap!=null) {
            mBitmap.recycle();
            mBitmap = null;
        }

        System.gc();
        Log.i("tag","ShowImageActivity life = onDestroy");
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_show_image, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
