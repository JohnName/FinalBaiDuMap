package host.msr.finalbaidumap;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SlidingDrawer;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Ymmmsick on 2016/5/20.
 */
public class SlidActivity extends Activity {
    private WrapSlidingDrawer mDrawer;
    private ImageButton imbg;
    private Boolean flag = false;
    private TextView tv;
    private ListView listView;
    ArrayList<String> arrayList;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slidmap);
        arrayList = new ArrayList<>();
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
        arrayList.add("aaaaaaaaaaaaaaaaaaaa");
//        arrayList = new ArrayList<>();
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
//        arrayList.add("1111111111111111");
        listView = (ListView) findViewById(R.id.listview_);
        listView.setAdapter(new NewAdapter(getApplicationContext(), arrayList));
        imbg = (ImageButton) findViewById(R.id.handle);
        mDrawer = (WrapSlidingDrawer) findViewById(R.id.slidingdrawer);
//        tv = (TextView) findViewById(R.id.tv);

        mDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                flag = true;
                imbg.setImageResource(R.mipmap.ic_launcher);
            }

        });
        mDrawer.open();
        mDrawer.setOnDrawerOpenListener(new SlidingDrawer.OnDrawerOpenListener() {
            @Override
            public void onDrawerOpened() {
                SlidingDrawer.LayoutParams linearParams = (WrapSlidingDrawer.LayoutParams) mDrawer.getLayoutParams();
                linearParams.height = 350;
                mDrawer.setLayoutParams(linearParams);

            }
        });
        mDrawer.setOnDrawerCloseListener(new SlidingDrawer.OnDrawerCloseListener() {

            @Override
            public void onDrawerClosed() {
                flag = false;
                imbg.setImageResource(R.mipmap.ic_launcher);
            }

        });


        mDrawer.setOnDrawerScrollListener(new SlidingDrawer.OnDrawerScrollListener() {

            @Override
            public void onScrollEnded() {
//                tv.setText("结束拖动");
//                mDrawer.setY(100);
            }

            @Override
            public void onScrollStarted() {
//                tv.setText("开始拖动");
            }

        });


    }
}
