package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cipherlab.barcode.GeneralString;
import com.example.karthikm.merlinwmscipher_vip_rdc.Activities.MainActivity;
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

public class PackingDetailsFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener {

    private static final String classCode = "API_FRAG_012";

    private View rootView;
    Button  btnpackComplete, btnObdComplete, btnGeneratePackSlip, btnCloseTwo,btnPrintDialog;
    TextView lblPackListNo, lblScannedSku;
    CardView cvScanPackingSlip, cvScanSku;
    ImageView ivScanPackingSlip, ivScanSku;
    SearchableSpinner selectPrinter;

    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private Gson gson;
    String userId = null;
    private Common common;
    private WMSCoreMessage core;

    private String packRefNo = "", packSlip = "",printer ="";
    int count = 0;
    private ScanValidator scanValidator;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    EditText etRSN, etPackSlip, etpendingQty;
    private Dialog dialog;

    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;


    int avaialbleQty = 0;

    // Cipher Barcode Scanner
    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public PackingDetailsFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_packing_details, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    private void loadFormControls() {

        lblPackListNo = (TextView) rootView.findViewById(R.id.lblPackListNo);

        etPackSlip = (EditText) rootView.findViewById(R.id.etPackSlip);
        etRSN = (EditText) rootView.findViewById(R.id.etRSN);
        etpendingQty = (EditText) rootView.findViewById(R.id.etpendingQty);

        btnpackComplete = (Button) rootView.findViewById(R.id.btnpackComplete);
        btnObdComplete = (Button) rootView.findViewById(R.id.btnObdComplete);
        btnGeneratePackSlip = (Button) rootView.findViewById(R.id.btnGeneratePackSlip);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);

        cvScanPackingSlip = (CardView) rootView.findViewById(R.id.cvScanPackingSlip);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);

        ivScanPackingSlip = (ImageView) rootView.findViewById(R.id.ivScanPackingSlip);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);

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

        btnpackComplete.setOnClickListener(this);
        btnObdComplete.setOnClickListener(this);
        btnGeneratePackSlip.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);

        common = new Common();
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        errorMessages = new ErrorMessages();

        packRefNo = getArguments().getString("packRefNo");

        lblPackListNo.setText(packRefNo);


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

            case R.id.btnpackComplete:


                break;

            case R.id.btnObdComplete:


                break;

            case R.id.btnGeneratePackSlip:

                dialog = new Dialog(getContext()) ;
                dialog.setContentView(R.layout.aleart_dialog_printer);// set layout to your dialog

                ArrayList printers = new ArrayList();

                printers.add("Printer");
                printers.add("One");
                printers.add("Two");


                selectPrinter = (SearchableSpinner) dialog.findViewById(R.id.spinnerSelectPrinter);
                btnPrintDialog = (Button) dialog.findViewById(R.id.btnPrintDialog);

                ArrayAdapter selectPrinterAdp = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, printers);
                selectPrinter.setAdapter(selectPrinterAdp);


                dialog.show();

                btnPrintDialog.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getContext(),printer,Toast.LENGTH_SHORT).show();
                    }
                });

                selectPrinter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        printer = selectPrinter.getSelectedItem().toString();

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });

                break;

            case R.id.btnCloseTwo:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
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

        if (common.isPopupActive()) {

        } else if (scannedData != null && !common.isPopupActive()) {
            if (ScanValidator.IsItemScanned(scannedData)) {
                if (!etPackSlip.getText().toString().isEmpty()) {

                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0035, getActivity(), getContext(), "Warning");
                }
            } /*else if (scanValidator.IsPackingSlipScanned(scannedData)) {
                etPackSlip.setText(scannedData);
                ValidatetPackingSlip();
            }*/
        } else {
            common.showUserDefinedAlertType(errorMessages.EMC_0043, getActivity(), getContext(), "Error");

        }
    }


    /*public void ValidatetPackingSlip() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outboundDTO = new OutbountDTO();
            outboundDTO.setOutboundID(packRefNo);

            message.setEntityObject(outboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidatetPackingSlip(message);
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
                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {
                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());

                                    cvScanPackingSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPackingSlip.setImageResource(R.drawable.warning_img);
                                    etPackSlip.setText("");

                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lPalletInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPalletInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if (_lPalletInventory != null) {
                                    if (_lPalletInventory.size() > 0) {
                                        cvScanPackingSlip.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPackingSlip.setImageResource(R.drawable.check);
                                        ProgressDialogUtils.closeProgressDialog();
                                    } else {
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showUserDefinedAlertType(errorMessages.EMC_0028, getActivity(), getContext(), "Warning");
                                        return;
                                    }
                                }
                            }
                        } else {
                            ProgressDialogUtils.closeProgressDialog();
                            common.showUserDefinedAlertType(errorMessages.EMC_0021, getActivity(), getContext(), "Error");
                            return;
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
    }*/


    /*public void UpsertPackedItems(String priorityLevel) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();

            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpsertPackedItems(message);

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
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
                                if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IN_DAL_001")) {

                                }

                            } else {
                                List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                OutbountDTO oOutboundDTO = null;
                                for (int i = 0; i < _lstPickitem.size(); i++) {
                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    _lstOutboundDTO.add(oOutboundDTO);
                                }
                                for (int i = 0; i < _lstPickitem.size(); i++) {

                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    _lstOutboundDTO.add(oOutboundDTO);

                                }
                                //If Picking is succeful clear all the feilds
                                //Item Pick is complte please re scan
                                // For Re scan Location pallet and rsn

                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                ivScanSku.setImageResource(R.drawable.fullscreen_img);
                                etRSN.setText("");
                                etpendingQty.setText("");
                                btnpackComplete.setEnabled(true);
                                common.showUserDefinedAlertType(errorMessages.EMC_0046, getActivity(), getContext(), "Success");
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_02", getActivity());
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
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }*/

    /*public void UpsertCompletedPackingSlip() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();

            message.setEntityObject(outbountDTO);
            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpsertCompletedPackingSlip(message);

            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
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
                                if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IN_DAL_001")) {

                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                OutbountDTO oOutboundDTO = null;
                                for (int i = 0; i < _lstPickitem.size(); i++) {
                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    _lstOutboundDTO.add(oOutboundDTO);
                                }
                                for (int i = 0; i < _lstPickitem.size(); i++) {

                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    _lstOutboundDTO.add(oOutboundDTO);

                                }
                                //If Picking is succeful clear all the feilds
                                //Item Pick is complte please re scan
                                // For Re scan Location pallet and rsn

                                cvScanPackingSlip.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                ivScanPackingSlip.setImageResource(R.drawable.fullscreen_img);
                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                etRSN.setText("");
                                etpendingQty.setText("");
                                btnObdComplete.setEnabled(true);

                                common.showUserDefinedAlertType("Pack successful for " + packRefNo, getActivity(), getContext(), "Success");
                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_02", getActivity());
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
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }*/

    /*public void UpsertOBDPackingComplete() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();

            message.setEntityObject(outbountDTO);
            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.UpsertOBDPackingComplete(message);
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_01", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
            }
            try {
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);
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
                                if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC02") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC03") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC01") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_OB_DAL_PICKSugg_EC04") || owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IN_DAL_001")) {

                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lstPickitem = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lstPickitem = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                List<OutbountDTO> _lstOutboundDTO = new ArrayList<OutbountDTO>();
                                OutbountDTO oOutboundDTO = null;
                                for (int i = 0; i < _lstPickitem.size(); i++) {
                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    _lstOutboundDTO.add(oOutboundDTO);
                                }
                                for (int i = 0; i < _lstPickitem.size(); i++) {
                                    oOutboundDTO = new OutbountDTO(_lstPickitem.get(i).entrySet());
                                    _lstOutboundDTO.add(oOutboundDTO);
                                }
                                //If Picking is succeful clear all the feilds
                                //Item Pick is complte please re scan
                                // For Re scan Location pallet and rsn

                                cvScanPackingSlip.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                ivScanPackingSlip.setImageResource(R.drawable.fullscreen_img);
                                cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
                                ivScanSku.setImageResource(R.drawable.fullscreen_img);

                                etRSN.setText("");
                                etpendingQty.setText("");

                                common.showUserDefinedAlertType(errorMessages.EMC_0047, getActivity(), getContext(), "Success");

                                ProgressDialogUtils.closeProgressDialog();
                            }
                        } catch (Exception ex) {
                            try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "007_02", getActivity());
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
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }*/


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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_packing));
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
