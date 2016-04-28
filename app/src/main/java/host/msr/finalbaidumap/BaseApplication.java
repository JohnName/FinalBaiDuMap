package host.msr.finalbaidumap;

import android.app.Application;
import android.content.Context;

import com.baidu.mapapi.SDKInitializer;

/**
 * Created by Ymmmsick on 2016/4/27.
 */
public class BaseApplication extends Application {

    Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        SDKInitializer.initialize(context);
    }
}
