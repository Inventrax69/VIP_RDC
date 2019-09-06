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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class PutawayDetailsFragment extends Fragment implements View.OnClickListener,BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private View rootView;
    private static final String classCode = "API_FRAG_012";
    private ScanValidator scanValidator;
    private TextInputLayout txtInputLayoutPallet, txtInputLayoutLocation, txtInputLayoutSerial,
            txtInputLayoutMfgDate, txtInputLayoutMop, txtInputLayoutMRP, txtInputLayoutdColor, txtInputLayoutSLoc;
    private EditText etPallet, etLocation, etSerial, etMfgDate, etMRP, etMOP, etColor, etSLoc;
    private ImageView ivScanLocation, ivScanPallet, ivScanSku;
    private Button  btnPalletComplete, btnBinFull, btnCloseTwo,btnPutawayComplete;
    private CardView cvScanPallet, cvScanLocation, cvScanSku;
    private TextView lblSuggestedText, lbldesc,lblCount;

    String scanner = null,SuggestedId=null,TenantID=null,WareHouseID=null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private String Materialcode=null,SKU=null;

    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private WMSCoreMessage core;
    private Common common=null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    List<String> lstLocMaterialQty=null;
    String userId =null;
    public boolean IsPutaway=false,IsFetchSuggestions=true;
    ExpandableListAdapter listAdapter=null;
    private SoundUtils soundUtils;
    public List<String> lstPalletnumberHeader=null;
    public  int BoxCount,RemaningQty=0,BalancedQty=0;
    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public PutawayDetailsFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_putaway_details, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    /// Loading form Controls
    private void loadFormControls() {

        exceptionLoggerUtils= new ExceptionLoggerUtils();
        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);
        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutSerial = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSerial);
        txtInputLayoutMfgDate = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMfgDate);
        txtInputLayoutMop = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMop);
        txtInputLayoutMRP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMRP);
        txtInputLayoutdColor = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutdColor);
        txtInputLayoutSLoc = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSLoc);

        etLocation = (EditText) rootView.findViewById(R.id.etLocation);
        etPallet = (EditText) rootView.findViewById(R.id.etPallet);
        etSerial = (EditText) rootView.findViewById(R.id.etSerial);
        etMfgDate = (EditText) rootView.findViewById(R.id.etMfgDate);
        etMOP = (EditText) rootView.findViewById(R.id.etMOP);
        etMRP = (EditText) rootView.findViewById(R.id.etMRP);
        etColor = (EditText) rootView.findViewById(R.id.etColor);
        etSLoc = (EditText) rootView.findViewById(R.id.etSLoc);

        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);

        btnPalletComplete = (Button) rootView.findViewById(R.id.btnPalletComplete);
        btnBinFull = (Button) rootView.findViewById(R.id.btnBinFull);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnPutawayComplete=(Button) rootView.findViewById(R.id.btnPutawayComplete) ;
        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);

        lblSuggestedText = (TextView) rootView.findViewById(R.id.lblSuggestedText);
        lbldesc = (TextView) rootView.findViewById(R.id.lblScannedSkuItem);
        lblCount=(TextView)rootView.findViewById(R.id.lblCount);
        btnCloseTwo.setOnClickListener(this);
        btnPalletComplete.setOnClickListener(this);
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

                }

            }

            if (getArguments().getString("TenantID") != null) {
                try {

                    TenantID = getArguments().getString("TenantID");

                } catch (Exception ex) {

                }

            }  if (getArguments().getString("WareHouseID") != null) {
                try {

                    WareHouseID = getArguments().getString("WareHouseID");

                } catch (Exception ex) {

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

            case R.id.btnPalletComplete:
                IsFetchSuggestions=false;
                if(!lblCount.getText().toString().isEmpty()) {
                    BalancedQty = Integer.parseInt(lblCount.getText().toString().split("[/]", 2)[1]) - (Integer.parseInt(lblCount.getText().toString().split("[/]", 2)[0]));
                }
              /*  if(RemaningQty!=0)
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_0051.replace("[Qty]",String.valueOf(RemaningQty)),getActivity(),getContext(),"Error");
                    return;
                }
                else {
                    ClearFields();

                }*/
                if(BalancedQty!=0) {
                    DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                            "The pallet contains" + " " + BalancedQty + " " + "more cartons. Do you wish to continue?", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            UpdatePalletComplete();
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
                }
                else
                {
                    DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                            "Are you sure you want to complete the Pallet?", new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    switch (which) {
                                        case DialogInterface.BUTTON_POSITIVE:
                                            UpdatePalletComplete();
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
                }

      /*

              if(RemaningQty==0)
              {
                  DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                          "The pallet contains" + " " + lblCount.getText().toString().split("[/]", 2)[1] +  " " + "more cartons. Do you wish to continue?", new DialogInterface.OnClickListener() {

                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                  switch (which) {
                                      case DialogInterface.BUTTON_POSITIVE:
                                          UpdatePalletComplete();
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
                  return;
              }
              else
              {
                  DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                          "The pallet contains" + " " + RemaningQty +  " " + "more cartons. Do you wish to continue?", new DialogInterface.OnClickListener() {

                              @Override
                              public void onClick(DialogInterface dialog, int which) {
                                  switch (which) {
                                      case DialogInterface.BUTTON_POSITIVE:
                                          UpdatePalletComplete();
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
              }*/

                break;
            case R.id.btnBinFull:
                Common.setIsPopupActive(true);
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

                break;
            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnPutawayComplete:
                if(etPallet.getText().toString().isEmpty())
                {
                    common.showUserDefinedAlertType(errorMessages.EMC_0023,getActivity(),getContext(),"Error");
                    return;
                }
                Bundle bundle1 = new Bundle();
                bundle1.putBoolean("IsFetchSuggestions", IsFetchSuggestions);
                bundle1.putString("RegeneratePalletcode",etPallet.getText().toString());
                PutAwayHeaderFragment putAwayFragmentSug = new PutAwayHeaderFragment();
                putAwayFragmentSug.setArguments(bundle1);
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, putAwayFragmentSug);

                break;
            default:
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
        if (scannedData != null && !Common.isPopupActive) {
            if (ScanValidator.IsPalletScanned(scannedData)) {

                    etPallet.setText(scannedData);
                    ValidatePalletCode();


            } else if (ScanValidator.IsItemScanned(scannedData)) {
                if (!(etPallet.getText().toString().isEmpty())) {
                        if(!(etLocation.getText().toString().isEmpty()))
                            {
                                Materialcode = scannedData;
                                SKU=scannedData.split("[-]", 2)[0];
                                lblSuggestedText.setText(scannedData.split("[-]", 2)[0]);
                                etSerial.setText(scannedData.split("[-]", 2)[1]);
                                UpdatePutAwayItem();
                            }
                        else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");
                        }
                    }

                else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0023,getActivity(),getContext(),"Error");

                    return;
                }


            } else if (ScanValidator.IsLocationScanned(scannedData)) {
                if (!(etPallet.getText().toString().isEmpty())) {
                    etLocation.setText(scannedData);
                    ValidateLocationCode();
                }
                else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0019,getActivity(),getContext(),"Error");
                }
            }
        }
    }

    //Clear the fileds
    public void ClearFields() {
        etPallet.setText("");
        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);
        lblCount.setText("");
        BoxCount=0;
        RemaningQty=0;
    }
    public void ClearAtBinfull() {

        etLocation.setText("");
        lbldesc.setText("");
        etSerial.setText("");
        etPallet.setText("");
        etMOP.setText("");
        etMfgDate.setText("");
        etMRP.setText("");
        etSLoc.setText("");

        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);

    }


       //Validating the PalletCode



    public void ValidatePalletCode() {
        try {
            Common.setIsPopupActive(true);
            if(etPallet.getText().toString().equals("Scan Pallet"))
            {
                common.showUserDefinedAlertType(errorMessages.EMC_0019,getActivity(),getContext(),"Error");
                return;
            }


            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Inbound,getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setPalletNo(etPallet.getText().toString());
            inboundDTO.setWarehouseId(WareHouseID);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidatePalletForPutAway(message);
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
                        if (response.body() != null) {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if (core != null) {
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPallet.setImageResource(R.drawable.warning_img);
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    }

                                    etLocation.setText("");
                                } else {

                                    List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    InventoryDTO oInventoryDTO = null;
                                    if (_lResult.size() > 0) {
                                        for (int i = 0; i < _lResult.size(); i++) {
                                            oInventoryDTO = new InventoryDTO(_lResult.get(i).entrySet());
                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                        Common.setIsPopupActive(false);
                                        if(oInventoryDTO.getBoxCount()!=null) {
                                            BoxCount = Integer.parseInt(oInventoryDTO.getBoxCount());
                                            lblCount.setText((BoxCount - BoxCount) + "/" + BoxCount);
                                        }
                                        if (oInventoryDTO.getResult().equals("0")) {
                                            common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Error");
                                            etPallet.setText("");
                                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanPallet.setImageResource(R.drawable.warning_img);
                                            ProgressDialogUtils.closeProgressDialog();
                                            return;
                                        } else {
                                            ProgressDialogUtils.closeProgressDialog();
                                            cvScanPallet.setCardBackgroundColor(Color.WHITE);
                                            ivScanPallet.setImageResource(R.drawable.check);

                                            etPallet.setEnabled(false);
                                        }
                                    }
                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                common.showUserDefinedAlertType(errorMessages.EMC_0021, getActivity(), getContext(), "Error");
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


       //Validating the LocationCode



    public void ValidateLocationCode() {
        try {
            if(etLocation.getText().toString().equals(""))
            {
                common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");
                return;
            }
            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Inbound,getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setDockLocation(etLocation.getText().toString());
            inboundDTO.setWarehouseId(WareHouseID);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidateLocationForPutAWay(message);
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
                                    return;


                                } else {
                                    LinkedTreeMap<String, String> _lResult = new LinkedTreeMap<String, String>();
                                    _lResult = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _lResult.entrySet()) {
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
                                    }
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


       // performing Putaway of Item for suggested Loc.


    public void UpdatePutAwayItem() {
        Common.setIsPopupActive(true);
        if(etPallet.getText().toString().isEmpty())
        {
            common.showUserDefinedAlertType(errorMessages.EMC_0013,getActivity(),getContext(),"Error");

            return;
        }
        if(etLocation.getText().toString().isEmpty())
        {
            common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");
        }
        if(Materialcode==null)
        {
            common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Error");
            return;
        }
        WMSCoreMessage message = new WMSCoreMessage();
        message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setContainerCode(etPallet.getText().toString());
        inventoryDTO.setLocationCode(etLocation.getText().toString());
        inventoryDTO.setRSN(Materialcode);
        inventoryDTO.setMaterialCode(SKU);
        inventoryDTO.setQuantity("1");
        inventoryDTO.setTenantID(TenantID);
        inventoryDTO.setSuggestionID(SuggestedId);
        inventoryDTO.setWareHouseID(WareHouseID);
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
            call = apiService.UpdatePutAwayItem(message);
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
                                    InventoryDTO oInventoryDTO = null;
                                    if (_lResult.size() > 0) {
                                        for (int i = 0; i < _lResult.size(); i++) {
                                            oInventoryDTO = new InventoryDTO(_lResult.get(i).entrySet());
                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.check);
                                        etColor.setText(oInventoryDTO.getColor());
                                        etColor.setEnabled(false);
                                        etMfgDate.setText(oInventoryDTO.getMonthOfMfg() + "/" + oInventoryDTO.getYearOfMfg());
                                        etMfgDate.setEnabled(false);
                                        etMOP.setText(oInventoryDTO.getMOP());
                                        etMOP.setEnabled(false);
                                        etMRP.setText(oInventoryDTO.getMRP());
                                        etMRP.setEnabled(false);
                                        etSLoc.setText(oInventoryDTO.getSLOC().toString());
                                        etSLoc.setEnabled(false);
                                        lbldesc.setText(oInventoryDTO.getMaterialShortDescription());
                                        int PendingQty=Integer.parseInt(oInventoryDTO.getResult());
                                        RemaningQty= PendingQty;
                                        lblCount.setText((BoxCount-PendingQty) +"/"+BoxCount);
                                        Common.setIsPopupActive(false);
                                    }

                                }
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0002,getActivity(),getContext(),"Error");
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

    //performing Pallet Complete if the pallet Completes it's Quantity
    public void UpdatePalletComplete()
    {
        if(etPallet.getText().toString().isEmpty())
        {
            common.showUserDefinedAlertType(errorMessages.EMC_0019,getActivity(),getContext(),"Error");
            return;
        }
        if(etLocation.getText().toString().isEmpty())
        {
            common.showUserDefinedAlertType(errorMessages.EMC_0007,getActivity(),getContext(),"Error");
            return;
        }
        WMSCoreMessage message = new WMSCoreMessage();
        message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
        InventoryDTO inboundDTO = new InventoryDTO();
        inboundDTO.setContainerCode(etPallet.getText().toString());
        message.setEntityObject(inboundDTO);


        Call<String> call = null;
        ApiInterface apiService =
                RestService.getClient().create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.PutAwayPalletComplete(message);
            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }

        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"003_01",getActivity());
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
                            if (response.body() != null) {
                                core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;

                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    }


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
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                common.showUserDefinedAlertType(errorMessages.EMC_0021,getActivity(),getContext(),"Error");
                                return;
                            }
                        }
                    } catch (Exception ex) {
                        try {
                            ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"003_02",getActivity());
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
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }
        //performing Bin Complete if the Bin is Full

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
        inventoryDTO.setLocationCode(etLocation.getText().toString());
        inventoryDTO.setContainerCode(etPallet.getText().toString());
        message.setEntityObject(inventoryDTO);


        Call<String> call = null;
        ApiInterface apiService =
                RestService.getClient().create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.Bincomplete(message);
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
                                //Converting List of Inventory
                                List<InventoryDTO> lstInventory = new ArrayList<InventoryDTO>();
                                //Header for Adapter
                                lstPalletnumberHeader = new ArrayList<String>();
                                //child for Hashmap
                                InventoryDTO inventorydto = null;
                                //Iterating through the Inventory list
                                if (_lInventory.size() > 0) {
                                    for (int i = 0; i < _lInventory.size(); i++) {

                                        // Each individual value of the map into DTO
                                        inventorydto = new InventoryDTO(_lInventory.get(i).entrySet());
                                        //Adding Inventory Dto into List
                                        lstInventory.add(inventorydto);
                                    }


                                    PalletInfo palletInfo = new PalletInfo();
                                    List<PalletInfo> lstPalletinfo = new ArrayList<PalletInfo>();

                                    List<MaterialInfo> lstMaterilinfo = new ArrayList<MaterialInfo>();
                                    lstLocMaterialQty = new ArrayList<String>();


                                    //Iterating to List of Inventory and Mapping it into  Local Model class
                                    for (InventoryDTO inventoryDTO : lstInventory) {
                                        MaterialInfo materialInfo = new MaterialInfo();
                                        materialInfo.setMcode(inventoryDTO.getMaterialCode());
                                        materialInfo.setLocation(inventoryDTO.getLocationCode());
                                        materialInfo.setQty(Double.parseDouble(inventoryDTO.getQuantity()));
                                        palletInfo.setPalletCode(inventoryDTO.getContainerCode());
                                        lstMaterilinfo.add(materialInfo);
                                    }
                                    // added Material Information into list

                                    // Setting Material information to Palletinfo
                                    palletInfo.setMaterialinfo(lstMaterilinfo);
                                    // Add pallet info to List
                                    lstPalletinfo.add(palletInfo);

                                    // Adding Pallet no to the Header list
                                    lstPalletnumberHeader.add(palletInfo.getPalletCode());
                                    for (int Palletlist = 0; Palletlist < palletInfo.getMaterialinfo().size(); Palletlist++) {
                                        // Adding the Loc,Matrial,Qty to the list
                                        lstLocMaterialQty.add(palletInfo.getMaterialinfo().get(Palletlist).getLocation() + System.getProperty("line.separator") + palletInfo.getMaterialinfo().get(Palletlist).getMcode() + System.getProperty("line.separator") + palletInfo.getMaterialinfo().get(Palletlist).getQty()
                                                + "/" + palletInfo.getMaterialinfo().get(Palletlist).getSuggestionId());
                                    }
                                    GetPutawayList();
                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showUserDefinedAlertType(errorMessages.EMC_0024,getActivity(),getContext(),"Error");
                                    return;
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
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");

        }
    }


    public void GetPutawayList()
    {
        Bundle bundle = new Bundle();
        String PalletnumberHeaderjson = gson.toJson(lstPalletnumberHeader);
        String LocaMaterialInfojson = gson.toJson(lstLocMaterialQty);
        bundle.putString("lstPalletnumberHeader", PalletnumberHeaderjson);
        bundle.putSerializable("LocaMaterialInfojson", LocaMaterialInfojson);
        bundle.putString("PalletNo",etPallet.getText().toString());
        bundle.putString("SuggestedID",SuggestedId);
        PutAwayHeaderFragment putAwayFragment = new PutAwayHeaderFragment();
        putAwayFragment.setArguments(bundle);
        FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, putAwayFragment);

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
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

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
