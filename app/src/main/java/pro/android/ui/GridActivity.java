package pro.android.ui;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.oglsamples.R;
public class GridActivity extends Activity {
	TextView earthText1;
	TextView earthText2;
	TextView earthText3;	
	TextView earthText4;	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_grid);
		final GridLayout myGridLayout = (GridLayout)findViewById(R.id.gridLayout);		
		earthText1 = (TextView)findViewById(R.id.tv1_earth);
		earthText2 = (TextView)findViewById(R.id.tv2_earth);		
		earthText3 = (TextView)findViewById(R.id.tv3_earth);
		earthText4 = (TextView)findViewById(R.id.tv4_earth);		
		final ImageButton earthButton = (ImageButton)findViewById(R.id.ib_earth);
		final ImageButton venusButton = (ImageButton)findViewById(R.id.ib_venus);
		final ImageButton jupiterButton = (ImageButton)findViewById(R.id.ib_jupiter);
		final ImageButton neptuneButton = (ImageButton)findViewById(R.id.ib_neptune);
		earthButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				myGridLayout.setBackgroundResource(R.drawable.stars480);
				earthButton.setBackgroundColor(Color.argb(128, 255, 255, 255));
				venusButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				jupiterButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				neptuneButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				earthText1.setText(R.string.planet_name_earth);
				earthText2.setText(R.string.planet_mass_earth);
				earthText3.setText(R.string.planet_grav_earth);
				earthText4.setText(R.string.planet_size_earth);
			}});
		venusButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				myGridLayout.setBackgroundResource(R.drawable.plasma480);				
				earthButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				venusButton.setBackgroundColor(Color.argb(128, 255, 255, 255));
				jupiterButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				neptuneButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				earthText1.setText(R.string.planet_name_venus);
				earthText2.setText(R.string.planet_mass_venus);
				earthText3.setText(R.string.planet_grav_venus);
				earthText4.setText(R.string.planet_size_venus);
			}});
		jupiterButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				myGridLayout.setBackgroundResource(R.drawable.plasma720);				
				earthButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				venusButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				jupiterButton.setBackgroundColor(Color.argb(128, 255, 255, 255));
				neptuneButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				earthText1.setText(R.string.planet_name_jupiter);
				earthText2.setText(R.string.planet_mass_jupiter);
				earthText3.setText(R.string.planet_grav_jupiter);
				earthText4.setText(R.string.planet_size_jupiter);
			}});
		neptuneButton.setOnClickListener(new View.OnClickListener(){
			@Override
			public void onClick(View arg0) {
				myGridLayout.setBackgroundResource(R.drawable.plasma1080);				
				earthButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				venusButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				jupiterButton.setBackgroundColor(Color.argb(0, 255, 255, 255));
				neptuneButton.setBackgroundColor(Color.argb(128, 255, 255, 255));
				earthText1.setText(R.string.planet_name_neptune);
				earthText2.setText(R.string.planet_mass_neptune);
				earthText3.setText(R.string.planet_grav_neptune);
				earthText4.setText(R.string.planet_size_neptune);
			}});
	}
}