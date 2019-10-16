package com.example.karthikm.merlinwmscipher_vip_rdc.application;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import java.util.Map;


public class AppController extends Application {

    public static final String TAG = AppController.class.getSimpleName();
    public static String DEVICE_GCM_REGISTER_ID;
    private static AppController mInstance;
    private  Context appContext;
    public static Map<String,String> mapUserRoutes;

    public static synchronized AppController getInstance() {
        return mInstance;
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //ACRA.init(this);
        //Initializing Acra
        mInstance = this;
        MultiDex.install(this);

        AbstractApplication.CONTEXT = getApplicationContext();
        appContext= getApplicationContext();
        //LocaleHelper.onCreate(this, "en");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        //LocaleHelper.onCreate(this, "en");

    }



    @Override
    public void onLowMemory() {
        super.onLowMemory();
        System.runFinalization();
        Runtime.getRuntime().gc();
        System.gc();

    }


}