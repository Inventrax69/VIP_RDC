package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.cipherlab.barcode.GeneralString;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InventoryDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
import com.example.karthikm.merlinwmscipher_vip_rdc.adapters.LiveStockAdapter;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.Common;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.EndpointConstants;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.ErrorMessages;
import com.example.karthikm.merlinwmscipher_vip_rdc.interfaces.ApiInterface;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.DialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ExceptionLoggerUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.FragmentUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ProgressDialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ScanValidator;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.SoundUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import com.honeywell.aidc.AidcManager;
import com.honeywell.aidc.BarcodeFailureEvent;
import com.honeywell.aidc.BarcodeReadEvent;
import com.honeywell.aidc.BarcodeReader;
import com.honeywell.aidc.ScannerUnavailableException;
import com.honeywell.aidc.TriggerStateChangeEvent;
import com.honeywell.aidc.UnsupportedPropertyException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by karthik.m on 05/08/2018.
 */

public class LiveStockFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {


    private static final String classCode = "API_FRAG_007";
    private View rootView;
    String scanner = null;
    private IntentFilter filter;
    private WMSCoreMessage core;
    String getScanner = null;
    private Gson gson;
    private ScanValidator scanValidator;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Common common;
    private TextView tvScanRSN, tvScanLocation, tvScanPallet, lblScannedData, progressText, lblCount;
    private CardView cvScanRSN, cvScanLocation, cvScanPallet;
    private ImageView ivScanRSN, ivScanLocation, ivScanPallet;
    private Button btnClose;
    private RecyclerView recyclerViewLiveStock;
    private LinearLayoutManager linearLayoutManager;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private SoundUtils soundUtils;
    private Boolean isLocationScanned = false, isRSNScanned = false, isPalletScanned = false;
    private String scannedLocation, scannedPallet, scannedRSN;
    private int count = 0;
    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public LiveStockFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_livestock, container, false);
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);

        ivScanRSN = (ImageView) rootView.findViewById(R.id.ivScanRSN);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);

        btnClose = (Button) rootView.findViewById(R.id.btnClose);

        tvScanRSN = (TextView) rootView.findViewById(R.id.tvScanRSN);
        tvScanLocation = (TextView) rootView.findViewById(R.id.tvScanLocation);
        tvScanPallet = (TextView) rootView.findViewById(R.id.tvScanPallet);
        lblScannedData = (TextView) rootView.findViewById(R.id.lblScannedData);
        lblCount = (TextView) rootView.findViewById(R.id.lblCount);
        recyclerViewLiveStock = (RecyclerView) rootView.findViewById(R.id.recycler_view_liveStock);
        recyclerViewLiveStock.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        recyclerViewLiveStock.setLayoutManager(linearLayoutManager);


        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        soundUtils = new SoundUtils();
        common = new Common();
        gson = new GsonBuilder().create();
        btnClose.setOnClickListener(this);
        //For Honeywell
        AidcManager.create(getActivity(), new AidcManager.CreatedCallback() {

            @Override
            public void onCreated(AidcManager aidcManager) {
                manager = aidcManager;
                barcodeReader = manager.createBarcodeReader();
                try {
                    barcodeReader.claim();
                    HoneyWellBarcodeListeners();
                } catch (ScannerUnavailableException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnClose:
                FragmentUtils.replaceFragment(getActivity(), R.id.container_body, new HomeFragment());
                break;
        }
    }


    // honeywell
    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                // update UI to reflect the data
                getScanner = barcodeReadEvent.getBarcodeData();
                ProcessScannedinfo(getScanner);

            }

        });
    }

    @Override
    public void onFailureEvent(BarcodeFailureEvent barcodeFailureEvent) {

    }

    @Override
    public void onTriggerEvent(TriggerStateChangeEvent triggerStateChangeEvent) {

    }

    //Honeywell Barcode reader Properties
    public void HoneyWellBarcodeListeners() {

        barcodeReader.addTriggerListener(this);

        if (barcodeReader != null) {
            // set the trigger mode to client control
            barcodeReader.addBarcodeListener(this);
            try {
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE, BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
            } catch (UnsupportedPropertyException e) {
                // Toast.makeText(this, "Failed to apply properties", Toast.LENGTH_SHORT).show();
            }

            Map<String, Object> properties = new HashMap<String, Object>();
            // Set Symbologies On/Off
            properties.put(BarcodeReader.PROPERTY_CODE_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_GS1_128_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_QR_CODE_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_CODE_39_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_DATAMATRIX_ENABLED, true);
            properties.put(BarcodeReader.PROPERTY_UPC_A_ENABLE, true);
            properties.put(BarcodeReader.PROPERTY_EAN_13_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_AZTEC_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_CODABAR_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_INTERLEAVED_25_ENABLED, false);
            properties.put(BarcodeReader.PROPERTY_PDF_417_ENABLED, false);
            // Set Max Code 39 barcode length
            properties.put(BarcodeReader.PROPERTY_CODE_39_MAXIMUM_LENGTH, 10);
            // Turn on center decoding
            properties.put(BarcodeReader.PROPERTY_CENTER_DECODE, true);
            // Enable bad read response
            properties.put(BarcodeReader.PROPERTY_NOTIFICATION_BAD_READ_ENABLED, true);
            // Apply the settings
            barcodeReader.setProperties(properties);
        }
    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {

        if (scannedData != null && !common.isPopupActive) {

            if (scanValidator.IsPalletScanned(scannedData)) {
                lblScannedData.setText(scannedData);
                scannedPallet = scannedData;
                ValidatePalletCode();
                return;
            }

            if (ScanValidator.IsItemScanned(scannedData)) {
                lblScannedData.setText(scannedData);
                scannedRSN = scannedData;

                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                ivScanPallet.setImageResource(R.drawable.fullscreen_img);
                scannedPallet = null;
                scannedLocation = null;
                isLocationScanned = false;
                isPalletScanned = false;

                ProgressDialogUtils.showProgressDialog("Please Wait..");
                GetActiveStockData();
                isRSNScanned = true;
                return;
            }

            if (scanValidator.IsLocationScanned(scannedData)) {
                lblScannedData.setText(scannedData);
                scannedLocation = scannedData;
                ValidateLocationCode();
                return;
            }
        }
    }


    // Validate Location
    public void ValidateLocationCode() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getActivity());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setLocationCode(scannedLocation);
            message.setEntityObject(inventoryDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidateHouseKeepingLocationCode(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
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
                                    //etLocation.setText("");
                                    ProgressDialogUtils.closeProgressDialog();
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.warning_img);
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

                                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanLocation.setImageResource(R.drawable.invalid_cross);
                                            lblScannedData.setText("");
                                            common.showUserDefinedAlertType(errorMessages.EMC_0031, getActivity(), getContext(), "Error");

                                            return;
                                        } else {                                                         // on valid location
                                            ProgressDialogUtils.closeProgressDialog();

                                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                            ivScanPallet.setImageResource(R.drawable.fullscreen_img);
                                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                            ivScanRSN.setImageResource(R.drawable.fullscreen_img);
                                            scannedPallet = null;
                                            scannedRSN = null;
                                            isPalletScanned = false;
                                            isRSNScanned = false;

                                            isLocationScanned = true;

                                            GetActiveStockData();

                                            return;
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    // Validate Pallet
    public void ValidatePalletCode() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getActivity());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setContainerCode(scannedPallet);
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidateHouseKeepingPalletCode(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
                return;
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                        if (core != null) {
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.warning_img);
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
                                            lblScannedData.setText("");
                                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanPallet.setImageResource(R.drawable.invalid_cross);
                                            common.showUserDefinedAlertType(errorMessages.EMC_0017, getActivity(), getContext(), "Error");
                                            return;
                                        } else {                                                             // on valid pallet
                                            ProgressDialogUtils.closeProgressDialog();

                                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                            ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                                            cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                            ivScanRSN.setImageResource(R.drawable.fullscreen_img);
                                            scannedRSN = null;
                                            scannedLocation = null;
                                            isLocationScanned = false;
                                            isRSNScanned = false;

                                            isPalletScanned = true;
                                            GetActiveStockData();
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                        return;
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
                return;
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            return;
        }
    }


    public void GetActiveStockData() {
        try {
            if (lblScannedData.getText().toString().isEmpty()) {
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0022);
                return;
            }
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setRSN(scannedRSN);
            inventoryDTO.setLocationCode(scannedLocation);
            inventoryDTO.setContainerCode(scannedPallet);
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetActivestock(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                    if (isRSNScanned) {
                                        cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanRSN.setImageResource(R.drawable.warning_img);
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        recyclerViewLiveStock.setAdapter(null);
                                        lblScannedData.setText("");
                                        ProgressDialogUtils.closeProgressDialog();
                                        return;
                                    }
                                    if (isLocationScanned) {
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.warning_img);
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        recyclerViewLiveStock.setAdapter(null);
                                        lblScannedData.setText("");
                                        ProgressDialogUtils.closeProgressDialog();
                                        return;
                                    }
                                    if (isPalletScanned) {
                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPallet.setImageResource(R.drawable.warning_img);
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        recyclerViewLiveStock.setAdapter(null);
                                        lblScannedData.setText("");
                                        ProgressDialogUtils.closeProgressDialog();
                                        return;
                                    }
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

                                if (isLocationScanned) {

                                    LiveStockAdapter liveStockAdapter = new LiveStockAdapter(getActivity(), lstInventory);
                                    recyclerViewLiveStock.setAdapter(liveStockAdapter);

                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanLocation.setImageResource(R.drawable.check);


                                    if (recyclerViewLiveStock.getAdapter() != null) {
                                        count = recyclerViewLiveStock.getAdapter().getItemCount();
                                    }

                                    lblCount.setText(Integer.toString(count));

                                    return;
                                }

                                if (isRSNScanned) {

                                    LiveStockAdapter liveStockAdapter = new LiveStockAdapter(getActivity(), lstInventory);
                                    recyclerViewLiveStock.setAdapter(liveStockAdapter);

                                    cvScanRSN.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanRSN.setImageResource(R.drawable.check);

                                    if (recyclerViewLiveStock.getAdapter() != null) {
                                        count = recyclerViewLiveStock.getAdapter().getItemCount();
                                    }

                                    lblCount.setText(Integer.toString(count));

                                    return;
                                }

                                if (isPalletScanned) {

                                    LiveStockAdapter liveStockAdapter = new LiveStockAdapter(getActivity(), lstInventory);
                                    recyclerViewLiveStock.setAdapter(liveStockAdapter);

                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.check);

                                    if (recyclerViewLiveStock.getAdapter() != null) {
                                        count = recyclerViewLiveStock.getAdapter().getItemCount();
                                    }

                                    lblCount.setText(Integer.toString(count));

                                    return;
                                }
                            }
                        } else {
                            recyclerViewLiveStock.setAdapter(null);
                            ProgressDialogUtils.closeProgressDialog();
                            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {

                        recyclerViewLiveStock.setAdapter(null);
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);

                        return;
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                recyclerViewLiveStock.setAdapter(null);
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
            return;
        }
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
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
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
        if (barcodeReader != null) {
            // release the scanner claim so we don't get any scanner
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
            }
            barcodeReader.release();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (barcodeReader != null) {
            try {
                barcodeReader.claim();
            } catch (ScannerUnavailableException e) {
                e.printStackTrace();
                // Toast.makeText(this, "Scanner unavailable", Toast.LENGTH_SHORT).show();
            }
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_live_stock));
    }

    @Override
    public void onDestroyView() {
        // Honeywell onDestroyView
        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);
            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }
        // Cipher onDestroyView
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();
    }
}