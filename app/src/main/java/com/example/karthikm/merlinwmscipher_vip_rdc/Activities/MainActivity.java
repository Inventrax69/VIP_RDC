package com.example.karthikm.merlinwmscipher_vip_rdc.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.AboutFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.CycleCountHeaderFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.DeNestingFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.DrawerFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.HomeFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.InternalTransferFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.LiveStockFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.LoadingFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.MRPChangeFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.OutboundFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.PackingHeaderFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.PutAwayFrontFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.PutAwayHeaderFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.RSNTrackingFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.SortingFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.UnloadingFragment;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.application.AbstractApplication;
import com.example.karthikm.merlinwmscipher_vip_rdc.logout.LogoutUtil;
import com.example.karthikm.merlinwmscipher_vip_rdc.model.NavDrawerItem;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.AndroidUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.DialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.FragmentUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ProgressDialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.SharedPreferencesUtils;
import com.honeywell.aidc.BarcodeReader;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements DrawerFragment.FragmentDrawerListener {


    private Toolbar mToolbar;
    //private DrawerFragment drawerFragment;
    private FragmentUtils fragmentUtils;
    private CharSequence[] userRouteCharSequences;
    private List<String> userRouteStringList;
    private String selectedRouteCode;
    private FragmentActivity fragmentActivity;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private LogoutUtil logoutUtil;
    private static BarcodeReader barcodeReader;

    private DrawerFragment drawerFragment;

    boolean doubleBackToExitPressedOnce = false;
    String TenantType="";

    public static BarcodeReader getBarcodeObject() {
        return barcodeReader;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            setContentView(R.layout.activity_main);

            loadFormControls();


        } catch (Exception ex) {
            // Logger.Log(MainActivity.class.getName(), ex);
        }
    }

    public void loadFormControls() {
        try {

            logoutUtil = new LogoutUtil();
            mToolbar = (Toolbar) findViewById(R.id.toolbar);

            fragmentUtils = new FragmentUtils();

            fragmentActivity = this;

            new ProgressDialogUtils(this);

            AbstractApplication.FRAGMENT_ACTIVITY = this;

            setSupportActionBar(mToolbar);

               /*
               if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayShowHomeEnabled(true);
                    getSupportActionBar().setIcon(R.mipmap.ic_launcher);
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                }*/

            View logoView = AndroidUtils.getToolbarLogoIcon(mToolbar);

            if (logoView != null) logoView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FragmentUtils.replaceFragmentWithBackStack(fragmentActivity, R.id.container_body, new HomeFragment());
                }
            });

            drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_navigation_drawer);
            drawerFragment.setUp(R.id.fragment_navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout), mToolbar);
            drawerFragment.setDrawerListener(this);


            sharedPreferencesUtils = new SharedPreferencesUtils("LoginActivity", getApplicationContext());
            TenantType=sharedPreferencesUtils.loadPreference("TenantType");
            userRouteStringList = new ArrayList<>();
            userRouteCharSequences = userRouteStringList.toArray(new CharSequence[userRouteStringList.size()]);
            logoutUtil.setActivity(this);
            logoutUtil.setFragmentActivity(fragmentActivity);
            logoutUtil.setSharedPreferencesUtils(sharedPreferencesUtils);
            // display the first navigation drawer view on app launch
            displayView(0, new NavDrawerItem(false, "Home"));


        } catch (Exception ex) {
            DialogUtils.showAlertDialog(this, "Error while loading form controls");
            return;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        switch (id) {
            case R.id.action_logout: {

                logoutUtil.doLogout();

            }
            break;

             /*   case R.id.action_settings: {
                    FragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, new SettingsFragment());
                }*/

            case R.id.action_about: {

                FragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, new AboutFragment());

            }
            break;

        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onDrawerItemSelected(View view, int position, NavDrawerItem menuItem) {

        displayView(position, menuItem);
    }


    private void displayView(int position, NavDrawerItem menuItem) {

        Fragment fragment = null;
        String title = getString(R.string.app_name);

        switch (menuItem.getTitle()) {

            case "Home": {
                fragment = new HomeFragment();
                title = "Home";
            }
            break;

            case "Goods In": {
                fragment = new UnloadingFragment();
                title = "Goods In";
            }
            break;

            case "Putaway": {
                Log.v("ABCDE",TenantType);
                if(TenantType.equals("Branch")){
                    fragment = new PutAwayFrontFragment();
                }else{
                    fragment = new PutAwayHeaderFragment();
                }

                title = "Putaway";
            }
            break;

            case "OBD Picking": {
                fragment = new OutboundFragment();
                title = "OBD Picking";
            }
            break;

            case "Sorting": {
                fragment = new SortingFragment();
                title = "Cycle Count";
            }
            break;


            case "Loading": {
                fragment = new LoadingFragment();
                title = "Loading";
            }
            break;

            case "Packing": {
                fragment = new PackingHeaderFragment();
                title = "Packing";
            }
            break;

            case "Nesting/Denesting": {
                fragment = new DeNestingFragment();
                title = "Denesting";
            }
            break;

            case "Cycle Count": {
                fragment = new CycleCountHeaderFragment();
                title = "Cycle Count";
            }
            break;


            case "MRP Changes": {
                fragment = new MRPChangeFragment();
                title = "MRP Changes";
            }
            break;
            case "House Keeping": {
                fragment = new InternalTransferFragment();
                title = "Transfers";
            }
            break;

            case "Live Stock": {
                fragment = new LiveStockFragment();
                title = "Live Stock";
            }
            break;
            case "RSN Tracking": {
                fragment = new RSNTrackingFragment();
                title = "RSN Tracking";
            }
            break;
            default:
            break;
        }

        if (fragment != null) {

            FragmentUtils.replaceFragmentWithBackStack(this, R.id.container_body, fragment);

            // set the toolbar title
            getSupportActionBar().setTitle(title);

        }
    }

    @Override
    protected void onPause() {

        super.onPause();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {

        if(((DrawerLayout)findViewById(R.id.drawer_layout)).isDrawerOpen(GravityCompat.START)) {
            ((DrawerLayout)findViewById(R.id.drawer_layout)).closeDrawer(GravityCompat.START);
        }else{
            if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
                getSupportFragmentManager().popBackStack();
            }else{
                if (doubleBackToExitPressedOnce) {
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory( Intent.CATEGORY_HOME );
                    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(homeIntent);
                    finish();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                final Toast toast = Toast.makeText(this, "Please click Back again to exit", Toast.LENGTH_SHORT);
                toast.show();
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        toast.cancel();
                    }
                }, 1000);

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce=false;
                    }
                }, 3000);
            }
        }
    }

    private void initiateBackgroundServices() {


    }

}
