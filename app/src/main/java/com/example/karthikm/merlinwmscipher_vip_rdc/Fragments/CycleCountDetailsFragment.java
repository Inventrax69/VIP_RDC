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
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.CycleCountDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InventoryDTO;
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

import org.json.JSONArray;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Padmaja on 06/27/2018.
 */

public class CycleCountDetailsFragment extends Fragment implements View.OnClickListener ,BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_005";
    private View rootView;
    DialogUtils dialogUtils;

    private Button btnBinComplete,btnClose,btnUpdate;
    private TextView lblCycleCount, lblScannedSku, tvScanLocation, tvSuggestedLoc,lblSuggestedLoc,tvQuantity;
    private CardView cvScanLocation,cvScanSKU;
    private SearchableSpinner spinnerSelectSloc,spinnerSelectColor;
    private TextInputLayout txtInputLayoutMOP,txtInputLayoutMRP,txtInputLayoutBatch,txtInputLayoutRSN,
            txtInputLayoutColorCC,txtInputLayoutSLocCC,txtInputLayoutMOPCC,
            txtInputLayoutMRPCC,txtInputLayoutBatchCC,txtInputLayoutRSNCC;
    private EditText etMOP,etMRP,etBatch,etRSN,
            etColorCC,etSLocCC,etMOPCC,etMRPCC,etBatchCC,etRSNCC;
    private ImageView ivScanLocation,ivScanSKU;
    private RelativeLayout rlccViewOne,rlccViewTwo;

    private ScanValidator scanValidator;
    String scanner = null;
    String getScanner = null;
    private String location=null;
    private double CapturedQty=0;
    private IntentFilter filter;
    private Gson gson;
    private WMSCoreMessage core;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String Materialcode= null;
    private Common common=null;
    ArrayList<String> sloc;
    ArrayList<String> color;
    String Sloc= null,Selectedcolor= null;
    String userId;
    private boolean isStockTake,isUserConfirmedDo;
    private String stockTakeLocation;
    private String DefaultSloc=null,Location=null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private SoundUtils soundUtils;
    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };
    public CycleCountDetailsFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView  = inflater.inflate(R.layout.fragment_cyclecount_details,container,false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        rlccViewOne = (RelativeLayout) rootView.findViewById(R.id.rlccViewOne);
        rlccViewTwo = (RelativeLayout) rootView.findViewById(R.id.rlccViewTwo);

        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanSKU = (CardView) rootView.findViewById(R.id.cvScanSKU);

        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanSKU = (ImageView) rootView.findViewById(R.id.ivScanSKU);

        spinnerSelectSloc = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectSloc);
        spinnerSelectColor = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectColor);
        spinnerSelectSloc.setOnItemSelectedListener(this);
        spinnerSelectColor.setOnItemSelectedListener(this);
        //DrawableCompat.setTint(isDockScanned.getDrawable(), ContextCompat.getColor(getContext(), R.color.green));
        btnBinComplete = (Button) rootView.findViewById(R.id.btnBinComplete);
        btnClose = (Button)rootView.findViewById(R.id.btnClose);
        btnUpdate = (Button) rootView.findViewById(R.id.btnUpdate);

        lblCycleCount = (TextView) rootView.findViewById(R.id.lblCycleCount);
        lblSuggestedLoc = (TextView) rootView.findViewById(R.id.lblSuggestedLoc);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        tvScanLocation = (TextView) rootView.findViewById(R.id.tvScanLocation);
        tvSuggestedLoc = (TextView) rootView.findViewById(R.id.tvSuggestedLoc);
        tvQuantity = (TextView) rootView.findViewById(R.id.tvQuantity);
        // For Stock take
        etMOP = (EditText) rootView.findViewById(R.id.etMOP);
        etMRP = (EditText) rootView.findViewById(R.id.etMRP);
        etBatch = (EditText) rootView.findViewById(R.id.etBatch);
        etRSN = (EditText) rootView.findViewById(R.id.etRSN);

        txtInputLayoutBatch = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBatch);
        txtInputLayoutMOP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMOP);
        txtInputLayoutMRP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMRP);
        txtInputLayoutRSN = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutRSN);

        // For Cycle count
        etMOPCC = (EditText) rootView.findViewById(R.id.etMOPCC);
        etMRPCC = (EditText) rootView.findViewById(R.id.etMRPCC);
        etBatchCC = (EditText) rootView.findViewById(R.id.etBatchCC);
        etRSNCC = (EditText) rootView.findViewById(R.id.etRSNCC);
        etColorCC = (EditText) rootView.findViewById(R.id.etColorCC);
        etSLocCC = (EditText) rootView.findViewById(R.id.etSLocCC);

        txtInputLayoutBatchCC = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutBatchCC);
        txtInputLayoutMOPCC = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMOPCC);
        txtInputLayoutMRPCC = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMRPCC);
        txtInputLayoutRSNCC = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutRSNCC);
        txtInputLayoutColorCC = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutColorCC);
        txtInputLayoutSLocCC = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSLocCC);

        lblCycleCount.setText(getArguments().getString("CCname"));
        location=getArguments().getString("Loc");
        isStockTake = getArguments().getBoolean("isStockTake");
        DefaultSloc= getArguments().getString("DefaultSLOC");
        tvSuggestedLoc.setVisibility(View.GONE);
        lblSuggestedLoc.setVisibility(View.GONE);
        etRSN.setEnabled(false);
        soundUtils = new SoundUtils();

        if(isStockTake == true){
            rlccViewOne.setVisibility(View.VISIBLE);
            rlccViewTwo.setVisibility(View.GONE);

        }else {
            rlccViewOne.setVisibility(View.GONE);
            rlccViewTwo.setVisibility(View.VISIBLE);
            tvSuggestedLoc.setVisibility(View.GONE);
            // tvSuggestedLoc.setVisibility(View.VISIBLE);
            lblSuggestedLoc.setVisibility(View.GONE);
            //lblSuggestedLoc.setText(location);

        }

        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        gson = new GsonBuilder().create();
        common= new Common();
        btnBinComplete.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);

        // added by padmaja on 03/07/2018
        sloc= new ArrayList<>();
        color= new ArrayList<>();

        // To Get SLoc
        if(getArguments().getString("SLOC")!=null) {
            try {

                JSONArray jsonArray = new JSONArray(getArguments().getString("SLOC").toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    sloc.add(jsonArray.getString(i));
                }
            }catch (Exception ex)
            {

            }

        }

        // To Get Color
        if(getArguments().getString("Color")!=null) {
            try {

                JSONArray jsonArray = new JSONArray(getArguments().getString("Color").toString());

                for (int i = 0; i < jsonArray.length(); i++) {
                    color.add(jsonArray.getString(i));
                }
            } catch (Exception ex) {

            }
        }
        if (getArguments() != null) {


            if (getArguments().getString("Loc") != null) {
                try {

                    Location= getArguments().getString("Loc");


                } catch (Exception ex) {

                }

            }
        }

        ArrayAdapter arrayAdapterSLoc = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, sloc);
        spinnerSelectSloc.setAdapter(arrayAdapterSLoc);
        int spinnerPosition = arrayAdapterSLoc.getPosition(DefaultSloc);

        //set the default according to value
        spinnerSelectSloc.setSelection(spinnerPosition);

        ArrayAdapter arrayAdapterColor = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, color);
        spinnerSelectColor.setAdapter(arrayAdapterColor);

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
        switch (v.getId()){
            case R.id.btnBinComplete:
                DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                        "Are you sure you want to complete the Bin?", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        Bincomplete();
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

                break;

            case R.id.btnUpdate:
                isUserConfirmedDo=true;
                stockTakeCapture();
                break;

            case R.id.btnClose:
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
        if (scannedData != null && !common.isPopupActive) {
            if (scanValidator.IsLocationScanned(scannedData)) {

                tvScanLocation.setText(scannedData);
                if(!Location.equals(tvScanLocation.getText()))
                {
                    tvScanLocation.setText("Scan Location");
                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                    ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                    common.showUserDefinedAlertType(errorMessages.EMC_0049.replace("[Location]",Location),getActivity(),getContext(),"Error");
                    return;

                }
                if (isStockTake == true) {

                    cvScanLocation.setCardBackgroundColor(Color.WHITE);
                    ivScanLocation.setImageResource(R.drawable.check);

                } else {


                    cvScanLocation.setCardBackgroundColor(Color.WHITE);
                    ivScanLocation.setImageResource(R.drawable.check);

                }


            } else if (ScanValidator.IsItemScanned(scannedData)) {
                if(tvScanLocation.getText().toString().equals("Scan Location"))
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");

                    return;
                }

                if(isStockTake == true) {
                    if(!lblScannedSku.getText().equals(""))
                    {
                        if(!scannedData.split("[-]", 2)[0].equals(lblScannedSku.getText().toString())) {
                            ClearAllFields();
                            common.showUserDefinedAlertType(errorMessages.EMC_0008,getActivity(),getContext(),"Error");
                            return;
                        }
                    }
                    lblScannedSku.setText(scannedData.split("[-]", 2)[0]);
                    etRSN.setText(scannedData.split("[-]", 2)[1]);
                    etRSN.setEnabled(false);
                    Materialcode = scannedData;
                    isUserConfirmedDo=false;
                    stockTakeCapture();
                }else {

                    lblScannedSku.setText(scannedData.split("[-]", 2)[0]);
                    isUserConfirmedDo=false;
                    etRSNCC.setText(scannedData.split("[-]", 2)[1]);
                    etRSNCC.setEnabled(false);
                    Materialcode = scannedData;
                    stockTakeCapture();

                }
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

    public void stockTakeCapture()
    {
        try {
            common.setIsPopupActive(true);
            if(isStockTake == true) {
                if (etMOP.getText().toString().equals("") || etMOP.getText().toString().equals("0")) {

                    cvScanSKU.setCardBackgroundColor(Color.WHITE);
                    ivScanSKU.setImageResource(R.drawable.warning_img);
                    etRSN.setText("");
                    common.showUserDefinedAlertType(errorMessages.EMC_0009,getActivity(),getContext(),"Error");
                    return;
                }
                if (etMRP.getText().toString().equals("") || etMRP.getText().toString().equals("0")) {

                    cvScanSKU.setCardBackgroundColor(Color.WHITE);
                    ivScanSKU.setImageResource(R.drawable.warning_img);
                    etRSN.setText("");
                    common.showUserDefinedAlertType(errorMessages.EMC_0010,getActivity(),getContext(),"Error");

                    return;
                }
               /* if (etBatch.getText().toString().equals("") || etBatch.getText().toString().equals("0")) {

                    cvScanSKU.setCardBackgroundColor(Color.WHITE);
                    ivScanSKU.setImageResource(R.drawable.warning_img);
                    etRSN.setText("");
                    DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0011);
                    return;
                }*/
            }

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.CycleCount,getActivity());
            CycleCountDTO cycleCountDTO = new CycleCountDTO();

            if(isStockTake == true){
                stockTakeLocation = tvScanLocation.getText().toString();
                cycleCountDTO.setLocation(stockTakeLocation);
            }else {
                cycleCountDTO.setLocation(location);
            }
            if(isUserConfirmedDo)
            {
                cycleCountDTO.setUserConfirmReDo(isUserConfirmedDo);
            }
            cycleCountDTO.setSerialNo(Materialcode);
            cycleCountDTO.setMOP(etMOP.getText().toString());
            cycleCountDTO.setMRP(etMRP.getText().toString());
            cycleCountDTO.setSelectedCCName(lblCycleCount.getText().toString());
            cycleCountDTO.setSelectedColorCode(Selectedcolor);
            cycleCountDTO.setLocation(tvScanLocation.getText().toString());

            cycleCountDTO.setSelectedSLOC(Sloc);

            cycleCountDTO.setBatchNo(etBatch.getText().toString());
            message.setEntityObject(cycleCountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.CaptureCycleCount(message);
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
                            if (core != null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {
                                        if (isStockTake) {
                                            btnUpdate.setVisibility(View.VISIBLE);

                                        }
                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());

                                        cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSKU.setImageResource(R.drawable.warning_img);

                                        etRSNCC.setText("");
                                        if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                            DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                                                    owmsExceptionMessage.getWMSMessage(), new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            switch (which) {
                                                                case DialogInterface.BUTTON_POSITIVE:
                                                                    common.setIsPopupActive(false);
                                                                    break;

                                                                case DialogInterface.BUTTON_NEGATIVE:
                                                                    ClearFields();
                                                                    common.setIsPopupActive(false);
                                                                    break;
                                                            }


                                                        }
                                                    });
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.setIsPopupActive(true);
                                            return;
                                        }
                                        return;

                                    }


                                } else {
                                    btnUpdate.setVisibility(View.VISIBLE);
                                    ///  ClearFields();
                                    List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    InventoryDTO oInventoryDto = null;
                                    if (_lResult.size() > 0) {
                                        for (int i = 0; i < _lResult.size(); i++) {
                                            oInventoryDto = new InventoryDTO(_lResult.get(i).entrySet());
                                        }

                                        if (isStockTake == true) {
                                            cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSKU.setImageResource(R.drawable.check);
                                            btnUpdate.setVisibility(View.VISIBLE);
                                            if (!isUserConfirmedDo) {
                                                tvQuantity.setText(oInventoryDto.getTotalScannedQty());
                                            }
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.setIsPopupActive(false);
                                            //DialogUtils.showAlertDialog(getActivity(),"Success","Captured Suceessfully");
                                            return;
                                        } else {

                                            ProgressDialogUtils.closeProgressDialog();

                                            cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSKU.setImageResource(R.drawable.check);
                                            etBatch.setText(oInventoryDto.getBatchNumber());
                                            etBatchCC.setText(oInventoryDto.getBatchNumber());
                                            etMOP.setText(oInventoryDto.getMOP());
                                            etMOPCC.setText(oInventoryDto.getMOP());
                                            etMRP.setText(oInventoryDto.getMRP());
                                            etMRPCC.setText(oInventoryDto.getMRP());
                                            etColorCC.setText(oInventoryDto.getColor());
                                            etSLocCC.setText(oInventoryDto.getSLOC());

                                            btnUpdate.setVisibility(View.GONE);
                                            tvQuantity.setText(oInventoryDto.getTotalScannedQty());
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.setIsPopupActive(false);
                                            //DialogUtils.showAlertDialog(getActivity(),"Success","Captured Suceessfully",R.drawable.success);
                                            return;
                                        }
                                    }
                                }
                            }
                        }
                        catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            common.setIsPopupActive(false);
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
                    ProgressDialogUtils.closeProgressDialog();
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        }catch (Exception ex)
        {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }

    public void ClearAllFields()
    {
        etMOP.setText("");
        etBatch.setText("");
        etMRP.setText("");
        etRSN.setText("");
        tvQuantity.setText("");
        lblScannedSku.setText("");
        cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSKU.setImageResource(R.drawable.fullscreen_img);
    }
    public void ClearFields()
    {

       /* etBatch.setText("");
        etMOP.setText("");
        etMRP.setText("");*/
        etRSN.setText("");
    }


    public void Bincomplete()
    {
        try {

            if(tvScanLocation.getText().toString().equals("Scan Location"))
            {
                common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");
                return;
            }

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.CycleCount,getContext());
            CycleCountDTO cycleCountDTO = new CycleCountDTO();
            cycleCountDTO.setSelectedCCName(lblCycleCount.getText().toString());

            if(isStockTake == true){
                stockTakeLocation = tvScanLocation.getText().toString();
                cycleCountDTO.setLocation(stockTakeLocation);
            }else {
                cycleCountDTO.setLocation(location);
            }

            message.setEntityObject(cycleCountDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.MarkBinComplete(message);
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
                            if(core!=null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());


                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage,getActivity(),getContext());
                                } else {

                                    LinkedTreeMap<String, String> _lSuggestedLoc = new LinkedTreeMap<String, String>();
                                    _lSuggestedLoc = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _lSuggestedLoc.entrySet()) {
                                        if (entry.getKey().equals("Location")) {
                                            // DialogUtils.showAlertDialog(getActivity(),entry.getValue());
                                            //   lblSuggestedLoc.setText(entry.getValue());
                                        }

                                    }
                                    if (core.getEntityObject() == null) {
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showUserDefinedAlertType(errorMessages.EMC_0028,getActivity(),getContext(),"Error");
                                    } else {

                                        etMOPCC.setText("");
                                        etMRPCC.setText("");
                                        etBatchCC.setText("");
                                        etRSNCC.setText("");
                                        etSLocCC.setText("");
                                        etColorCC.setText("");
                                        etMOP.setText("");
                                        etMRP.setText("");
                                        etBatch.setText("");
                                        etRSN.setText("");
                                        tvQuantity.setText("");


                                        tvScanLocation.setText("Scan Location");
                                        lblScannedSku.setText("");
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                        ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                                        cvScanSKU.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanSKU.setImageResource(R.drawable.fullscreen_img);

                                        backToHeaderFragment();


                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showUserDefinedAlertType(errorMessages.EMC_0032,getActivity(),getContext(),"Success");
                                        return;
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
                            common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
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
        }catch (Exception ex)
        {
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



    public void backToHeaderFragment(){

        Bundle bundle = new Bundle();
        bundle.putString("CCname", lblCycleCount.getText().toString());
        CycleCountHeaderFragment cycleCountHeaderFragment = new CycleCountHeaderFragment();
        cycleCountHeaderFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, cycleCountHeaderFragment);
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
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
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

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        Sloc= spinnerSelectSloc.getSelectedItem().toString();
        Selectedcolor=spinnerSelectColor.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}