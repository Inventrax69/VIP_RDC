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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.cipherlab.barcode.GeneralString;
import com.example.karthikm.merlinwmscipher_vip_rdc.Activities.MainActivity;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InboundDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InventoryDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
import com.example.karthikm.merlinwmscipher_vip_rdc.adapters.ExpandableListAdapter;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.Common;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.EndpointConstants;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.ErrorMessages;
import com.example.karthikm.merlinwmscipher_vip_rdc.interfaces.ApiInterface;
import com.example.karthikm.merlinwmscipher_vip_rdc.model.MaterialInfo;
import com.example.karthikm.merlinwmscipher_vip_rdc.model.PalletInfo;
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
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PutawayDetailsWOPalletFragment extends Fragment implements View.OnClickListener,BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private View rootView;
    private static final String classCode = "API_FRAG_012";
    private ScanValidator scanValidator;
    private ImageView ivScanLocation, ivScanSku;
    private Button  btnBinFull, btnCloseTwo,btnPutawayComplete;
    private CardView cvScanPallet, cvScanLocation, cvScanSku;
    private TextView lblSuggestedText, lbldesc,lblCount;
    String scanner = null,SuggestedId=null,TenantID=null,WareHouseID=null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private String Materialcode=null,SKU=null,Qty="0",inboundID="",Storefno="";
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private WMSCoreMessage core;
    private Common common=null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    List<String> lstLocMaterialQty=null;
    String userId =null;
    public boolean IsPutaway=false,IsFetchSuggestions=true,isLoactionScaned=false;
    ExpandableListAdapter listAdapter=null;
    private SoundUtils soundUtils;
    public List<String> lstPalletnumberHeader=null;
    public  int BoxCount,RemaningQty=0,BalancedQty=0;
    EditText etSerial,etLocation,etSerial1,etLocation1;
    LinearLayout newLocation;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public PutawayDetailsWOPalletFragment(){}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_putaway_details_wo_pallet, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    /// Loading form Controls
    private void loadFormControls() {

        exceptionLoggerUtils= new ExceptionLoggerUtils();
        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);

        btnBinFull = (Button) rootView.findViewById(R.id.btnBinFull);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnPutawayComplete=(Button) rootView.findViewById(R.id.btnPutawayComplete) ;
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);

        etSerial = (EditText) rootView.findViewById(R.id.etSerial);
        etLocation = (EditText) rootView.findViewById(R.id.etLocation);

        etSerial1 = (EditText) rootView.findViewById(R.id.etSerial1);
        etLocation1 = (EditText) rootView.findViewById(R.id.etLocation1);

        newLocation = (LinearLayout) rootView.findViewById(R.id.newLocation);

        lblSuggestedText = (TextView) rootView.findViewById(R.id.lblSuggestedText);
        lbldesc = (TextView) rootView.findViewById(R.id.lblScannedSkuItem);
        lblCount=(TextView)rootView.findViewById(R.id.lblCount);
        btnCloseTwo.setOnClickListener(this);
        btnBinFull.setOnClickListener(this);
        btnPutawayComplete.setOnClickListener(this);
        common = new Common();
        gson = new GsonBuilder().create();
        errorMessages = new ErrorMessages();
        soundUtils= new SoundUtils();


        if(getArguments()!=null) {
            if (getArguments().getString("SuggestedId") != null) {
                try {

                    SuggestedId = getArguments().getString("SuggestedId");

                } catch (Exception ex) {
                    //
                }
            }

            if (getArguments().getString("Storefno") != null) {
                try {

                    Storefno = getArguments().getString("Storefno");

                } catch (Exception ex) {
                    //
                }
            }


            if (getArguments().getString("tenantID") != null) {
                try {

                    TenantID = getArguments().getString("tenantID");


                } catch (Exception ex) {
                    //
                }

            }
            if (getArguments().getString("WareHouseID") != null) {
                try {

                    WareHouseID = getArguments().getString("WareHouseID");

                } catch (Exception ex) {
                    //
                }


            }

            if (getArguments().getString("inboundID") != null) {
                try {

                    inboundID = getArguments().getString("inboundID");

                } catch (Exception ex) {
                    //
                }

            }
        }
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

    }


    //Button clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnPutawayComplete:

                if(etSerial.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Warning");
                }else{
                    if(!isLoactionScaned){
                        common.showUserDefinedAlertType(errorMessages.EMC_0031,getActivity(),getContext(),"Warning");
                    }else{
                        if(BoxCount > (int)Double.parseDouble(Qty)){
                            DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                                    "The putaway contains" + " " + (BoxCount - (int)Double.parseDouble(Qty)) + " " + "more boxes. Do you wish to continue?", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    PutAwayComplete();
                                                    Common.setIsPopupActive(false);
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    Common.setIsPopupActive(false);
                                                    break;
                                            }

                                        }
                                    });
                            Common.setIsPopupActive(true);
                            ProgressDialogUtils.closeProgressDialog();
                        }else{
                            common.showUserDefinedAlertType(errorMessages.EMC_0056,getActivity(),getContext(),"Warning");
                        }
                    }
                }
/*                if(!isLoactionScaned && !etSerial.getText().toString().isEmpty()){
                    common.showUserDefinedAlertType(errorMessages.EMC_0054,getActivity(),getContext(),"Warning");
                }else{
                    if(BoxCount > (int)Double.parseDouble(Qty) && !etSerial.getText().toString().isEmpty() && isLoactionScaned){
                        DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                                "The putaway contains" + " " + BalancedQty + " " + "more boxes. Do you wish to continue?", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case DialogInterface.BUTTON_POSITIVE:
                                                PutAwayComplete();
                                                Common.setIsPopupActive(false);
                                                break;

                                            case DialogInterface.BUTTON_NEGATIVE:
                                                Common.setIsPopupActive(false);
                                                break;
                                        }

                                    }
                                });
                        Common.setIsPopupActive(true);
                        ProgressDialogUtils.closeProgressDialog();
                    }else if(BoxCount == (int)Double.parseDouble(Qty) && !etSerial.getText().toString().isEmpty()){
                        common.showUserDefinedAlertType(errorMessages.EMC_0056,getActivity(),getContext(),"Warning");
                    }
                    else{
                        if(etSerial.getText().toString().isEmpty()){
                            common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Warning");
                        }else{
                            common.showUserDefinedAlertType(errorMessages.EMC_0055,getActivity(),getContext(),"Warning");
                        }
                    }
                }*/

                break;
            case R.id.btnCloseTwo:
                    FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                    break;
            case R.id.btnBinFull:
                    Common.setIsPopupActive(true);

                    if(etSerial.getText().toString().isEmpty()){
                        common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Warning");
                    }else{
                        if(isLoactionScaned){
                            common.showUserDefinedAlertType(errorMessages.EMC_0054,getActivity(),getContext(),"Warning");
                        }else{

                            if(BoxCount > (int)Double.parseDouble(Qty)){
                                DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                                        "Are you sure you want to complete the Bin?", new DialogInterface.OnClickListener() {

                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                switch (which) {
                                                    case DialogInterface.BUTTON_POSITIVE:
                                                        UpdateBincomplete();
                                                        Common.setIsPopupActive(false);
                                                        break;

                                                    case DialogInterface.BUTTON_NEGATIVE:
                                                        Common.setIsPopupActive(false);
                                                        break;
                                                }

                                            }
                                        });
                                Common.setIsPopupActive(true);
                                ProgressDialogUtils.closeProgressDialog();
                            }else{
                                common.showUserDefinedAlertType(errorMessages.EMC_0056,getActivity(),getContext(),"Warning");
                            }
                        }
                    }
/*                    if(isLoactionScaned && !etSerial.getText().toString().isEmpty()){
                        common.showUserDefinedAlertType(errorMessages.EMC_0054,getActivity(),getContext(),"Warning");
                    }else{
                        if(BoxCount > (int)Double.parseDouble(Qty)){
                            DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                                    "Are you sure you want to complete the Bin?", new DialogInterface.OnClickListener() {

                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {

                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    UpdateBincomplete();
                                                    Common.setIsPopupActive(false);
                                                    break;

                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    Common.setIsPopupActive(false);
                                                    break;
                                            }

                                        }
                                    });
                            Common.setIsPopupActive(true);
                            ProgressDialogUtils.closeProgressDialog();
                        }else if(BoxCount == (int)Double.parseDouble(Qty) && !etSerial.getText().toString().isEmpty()){
                            common.showUserDefinedAlertType(errorMessages.EMC_0056,getActivity(),getContext(),"Warning");
                        }
                        else{
                            if(etSerial.getText().toString().isEmpty()){
                                common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Warning");
                            }else{
                                common.showUserDefinedAlertType(errorMessages.EMC_0055,getActivity(),getContext(),"Warning");
                            }
                        }
                    }*/

                    break;
        }

    }


    // Honeywell Barcode read event
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

            Map<String, Object> properties = new HashMap<>();
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
        if (scannedData != null && !Common.isPopupActive) {
            if (ScanValidator.IsLocationScanned(scannedData)) {
                if(!etSerial.getText().toString().isEmpty()){
                    ValidateLocationCode(scannedData);
                }else{
                    common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Error");
                }
            } else if (ScanValidator.IsItemScanned(scannedData)) {
                Materialcode = scannedData;
                UpdatePutAwayItem();
            }
        }
    }

    public void ValidateLocationCode(final String scannedData) {
        try {
            if(etLocation.getText().toString().equals(""))
            {
                common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");
                return;
            }

            WMSCoreMessage message;
            message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setContainerCode("");
            inventoryDTO.setLocationCode(scannedData);
            inventoryDTO.setRSN(Materialcode);
            inventoryDTO.setMaterialCode(SKU);
            inventoryDTO.setQuantity(Qty);
            inventoryDTO.setTenantID(TenantID);
            inventoryDTO.setSuggestionID(SuggestedId);
            inventoryDTO.setWareHouseID(WareHouseID);
            inventoryDTO.setUserId(Integer.parseInt(userId));
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            Log.v("ABCDE",new Gson().toJson(message));


            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpdatePutAwayItemWithoutPallet(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"008_01",getActivity());
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
                        if(response.body()!=null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if (core != null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.warning_img);
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    }

                                    etLocation.setText("");
                                //    etSerial.setText("");
                                //   lblSuggestedText.setText("");

                                    return;
                                } else {
                                    List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    InventoryDTO inventoryDTO=null;
                                    inventoryDTO=new InventoryDTO(_lResult.get(0).entrySet());

                                    int availableQty = (int)(Double.parseDouble(inventoryDTO.getQuantity())/Double.parseDouble(inventoryDTO.getMOP()));
                                    availableQty=  availableQty+(int)(Double.parseDouble(Qty));
                                    lblCount.setText((availableQty)+ " / " + BoxCount);

                                    Qty=String.valueOf(availableQty);

                                    if(!etLocation.getText().toString().equals(scannedData)){
                                        newLocation.setVisibility(View.VISIBLE);
                                        etSerial1.setText(etSerial.getText().toString());
                                        etLocation1.setText(scannedData);
                                    }else{
                                        newLocation.setVisibility(View.INVISIBLE);
                                    }
                                    cvScanLocation.setCardBackgroundColor(Color.WHITE);
                                    ivScanLocation.setImageResource(R.drawable.check);
                                    isLoactionScaned=true;
                                    ProgressDialogUtils.closeProgressDialog();
                                    Common.setIsPopupActive(false);

/*                                    for (Map.Entry<String, String> entry : _lResult.entrySet()) {
                                        if (entry.getKey().equals("Result")) {
                                            String Result = entry.getValue();
                                            if (Result.equals("0")) {
                                                common.showUserDefinedAlertType(errorMessages.EMC_0031,getActivity(),getContext(),"Error");

                                                etLocation.setText("");
                                                cvScanLocation.setCardBackgroundColor(Color.WHITE);
                                                ivScanLocation.setImageResource(R.drawable.warning_img);
                                                ProgressDialogUtils.closeProgressDialog();
                                                return;
                                            } else {
                                                cvScanLocation.setCardBackgroundColor(Color.WHITE);
                                                ivScanLocation.setImageResource(R.drawable.check);
                                                ProgressDialogUtils.closeProgressDialog();
                                                etLocation.setEnabled(false);
                                            }
                                        }
                                    }*/
                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0021);
                                return;
                            }
                        }
                    }
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                        return;
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"008_02",getActivity());
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
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"008_03",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
            return;
        }
    }

    public void PutAwayComplete() {

        WMSCoreMessage message = new WMSCoreMessage();
        message= common.SetAuthentication(EndpointConstants.Inbound,getContext());
        InboundDTO inboundDTO = new InboundDTO();
        inboundDTO.setInboundID(inboundID);
        inboundDTO.setStoreRefNo(Storefno);
        inboundDTO.setSKU(Materialcode);
        message.setEntityObject(inboundDTO);



        Call<String> call = null;
        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        try {

            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.PutAwayComplete(message);


        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_01",getActivity());
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
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.showUserDefinedAlertType(errorMessages.EMC_0030,getActivity(),getContext(),"Error");
                                            return;
                                        } else {
                                            ProgressDialogUtils.closeProgressDialog();
                                            ClearFields();
                                        }
                                    }
                                }
                            }

                        }

                    } catch (Exception ex) {
                        try {
                            ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_02",getActivity());
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
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_03",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");

        }
    }

    private void ClearFields() {
        etSerial.setText("");
        etSerial1.setText("");
        etLocation.setText("");
        etLocation1.setText("");
        lblSuggestedText.setText("");
        lblCount.setText("");
        BoxCount=0;
        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);
        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);
        Qty="0";
    }

    String InbondId="";

    public void UpdateBincomplete() {
        if(etLocation.getText().toString().isEmpty())
        {
            Common.setIsPopupActive(true);
            soundUtils.alertError(getActivity(), getContext());
            DialogUtils.showAlertDialog(getActivity(), "Error", errorMessages.EMC_0007, R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Common.setIsPopupActive(false);
                            break;
                    }
                }
            });
            return;
        }
        WMSCoreMessage message = new WMSCoreMessage();
        message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setContainerCode("");
        inventoryDTO.setLocationCode(etLocation.getText().toString());
        inventoryDTO.setRSN(Materialcode);
        inventoryDTO.setMaterialCode(SKU);
        inventoryDTO.setQuantity("1");
        inventoryDTO.setTenantID(TenantID);
        inventoryDTO.setSuggestionID(SuggestedId);
        inventoryDTO.setWareHouseID(WareHouseID);
        inventoryDTO.setStoreRefNo(Storefno);
        inventoryDTO.setUserId(Integer.parseInt(userId));
        inventoryDTO.setInboundID(""+(int)Double.parseDouble(inboundID));
        message.setEntityObject(inventoryDTO);



        Call<String> call = null;
        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        try {
            // Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.BincompleteWithoutPallet(message);
            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }

        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_01",getActivity());
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
                                List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                InventoryDTO inventoryDTO=null;

                                inventoryDTO=new InventoryDTO(_lInventory.get(_lInventory.size()-1).entrySet());
                                SKU=Materialcode.split("[-]", 2)[0];
                                lblSuggestedText.setText(Materialcode.split("[-]", 2)[0]);
                                etSerial.setText(Materialcode.split("[-]", 2)[1]);
                                cvScanSku.setCardBackgroundColor(Color.WHITE);
                                ivScanSku.setImageResource(R.drawable.check);
                                newLocation.setVisibility(View.INVISIBLE);
                                etLocation.setText(inventoryDTO.getLocationCode());
                                Qty="0";
                                if(inventoryDTO.getBoxCount()!=null)
                                    lblCount.setText((int)(Double.parseDouble(Qty)) + " / " + (int) Double.parseDouble(inventoryDTO.getBoxCount()));
                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                                isLoactionScaned=false;
                                WareHouseID = inventoryDTO.getWareHouseID();
                                SuggestedId = inventoryDTO.getSuggestionID();
                                InbondId = inventoryDTO.getInboundID();
                                BoxCount = (int)Double.parseDouble(inventoryDTO.getBoxCount());

                                ProgressDialogUtils.closeProgressDialog();
                                Common.setIsPopupActive(false);

                            }

                        }

                    } catch (Exception ex) {
                        try {
                            ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_02",getActivity());
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
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_03",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");

        }
    }

    public void UpdatePutAwayItem() {
        Common.setIsPopupActive(true);
        if(Materialcode==null)
        {
            common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Error");
            return;
        }
        WMSCoreMessage message = new WMSCoreMessage();
        message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setContainerCode("");
        inventoryDTO.setLocationCode("");
        inventoryDTO.setRSN(Materialcode);
        inventoryDTO.setMaterialCode(SKU);
        inventoryDTO.setQuantity("1");
        inventoryDTO.setTenantID(TenantID);
        inventoryDTO.setSuggestionID(SuggestedId);
        inventoryDTO.setWareHouseID(WareHouseID);
        inventoryDTO.setInboundID(""+(int)Double.parseDouble(inboundID));
        inventoryDTO.setUserId(Integer.parseInt(userId));
        message.setEntityObject(inventoryDTO);



        Call<String> call = null;
        ApiInterface apiService =
                RestService.getClient().create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.GetPutawaySuggestionsBasedOnSerialNo(message);
            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }kn 0.

        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_01",getActivity());
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

                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if (core != null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;

                                    for (int i = 0; i < _lExceptions.size(); i++) {
                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.warning_img);
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        etSerial.setText("");
                                    }

                                } else {

                                    List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    InventoryDTO inventoryDTO=null;
                                    Log.v("ABCDE_B1",new Gson().toJson(core.getEntityObject()));

                                    inventoryDTO=new InventoryDTO(_lResult.get(_lResult.size()-1).entrySet());
                                    SKU=Materialcode.split("[-]", 2)[0];
                                    lblSuggestedText.setText(Materialcode.split("[-]", 2)[0]);
                                    etSerial.setText(Materialcode.split("[-]", 2)[1]);
                                    cvScanSku.setCardBackgroundColor(Color.WHITE);
                                    ivScanSku.setImageResource(R.drawable.check);
                                    newLocation.setVisibility(View.INVISIBLE);
                                    etLocation.setText(inventoryDTO.getLocationCode());
                                    Qty=inventoryDTO.getTotalScannedQty();
                                    if(inventoryDTO.getBoxCount()!=null)
                                    lblCount.setText((int)(Double.parseDouble(Qty)) + " / " + (int) Double.parseDouble(inventoryDTO.getBoxCount()));
                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                    ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                                    isLoactionScaned=false;
                                    InbondId = inventoryDTO.getInboundID();
                                    WareHouseID = inventoryDTO.getWareHouseID();
                                    SuggestedId = inventoryDTO.getSuggestionID();
                                    BoxCount = (int)Double.parseDouble(inventoryDTO.getBoxCount());

                                    ProgressDialogUtils.closeProgressDialog();
                                    Common.setIsPopupActive(false);

                                }
                            }else{
                                ProgressDialogUtils.closeProgressDialog();
                                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0021);
                                return;
                            }
                        }
                    }
                    catch (Exception ex) {
                        try {
                            ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_02",getActivity());
                            logException();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0057,getActivity(),getContext(),"Error");
                    }
                }
                @Override
                public void onFailure(Call<String> call, Throwable throwable) {

                    ProgressDialogUtils.closeProgressDialog();
                    common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");
                }
            });
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_03",getActivity());
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

            String textFromFile = ExceptionLoggerUtils.readFromFile(getActivity());

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
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();



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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_putaway));
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
