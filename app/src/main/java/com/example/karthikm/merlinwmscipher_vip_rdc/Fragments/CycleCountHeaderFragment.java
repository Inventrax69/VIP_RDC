package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cipherlab.barcode.GeneralString;
import com.example.karthikm.merlinwmscipher_vip_rdc.Activities.MainActivity;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.ColorDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.CycleCountDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.CycleCountType;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.StorageLocationDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.SearchableSpinner.SearchableSpinner;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
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
 * Created by karthik.m on 06/27/2018.
 */

public class CycleCountHeaderFragment extends Fragment implements View.OnClickListener,BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_004";
    private View rootView;
    SearchableSpinner spinnerSelectCycleCount;
    Button btnGo, btnCloseOne,btnBinComplete,btnCloseTwo;
    RelativeLayout rlCCHeaderOne,rlCCHeaderTwo;
    TextView lblCycleCount, lblSuggestedLoc,lblLocationsScanned,tvLocationsScanned;
    CardView cvScanLocation;
    ImageView ivScanLocation;
    TextInputLayout txtInputLayoutBoxQty, txtInputLayoutLocation;
    EditText etBoxQty,etLocation;

    DialogUtils dialogUtils;

    private WMSCoreMessage core;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    String userId =null;
    private ScanValidator scanValidator;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private String CCname = null;
    private Common common=null;
    List<String> sLoc;
    List<String> colorCodes;
    private boolean IsSatisfiedboxqty=false;
    private boolean isStockTakeMode=false;
    List<CycleCountType> CCType=null;
    List<List<StorageLocationDTO>> _lstSloc= null;
    String DefaultSloc=null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private SoundUtils soundUtils;
    private String isLocationFlagged = "";


    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public CycleCountHeaderFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_cyclecount_header, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }


    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        exceptionLoggerUtils= new ExceptionLoggerUtils();

        rlCCHeaderOne = (RelativeLayout) rootView.findViewById(R.id.rlCCHeaderOne);
        rlCCHeaderTwo = (RelativeLayout) rootView.findViewById(R.id.rlCCHeaderTwo);

        spinnerSelectCycleCount = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectCycleCount);
        spinnerSelectCycleCount.setOnItemSelectedListener(this);

        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnBinComplete = (Button) rootView.findViewById(R.id.btnBinComplete);
        btnCloseTwo = (Button)rootView.findViewById(R.id.btnCloseTwo);
        btnCloseOne.setOnClickListener(this);
        btnGo.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);
        btnBinComplete.setOnClickListener(this);

        lblCycleCount = (TextView) rootView.findViewById(R.id.lblCycleCount);
        lblSuggestedLoc = (TextView) rootView.findViewById(R.id.lblSuggestedLoc);

        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);

        txtInputLayoutBoxQty = (TextInputLayout)rootView.findViewById(R.id.txtInputLayoutBoxQty);
        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);

        etBoxQty = (EditText) rootView.findViewById(R.id.etBoxQty);
        etLocation = (EditText) rootView.findViewById(R.id.etLocation);

        colorCodes = new ArrayList<String>();
        sLoc = new ArrayList<String>();
        soundUtils= new SoundUtils();

        lblLocationsScanned=(TextView)rootView.findViewById(R.id.lblTotalNoOfLocationsToScan);
        tvLocationsScanned=(TextView) rootView.findViewById(R.id.tvTotalNoOfLocationsToScan);

        _lstSloc=new ArrayList<>();
        errorMessages = new ErrorMessages();

        CCType= new ArrayList<>();
        common= new Common();
        gson = new GsonBuilder().create();


        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

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

        try {
            FetchCCName();
        } catch (Exception ex) {

        }

        if (getArguments() != null){

            rlCCHeaderOne.setVisibility(View.GONE);
            rlCCHeaderTwo.setVisibility(View.VISIBLE);
            lblCycleCount.setText(getArguments().getString("CCname"));

            CCname = getArguments().getString("CCname");

        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnGo:
                if(CCname!=null) {
                    for (CycleCountType oCCType : CCType) {
                        if (oCCType.getCCName().equals(CCname)) {
                            isStockTakeMode = oCCType.getCCForStockTake();
                        }
                    }

                    if (isStockTakeMode == true) {
                        GetCCdetails();
                        return;
                    } else {
                        rlCCHeaderOne.setVisibility(View.GONE);
                        rlCCHeaderTwo.setVisibility(View.VISIBLE);
                        lblCycleCount.setText(CCname);

                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                        etLocation.setText("");
                        etBoxQty.setText("");
                    }
                }
                else
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_0045,getActivity(),getContext(),"Error");
                }
                break;
            case R.id.btnBinComplete:
                if(etLocation.getText().toString().isEmpty())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");


                }
                else if(etBoxQty.getText().toString().isEmpty())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_0040,getActivity(),getContext(),"Error");

                }
                else {
                    DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                            "Are you sure you want to complete the Bin?", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            ValidateLocationBoxQuantity();
                                            common.setIsPopupActive(false);
                                            break;

                                        case DialogInterface.BUTTON_NEGATIVE:
                                            common.setIsPopupActive(false);
                                            break;
                                    }

                                }
                            });
                    common.setIsPopupActive(true);
                    ProgressDialogUtils.closeProgressDialog();

                }
                break;
            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
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
                barcodeReader.setProperty(BarcodeReader.PROPERTY_TRIGGER_CONTROL_MODE,
                        BarcodeReader.TRIGGER_CONTROL_MODE_AUTO_CONTROL);
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

        if (scannedData != null) {
            if (scanValidator.IsLocationScanned(scannedData)) {
                etLocation.setText(scannedData);

                /*if(!(etLocation.getText().toString().equals(lblSuggestedLoc.getText().toString()))){

                    cvScanLocation.setCardBackgroundColor(Color.WHITE);
                    ivScanLocation.setImageResource(R.drawable.invalid_cross);

                    DialogUtils.showAlertDialog(getActivity(),errorMessages.EMC_0012);

                    return;
                }
                if(!(etLocation.getText().toString().equals(lblSuggestedLoc.getText().toString()))) {
                    cvScanLocation.setCardBackgroundColor(Color.WHITE);
                    ivScanLocation.setImageResource(R.drawable.warning_img);

                    DialogUtils.showAlertDialog(getActivity(),errorMessages.EMC_0013);
                    return;
                }*/


                BlockLocationForCycleCount();

            }else {
                common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");
                return;
            }
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_cycle_count));
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

    // bundle the Details for Deatils Fragemnt
    public void GetCCdetails() {

        Bundle bundle = new Bundle();
        String SLOCjson = gson.toJson(sLoc);
        String colorCodeJson = gson.toJson(colorCodes);
        bundle.putString("CCname", CCname);
        bundle.putString("Loc", etLocation.getText().toString());
        bundle.putString("SLOC", SLOCjson);
        bundle.putString("Color",colorCodeJson);
        bundle.putBoolean("isStockTake", isStockTakeMode);
        bundle.putString("DefaultSLOC", DefaultSloc);
        CycleCountDetailsFragment cycleCountDetailsFragment = new CycleCountDetailsFragment();
        cycleCountDetailsFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, cycleCountDetailsFragment);

    }

    // Get Cycle count names list
    public void FetchCCName() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.CycleCount,getActivity());
            final CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetCycleCountByUserId(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_01",getActivity());
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

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if(core!=null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();

                                    }
                                    common.showAlertType(owmsExceptionMessage,getActivity(),getContext());
                                } else {


                                    List<LinkedTreeMap<?, ?>> _lstCycleCount = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lstCycleCount = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    if (_lstCycleCount.size() > 0) {

                                        List<CycleCountDTO> lstDto = new ArrayList<CycleCountDTO>();
                                        List<String> _lstCCNames = new ArrayList<>();


                                        for (int i = 0; i < _lstCycleCount.size(); i++) {
                                            CycleCountDTO dto = new CycleCountDTO(_lstCycleCount.get(i).entrySet());
                                            lstDto.add(dto);
                                        }

                                        for (int i = 0; i < lstDto.size(); i++) {
                                            for (int j = 0; j < lstDto.get(i).getCCType().size(); j++) {
                                                _lstCCNames.add(lstDto.get(i).getCCType().get(j).getCCName());
                                                CCType = lstDto.get(i).getCCType();
                                            }
                                        }

                                        ProgressDialogUtils.closeProgressDialog();
                                        ArrayAdapter arrayAdapterCycleCount = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, _lstCCNames);
                                        spinnerSelectCycleCount.setAdapter(arrayAdapterCycleCount);

                                        // Added by padmaja 02/07/2018
                                        // for getting ColorCodes
                                        List<List<ColorDTO>> _lstColorCodes = new ArrayList<>();

                                        for (int i = 0; i < lstDto.size(); i++) {

                                            _lstColorCodes.add(lstDto.get(i).getColorCodes());

                                        }
                                        for (int i = 0; i < _lstColorCodes.size(); i++) {
                                            for (int j = 0; j < _lstColorCodes.get(i).size(); j++) {
                                                colorCodes.add(_lstColorCodes.get(i).get(j).getColorcode());
                                            }
                                        }


                                        for (int i = 0; i < lstDto.size(); i++) {

                                            _lstSloc.add(lstDto.get(i).getSLOC());

                                        }
                                        for (int i = 0; i < _lstSloc.size(); i++) {
                                            for (int j = 0; j < _lstSloc.get(i).size(); j++) {
                                                sLoc.add(_lstSloc.get(i).get(j).getSLOCcode());
                                                if (_lstSloc.get(i).get(j).getIsDefault().equals("True")) {
                                                    DefaultSloc = _lstSloc.get(i).get(j).getSLOCcode();

                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_03",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
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
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_01",getActivity());
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
                                    common.showAlertType(owmsExceptionMessage, getActivity(),getContext());
                                    return;
                                }
                            } else {
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {

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
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_02",getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }

    // Validating the Box Quantity..If it returns true the Location was released if not it will scan each SKU in cyclecount details
    public void ValidateLocationBoxQuantity() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.CycleCount,getActivity());
            CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setLocation(etLocation.getText().toString());
            cycleCountDTO.setSelectedCCName(CCname);
            cycleCountDTO.setBoxQty(Double.parseDouble(etBoxQty.getText().toString()));
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidateLocationBoxQuantity(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"003_01",getActivity());
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

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if(core!=null){
                                if((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(),getContext());

                                    }
                                }else {
                                    List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    if (core.getEntityObject() != null) {

                                        CycleCountDTO _oCycleCountDTO = null;
                                        if (_lResult.size() > 0) {
                                            for (int i = 0; i < _lResult.size(); i++) {
                                                _oCycleCountDTO = new CycleCountDTO(_lResult.get(i).entrySet());
                                            }

                                        }

                                        if (_oCycleCountDTO.getSatisfiedBoxQty()) {
                                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                            ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                                            etLocation.setText("");
                                            etBoxQty.setText("");

                                            ProgressDialogUtils.closeProgressDialog();
                                            common.showUserDefinedAlertType(errorMessages.EMC_0014,getActivity(),getContext(),"Success");
                                        } else {


                                            GetCCdetails();
                                            cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                            ivScanLocation.setImageResource(R.drawable.fullscreen_img);

                                            etLocation.setText("");
                                            etBoxQty.setText("");

                                            ProgressDialogUtils.closeProgressDialog();
                                            common.showUserDefinedAlertType(errorMessages.EMC_0015,getActivity(),getContext(),"Warning");



                                        }
                                        //FetchLocationForCycleCount();

                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"003_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"003_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"003_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }



    // Fetching the suggested Locations based on User and Cyclecount

    public void FetchLocationForCycleCount() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.CycleCount,getActivity());
            CycleCountDTO cycleCountDTO= new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setSelectedCCName(CCname);
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.FetchLocationForCycleCount(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_01",getActivity());
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

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if(core!=null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {
                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                        if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                            DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage().toString(), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:

                                                            break;
                                                        case DialogInterface.BUTTON_NEGATIVE:

                                                            break;
                                                    }
                                                }
                                            });

                                            return;
                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    }
                                } else {

                                    LinkedTreeMap<String, String> _lSuggestedLoc = new LinkedTreeMap<String, String>();
                                    _lSuggestedLoc = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _lSuggestedLoc.entrySet()) {
                                        if (entry.getKey().equals("Location")) {
                                            // DialogUtils.showAlertDialog(getActivity(),entry.getValue());
                                            lblSuggestedLoc.setText(entry.getValue());
                                            ProgressDialogUtils.closeProgressDialog();
                                        }

                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        }catch (Exception ex)
        {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }


    public void BlockLocationForCycleCount() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.CycleCount,getActivity());
            CycleCountDTO cycleCountDTO= new CycleCountDTO();
            cycleCountDTO.setUserId(userId);
            cycleCountDTO.setSelectedCCName(CCname);
            cycleCountDTO.setLocation(etLocation.getText().toString());
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.BlockLocationForCycleCount(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"005_01",getActivity());
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

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if(core!=null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {
                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                        if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                            DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage().toString(), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:

                                                            break;
                                                        case DialogInterface.BUTTON_NEGATIVE:

                                                            break;
                                                    }
                                                }
                                            });

                                            return;
                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.warning_img);
                                        etLocation.setText("");
                                    }
                                } else {
                                    List<LinkedTreeMap<?, ?>> _lLocation = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lLocation = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    CycleCountDTO _oCycleCountDTO = null;


                                    for (int i = 0; i < _lLocation.size(); i++) {

                                        _oCycleCountDTO = new CycleCountDTO(_lLocation.get(i).entrySet());

                                    }

                                    ProgressDialogUtils.closeProgressDialog();

                                    cvScanLocation.setCardBackgroundColor(Color.WHITE);
                                    ivScanLocation.setImageResource(R.drawable.check);

                                    isLocationFlagged = _oCycleCountDTO.getLocationFlagged();

                                    if(isLocationFlagged.equalsIgnoreCase("Yes")){

                                        GetCCdetails();

                                    }else {

                                        tvLocationsScanned.setText(_oCycleCountDTO.getTotalNoOfLocationsScanned() + "/" + _oCycleCountDTO.getTotalNoOfLocationsToScan());

                                    }

                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"005_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
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
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"005_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        }catch (Exception ex)
        {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"005_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        CCname= spinnerSelectCycleCount.getSelectedItem().toString();
       /* if(!isStockTakeMode) {
            FetchLocationForCycleCount();
        }*/
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}