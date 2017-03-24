package jet.learning.android.examples;

import com.example.oglsamples.R;

import android.app.Activity;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageViewActivity extends Activity{
	
	ImageView imageView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.imageview_layout);
		
		imageView = (ImageView) findViewById(R.id.imageView1);
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(getResources(), R.drawable.data, options);
		int viewWidth = getWindowManager().getDefaultDisplay().getWidth();
		int viewHeight = viewWidth * options.outHeight /options.outWidth;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(viewWidth, viewHeight);
		imageView.setLayoutParams(params);
		imageView.setImageResource(R.drawable.data);
	}
}
