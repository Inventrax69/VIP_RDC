package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.SearchableSpinner.SearchableSpinner;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.Common;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.EndpointConstants;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.ErrorMessages;
import com.example.karthikm.merlinwmscipher_vip_rdc.interfaces.ApiInterface;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ExceptionLoggerUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.FragmentUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ProgressDialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ScanValidator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeneratePackingSlipFragment extends Fragment implements View.OnClickListener {

    private View rootView;
    private static final String classCode = "API_FRAG_014";
    private TextView lblPackingNo;
    private Button btnPrint, btnCancel;
    SearchableSpinner spinnerSelectPrinter, spinnerSelectPackType;
    private String printer = "Select Printer", PackType = "Select Packing Type", deviceIp = "";
    RelativeLayout rlPackListOne, rlPackListgenration;
    private EditText etweight, etVolume;

    List<String> deviceIPList;
    String packOBDno = "", moHStatus = "", packSlipNo = "", orderTypeId = "";

    private IntentFilter filter;
    private Gson gson;
    String userId = null;
    private Common common;
    private WMSCoreMessage core;

    private ScanValidator scanValidator;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;


    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        boolean packslip;
        rootView = inflater.inflate(R.layout.fragment_generate_packslip, container, false);

        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {


        rlPackListgenration = (RelativeLayout) rootView.findViewById(R.id.rlPackListgenration);

        btnPrint = (Button) rootView.findViewById(R.id.btnPrint);
        btnCancel = (Button) rootView.findViewById(R.id.btnCancel);

        lblPackingNo = (TextView) rootView.findViewById(R.id.lblPackingNo);
        etweight = (EditText) rootView.findViewById(R.id.etweight);
        etVolume = (EditText) rootView.findViewById(R.id.etVolume);


        spinnerSelectPrinter = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectPrinter);
        spinnerSelectPrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (!selectedItem.equals("Select Printer")) {
                    // do your stuff
                    printer = spinnerSelectPrinter.getSelectedItem().toString().split("[-]", 2)[0];
                    deviceIp = deviceIPList.get(position).toString();
                }
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        spinnerSelectPackType = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectPackType);
        spinnerSelectPackType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = parent.getItemAtPosition(position).toString();
                if (!selectedItem.equals("Select Packing Type")) {
                    // do your stuff
                    PackType = spinnerSelectPackType.getSelectedItem().toString().split("[-]", 2)[0];
                }
            } // to close the onItemSelected

            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        gson = new GsonBuilder().create();

        packOBDno = getArguments().getString("packOBDno");
        moHStatus = getArguments().getString("moHStatus");
        orderTypeId = getArguments().getString("orderTypeId");
        btnPrint.setOnClickListener(this);
        btnCancel.setOnClickListener(this);

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();


        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");

        //GetHandlingType();
        //LoadPrinters();
    }

    public GeneratePackingSlipFragment() {

    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPrint:

                break;

            case R.id.btnCancel:
                getActivity().onBackPressed();
                //  FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new PackingDetailsFragment());
                break;

        }

    }

    private boolean ValidateInputParameters() {
        boolean ret = true;
        if (PackType.equalsIgnoreCase("Select Packing Type")) {
            ret = false;
            common.showUserDefinedAlertType("Please Select Packing type", getActivity(), getContext(), "Warning");

        } else if (etweight.getText().toString().equalsIgnoreCase("")) {
            ret = false;
            common.showUserDefinedAlertType("Please Enter Max Widhth", getActivity(), getContext(), "Warning");

        } else if (etVolume.getText().toString().equalsIgnoreCase("")) {
            ret = false;
            common.showUserDefinedAlertType("Please Enter Max Volume", getActivity(), getContext(), "Warning");

        } else if (printer.equalsIgnoreCase("Select Printer")) {
            ret = false;
            common.showUserDefinedAlertType("Please Select Printer", getActivity(), getContext(), "Warning");
        }
        return ret;
    }


    public void getPackListdetails() {
        Bundle bundle = new Bundle();
        bundle.putString("packOBDno", packOBDno);
        bundle.putString("moHStatus", moHStatus);
        bundle.putString("packSlip", packSlipNo);
        bundle.putString("orderTypeId", orderTypeId);
        PackingDetailsFragment packingDetailsFragment = new PackingDetailsFragment();
        packingDetailsFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, packingDetailsFragment);
    }






    // sending exception to the database
    public void logException() {
        try {

            String textFromFile = exceptionLoggerUtils.readFromFile(getActivity());
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, getActivity());
            WMSExceptionMessage wmsExceptionMessage = new WMSExceptionMessage();
            wmsExceptionMessage.setWMSMessage(textFromFile);
            message.setEntityObject(wmsExceptionMessage);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.LogException(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

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
                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                            //Log.d("Message", core.getEntityObject().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.generatepackSlip));
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
