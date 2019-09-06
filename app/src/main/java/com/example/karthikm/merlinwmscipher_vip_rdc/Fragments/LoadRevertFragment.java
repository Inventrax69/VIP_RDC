package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.cipherlab.barcode.GeneralString;
import com.example.karthikm.merlinwmscipher_vip_rdc.Activities.MainActivity;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InventoryDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.LoadDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.OutbountDTO;
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
 *   Created by Padmaja on 06/26/2018.
 */

public class LoadRevertFragment extends Fragment implements View.OnClickListener,BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_009";
    private View rootView;
    Button btnGo, btnCloseOne, btnLoadingComplete, btnCloseTwo,btnRevert;
    RelativeLayout rlLoadingOne,rlLoadListTwo,rlImagePath;
    TextView lblLoadSheetNo,lblVehicleNo,lblDockNo,lblScannedSku,lblDesc,lblImagePath,lblBoxQty,lblVolume,lblWeight;
    CardView cvScanSku;
    ImageView ivScanSku;
    SearchableSpinner spinnerSelectLoadList;
    EditText etQty;
    TextInputLayout txtInputLayoutQty;
    String userId = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    private Common common;
    private WMSCoreMessage core;
    List<LoadDTO> lstloaddata= null;
    String loadSheetNo=null,loadNoCustomerCode=null;
    private String Materialcode = null;
    private boolean IsUserConfirmedRedo=false;

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

    public LoadRevertFragment(){

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView  = inflater.inflate(R.layout.fragment_load_revert,container,false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        rlLoadingOne = (RelativeLayout) rootView.findViewById(R.id.rlLoadingOne);
        rlLoadListTwo = (RelativeLayout) rootView.findViewById(R.id.rlLoadListTwo);
        lstloaddata = new ArrayList<>();
        spinnerSelectLoadList = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectLoadList);
        spinnerSelectLoadList.setOnItemSelectedListener(this);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        etQty = (EditText) rootView.findViewById(R.id.etQty);
        rlImagePath = (RelativeLayout) rootView.findViewById(R.id.rlImagePath);

        // DrawableCompat.setTint(isDockScanned.getDrawable(), ContextCompat.getColor(getContext(), R.color.green));
        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnCloseTwo = (Button)rootView.findViewById(R.id.btnCloseTwo);
        btnLoadingComplete = (Button)rootView.findViewById(R.id.btnLoadingComplete );
        btnRevert = (Button) rootView.findViewById(R.id.btnRevert);
        lblLoadSheetNo = (TextView) rootView.findViewById(R.id.lblLoadSheetNo);
        lblDockNo = (TextView) rootView.findViewById(R.id.lblDockNo);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblDesc = (TextView) rootView.findViewById(R.id.lblScannedSkuItem);
        lblVehicleNo = (TextView) rootView.findViewById(R.id.lblVehicleNo);
        lblImagePath= (TextView) rootView.findViewById(R.id.lblImagePath);
        lblBoxQty = (TextView) rootView.findViewById(R.id.lblBoxQty);
        lblVolume = (TextView) rootView.findViewById(R.id.lblVolume);
        lblWeight = (TextView) rootView.findViewById(R.id.lblWeight);

        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);

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

        btnGo.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);
        btnRevert.setOnClickListener(this);

        common = new Common();
        core= new WMSCoreMessage();
        soundUtils = new SoundUtils();
        GetLoadCancelSheetNo();

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
            case R.id.btnGo:
                if(loadSheetNo!=null) { GetLoaddetails(); }
                else { common.showUserDefinedAlertType(errorMessages.EMC_0046,getActivity(),getContext(),"Error"); }
                break;
            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnRevert:
                if(lblScannedSku.getText().toString().equals(""))
                {
                    Common.setIsPopupActive(true);
                    soundUtils.alertError(getActivity(), getContext());
                    DialogUtils.showAlertDialog(getActivity(), "Error", errorMessages.EMC_0027, R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (which == DialogInterface.BUTTON_POSITIVE) {
                                Common.setIsPopupActive(false);
                            }
                        }
                    });

                    return;
                }
                DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                        "Are you sure you want to revert the LoadItem?", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        ///RevertLoading();
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
            case  R.id.btnLoadingComplete:
                DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                        "Are you sure you want to complete the Load?", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        LoadingComplete();
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

            Map<String, Object> properties = new HashMap<>();
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
            if (ScanValidator.IsItemScanned(scannedData)) {
                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                ivScanSku.setImageResource(R.drawable.fullscreen_img);
                lblScannedSku.setText(scannedData.split("[-]",2)[0]);
                Materialcode = scannedData;
                ConfirmRevertLoading();
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        loadSheetNo = spinnerSelectLoadList.getSelectedItem().toString().split("[-]", 2)[0].trim();
        loadNoCustomerCode = spinnerSelectLoadList.getSelectedItem().toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void GetLoaddetails()
    {
        rlLoadingOne.setVisibility(View.GONE);
        rlLoadListTwo.setVisibility(View.VISIBLE);
        btnRevert.setVisibility(View.INVISIBLE);
        lblLoadSheetNo.setText(loadNoCustomerCode);
        for(LoadDTO oLoditem:lstloaddata)
        {
            if(oLoditem.getLoadSheetNo().equals(loadSheetNo))
            {
                lblVehicleNo.setText(oLoditem.getVehicleNumber());
                lblDockNo.setText(oLoditem.getDockNumber());
                String BoxQty=oLoditem.getLoadedQuantity() + "/" + oLoditem.getLoadSheetQuantity();
                lblBoxQty.setText(BoxQty);
                lblVolume.setText(String.valueOf(oLoditem.getVolume()));
                lblWeight.setText(String.valueOf(oLoditem.getWeight()));
            }
        }
    }

    public  void GetLoadCancelSheetNo()
    {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound,getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            message.setEntityObject(outbountDTO);

            Log.v("ABCDE_C",new Gson().toJson(message));


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetLoadCancelSheetNo(message);
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

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if(core!=null) {
                                List<LinkedTreeMap<?, ?>> _lLoadSheetNo = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lLoadSheetNo = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                List<OutbountDTO> lstDto = new ArrayList<OutbountDTO>();
                                List<String> lstLoadSheetNo = new ArrayList<>();


                                for (int i = 0; i < _lLoadSheetNo.size(); i++) {
                                    OutbountDTO dto = new OutbountDTO(_lLoadSheetNo.get(i).entrySet());
                                    lstDto.add(dto);
                                }

                                for (int i = 0; i < lstDto.size(); i++) {
                                    for (int j = 0; j < lstDto.get(i).getLoadList().size(); j++) {

                                        if(lstDto.get(i).getLoadList().get(j).getCustomerCode().equalsIgnoreCase("")){
                                            lstLoadSheetNo.add(lstDto.get(i).getLoadList().get(j).getLoadSheetNo());
                                        }else {
                                            lstLoadSheetNo.add(lstDto.get(i).getLoadList().get(j).getLoadSheetNo() + "-" + lstDto.get(i).getLoadList().get(j).getCustomerCode());
                                        }
                                        lstloaddata = lstDto.get(i).getLoadList();
                                    }
                                }

                                ProgressDialogUtils.closeProgressDialog();
                                ArrayAdapter arrayAdapterLoadSheet = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstLoadSheetNo);
                                spinnerSelectLoadList.setAdapter(arrayAdapterLoadSheet);

                            }


                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }


                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {

                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"001_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }

    public  void FetchInventoryForLoadSheet()
    {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Outbound,getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setSelectedLoadSheetNumber(loadSheetNo);
            message.setEntityObject(outbountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.FetchInventoryForLoadSheet(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"006_01",getActivity());
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

                                    DialogUtils.showAlertDialog(getActivity(), owmsExceptionMessage.getWMSMessage());
                                } else {
                                    ProgressDialogUtils.closeProgressDialog();

                                    rlLoadingOne.setVisibility(View.GONE);
                                    rlLoadListTwo.setVisibility(View.VISIBLE);
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"006_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();

                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();

                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"006_03",getActivity());
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
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"006_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();

            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }

    public  void ConfirmRevertLoading()
    {

        try {
            Common.setIsPopupActive(true);
            if(Materialcode.equals("") || Materialcode==null)
            {
                common.showUserDefinedAlertType(errorMessages.EMC_0027,getActivity(),getContext(),"Error");
                return;
            }
            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setRSN(Materialcode);
            inventoryDTO.setReferenceDocumentNumber(lblLoadSheetNo.getText().toString().split("[-]",2)[0]);
            inventoryDTO.setVehicleNumber(lblVehicleNo.getText().toString());
            String Qty="1";
            inventoryDTO.setQuantity(Qty);
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ConfirmRevertLoading(message);
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
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {
                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();
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


                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.warning_img);
                                            return;
                                        } else

                                        {
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.warning_img);
                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                        return;
                                    }

                                } else {

                                    ProgressDialogUtils.closeProgressDialog();
                                    List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<InventoryDTO> lstDto = new ArrayList<InventoryDTO>();
                                    InventoryDTO dto = null;
                                    if (_lInventory != null && _lInventory.size() > 0) {
                                        for (int i = 0; i < _lInventory.size(); i++) {

                                            dto = new InventoryDTO(_lInventory.get(i).entrySet());
                                            lstDto.add(dto);

                                        }
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.check);
                                        if (dto.getQuantity() != "0") {
                                            lblBoxQty.setText(String.valueOf(dto.getQuantity()));
                                        }
                                        lblDesc.setText(dto.getMaterialShortDescription());
                                        Common.setIsPopupActive(false);
                                    }
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

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"003_03",getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        }catch (Exception ex)
        {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"003_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }

    public  void RevertLoading() {

        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
            final InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setReferenceDocumentNumber(lblLoadSheetNo.getText().toString().split("[-]",2)[0]);
            inventoryDTO.setMaterialCode(lblScannedSku.getText().toString());
            inventoryDTO.setRSN(Materialcode);
            message.setEntityObject(inventoryDTO);
            if(IsUserConfirmedRedo)
            {
                inventoryDTO.setUserConfirmReDo(true);
            }

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.RevertLoading(message);
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
                                        ProgressDialogUtils.closeProgressDialog();

                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_008")) {
                                            IsUserConfirmedRedo = true;
                                            inventoryDTO.setQuantity("0");
                                        }
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_009")) {
                                            etQty.setVisibility(View.VISIBLE);
                                            txtInputLayoutQty.setVisibility(View.VISIBLE);
                                        }
                                        if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                            DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage().toString(), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            if (inventoryDTO.getQuantity().equals("0")) {
                                                                RevertLoading();
                                                            }
                                                            break;
                                                        case DialogInterface.BUTTON_NEGATIVE:

                                                            break;
                                                    }
                                                }
                                            });
                                            return;
                                        } else if (owmsExceptionMessage.isShowAsSuccess()) {
                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.check);
                                            return;
                                        } else {
                                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        }
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.warning_img);
                                        return;
                                    }
                                } else {
                                    InventoryDTO oInventoryDTO = null;
                                    List<LinkedTreeMap<?, ?>> _lLoadingList = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lLoadingList = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    for (int i = 0; i < _lLoadingList.size(); i++) {
                                        oInventoryDTO = new InventoryDTO(_lLoadingList.get(i).entrySet());
                                    }
                                    lblBoxQty.setText(oInventoryDTO.getQuantity());
                                    lblScannedSku.setText("");
                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                    ivScanSku.setImageResource(R.drawable.fullscreen_img);
                                    ProgressDialogUtils.closeProgressDialog();
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

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {

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
                common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
            }
        }catch (Exception ex)
        {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"004_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003,getActivity(),getContext(),"Error");
        }
    }

    public  void LoadingComplete()
    {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message= common.SetAuthentication(EndpointConstants.Inventory,getContext());
            final InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setReferenceDocumentNumber(lblLoadSheetNo.getText().toString().split("[-]",2)[0]);
            inventoryDTO.setMaterialCode(lblScannedSku.getText().toString());
            inventoryDTO.setRSN(Materialcode);
            message.setEntityObject(inventoryDTO);
            if(IsUserConfirmedRedo)
            {
                inventoryDTO.setUserConfirmReDo(true);
            }

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.LoadingComplete(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"005_01",getActivity());
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
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_008")) {
                                            IsUserConfirmedRedo = true;
                                            inventoryDTO.setQuantity("0");


                                        }
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_009")) {
                                            etQty.setVisibility(View.VISIBLE);
                                            txtInputLayoutQty.setVisibility(View.VISIBLE);
                                        }
                                        if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                            DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage().toString(), new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            if (inventoryDTO.getQuantity().equals("0")) {
                                                                RevertLoading();
                                                            }
                                                            break;
                                                        case DialogInterface.BUTTON_NEGATIVE:

                                                            break;
                                                    }
                                                }
                                            });
                                            return;
                                        } else if (owmsExceptionMessage.isShowAsSuccess()) {
                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.check);
                                            return;
                                        } else {
                                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        }
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.warning_img);
                                        return;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"005_02",getActivity());
                                logException();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001,getActivity(),getContext(),"Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"005_03",getActivity());
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
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"005_04",getActivity());
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
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_01",getActivity());
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
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_02",getActivity());
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
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
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
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002_04",getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0003);
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_load_revert));
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