package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;


import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.adapters.NavDrawerAdapter;
import com.example.karthikm.merlinwmscipher_vip_rdc.model.MenuModel;
import com.example.karthikm.merlinwmscipher_vip_rdc.model.NavDrawerItem;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.DialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.FragmentUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ProgressDialogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class DrawerFragment extends Fragment implements View.OnClickListener {

    private static final String classCode = "API_FRAG_NAVIGATION DRAWER";

    private static String TAG = DrawerFragment.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private Context mContext;
    private ActionBarDrawerToggle mDrawerToggle;
    public DrawerLayout mDrawerLayout;
    private View containerView;
    private FragmentDrawerListener drawerListener;
    private View layout;
    private TextView txtLoginUser, tvCycleCount;

    public DrawerLayout drawerLayout;

    private AppCompatActivity appCompatActivity;

    private IntentFilter mIntentFilter;

    private String userName;
    private String division, menuLink;
    SharedPreferences sp;

    String TenantType="";
    NavDrawerAdapter expandableListAdapter;
    ExpandableListView expandableListView;
    List<MenuModel> headerList = new ArrayList<>();
    HashMap<MenuModel, List<MenuModel>> childList = new HashMap<>();

    public void setDrawerListener(FragmentDrawerListener listener) {
        this.drawerListener = listener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflating view layout
        layout = inflater.inflate(R.layout.nav_drawer_fragment, container, false);

        appCompatActivity = (AppCompatActivity) getActivity();

        loadFormControls();


        return layout;
    }

    public void loadFormControls() {

        try {


            sp = getContext().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
            userName = sp.getString("UserName", "");   // Getting User name and division from Login
            division = sp.getString("division", "");
            TenantType=sp.getString("TenantType","");
            mIntentFilter = new IntentFilter();
            mIntentFilter.addAction("com.example.broadcast.counter");

            new ProgressDialogUtils(getContext());

            txtLoginUser = (TextView) layout.findViewById(R.id.txtLoginUser);

            if (userName.endsWith(",")) {
                userName = userName.substring(0, userName.length() - 1);
            }

            txtLoginUser.setText(userName);
            mContext = getContext();

            expandableListView = (ExpandableListView) layout.findViewById(R.id.expandableListView);
            prepareMenuData();
            // To add menu items
            populateExpandableList();

        } catch (Exception ex) {
            //Logger.Log(DrawerFragment.class.getName(), ex);
            DialogUtils.showAlertDialog(getActivity(), "Error while loading menu list");
            return;
        }
    }

    private void prepareMenuData() {

        // if child is not there for a header, then add "null" in place of childModeList

        MenuModel menuModel = new MenuModel("Inbound", true, true, "Inbound");
        headerList.add(menuModel);

        List<MenuModel> childModelsList = new ArrayList<>();

        MenuModel childModel = new MenuModel("Goods In", false, false, "Goods In");
        childModelsList.add(childModel);

        childModel = new MenuModel("Putaway", false, false, "Putaway");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


        menuModel = new MenuModel("Outbound", true, true, "Outbound");
        headerList.add(menuModel);

        childModelsList = new ArrayList<>();

        childModel = new MenuModel("OBD Picking", false, false, "OBD Picking");
        childModelsList.add(childModel);

        /*childModel = new MenuModel("Packing", false, false, "Packing");
        childModelsList.add(childModel);*/

        childModel = new MenuModel("Loading", false, false, "Loading");
        childModelsList.add(childModel);

        childModel = new MenuModel("Load Revert", false, false, "Load Revert");
        childModelsList.add(childModel);

        childModel = new MenuModel("Sorting", false, false, "Sorting");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

        menuModel = new MenuModel("House Keeping", true, true, "House Keeping");
        headerList.add(menuModel);

        childModelsList = new ArrayList<>();

        childModel = new MenuModel("Bin to Bin", false, false, "Bin to Bin");
        childModelsList.add(childModel);

        childModel = new MenuModel("Nesting / Denesting", false, false, "Nesting / Denesting");
        childModelsList.add(childModel);

        childModel = new MenuModel("Cycle Count", false, false, "Cycle Count");
        childModelsList.add(childModel);

        childModel = new MenuModel("MRP Change", false, false, "MRP Change");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }


        menuModel = new MenuModel("Inventory", true, true, "Inventory");
        headerList.add(menuModel);

        childModelsList = new ArrayList<>();

        childModel = new MenuModel("Live Stock", false, false, "Live Stock");
        childModelsList.add(childModel);

        childModel = new MenuModel("RSN Tracking", false, false, "RSN Tracking");
        childModelsList.add(childModel);


        if (menuModel.hasChildren) {
            childList.put(menuModel, childModelsList);
        }

    }


    private void populateExpandableList() {
        expandableListAdapter = new NavDrawerAdapter(getContext(), headerList, childList);
        expandableListView.setAdapter(expandableListAdapter);       // adding menu items to the view
        // Header Click event
        expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

                if (headerList.get(groupPosition).isGroup) {
                    if (!headerList.get(groupPosition).hasChildren) {
                        menuLink = headerList.get(groupPosition).getMenuItemName();         // getting Header item
                        openFragment();                                                     // managing navigation menu clicks
                        onBackPressed();                                                  // closing navigaiton
                    }
                }
                return false;
            }
        });

        // To expand only one Item of an ExpandableListView and closing the previously expanded Item
        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;

            @Override
            public void onGroupExpand(int groupPosition) {
                if (groupPosition != previousGroup)
                    expandableListView.collapseGroup(previousGroup);
                previousGroup = groupPosition;
            }
        });

        // Child Click event
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                if (childList.get(headerList.get(groupPosition)) != null) {
                    MenuModel model = childList.get(headerList.get(groupPosition)).get(childPosition);
                    if (model.menuItemName.length() > 0) {
                        menuLink = model.getMenuItemName();                                 // getting Header item
                        openFragment();                                                    // managing navigation menu clicks
                        onBackPressed();                                                    // closing navigaiton
                    }
                }
                return false;
            }
        });
    }

    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) getActivity().findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }


    public void openFragment() {

        switch (menuLink) {

            case "Goods In":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new UnloadingFragment());
                break;

            case "Putaway":{
                if(TenantType.equals("Branch")){
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PutAwayFrontFragment());
                }else{
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PutAwayHeaderFragment());
                }
            }
                break;

            case "OBD Picking":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new OutboundFragment());
                break;

            case "Packing":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PackingHeaderFragment());
                break;

            case "Loading":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new LoadingFragment());
                break;

            case "Load Revert":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new LoadRevertFragment());
                break;

            case "Cycle Count":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new CycleCountHeaderFragment());
                break;

            case "Bin to Bin":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new InternalTransferFragment());
                break;

            case "Live Stock":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new LiveStockFragment());
                break;

            case "Nesting / Denesting":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new DeNestingFragment());
                break;

            case "MRP Change":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new MRPChangeFragment());
                break;

            case "Sorting":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new SortingFragment());
                break;

            case "RSN Tracking":
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new RSNTrackingFragment());
                break;


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
        } catch (Exception ex) {
            // Logger.Log(DrawerFragment.class.getName(),ex);
            return;
        }

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        }
    }


    public interface FragmentDrawerListener {
        void onDrawerItemSelected(View view, int position, NavDrawerItem menuItem);
    }

}