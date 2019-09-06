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
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.OutbountDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.SkipReasonDTO;
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

import static android.view.View.GONE;

/**
 * Created by Prasanna ch on 06/26/2018.
 */

public class OutboundFragment extends Fragment implements View.OnClickListener, AdapterView.OnItemSelectedListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_008";

    private View rootView;
    ImageView ivScanLocation, ivScanPallet, ivScanSku, ivScanNewRSN;
    Button btnGo, btnCloseOne, btnMaterialSkip, btnItemNotFound, btnCloseTwo, btnOk, btnCloseThree;
    RelativeLayout rlPickListOne, rlPickListTwo, rlSelect;
    TextView lblPickListNo, lblDockNo, lblLocationSuggested, lblScannedSku, lblSKUSuggested, lblRequiredQty,
            lblPickedQty, lblScanNewRSN, lblDesc;
    CardView cvScanLocation, cvScanPallet, cvScanSku, cvScanNewRSN;
    SearchableSpinner spinnerSelectPickList, spinnerSelectReason;
    TextInputLayout txtInputLayoutLocation, txtInputLayoutPallet, txtInputLayoutSerial, txtInputLayoutQty;
    EditText etLocation, etPallet, etSerial, etQty;
    boolean IsStrictlycomplaince = false;
    String Mcode = null, NewMcode = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    String userId = null;
    private Common common;
    private WMSCoreMessage core;
    private String SuggestedId = null;
    boolean IsuserConfirmed = false, IsUsercanceled = false;
    private String pickRefNo, skipReason = null, skipReasonId = null, tripSheet = "",selectedPickRefNo = "";
    int count = 0;
    private ScanValidator scanValidator;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private SoundUtils soundUtils;
    List<SkipReasonDTO> lstSkipDto;
    List<OutbountDTO> lstOutbound = null;
    List<String> lstTripSheetNo = null;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public OutboundFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_picking, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();

        return rootView;
    }

    private void loadFormControls() {

        rlPickListOne = (RelativeLayout) rootView.findViewById(R.id.rlPickListOne);
        rlPickListTwo = (RelativeLayout) rootView.findViewById(R.id.rlPickListTwo);

        spinnerSelectPickList = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectPickList);
        spinnerSelectPickList.setOnItemSelectedListener(this);

        ivScanLocation = (ImageView) rootView.findViewById(R.id.ivScanLocation);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);
        ivScanNewRSN = (ImageView) rootView.findViewById(R.id.ivScanNewRSN);
        //DrawableCompat.setTint(isDockScanned.getDrawable(), ContextCompat.getColor(getContext(), R.color.green));
        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnCloseOne = (Button) rootView.findViewById(R.id.btnCloseOne);
        btnItemNotFound = (Button) rootView.findViewById(R.id.btnItemNotFound);
        btnMaterialSkip = (Button) rootView.findViewById(R.id.btnMaterialSkip);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        IsuserConfirmed = false;
        lblPickListNo = (TextView) rootView.findViewById(R.id.lblPickListNo);
        lblDockNo = (TextView) rootView.findViewById(R.id.lblDockNo);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);

        lblLocationSuggested = (TextView) rootView.findViewById(R.id.lblLocationSuggested);
        lblSKUSuggested = (TextView) rootView.findViewById(R.id.lblSKUSuggested);
        lblRequiredQty = (TextView) rootView.findViewById(R.id.lblRequiredQty);
        lblPickedQty = (TextView) rootView.findViewById(R.id.lblPickedQty);
        lblScanNewRSN = (TextView) rootView.findViewById(R.id.lblScanNewRSN);
        lblDesc = (TextView) rootView.findViewById(R.id.lblScannedSkuItem);
        cvScanLocation = (CardView) rootView.findViewById(R.id.cvScanLocation);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);
        cvScanNewRSN = (CardView) rootView.findViewById(R.id.cvScanNewRSN);

        txtInputLayoutLocation = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutLocation);
        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutSerial = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSerial);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);

        etLocation = (EditText) rootView.findViewById(R.id.etLocation);
        etPallet = (EditText) rootView.findViewById(R.id.etPallet);
        etSerial = (EditText) rootView.findViewById(R.id.etSerial);
        etQty = (EditText) rootView.findViewById(R.id.etQty);
        etQty.setVisibility(GONE);
        btnMaterialSkip.setVisibility(GONE);


        spinnerSelectReason = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectReason);
        spinnerSelectReason.setOnItemSelectedListener(this);
        btnOk = (Button) rootView.findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
        btnCloseThree = (Button) rootView.findViewById(R.id.btnCloseThree);
        btnCloseThree.setOnClickListener(this);
        rlSelect = (RelativeLayout) rootView.findViewById(R.id.rlSelect);


        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        // For Cipher Barcode reader
        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", true);
        getActivity().sendBroadcast(RTintent);
        this.filter = new IntentFilter();
        this.filter.addAction("sw.reader.decode.complete");
        getActivity().registerReceiver(this.myDataReceiver, this.filter);

        gson = new GsonBuilder().create();
        soundUtils = new SoundUtils();
        lstOutbound = new ArrayList<OutbountDTO>();
        lstTripSheetNo = new ArrayList<>();

        btnGo.setOnClickListener(this);
        btnCloseOne.setOnClickListener(this);
        btnItemNotFound.setOnClickListener(this);
        btnMaterialSkip.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);
        // btnMaterialDamaged.setVisibility(View.GONE);
        //rlPickListTwo.setVisibility(View.GONE);

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();
        lstSkipDto = new ArrayList<SkipReasonDTO>();


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

        spinnerSelectPickList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                pickRefNo = spinnerSelectPickList.getSelectedItem().toString().split("[-]", 2)[0];
                getTripSheetNumber();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        LoadPickRefnos();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {


            case R.id.btnOk:

                btnOk.setEnabled(false);
                DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                        "Are you sure you want to skip the suggested item?", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:

                                        SkipSuggestedItem();
                                        common.setIsPopupActive(false);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        btnMaterialSkip.setEnabled(true);
                                        btnOk.setEnabled(true);
                                        common.setIsPopupActive(false);
                                        break;
                                }

                            }
                        });
                common.setIsPopupActive(true);
                ProgressDialogUtils.closeProgressDialog();
                break;

            case R.id.btnCloseThree:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;


            case R.id.btnCloseOne:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnGo:
                rlPickListOne.setVisibility(View.GONE);
                if (pickRefNo != null) {
                    GetPickItem();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0043, getActivity(), getContext(), "Error");
                }
                break;

            case R.id.btnCancel:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnMaterialSkip:
                /*btnMaterialSkip.setEnabled(false);
                GetSkipReason();
                DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                        "Are you sure you want to skip the suggested item?", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        SkipSuggestedItem();
                                        common.setIsPopupActive(false);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        btnMaterialSkip.setEnabled(true);
                                        common.setIsPopupActive(false);
                                        break;
                                }

                            }
                        });
                common.setIsPopupActive(true);
                ProgressDialogUtils.closeProgressDialog();*/


                rlPickListOne.setVisibility(GONE);
                rlPickListTwo.setVisibility(GONE);
                rlSelect.setVisibility(View.VISIBLE);
                GetSkipReason();
                break;
            case R.id.btnItemNotFound:
                btnItemNotFound.setEnabled(false);
                DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                        "Are you sure you want to move the items  into LNF?", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                switch (which) {
                                    case DialogInterface.BUTTON_POSITIVE:
                                        MarkMaterialNotFound();
                                        common.setIsPopupActive(false);
                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        btnItemNotFound.setEnabled(true);
                                        common.setIsPopupActive(false);
                                        break;
                                }

                            }
                        });
                common.setIsPopupActive(true);
                ProgressDialogUtils.closeProgressDialog();
                break;
        }
    }


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

    public boolean Validations() {

        if (etPallet.getText().toString() == "") {
            return false;
        } else if (etLocation.getText().toString() == "") {
            return false;
        } else if (lblScannedSku.getText().toString() == "") {
            return false;
        } else {
            return true;
        }
    }

    public void ClearFields() {

        etLocation.setText("");
        etPallet.setText("");
        etSerial.setText("");
        lblLocationSuggested.setText("");
        lblPickedQty.setText("");
        lblRequiredQty.setText("");

        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
        ivScanLocation.setImageResource(R.drawable.fullscreen_img);

        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
        ivScanPallet.setImageResource(R.drawable.fullscreen_img);

        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);
    }

    //Assigning scanned value to the respective fields
    public void ProcessScannedinfo(String scannedData) {
        if (scannedData != null && !common.isPopupActive()) {
            if (scanValidator.IsPalletScanned(scannedData)) {

                etPallet.setText(scannedData);
                ValidatePalletCode();

            } else if (ScanValidator.IsItemScanned(scannedData)) {
                NewMcode = null;

                if (etSerial.getText().toString().equals(scannedData.split("[-]", 2)[1]) && !IsUsercanceled) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0034, getActivity(), getContext(), "Error");
                    lblScanNewRSN.setText("Scan New RSN");
                    IsUsercanceled = false;
                    return;
                }
                lblScanNewRSN.setText("Scan New RSN");

                if (!(etLocation.getText().toString().isEmpty())) {
                    if (etSerial.getText().toString().isEmpty()) {
                        lblScannedSku.setText(scannedData.split("[-]", 2)[0]);
                        etSerial.setText(scannedData.split("[-]", 2)[1]);
                        Mcode = scannedData;
                        IsuserConfirmed = false;
                        IsUsercanceled = false;
                        UpdatePickingItem();
                    } else {
                        if (lblScanNewRSN.getText().equals("Scan New RSN") && NewMcode == null) {
                            lblScanNewRSN.setText(scannedData);
                            IsUsercanceled = false;
                            NewMcode = scannedData;
                            UpdatePickingItem();
                        } else {
                            common.showUserDefinedAlertType(errorMessages.EMC_0034, getActivity(), getContext(), "Error");
                        }
                    }

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");

                    return;
                }


            } else if (scanValidator.IsLocationScanned(scannedData)) {

                if (IsStrictlycomplaince && lblLocationSuggested.getText() != "") {
                    if (lblLocationSuggested.getText().equals(scannedData)) {
                        etLocation.setText(scannedData);
                        ValidateLocationCode();
                    } else {
                        common.showUserDefinedAlertType(errorMessages.EMC_0013, getActivity(), getContext(), "Error");
                        return;
                    }
                } else {
                    etLocation.setText(scannedData);
                    ValidateLocationCode();
                }
            }
        }
    }



    public void getTripSheetNumber() {

        for (String TSnumber : lstTripSheetNo) {

            String pickListNo =  TSnumber.split("[-]", 2)[0].trim();

            if(pickRefNo.trim().equalsIgnoreCase(pickListNo)){
                tripSheet = TSnumber.split("[-]", 2)[1].trim();

            }
        }

    }

    public void Clearfields() {
        lblRequiredQty.setText("");
        lblPickedQty.setText("");
        lblSKUSuggested.setText("");
        lblLocationSuggested.setText("");
        lblDockNo.setText("");
        lblScannedSku.setText("");
        etSerial.setText("");
        etLocation.setText("");
        etPallet.setText("");

    }

    //padmaja 7/3/2018
    public void LoadPickRefnos() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            message.setEntityObject(outbountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetPickRefNo(message);
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
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();

                                    }
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                } else {
                                    List<LinkedTreeMap<?, ?>> _lPickRefNo = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lPickRefNo = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    if (_lPickRefNo.size() > 0) {


                                        List<String> lstPickRefNo = new ArrayList<>();
                                        List<OutbountDTO> lstDto = new ArrayList<OutbountDTO>();

                                        for (int i = 0; i < _lPickRefNo.size(); i++) {
                                            OutbountDTO dto = new OutbountDTO(_lPickRefNo.get(i).entrySet());
                                            lstDto.add(dto);
                                            lstOutbound.add(dto);
                                        }

                                        for (int i = 0; i < lstDto.size(); i++) {
                                            lstPickRefNo = lstDto.get(i).getPickRefNo();
                                            lstTripSheetNo = lstDto.get(i).getTripSheetRefNo();
                                        }

                                        if (lstPickRefNo == null) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.showUserDefinedAlertType(errorMessages.EMC_0035, getActivity(), getContext(), "Error");
                                        } else {
                                            ProgressDialogUtils.closeProgressDialog();
                                            ArrayAdapter arrayAdapterPickList = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstPickRefNo);
                                            spinnerSelectPickList.setAdapter(arrayAdapterPickList);
                                        }
                                    }
                                }
                            }
                            ProgressDialogUtils.closeProgressDialog();
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

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
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

    public void GetPickItem() {

        try {
            etSerial.setText("");

            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
            ivScanPallet.setImageResource(R.drawable.fullscreen_img);
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            final OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setSelectedPickRefNumber(pickRefNo);
            outbountDTO.setSelectedTripsheetRefNumber(tripSheet);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetPickItem(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_01", getActivity());
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


                                        }
                                        ProgressDialogUtils.closeProgressDialog();

                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03")
                                                || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04")) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            Clearfields();
                                        }

                                    } else {

                                        rlPickListTwo.setVisibility(View.VISIBLE);
                                        List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                        _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                        List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                        OutbountDTO oOutboundDTO = null;
                                        for (int i = 0; i < _lstPickitem.size(); i++) {

                                            oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                            _lstOutboundDTO.add(oOutboundDTO);


                                            if (oOutboundDTO.getRequiredQty().equals("0")) {
                                                ProgressDialogUtils.closeProgressDialog();

                                                common.showUserDefinedAlertType(errorMessages.EMC_0036, getActivity(), getContext(), "Warning");
                                                btnItemNotFound.setVisibility(GONE);
                                                btnMaterialSkip.setVisibility(GONE);
                                                lblSKUSuggested.setText("");
                                                lblScannedSku.setText("");
                                                lblScanNewRSN.setText("");
                                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                                ivScanSku.setImageResource(R.drawable.fullscreen_img);
                                                lblDesc.setText("");

                                                ClearFields();
                                            } else {

                                                ProgressDialogUtils.closeProgressDialog();
                                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                                ivScanSku.setImageResource(R.drawable.fullscreen_img);
                                                btnMaterialSkip.setVisibility(View.VISIBLE);
                                                lblDockNo.setText(oOutboundDTO.getDockNumber());
                                                lblLocationSuggested.setText(oOutboundDTO.getLocation());
                                                lblSKUSuggested.setText(oOutboundDTO.getSKU());
                                                lblRequiredQty.setText(oOutboundDTO.getRequiredQty());
                                                lblPickedQty.setText(oOutboundDTO.getPickedQty());
                                                SuggestedId = oOutboundDTO.getSuggestionID();

                                                selectedPickRefNo = oOutboundDTO.getSelectedPickRefNumber();

                                                if(pickRefNo.startsWith("TS")){
                                                    lblPickListNo.setText(pickRefNo);
                                                }else {
                                                    lblPickListNo.setText(pickRefNo + "-" + oOutboundDTO.getCustomerCode());
                                                }
                                                lblDesc.setText(oOutboundDTO.getMaterialDescription());
                                                if (!lblLocationSuggested.getText().toString().equals(etLocation.getText().toString())) {
                                                    etLocation.setText("");
                                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                                    ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                                                }
                                                if (!lblSKUSuggested.getText().toString().equals(lblScannedSku.getText().toString())) {
                                                    etSerial.setText("");
                                                    lblScannedSku.setText("");
                                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                                    ivScanSku.setImageResource(R.drawable.fullscreen_img);
                                                }
                                                if (oOutboundDTO.getStrictComplianceToPicking()) {
                                                    IsStrictlycomplaince = true;
                                                }
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                            ProgressDialogUtils.closeProgressDialog();
                            if(pickRefNo.startsWith("PL")) {
                                common.showUserDefinedAlertType("There are no materials in this pick list", getActivity(), getContext(), "Warning");
                                ClearFields();
                            }else {
                                common.showUserDefinedAlertType("There are no materials in this trip sheet", getActivity(), getContext(), "Warning");
                                ClearFields();
                            }
                            } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    //GetSkipReason
    public void GetSkipReason() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            message.setEntityObject(outbountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetSkipReason(message);
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
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();

                                    }
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    rlPickListOne.setVisibility(GONE);
                                    rlPickListTwo.setVisibility(View.VISIBLE);
                                    rlSelect.setVisibility(View.GONE);
                                    btnOk.setEnabled(true);
                                } else {
                                    List<LinkedTreeMap<?, ?>> _lSkipReason = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lSkipReason = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    if (_lSkipReason.size() > 0) {


                                        List<String> lstSkipData = new ArrayList<>();

                                        lstSkipDto.clear();
                                        for (int i = 0; i < _lSkipReason.size(); i++) {
                                            SkipReasonDTO dto = new SkipReasonDTO(_lSkipReason.get(i).entrySet());
                                            lstSkipDto.add(dto);
                                        }
                                        lstSkipData.clear();
                                        for (int i = 0; i < lstSkipDto.size(); i++) {
                                            lstSkipData.add(lstSkipDto.get(i).getSkipReason());
                                        }

                                        if (lstSkipData == null) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.showUserDefinedAlertType(errorMessages.EMC_0050, getActivity(), getContext(), "Error");
                                        } else {
                                            ProgressDialogUtils.closeProgressDialog();
                                            ArrayAdapter arrayAdapterPickList = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstSkipData);
                                            spinnerSelectReason.setAdapter(arrayAdapterPickList);
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

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_05", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_05", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public void ValidatePalletCode() {
        try {
            if (etPallet.getText().toString().equals("")) {
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0019);
                return;
            }


            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outboundDTO = new OutbountDTO();
            outboundDTO.setPalletNo(etPallet.getText().toString());
            message.setEntityObject(outboundDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidatePalletAtPicking(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_01", getActivity());
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


                                } else {


                                    LinkedTreeMap<String, String> _lResult = new LinkedTreeMap<String, String>();
                                    _lResult = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _lResult.entrySet()) {
                                        if (entry.getKey().equals("Result")) {
                                            String Result = entry.getValue();
                                            if (Result.equals("0")) {
                                                etPallet.setText("");
                                                ProgressDialogUtils.closeProgressDialog();
                                                common.showUserDefinedAlertType(errorMessages.EMC_0030, getActivity(), getContext(), "Error");
                                                return;
                                            } else {
                                                ProgressDialogUtils.closeProgressDialog();
                                                cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                ivScanPallet.setImageResource(R.drawable.check);
                                            }
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
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                        return;
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            return;
        }
    }

    /*Validating the LocationCode*/
    public void ValidateLocationCode() {
        try {
            if (lblLocationSuggested.getText().toString().equals("")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0048, getActivity(), getContext(), "Error");
                etLocation.setText("");
                return;
            }
            if (etLocation.getText().toString().equals("")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0007, getActivity(), getContext(), "Error");
                return;
            }
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setLocation(etLocation.getText().toString());

            message.setEntityObject(outbountDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidateLocationAtPicking(message);
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
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
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
                                        cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanLocation.setImageResource(R.drawable.warning_img);
                                    }


                                } else {
                                    LinkedTreeMap<String, String> _lResult = new LinkedTreeMap<String, String>();
                                    _lResult = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _lResult.entrySet()) {
                                        if (entry.getKey().equals("Result")) {
                                            String Result = entry.getValue();
                                            if (Result.equals("0")) {
                                                etLocation.setText("");
                                                ProgressDialogUtils.closeProgressDialog();
                                                common.showUserDefinedAlertType(errorMessages.EMC_0028, getActivity(), getContext(), "Error");
                                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                ivScanLocation.setImageResource(R.drawable.warning_img);
                                                return;
                                            } else {
                                                ProgressDialogUtils.closeProgressDialog();
                                                cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.white));
                                                ivScanLocation.setImageResource(R.drawable.check);
                                            }
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
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                        return;
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "005_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
            return;
        }
    }


    public void UpdatePickingItem() {
        try {
            common.setIsPopupActive(true);
            if (lblSKUSuggested.getText().equals("")) {
                common.showUserDefinedAlertType(errorMessages.EMC_0047, getActivity(), getContext(), "Error");
                lblScannedSku.setText("");
                return;
            }
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();

            if(pickRefNo.startsWith("PL")) {
                inventoryDTO.setReferenceDocumentNumber(pickRefNo);  // pick list no.
            }else {
                inventoryDTO.setReferenceDocumentNumber(selectedPickRefNo);   // pick list no. under a trip sheet
            }

            inventoryDTO.setUserId(Integer.parseInt(userId));
            inventoryDTO.setMaterialCode(lblScannedSku.getText().toString());
            inventoryDTO.setRSN(Mcode);
            inventoryDTO.setLocationCode(etLocation.getText().toString());
            inventoryDTO.setContainerCode(etPallet.getText().toString());
            inventoryDTO.setSuggestionID(SuggestedId);

            if (IsuserConfirmed) {
                if (etQty.getText().toString().isEmpty()) {
                    common.showUserDefinedAlertType(errorMessages.EMC_0037, getActivity(), getContext(), "Error");
                    lblScanNewRSN.setText("Scan New RSN");
                    return;
                }

                if (etQty.getText().toString().equals("0")) {

                    common.showUserDefinedAlertType(errorMessages.EMC_0038, getActivity(), getContext(), "Error");


                    lblScanNewRSN.setText("Scan New RSN");
                    return;
                }
                int Qty = Integer.parseInt(etQty.getText().toString());
                int requiredqty = Integer.parseInt(lblRequiredQty.getText().toString().split("[.]", 2)[0]);
                if (Qty <= requiredqty) {
                    inventoryDTO.setNewRSN(NewMcode);
                    inventoryDTO.setQuantity(etQty.getText().toString());
                    inventoryDTO.setUserConfirmedExcessTransaction(true);
                } else {

                    common.showUserDefinedAlertType(errorMessages.EMC_0039, getActivity(), getContext(), "Error");
                    lblScanNewRSN.setText("Scan New RSN");
                    common.setIsPopupActive(false);
                    return;
                }
            }
            message.setEntityObject(inventoryDTO);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpdatePickingItem(message);

                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_01", getActivity());
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
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    ProgressDialogUtils.closeProgressDialog();
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.warning_img);

                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        etQty.setVisibility(View.GONE);
                                        lblScanNewRSN.setVisibility(View.GONE);
                                        cvScanNewRSN.setVisibility(View.GONE);
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_020")) {
                                            lblScanNewRSN.setText("Scan New RSN");
                                        }
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03")
                                                || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04")) {
                                            Clearfields();
                                        }

                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_BL_017")) {
                                            IsuserConfirmed = true;
                                            lblScanNewRSN.setText("Scan New RSN");
                                            etQty.setText("");
                                        }
                                        if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                            DialogUtils.showConfirmDialog(getActivity(), "Confirm",
                                                    owmsExceptionMessage.getWMSMessage(), new DialogInterface.OnClickListener() {

                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {

                                                            switch (which) {
                                                                case DialogInterface.BUTTON_POSITIVE:
                                                                    common.setIsPopupActive(false);
                                                                    etQty.setVisibility(View.VISIBLE);
                                                                    lblScanNewRSN.setVisibility(View.VISIBLE);
                                                                    cvScanNewRSN.setVisibility(View.VISIBLE);
                                                                    break;

                                                                case DialogInterface.BUTTON_NEGATIVE:
                                                                    etSerial.setText("");
                                                                    lblScannedSku.setText("");
                                                                    IsuserConfirmed = false;
                                                                    IsUsercanceled = true;
                                                                    common.setIsPopupActive(false);
                                                                    break;
                                                            }

                                                        }
                                                    });
                                            common.setIsPopupActive(true);

                                            return;
                                        }

                                    }
                                    etSerial.setText("");
                                    lblScannedSku.setText("");

                                } else {

                                    List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<InventoryDTO> lstDto = new ArrayList<InventoryDTO>();
                                    InventoryDTO dto = null;
                                    if (_lInventory != null && _lInventory.size() > 0) {
                                        for (int i = 0; i < _lInventory.size(); i++) {

                                            dto = new InventoryDTO(_lInventory.get(i).entrySet());
                                            lstDto.add(dto);

                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanSku.setImageResource(R.drawable.check);

                                        etQty.setText("");
                                        etQty.setVisibility(GONE);
                                        cvScanNewRSN.setVisibility(GONE);
                                        lblScanNewRSN.setText("Scan New RSN");
                                        lblScanNewRSN.setVisibility(GONE);
                                        common.setIsPopupActive(false);

                                        GetPickItem();
                                    }

                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "006_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void MarkMaterialNotFound()
    {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setLocation(lblLocationSuggested.getText().toString());
            outbountDTO.setDockNumber(lblDockNo.getText().toString());
            outbountDTO.setSKU(lblSKUSuggested.getText().toString());
            outbountDTO.setRequiredQty(lblRequiredQty.getText().toString());
            outbountDTO.setPickedQty(lblPickedQty.getText().toString());
            outbountDTO.setPalletNo(etPallet.getText().toString());
            outbountDTO.setSelectedPickRefNumber(lblPickListNo.getText().toString().split("[-]", 2)[0]);
            outbountDTO.setSuggestionID(SuggestedId);
            outbountDTO.setSelectedPickRefNumber(lblPickListNo.getText().toString().split("[-]", 2)[0]);
            message.setEntityObject(outbountDTO);
            message.setEntityObject(outbountDTO);

            ProgressDialogUtils.showProgressDialog("Please Wait");

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.MarkMaterialNotFound(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_01", getActivity());
                    common.setIsPopupActive(false);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");

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

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                        ProgressDialogUtils.closeProgressDialog();

                                    }
                                    common.setIsPopupActive(true);
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03")
                                            || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IN_DAL_001")) {
                                        Clearfields();
                                        GetPickItem();
                                    }

                                    btnItemNotFound.setEnabled(true);
                                } else {

                                    rlPickListTwo.setVisibility(View.VISIBLE);
                                    List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                    OutbountDTO oOutboundDTO = null;
                                    for (int i = 0; i < _lstPickitem.size(); i++) {

                                        oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                        _lstOutboundDTO.add(oOutboundDTO);

                                    }
                                    if (oOutboundDTO.getRequiredQty().equals("0")) {
                                        ProgressDialogUtils.closeProgressDialog();

                                        common.showUserDefinedAlertType(errorMessages.EMC_0036, getActivity(), getContext(), "Warning");
                                        btnItemNotFound.setVisibility(GONE);
                                        btnMaterialSkip.setVisibility(GONE);
                                        lblSKUSuggested.setText("");
                                        lblScannedSku.setText("");
                                        lblScanNewRSN.setText("");
                                        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                        ivScanSku.setImageResource(R.drawable.fullscreen_img);
                                        lblDesc.setText("");

                                        ClearFields();
                                    } else {
                                        ProgressDialogUtils.closeProgressDialog();

                                        lblDockNo.setText(oOutboundDTO.getDockNumber());
                                        lblLocationSuggested.setText(oOutboundDTO.getLocation());
                                        lblSKUSuggested.setText(oOutboundDTO.getSKU());
                                        lblRequiredQty.setText(oOutboundDTO.getRequiredQty());
                                        lblPickedQty.setText(oOutboundDTO.getPickedQty());
                                        lblDesc.setText(oOutboundDTO.getMaterialDescription());
                                        SuggestedId = oOutboundDTO.getSuggestionID();
                                        lblPickListNo.setText(pickRefNo + "-" + oOutboundDTO.getCustomerCode());
                                        if (oOutboundDTO.getStrictComplianceToPicking()) {
                                            IsStrictlycomplaince = true;
                                        }
                                    }
                                }
                                btnItemNotFound.setEnabled(true);
                            }
                        } catch (Exception ex) {

                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_02", getActivity());
                                common.setIsPopupActive(false);
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_03", getActivity());
                    common.setIsPopupActive(false);
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_04", getActivity());
                common.setIsPopupActive(false);
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }


    public void GetSkipReasonId() {
        for (SkipReasonDTO oSkipItem : lstSkipDto) {
            if (oSkipItem.getSkipReason().equals(skipReason)) {
                skipReasonId = oSkipItem.getSkipReasonID();
            }
        }
    }

    public void SkipSuggestedItem() {

        try {
            etSerial.setText("");
            cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
            ivScanSku.setImageResource(R.drawable.fullscreen_img);
            cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
            ivScanPallet.setImageResource(R.drawable.fullscreen_img);
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO oOutboundDTO = new OutbountDTO();
            SkipReasonDTO oSkipReason = new SkipReasonDTO();

            if(pickRefNo.startsWith("PL")) {
                oOutboundDTO.setSelectedPickRefNumber(pickRefNo); // pick list no
            }else {
                oOutboundDTO.setSelectedPickRefNumber(selectedPickRefNo);   // pick list no. under a trip sheet
            }

            oOutboundDTO.setSuggestionID(SuggestedId);
            oOutboundDTO.setUserId(userId);
            //oOutboundDTO.setSelectedTripsheetRefNumber(tripSheet);
            oSkipReason.setSkipReasonID(skipReasonId);
            oSkipReason.setSkipReason(skipReason);
            oOutboundDTO.setoSkipReasonDTO(oSkipReason);
            message.setEntityObject(oOutboundDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.SkipSuggestedItem(message);
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


                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03")
                                                || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04")) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            Clearfields();
                                        }
                                        rlPickListOne.setVisibility(GONE);
                                        rlPickListTwo.setVisibility(View.VISIBLE);
                                        rlSelect.setVisibility(View.GONE);
                                        btnOk.setEnabled(true);
                                    } else {

                                        rlPickListTwo.setVisibility(View.VISIBLE);
                                        List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                        _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                        List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                        OutbountDTO oOutboundDTO = null;
                                        for (int i = 0; i < _lstPickitem.size(); i++) {

                                            oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                            _lstOutboundDTO.add(oOutboundDTO);


                                            if (oOutboundDTO.getRequiredQty().equals("0")) {
                                                ProgressDialogUtils.closeProgressDialog();

                                                common.showUserDefinedAlertType(errorMessages.EMC_0036, getActivity(), getContext(), "Warning");
                                                btnItemNotFound.setVisibility(GONE);
                                                btnMaterialSkip.setVisibility(GONE);
                                                lblSKUSuggested.setText("");
                                                lblScannedSku.setText("");
                                                lblScanNewRSN.setText("");
                                                lblDesc.setText("");
                                                rlPickListOne.setVisibility(GONE);
                                                rlPickListTwo.setVisibility(View.VISIBLE);
                                                rlSelect.setVisibility(View.GONE);
                                                btnOk.setEnabled(true);
                                                ClearFields();
                                            } else {

                                                ProgressDialogUtils.closeProgressDialog();

                                                lblDockNo.setText(oOutboundDTO.getDockNumber());
                                                lblLocationSuggested.setText(oOutboundDTO.getLocation());
                                                lblSKUSuggested.setText(oOutboundDTO.getSKU());
                                                lblRequiredQty.setText(oOutboundDTO.getRequiredQty());
                                                lblPickedQty.setText(oOutboundDTO.getPickedQty());
                                                SuggestedId = oOutboundDTO.getSuggestionID();
                                                lblPickListNo.setText(pickRefNo + "-" + oOutboundDTO.getCustomerCode());
                                                lblDesc.setText(oOutboundDTO.getMaterialDescription());
                                                if (!lblLocationSuggested.getText().toString().equals(etLocation.getText().toString())) {
                                                    etLocation.setText("");
                                                    cvScanLocation.setCardBackgroundColor(getResources().getColor(R.color.locationColor));
                                                    ivScanLocation.setImageResource(R.drawable.fullscreen_img);
                                                }
                                                if (!lblSKUSuggested.getText().toString().equals(lblScannedSku.getText().toString())) {
                                                    etSerial.setText("");
                                                    lblScannedSku.setText("");
                                                    cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                                    ivScanSku.setImageResource(R.drawable.fullscreen_img);
                                                }
                                                if (oOutboundDTO.getStrictComplianceToPicking()) {
                                                    IsStrictlycomplaince = true;
                                                }
                                                btnMaterialSkip.setEnabled(true);
                                                rlPickListOne.setVisibility(GONE);
                                                rlPickListTwo.setVisibility(View.VISIBLE);
                                                rlSelect.setVisibility(View.GONE);

                                                btnOk.setEnabled(true);
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
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
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    public void MarkMaterialDamaged() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            outbountDTO.setLocation(lblLocationSuggested.getText().toString());
            outbountDTO.setDockNumber(lblDockNo.getText().toString());
            outbountDTO.setSKU(lblSKUSuggested.getText().toString());
            outbountDTO.setRequiredQty(lblRequiredQty.getText().toString());
            outbountDTO.setPickedQty(lblPickedQty.getText().toString());
            outbountDTO.setPalletNo(etPallet.getText().toString());
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.MarkMaterialDamaged(message);
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;

                // }

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "008_01", getActivity());
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
                                if ((core.getType().toString().equals("Exception"))) {
                                    List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                    WMSExceptionMessage owmsExceptionMessage = null;
                                    for (int i = 0; i < _lExceptions.size(); i++) {

                                        owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());


                                    }
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                } else {

                                    rlPickListTwo.setVisibility(View.VISIBLE);
                                    List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                    OutbountDTO oOutboundDTO = null;
                                    for (int i = 0; i < _lstPickitem.size(); i++) {

                                        oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                        _lstOutboundDTO.add(oOutboundDTO);

                                    }

                                    ProgressDialogUtils.closeProgressDialog();
                                    lblDockNo.setText(oOutboundDTO.getDockNumber());
                                    lblLocationSuggested.setText(oOutboundDTO.getLocation());
                                    lblSKUSuggested.setText(oOutboundDTO.getSKU());
                                    lblRequiredQty.setText(oOutboundDTO.getRequiredQty());
                                    lblPickedQty.setText(oOutboundDTO.getPickedQty());
                                    lblPickListNo.setText(pickRefNo + "-" + oOutboundDTO.getCustomerCode());
                                    if (oOutboundDTO.getStrictComplianceToPicking()) {
                                        IsStrictlycomplaince = true;
                                    }
                                }
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "008_02", getActivity());
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
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "008_03", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "008_04", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
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
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_01", getActivity());
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
                        DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0001);
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
                DialogUtils.showAlertDialog(getActivity(), errorMessages.EMC_0002);
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_04", getActivity());
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_picking));
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

        if (spinnerSelectReason.getSelectedItem() != null && spinnerSelectReason.getSelectedItem() != "") {
            skipReason = spinnerSelectReason.getSelectedItem().toString();
            if (skipReason != null) {
                GetSkipReasonId();
            }
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}