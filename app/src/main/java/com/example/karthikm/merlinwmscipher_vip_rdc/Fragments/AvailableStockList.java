package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InventoryDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
import com.example.karthikm.merlinwmscipher_vip_rdc.adapters.DeNestingParentStockAdapter;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.Common;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.EndpointConstants;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.ErrorMessages;
import com.example.karthikm.merlinwmscipher_vip_rdc.interfaces.ApiInterface;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ExceptionLoggerUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.FragmentUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ProgressDialogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by karthik.m on 08/13/2018.
 */

public class AvailableStockList extends Fragment implements  View.OnClickListener {
    private static final String classCode = "API_FRAG_012";
    private View rootView;
    private RecyclerView recycler_view_denesting_parent_item;
    private Button btnClose;
    private LinearLayoutManager linearLayoutManager;
    private String mCode=null,JoborderNo=null;
    private Common common=null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private WMSCoreMessage core;
    private Gson gson;
    private boolean IsFromAvailList =false;
    private String jobOrderTypeId = "";


    public AvailableStockList() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_availableatockrecyclerview, container, false);
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        recycler_view_denesting_parent_item = (RecyclerView) rootView.findViewById(R.id.recycler_view_denesting_parent_item);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(this);

        recycler_view_denesting_parent_item.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        recycler_view_denesting_parent_item.setLayoutManager(linearLayoutManager);
        exceptionLoggerUtils= new ExceptionLoggerUtils();
        common= new Common();
        core= new WMSCoreMessage();
        gson = new GsonBuilder().create();
        errorMessages = new ErrorMessages();
        if (getArguments() != null) {


            if (getArguments().getString("MaterialCode") != null) {
                try {

                    mCode = getArguments().getString("MaterialCode");
                    JoborderNo = getArguments().getString("JobOrdeNo");
                    jobOrderTypeId = getArguments().getString("jobOrderTypeId");
                    GetActiveStockData();

                } catch (Exception ex) {

                }

            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnClose:
                IsFromAvailList= true;
                Bundle bundle = new Bundle();
                bundle.putBoolean("IsFromAvailList",IsFromAvailList);
                bundle.putString("JoborderNo",JoborderNo);
                bundle.putString("jobOrderTypeId",jobOrderTypeId);
                DeNestingFragment odenestingfragment= new DeNestingFragment();
                odenestingfragment.setArguments(bundle);
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, odenestingfragment);
                break;
        }
    }


    public void GetActiveStockData() {
        try {
            if(mCode==null)
            {
                common.showUserDefinedAlertType(errorMessages.EMC_0041,getActivity(),getContext(),"Error");
                return;
            }


            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setMaterialCode(mCode);
            inventoryDTO.setReferenceDocumentNumber(JoborderNo);
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetActivestockWithOutRSN(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_01",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            if (core != null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;

                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        ProgressDialogUtils.closeProgressDialog();
                                        return;


                                    }


                                } else {


                                    List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<InventoryDTO> lstInventory = new ArrayList<InventoryDTO>();

                                    InventoryDTO inventorydto = null;
                                    for (int i = 0; i < _lInventory.size(); i++) {

                                        inventorydto = new InventoryDTO(_lInventory.get(i).entrySet());
                                        lstInventory.add(inventorydto);
                                    }
                                    ProgressDialogUtils.closeProgressDialog();

                                    DeNestingParentStockAdapter deNestingActiveStockAdapter = new DeNestingParentStockAdapter(getActivity(), lstInventory);
                                    recycler_view_denesting_parent_item.setAdapter(deNestingActiveStockAdapter);


                                    return;

                                }
                            } else {
                                recycler_view_denesting_parent_item.setAdapter(null);
                                ProgressDialogUtils.closeProgressDialog();
                                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");

                                return;
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {

                        recycler_view_denesting_parent_item.setAdapter(null);
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");

                        return;
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_02",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                recycler_view_denesting_parent_item.setAdapter(null);
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");


            }
        }
        catch (Exception ex)
        {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_03",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }

            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
            return;
        }
    }
    // sending exception to the database
    public void logException() {
        try {

            String textFromFile = ExceptionLoggerUtils.readFromFile(getActivity());

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, getActivity());
            WMSExceptionMessage wmsExceptionMessage = new WMSExceptionMessage();
            wmsExceptionMessage.setWMSMessage(textFromFile);
            message.setEntityObject(wmsExceptionMessage);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                // Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.LogException(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if(core!=null) {
                                // if any Exception throws
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {
                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        return;
                                    }
                                } else {
                                    LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                    _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                        if (entry.getKey().equals("Result")) {
                                            String Result = entry.getValue();
                                            if (Result.equals("0")) {
                                                ProgressDialogUtils.closeProgressDialog();
                                                return;
                                            } else {
                                                ProgressDialogUtils.closeProgressDialog();
                                                exceptionLoggerUtils.deleteFile(getActivity());
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {

                            /*try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();*/


                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_de_nesting_parent_items));
    }
}