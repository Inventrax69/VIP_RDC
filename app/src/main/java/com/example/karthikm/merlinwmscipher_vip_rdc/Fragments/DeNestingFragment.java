package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.DenestingDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InventoryDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.JobDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.SearchableSpinner.SearchableSpinner;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
import com.example.karthikm.merlinwmscipher_vip_rdc.adapters.DeNestingAdapter;
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


public class DeNestingFragment extends Fragment implements BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final String classCode = "API_FRAG_006";
    private View rootView;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    private ScanValidator scanValidator;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    //Component declarations
    private TextView lblJobOrder, lblScannedSku;
    private Button btnGo, btnCloseOne, btnCloseTwo;
    private TextInputLayout txtInputLayoutLocation, txtInputLayoutPallet;
    private EditText etLocation, etPallet, etSerial, etMOP;
    private RelativeLayout rlDeNestingOne, rlDeNestingTwo;
    private SearchableSpinner spinnerSelectJobOrder;
    private CardView cvScanLocation, cvScanPallet, cvScanSku;
    private ImageView ivScanLocation, ivScanPallet, ivScanSku, iv_btnRevert;
    private RecyclerView recycler_view_denesting;

    private String jobOrderNo;
    Common common;
    private WMSCoreMessage core;

    List<JobDTO> Joblist = null;
    private boolean isLocationValid = false;
    private boolean isPalletValid = false;
    private String materialCode = null;
    private String RSNNumber = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private boolean IsuserConfirmedRedo = false;
    private LinearLayoutManager linearLayoutManager;
    private SoundUtils soundUtils;
    private String jobOrderTypeId = "",userId = "";
    private boolean isRevertPressed = false;


    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public DeNestingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_de_nesting, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {


        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        rlDeNestingOne = (RelativeLayout) rootView.findViewById(R.id.rlDeNestingOne);
        rlDeNestingTwo = (RelativeLayout) rootView.findViewById(R.id.rlDeNestingTwo);

        lblJobOrder = (TextView) rootView.findViewById(R.id.lblJobOrder);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);

        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);

        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);
        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);

        etLocation = (EditText) rootView.findViewById(R.id.etLocation);
        etPallet = (EditText) rootView.findViewById(R.id.etPallet);
        etSerial = (EditText) rootView.findViewById(R.id.etSerial);
        etMOP = (EditText) rootView.findViewById(R.id.etMOP);
        IsuserConfirmedRedo = false;
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);

        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);

        //iv_btnRevert = (ImageView) rootView.findViewById(R.id.iv_btnRevert);

        spinnerSelectJobOrder = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectJobOrder);

        spinnerSelectJobOrder.setOnItemSelectedListener(this);

        recycler_view_denesting = (RecyclerView) rootView.findViewById(R.id.recycler_view_denesting);
        recycler_view_denesting.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        recycler_view_denesting.setLayoutManager(linearLayoutManager);


        btnGo.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);

        //iv_btnRevert.setOnClickListener(this);

        common = new Common();
        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        soundUtils = new SoundUtils();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        Joblist = new ArrayList<JobDTO>();
        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);





        //For Honeywell Broadcast receiver intiation
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

        if (getArguments() != null) {

            if (getArguments().getString("JoborderNo") != null) {
                try {

                    jobOrderNo = getArguments().getString("JoborderNo");
                    jobOrderTypeId = getArguments().getString("jobOrderTypeId");

                    GetjobOrderDetails();

                } catch (Exception ex) {

                }
            }
        } else {
            // To get Job order List
            getDenestingJobOrders();
        }
    }

    // Onclick functions
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnGo:
                if (jobOrderNo != null) {

                    getJobTypeId();
                    GetjobOrderDetails();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0042, getActivity(), getContext(), "Error");
                }
                break;

            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

           /* case R.id.iv_btnRevert:


                if(iv_btnRevert.getTag().equals("off")){
                    iv_btnRevert.setBackgroundResource(0);
                    iv_btnRevert.setImageResource(R.drawable.on);
                    Toast.makeText(getContext(), "Turning ON", Toast.LENGTH_SHORT).show();
                    iv_btnRevert.setTag("on");
                }else if(iv_btnRevert.getTag().equals("on")){
                    iv_btnRevert.setBackgroundResource(0);
                    iv_btnRevert.setImageResource(R.drawable.off);
                    Toast.makeText(getContext(), "Turning OFF", Toast.LENGTH_SHORT).show();
                    iv_btnRevert.setTag("off");
                }

                break;*/

        }
    }

/*
    public void GetjobOrderDetails()
    {
        rlDeNestingOne.setVisibility(View.GONE);
        rlDeNestingTwo.setVisibility(View.VISIBLE);
        List<InventoryDTO> lstjobdata = new ArrayList<>();
        InventoryDTO oInventoryDTO= new InventoryDTO();
        lblJobOrder.setText(jobOrderNo);
        for(JobDTO ojob:Joblist)
        {
            if(ojob.getJobRefNumber().equals(jobOrderNo))
            {
                oInventoryDTO.setMaterialCode(ojob.getParentSKU());
                oInventoryDTO.setDocumentProcessedQuantity(ojob.getConsumedQuantity().toString());
                oInventoryDTO.setDocumentQuantity(ojob.getReceiveExpectedQuantity().toString());
                oInventoryDTO.setMaterialParent(true);
                oInventoryDTO.setMaterialShortDescription(ojob.getParentSKUDesc());
                oInventoryDTO.setReferenceDocumentNumber(lblJobOrder.getText().toString());
                lstjobdata.add(oInventoryDTO);
            }

        }
        DeNestingAdapter deNestingAdapter = new DeNestingAdapter(getActivity(), lstjobdata,getActivity(),oInventoryDTO.getMaterialParent());
        recycler_view_denesting.setAdapter(deNestingAdapter);
    }*/


    // honeywell Barcode reader
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

    public void getJobTypeId() {

        for (JobDTO oDto : Joblist) {

            if (oDto.getJobRefNumber().equals(jobOrderNo)) {
                jobOrderTypeId = String.valueOf(oDto.getJobOrderTypeID());

            }
        }

    }


    // Get Job list
    public void getDenestingJobOrders() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.DenestingDTO, getActivity());
            DenestingDTO denestingDTO = new DenestingDTO();
            message.setEntityObject(denestingDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetDenestingJobOrders(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_01", getActivity());
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

                            List<LinkedTreeMap<?, ?>> _lJobOrder = new ArrayList<LinkedTreeMap<?, ?>>();
                            _lJobOrder = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                            List<DenestingDTO> lstDto = new ArrayList<DenestingDTO>();
                            List<String> lstJobOrder = new ArrayList<>();


                            for (int i = 0; i < _lJobOrder.size(); i++) {
                                DenestingDTO dto = new DenestingDTO(_lJobOrder.get(i).entrySet());
                                lstDto.add(dto);
                            }

                            for (int i = 0; i < lstDto.size(); i++) {
                                for (int j = 0; j < lstDto.get(i).getJobList().size(); j++) {
                                    lstJobOrder.add(lstDto.get(i).getJobList().get(j).getJobRefNumber() + " " + "-" + " " + lstDto.get(i).getJobList().get(j).getParentSKU());
                                    Joblist = lstDto.get(i).getJobList();
                                }


                            }


                            /* To remove the duplicate job orders */

                            // Copy the list.
                            ArrayList<String> newList = new ArrayList<>();
                                    newList = (ArrayList<String>) lstJobOrder;

                            // Iterate
                            for (int i = 0; i < lstJobOrder.size(); i++) {
                                for (int j = lstJobOrder.size() - 1; j >= i; j--) {
                                    // If i is j, then it's the same object and don't need to be compared.
                                    if (i == j) {
                                        continue;
                                    }
                                    // If the compared objects are equal, remove them from the copy and break
                                    // to the next loop
                                    if (lstJobOrder.get(i).equals(lstJobOrder.get(j))) {
                                        newList.remove(lstJobOrder.get(i));
                                        break;
                                    }
                                    //System.out.println("" + i + "," + j + ": " + lstJobOrder.get(i) + "-" + lstJobOrder.get(j));
                                }
                            }

                            for (int i = 0; i < newList.size(); i++) {
                                for (int j = newList.size() - 1; j >= i; j--) {
                                    // If i is j, then it's the same object and don't need to be compared.
                                    if (i == j) {
                                        continue;
                                    }
                                    // If the compared objects are equal, remove them from the copy and break
                                    // to the next loop
                                    if (newList.get(i).equals(newList.get(j))) {
                                        newList.remove(newList.get(i));
                                        break;
                                    }
                                    //System.out.println("" + i + "," + j + ": " + lstJobOrder.get(i) + "-" + lstJobOrder.get(j));
                                }
                            }

                            ProgressDialogUtils.closeProgressDialog();
                            ArrayAdapter arrayAdapterLoadSheet = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, newList);
                            spinnerSelectJobOrder.setAdapter(arrayAdapterLoadSheet);

                            if (getArguments() != null) {
                                if (getArguments().getBoolean("IsFromAvailList")) {
                                    try {

                                        jobOrderNo = getArguments().getString("JoborderNo");
                                        GetjobOrderDetails();

                                    } catch (Exception ex) {

                                    }
                                }
                            }


                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
        }
    }


    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {
        if (scannedData != null && !common.isPopupActive()) {
            if (scanValidator.IsPalletScanned(scannedData)) {

                if (!(etLocation.getText().toString().isEmpty())) {
                    etPallet.setText(scannedData);
                    ValidatePalletCode();
                } else {

                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");

                }

            } else if (ScanValidator.IsItemScanned(scannedData)) {

                if (!(etLocation.getText().toString().isEmpty())) {
                    lblScannedSku.setText(scannedData.split("[-]", 2)[0]);
                    materialCode = scannedData;
                    RSNNumber = scannedData;

                    updateDenestingItem();

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                    return;
                }

            } else if (scanValidator.IsLocationScanned(scannedData)) {

                etLocation.setText(scannedData);
                ValidateLocationCode();
            }
        }
    }

    // Validate Location
    public void ValidateLocationCode() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.DenestingDTO, getActivity());
            DenestingDTO denestingDTO = new DenestingDTO();
            denestingDTO.setUserID(userId);
            denestingDTO.setPutawayLocation(etLocation.getText().toString());
            message.setEntityObject(denestingDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidateDenestingLocationCode(message);
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
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {

                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
                            if (core != null) {
                                // if any Exception throws
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                        ProgressDialogUtils.closeProgressDialog();
                                        etLocation.setText("");
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
                                                common.showUserDefinedAlertType(errorMessages.EMC_0016, getActivity(), getContext(), "Error");
                                                return;
                                            } else {                                                         // on valid location
                                                ProgressDialogUtils.closeProgressDialog();
                                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                ivScanLocation.setImageResource(R.drawable.check);
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    // Validate Pallet
    public void ValidatePalletCode() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.DenestingDTO, getActivity());
            DenestingDTO denestingDTO = new DenestingDTO();
            denestingDTO.setPutawayPallet(etPallet.getText().toString());
            message.setEntityObject(denestingDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidateDenestingPalletCode(message);
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
                                //DialogUtils.showAlertDialog(getActivity(), owmsExceptionMessage.getWMSMessage());
                            } else {
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet()) {
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanPallet.setImageResource(R.drawable.invalid_cross);

                                            common.showUserDefinedAlertType(errorMessages.EMC_0017, getActivity(), getContext(), "Error");
                                            return;
                                        } else {                                                             // on valid pallet
                                            ProgressDialogUtils.closeProgressDialog();
                                            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanPallet.setImageResource(R.drawable.check);
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
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
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
        }
    }


    public void updateDenestingItem() {
        try {
            common.setIsPopupActive(true);
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getActivity());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setLocationCode(etLocation.getText().toString());
            inventoryDTO.setContainerCode(etPallet.getText().toString());
            inventoryDTO.setReferenceDocumentNumber(jobOrderNo);
            inventoryDTO.setRSN(RSNNumber);
            inventoryDTO.setJobOrderTypeID(Double.parseDouble(jobOrderTypeId));
            if (IsuserConfirmedRedo) {
                inventoryDTO.setUserConfirmReDo(true);

            }
            message.setEntityObject(inventoryDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                common.setIsPopupActive(true);
                call = apiService.UpdateDenestingItem(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_01", getActivity());
                    common.setIsPopupActive(false);
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


                                            if (owmsExceptionMessage.getWMSExceptionCode().equals("WMC_DENEST_BL_0016")) {
                                                IsuserConfirmedRedo = true;
                                            }
                                            if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                                common.setIsPopupActive(true);
                                                DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage(), new DialogInterface.OnClickListener() {

                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        switch (which) {
                                                            case DialogInterface.BUTTON_POSITIVE:
                                                                common.setIsPopupActive(false);
                                                                updateDenestingItem();
                                                                break;

                                                            case DialogInterface.BUTTON_NEGATIVE:
                                                                IsuserConfirmedRedo = false;
                                                                common.setIsPopupActive(false);
                                                                break;
                                                        }

                                                    }
                                                });
                                                common.setIsPopupActive(true);
                                                ProgressDialogUtils.closeProgressDialog();
                                                return;
                                            } else {
                                                ProgressDialogUtils.closeProgressDialog();
                                                common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                                IsuserConfirmedRedo = false;
                                                return;
                                            }


                                        }
                                    } else {

                                        List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                        _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                        InventoryDTO oInventoryDto = null;
                                        List<InventoryDTO> lstInventory = new ArrayList<InventoryDTO>();

                                        if (_lResult.size() > 0) {
                                            for (int i = 0; i < _lResult.size(); i++) {
                                                oInventoryDto = new InventoryDTO(_lResult.get(i).entrySet());
                                                lstInventory.add(oInventoryDto);

                                           /* for (int j = 0; j < _lResult.size(); j++) {
                                                oInventoryDto = new InventoryDTO(_lResult.get(j).entrySet());
                                                lstInventory.add(oInventoryDto);
                                            }*/
                                            }

                                            ProgressDialogUtils.closeProgressDialog();
                                            common.setIsPopupActive(false);

                                            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                            ivScanSku.setImageResource(R.drawable.check);

                                            DeNestingAdapter deNestingAdapter = new DeNestingAdapter(getActivity(), lstInventory, getActivity(), jobOrderTypeId);
                                            recycler_view_denesting.setAdapter(deNestingAdapter);
                                            IsuserConfirmedRedo = false;
                                            return;
                                        }
                                    }
                                }
                                IsuserConfirmedRedo = false;
                            }
                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_03", getActivity());
                    common.setIsPopupActive(false);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_04", getActivity());
                common.setIsPopupActive(false);
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public void GetjobOrderDetails() {

        WMSCoreMessage message = new WMSCoreMessage();
        message = common.SetAuthentication(EndpointConstants.Inventory, getActivity());
        InventoryDTO inventoryDTO = new InventoryDTO();
        inventoryDTO.setReferenceDocumentNumber(jobOrderNo);
        message.setEntityObject(inventoryDTO);

        Call<String> call = null;
        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        try {
            // Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.FetchDenstingJobItemsList(message);

            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }

        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_01", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            return;
        }
        try {
            //Getting response from the method
            call.enqueue(new Callback<String>() {

                @Override
                public void onResponse(Call<String> call, Response<String> response) {

                    try {
                        if (response.body() != null) {

                            if (core != null) {
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
                                            common.setIsPopupActive(false);
                                            return;

                                        }
                                    } else {

                                        List<LinkedTreeMap<?, ?>> _lResult = new ArrayList<LinkedTreeMap<?, ?>>();
                                        _lResult = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                        InventoryDTO oInventoryDto = null;
                                        List<InventoryDTO> lstInventory = new ArrayList<InventoryDTO>();

                                        if (_lResult.size() > 0) {
                                            for (int i = 0; i < _lResult.size(); i++) {
                                                oInventoryDto = new InventoryDTO(_lResult.get(i).entrySet());
                                                lstInventory.add(oInventoryDto);

                                            }

                                            ProgressDialogUtils.closeProgressDialog();
                                            common.setIsPopupActive(false);
                                            rlDeNestingOne.setVisibility(View.GONE);
                                            rlDeNestingTwo.setVisibility(View.VISIBLE);
                                            lblJobOrder.setText(jobOrderNo);


                                            DeNestingAdapter deNestingAdapter = new DeNestingAdapter(getActivity(), lstInventory,
                                                    getActivity(), jobOrderTypeId);
                                            recycler_view_denesting.setAdapter(deNestingAdapter);

/*                                            if(jobOrderTypeId.equalsIgnoreCase("2.0")){


                                            }*/

                                            return;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception ex) {
                        try {
                            ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_02", getActivity());
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
                    common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    return;
                }
            });
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
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
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();

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
                                    common.setIsPopupActive(false);
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
                                            common.setIsPopupActive(false);
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();

            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.nesting_denesting));
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


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        jobOrderNo = spinnerSelectJobOrder.getSelectedItem().toString().split("[-]", 2)[0].trim();

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}