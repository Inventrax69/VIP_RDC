package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.adapters.NavigationDrawerAdapter;
import com.example.karthikm.merlinwmscipher_vip_rdc.model.NavDrawerItem;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.DialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ProgressDialogUtils;

import java.util.ArrayList;
import java.util.List;

public class DrawerFragment_withList extends Fragment implements View.OnClickListener {

    private static String TAG = DrawerFragment_withList.class.getSimpleName();
    private RecyclerView recyclerView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    // Using NavigationDrawerAdapter
    private NavigationDrawerAdapter adapter;
    private View containerView;
    private FragmentDrawerListener drawerListener;
    private View layout;
    private TextView txtLoginUser;

    private AppCompatActivity appCompatActivity;
    private List<NavDrawerItem> menuItemList;
    private IntentFilter mIntentFilter;
    private CounterBroadcastReceiver counterBroadcastReceiver;
    private String userName;


    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflating view layout
        layout = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

        appCompatActivity = (AppCompatActivity) getActivity();

        menuItemList = new ArrayList<>();



        loadFormControls();

        return layout;
    }

    public void loadFormControls() {
        try {

            SharedPreferences sp = getContext().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            userName = sp.getString("UserName", "");
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction("com.example.broadcast.counter");

            counterBroadcastReceiver = new CounterBroadcastReceiver();

            menuItemList = getMenuItemsByUserType("1");

            new ProgressDialogUtils(getContext());



            recyclerView = (RecyclerView) layout.findViewById(R.id.drawerList);
            txtLoginUser = (TextView) layout.findViewById(R.id.txtLoginUser);
            txtLoginUser.setText(userName);


            adapter = new NavigationDrawerAdapter(getActivity(), menuItemList);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getActivity(), recyclerView, new ClickListener() {
                @Override
                public void onClick(View view, int position) {

                    NavigationDrawerAdapter.setSelectedItemPosition(position);

                    recyclerView.getAdapter().notifyDataSetChanged();

                    drawerListener.onDrawerItemSelected(view, position, menuItemList.get(position));
                    mDrawerLayout.closeDrawer(containerView);
                }

                @Override
                public void onLongClick(View view, int position) {

                }
            }));


        } catch (Exception ex) {
            //Logger.Log(DrawerFragment.class.getName(), ex);
            DialogUtils.showAlertDialog(getActivity(), "Error while loading menu list");
            return;
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        /*try
        {
            if ( user != null ) {

                appCompatActivity.getSupportActionBar().setTitle(StringUtils.toCamelCase(user.getFirstName()));
                appCompatActivity.getSupportActionBar().setSubtitle(user.getUserType().toUpperCase() + "  " + new SimpleDateFormat("dd-MM-yyyy").format(new Date()) );

            }

        }catch (Exception ex){
            Logger.Log(DrawerFragment.class.getName(),ex);
            DialogUtils.showAlertDialog(getActivity(), "Error while loading menu list");
            return;
        }*/

    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {

        try {
            containerView = getActivity().findViewById(fragmentId);
            mDrawerLayout = drawerLayout;
            mDrawerToggle = new ActionBarDrawerToggle(getActivity(), drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close) {
                @Override
                public void onDrawerOpened(View drawerView) {
                    super.onDrawerOpened(drawerView);
                    getActivity().invalidateOptionsMenu();
                }

                @Override
                public void onDrawerClosed(View drawerView) {
                    super.onDrawerClosed(drawerView);
                    getActivity().invalidateOptionsMenu();
                }

                @Override
                public void onDrawerSlide(View drawerView, float slideOffset) {
                    super.onDrawerSlide(drawerView, slideOffset);
                    toolbar.setAlpha(1 - slideOffset / 2);
                }
            };

            mDrawerLayout.setDrawerListener(mDrawerToggle);
            mDrawerLayout.post(new Runnable() {
                @Override
                public void run() {
                    mDrawerToggle.syncState();
                }
            });
        }catch (Exception ex){
            // Logger.Log(DrawerFragment.class.getName(),ex);
            return;
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

        }

    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().registerReceiver(counterBroadcastReceiver, mIntentFilter);

    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(counterBroadcastReceiver);
        super.onPause();

    }
    public  List<NavDrawerItem>  getMenuItemsByUserType(String userType){

        List<NavDrawerItem> menuList=new ArrayList<>();

        switch (userType)
        {

            case "1":{

                menuList.add(new NavDrawerItem("Goods In",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("Putaway",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("OBD Picking",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("Sorting",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("Loading",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("Packing",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("Nesting/Denesting",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("MRP Changes",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("Cycle Count",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("House Keeping",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("Live Stock",R.drawable.menu_goodsin));
                menuList.add(new NavDrawerItem("RSN Tracking",R.drawable.menu_goodsin));
            }break;


            case "2":{



            }break;


            case "3":{


            }break;


            case "4":{


            }break;


            case "5":{


            }break;


            case "6":{


            }break;

        }

        return menuList;
    }

    public interface ClickListener {
        void onClick(View view, int position);

        void onLongClick(View view, int position);
    }

    public interface FragmentDrawerListener {
        void onDrawerItemSelected(View view, int position, NavDrawerItem menuItem);
    }

    static class RecyclerTouchListener implements RecyclerView.OnItemTouchListener {

        private GestureDetector gestureDetector;
        private ClickListener clickListener;

        public RecyclerTouchListener(Context context, final RecyclerView recyclerView, final ClickListener clickListener) {
            this.clickListener = clickListener;
            gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
                    if (child != null && clickListener != null) {
                        clickListener.onLongClick(child, recyclerView.getChildPosition(child));
                    }
                }
            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {

            try {

                View child = rv.findChildViewUnder(e.getX(), e.getY());
                if (child != null && clickListener != null && gestureDetector.onTouchEvent(e)) {
                    clickListener.onClick(child, rv.getChildPosition(child));
                }
                return false;

            }catch(Exception ex){
                // Logger.Log(DrawerFragment.class.getName(),ex);
                return false;

            }
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {
        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }

    public class CounterBroadcastReceiver extends BroadcastReceiver {

        public CounterBroadcastReceiver() {

        }

        @Override
        public void onReceive(Context context, Intent intent) {

            try {

                if (menuItemList != null)
                    menuItemList.clear();

                menuItemList = getMenuItemsByUserType("1");

                adapter.setNavigationDrawerAdapter(getActivity(), menuItemList);
                adapter.notifyDataSetChanged();

            }catch (Exception ex){
                //  Logger.Log(DrawerFragment.class.getName(),ex);
                return;
            }

        }


    }
}