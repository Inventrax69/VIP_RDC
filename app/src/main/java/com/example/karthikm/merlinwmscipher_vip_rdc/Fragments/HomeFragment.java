package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.FragmentUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.SharedPreferencesUtils;


public class HomeFragment extends Fragment implements View.OnClickListener {

    private View rootView;

    Fragment fragment = null;

    CardView cvReceive,cvPutaway,cvPicking,cvHousekeeping;

    String TenantType="";

    private SharedPreferencesUtils sharedPreferencesUtils;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_home,container,false);
        //loadFormControls();

        cvPicking = (CardView) rootView.findViewById(R.id.cvPicking);
        cvReceive = (CardView) rootView.findViewById(R.id.cvReceive);
        cvPutaway = (CardView) rootView.findViewById(R.id.cvPutaway);
        cvHousekeeping=(CardView)rootView.findViewById(R.id.cvHousekeeping);

        sharedPreferencesUtils = new SharedPreferencesUtils("LoginActivity", getActivity());
        TenantType=sharedPreferencesUtils.loadPreference("TenantType");

        cvPutaway.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TenantType.equals("Branch")){
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PutAwayFrontFragment());
                }else{
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PutAwayHeaderFragment());
                }
            }
        });

        cvPicking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new OutboundFragment());
            }
        });

        cvReceive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new UnloadingFragment());
            }
        });

        cvHousekeeping.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new InternalTransferFragment());
            }
        });

        return rootView;
    }

    private void loadFormControls() {

        /*rlHomeMenuOne = (RelativeLayout) rootView.findViewById(R.id.rlHomeMenuOne);
        rlHomeMenuTwo = (RelativeLayout) rootView.findViewById(R.id.rlHomeMenuTwo);

        ivReceiveImg = (ImageView) rootView.findViewById(R.id.ivReceiveImg);
        ivPickingImg = (ImageView) rootView.findViewById(R.id.ivPickingImg);
        ivPutawayImg = (ImageView) rootView.findViewById(R.id.ivPutawayImg);
        ivHouseKeepingImg = (ImageView) rootView.findViewById(R.id.ivHouseKeepingImg);
        ivCycleCountImg = (ImageView) rootView.findViewById(R.id.ivCycleCountImg);
        ivInternalTransferImg = (ImageView) rootView.findViewById(R.id.ivInternalTransferImg);

        ivReceiveImg.setOnClickListener(this);
        ivPickingImg.setOnClickListener(this);
        ivPutawayImg.setOnClickListener(this);
        ivHouseKeepingImg.setOnClickListener(this);
        ivCycleCountImg.setOnClickListener(this);
        ivInternalTransferImg.setOnClickListener(this);*/

        // rlHomeMenuTwo.setVisibility(View.GONE);
    }



    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            /*case R.id.ivReceiveImg:
                UnloadingFragment unloadingFragment = new UnloadingFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, unloadingFragment);
                break;
            case R.id.ivPickingImg:
                OutBoundFragment outBoundFragment = new OutBoundFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, outBoundFragment);
                break;
            case R.id.ivPutawayImg:
                PutAwayFragment putAwayFragment = new PutAwayFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, putAwayFragment);
                break;
            case R.id.ivHouseKeepingImg:
                rlHomeMenuTwo.setVisibility(View.VISIBLE);
                rlHomeMenuOne.setVisibility(View.GONE);
                break;
            case R.id.ivCycleCountImg:
                CycleCountHeaderFragment cycleCountHeaderFragment = new CycleCountHeaderFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, cycleCountHeaderFragment);
                break;
            case R.id.ivInternalTransferImg:
                InternalTransferFragment internalTransferFragment = new InternalTransferFragment();
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, internalTransferFragment);
                break;*/
        }

    }
}