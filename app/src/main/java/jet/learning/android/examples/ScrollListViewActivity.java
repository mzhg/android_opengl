package jet.learning.android.examples;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.example.oglsamples.R;

public class ScrollListViewActivity extends Activity{

	Button   button;
	ScrollView listview;
	int cursor = 0;
	int viewHeight = -1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_listview);
		
		button = (Button) findViewById(R.id.button);
		listview = (ScrollView) findViewById(R.id.listview);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
		adapter.add("ÌÆÉ®");
		adapter.add("ËïÎò¿Õ");
		adapter.add("Öí°Ë½ä");
		adapter.add("É³É®");
		adapter.add("°×ÁúÂí");
		adapter.add("ÌÆÌ«×Ú");
		adapter.add("ÌÆ¸ß×Ü");
		adapter.add("¹ÛÊÀÒôÆĞÈø");
		adapter.add("Óñ»Ê´óµÛ");
		adapter.add("ÈçÀ´·ğ×æ");
		adapter.add("Ì«ÉÏÀÏ¾ı");
		adapter.add("ÍĞËşÀîÌìÍõ");
		adapter.add("ÄÄß¸");
		adapter.add("Ì«°×½ğĞÇ");
		adapter.add("ÌìÅîÔªË§");
		adapter.add("ºìº¢¶ù");
		adapter.add("ÆĞÌáÀÏ×æ");
		adapter.add("ÇØÇí");
		adapter.add("Î¾³Ù¾´µÂ");
		adapter.add("ÌìÅîÔªË§");
		
		final LinearLayout container = (LinearLayout) findViewById(R.id.container);
		final int count = adapter.getCount();
		for(int i = 0; i < count; i++){
			container.addView(adapter.getView(i, null, container));
		}
		
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				int height = container.getMeasuredHeight()/count;
				
				cursor++;
//				listview.smoothScrollToPosition(cursor);
				if(cursor < count){
//					listview.scrollBy(0, 50);
//					listview.smoothScrollToPosition(cursor);
					listview.smoothScrollTo(0, height * cursor);
				}else{
					cursor = 0;
					listview.scrollTo(0, 0);
				}
			}
		});
		
		
	}
}
