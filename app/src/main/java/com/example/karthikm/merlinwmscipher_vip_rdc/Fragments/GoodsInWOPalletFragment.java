package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
import android.widget.TextView;
import android.widget.Toast;

import com.cipherlab.barcode.GeneralString;
import com.example.karthikm.merlinwmscipher_vip_rdc.Activities.MainActivity;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.ColorDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InboundDTO;
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
 * Created by Prasanna.ch on 05/08/2018
 *
 */

public class GoodsInWOPalletFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener, View.OnLongClickListener {

    private static final String classCode = "API_FRAG_002A";
    private View rootView;
    private TextView lblVehicleNo, tvPalletQty, lblStoreRefNo, lblInboundQty, lblScannedSku, lblDesc, lblPalletQty,tvScanPallet;
    private CardView cvScanDock, cvScanPallet, cvScanSku;
    private ImageView ivScanDock, ivScanPallet, ivScanSku;
    private TextInputLayout txtInputLayoutDock, txtInputLayoutPallet, txtInputLayoutSerial, txtInputLayoutMfgDate, txtInputLayoutQty, txtInputLayoutMRP;
    private EditText etDock, etStoreRefNo, etSerial, etMfgDate, etMRP, etQty, etBoxcount;
    private SearchableSpinner spinnerSelectColor, spinnerSelectSloc;
    private Button btnUpdate, btnReceiveComplete, btnClose;
    private String MRP = null, MOP = null, Qty = null, MfgDate = null;
    DialogUtils dialogUtils;
    FragmentUtils fragmentUtils;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private WMSCoreMessage core;
    private String Materialcode = null, VehicleReceivedQty = null, VehicleInventoryQty = null;
    String userId = null;
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String storageloc = null, color = null, DefaultSLoc = null, wareHouseID = "", tenantID = "";
    List<List<ColorDTO>> lstColorData;
    ArrayList<String> sloc;
    ArrayList<String> lstcolor;
    SoundUtils soundUtils = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    boolean IsuserConfirmed = false, IsuserConfirmedRedo = false, IsAssorted = false;
    private String POHeaderID = "", accountID = "";
    ToneGenerator toneGenerator;
    private ErrorMessages errorMessages;

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim());
        }
    };

    public GoodsInWOPalletFragment() { }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_goodsin_without_pallet, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        lblVehicleNo = (TextView) rootView.findViewById(R.id.lblVehicleNo);
        lblPalletQty = (TextView) rootView.findViewById(R.id.lblPalletQty);
        lblStoreRefNo = (TextView) rootView.findViewById(R.id.lblStoreRefNo);
        lblInboundQty = (TextView) rootView.findViewById(R.id.lblInboundQty);
        lblScannedSku = (TextView) rootView.findViewById(R.id.lblScannedSku);
        lblDesc = (TextView) rootView.findViewById(R.id.lblScannedSkuItem);
        IsuserConfirmed = false;
        IsuserConfirmedRedo = false;
        cvScanDock = (CardView) rootView.findViewById(R.id.cvScanDock);
        cvScanPallet = (CardView) rootView.findViewById(R.id.cvScanPallet);
        cvScanSku = (CardView) rootView.findViewById(R.id.cvScanSku);

        ivScanDock = (ImageView) rootView.findViewById(R.id.ivScanDock);
        ivScanPallet = (ImageView) rootView.findViewById(R.id.ivScanPallet);
        ivScanSku = (ImageView) rootView.findViewById(R.id.ivScanSku);

        txtInputLayoutDock = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutDock);
        txtInputLayoutPallet = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutPallet);
        txtInputLayoutSerial = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutSerial);
        txtInputLayoutMfgDate = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMfgDate);
        txtInputLayoutQty = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutQty);
        txtInputLayoutMRP = (TextInputLayout) rootView.findViewById(R.id.txtInputLayoutMRP);

        etDock = (EditText) rootView.findViewById(R.id.etDock);
        etStoreRefNo = (EditText) rootView.findViewById(R.id.etStoreRefNo);
        etSerial = (EditText) rootView.findViewById(R.id.etSerial);
        etMfgDate = (EditText) rootView.findViewById(R.id.etMfgDate);
        etQty = (EditText) rootView.findViewById(R.id.etQty);
        etMRP = (EditText) rootView.findViewById(R.id.etMRP);
        tvPalletQty = (TextView) rootView.findViewById(R.id.tvPalletQty);
        tvPalletQty.setVisibility(View.GONE);
        spinnerSelectColor = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectColor);
        spinnerSelectSloc = (SearchableSpinner) rootView.findViewById(R.id.spinnerSelectSloc);
        spinnerSelectColor.setOnItemSelectedListener(this);
        spinnerSelectSloc.setOnItemSelectedListener(this);

        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnReceiveComplete = (Button) rootView.findViewById(R.id.btnReceiveComplete);
        btnUpdate = (Button) rootView.findViewById(R.id.btnUpdate);

        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        btnClose.setOnClickListener(this);
        btnReceiveComplete.setOnClickListener(this);
        btnUpdate.setOnClickListener(this);
        btnUpdate.setOnLongClickListener(this);
        soundUtils = new SoundUtils();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
        sloc = new ArrayList<>();
        lstcolor = new ArrayList<>();
        lstColorData = new ArrayList<>();
        common = new Common();
        errorMessages = new ErrorMessages();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IsuserConfirmedRedo = true;
                //Toast.makeText(getActivity(),"Shortclick",Toast.LENGTH_SHORT).show();
                ValidateRSNAndReceive();
            }
        });

       /* btnUpdate.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {

                btnUpdate.setBackgroundResource(R.drawable.button_shape_red);
                btnUpdate.setText("Remove");

                DialogUtils.showConfirmDialog(getActivity(), "Confirm Remove", "Do you want to remove?", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                //Toast.makeText(getActivity(),"Pressed ok..!",Toast.LENGTH_LONG).show();
                                btnUpdate.setBackgroundResource(R.drawable.button_shape);
                                btnUpdate.setText("Update");
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //Toast.makeText(getActivity(),"Pressed cancel..!",Toast.LENGTH_LONG).show();
                                btnUpdate.setBackgroundResource(R.drawable.button_shape);
                                btnUpdate.setText("Update");
                                break;
                        }
                    }
                });
                return true;
            }
        });*/

        lblVehicleNo.setText(getArguments().getString("Vehicleno"));
        lblStoreRefNo.setText(getArguments().getString("Storefno"));
        etStoreRefNo.setText(getArguments().getString("Storefno"));
        wareHouseID = getArguments().getString("wareHouseID");
        tenantID = getArguments().getString("tenantID");
        DefaultSLoc = getArguments().getString("DefaultSLOC");
        VehicleReceivedQty = getArguments().getString("VehicleReceivedQty");
        VehicleInventoryQty = getArguments().getString("VehicleInventoryQty");
        accountID = getArguments().getString("accountID");
        POHeaderID = getArguments().getString("POHeaderID");
        //DialogUtils.showAlertDialog(getActivity(),VehicleInventoryQty+"/"+VehicleReceivedQty);
        if (getArguments() != null) {
            if (getArguments().getString("SLOC") != null) {
                try {

                    JSONArray jsonArray = new JSONArray(getArguments().getString("SLOC"));

                    for (int i = 0; i < jsonArray.length(); i++) {
                        sloc.add(jsonArray.getString(i));
                    }
                } catch (Exception ex) {
                    System.out.println("");
                }

            }
            if (getArguments().getString("ColorCodejson") != null) {
                try {

                    JSONArray colorcodeArray = new JSONArray(getArguments().getString("ColorCodejson"));

                    for (int i = 0; i < colorcodeArray.length(); i++) {
                        lstcolor.add(colorcodeArray.getString(i));
                    }
                } catch (Exception ex) {
                    System.out.println("");
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

        gson = new GsonBuilder().create();
        core = new WMSCoreMessage();
        ArrayAdapter arrayAdapter1 = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, sloc);
        spinnerSelectSloc.setAdapter(arrayAdapter1);
        int spinnerPosition = arrayAdapter1.getPosition(DefaultSLoc);
        //set the default according to value
        spinnerSelectSloc.setSelection(spinnerPosition);
        spinnerSelectSloc.setEnabled(false);
        spinnerSelectColor.setEnabled(false);

        //For Honey well
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

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnClose:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;
            case R.id.btnReceiveComplete:
                DialogUtils.showConfirmDialog(getActivity(), "Confirm", "Are you sure you want to complete the receive?", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        switch (which) {
                            case DialogInterface.BUTTON_POSITIVE:
                                Toast.makeText(getActivity(), "Receive Completed Successfully", Toast.LENGTH_SHORT).show();
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

            default:
                break;
        }
    }

    public void ClearFields() {
        cvScanSku.setCardBackgroundColor(getResources().getColor(R.color.skuColor));
        ivScanSku.setImageResource(R.drawable.fullscreen_img);
        etQty.setText("");
        etMfgDate.setText("");
        etMRP.setText("");
        etSerial.setText("");
        lblScannedSku.setText("");
        lblDesc.setText("");
    }


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update UI to reflect the data
                //List<String> list = new ArrayList<String>();
                //list.add("Barcode data: " + barcodeReadEvent.getBarcodeData());
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
        if (scannedData != null && !Common.isPopupActive()) {
            if (ScanValidator.IsItemScanned(scannedData)) {
                ClearFields();
                if (!(etDock.getText().toString().isEmpty())) {
                    lblScannedSku.setText(scannedData.split("[-]", 2)[0]);
                    etSerial.setText(scannedData.split("[-]", 2)[1]);
                    Materialcode = scannedData;
                    IsuserConfirmed = false;
                    IsuserConfirmedRedo = false;
                    ValidateRSNAndReceive();
                } else {
                    common.showUserDefinedAlertType(errorMessages.EMC_0018, getActivity(), getContext(), "Error");
                    return;
                }
            } else if (ScanValidator.IsDockScanned(scannedData)) {
                etDock.setText(scannedData);
                ValidateLocationCode();
            }
        }
    }

    public void ValidateLocationCode() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setDockLocation(etDock.getText().toString());
            inboundDTO.setWarehouseId(wareHouseID);
            inboundDTO.setTenantID(tenantID);
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                // Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ValidateLocationCode(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
                // } else {
                // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
                // return;
                // }

            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getActivity());
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
                                        etDock.setText("");
                                        cvScanDock.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanDock.setImageResource(R.drawable.warning_img);
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        return;
                                    }


                                } else {
                                    List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    if (_lInventory.size() > 0) {

                                        cvScanDock.setCardBackgroundColor(Color.WHITE);
                                        ivScanDock.setImageResource(R.drawable.check);
                                        etDock.setEnabled(false);
                                        ProgressDialogUtils.closeProgressDialog();
                                    } else {
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showUserDefinedAlertType(errorMessages.EMC_0028, getActivity(), getContext(), "Error");

                                        return;
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
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
        }
    }

    public void ValidatePalletCode() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            inboundDTO.setPalletNo("");
            inboundDTO.setTenantID(tenantID);
            inboundDTO.setWarehouseId(wareHouseID);
            message.setEntityObject(inboundDTO);


            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.ValidatePalletCode(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
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
                                    etStoreRefNo.setText("");
                                    cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                    ivScanPallet.setImageResource(R.drawable.warning_img);
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                    return;
                                }
                            } else {
                                List<LinkedTreeMap<?, ?>> _lPalletInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lPalletInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                if (_lPalletInventory != null) {
                                    if (_lPalletInventory.size() > 0) {
                                        cvScanPallet.setCardBackgroundColor(Color.WHITE);
                                        ivScanPallet.setImageResource(R.drawable.check);
                                        InventoryDTO inventorydto = null;
                                        etStoreRefNo.setEnabled(false);
                                        for (int i = 0; i < _lPalletInventory.size(); i++) {
                                            inventorydto = new InventoryDTO(_lPalletInventory.get(i).entrySet());
                                        }
                                        if (!inventorydto.getBoxCount().equals("0")) {
                                            tvPalletQty.setVisibility(View.VISIBLE);
                                            lblPalletQty.setText(inventorydto.getBoxCount());
                                        } else {
                                            tvPalletQty.setVisibility(View.VISIBLE);
                                            lblPalletQty.setText("0");
                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                    } else {
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showUserDefinedAlertType(errorMessages.EMC_0029, getActivity(), getContext(), "Error");
                                        return;
                                    }
                                }
                            }
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
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "002_03", getActivity());
                logException();

            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
        }
    }

    // To remove the item from the Pallet
    public void RemoveItem() {
        WMSCoreMessage message = new WMSCoreMessage();
        message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
        InboundDTO inboundDTO = new InboundDTO();
        inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
        inboundDTO.setDockLocation(etDock.getText().toString());
        inboundDTO.setPalletNo("");
        inboundDTO.setVehicleNumber(lblVehicleNo.getText().toString());
        message.setEntityObject(inboundDTO);


        Call<String> call = null;
        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            //  call = apiService.(message);
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
            Common.setIsPopupActive(true);
            soundUtils.alertError(getActivity(), getContext());
            DialogUtils.showAlertDialog(getActivity(), "Error", errorMessages.EMC_0002, R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        Common.setIsPopupActive(false);
                    }
                }
            });
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

                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        return;
                                    }
                                } else {
                                    LinkedTreeMap<String, String> _lSuggestedGate = new LinkedTreeMap<String, String>();
                                    _lSuggestedGate = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _lSuggestedGate.entrySet()) {
                                        if (entry.getKey().equals("AssociatedGateOfAccess")) {
                                            DialogUtils.showAlertDialog(getActivity(), entry.getValue());
                                        }

                                    }
                                }

                            } else {
                                common.showUserDefinedAlertType(errorMessages.EMC_0020, getActivity(), getContext(), "Error");
                            }
                        }

                    } catch (Exception ex) {
                        try {
                            ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
                            logException();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable throwable) {
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
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
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

                            /*try {
                                exceptionLoggerUtils.createExceptionLog(ex.toString(),classCode,"002",getContext());

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            logException();*/


                            ProgressDialogUtils.closeProgressDialog();
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        ProgressDialogUtils.closeProgressDialog();
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                ProgressDialogUtils.closeProgressDialog();
                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0003, getActivity(), getContext(), "Error");
        }
    }

    // Update the shipment
    public void UpdatePalletComplete() {
        Common.setIsPopupActive(true);
        if (etStoreRefNo.getText().toString().isEmpty()) {
            common.showUserDefinedAlertType(errorMessages.EMC_0019, getActivity(), getContext(), "Error");
            return;
        }
        WMSCoreMessage message = new WMSCoreMessage();
        message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
        InboundDTO inboundDTO = new InboundDTO();
        inboundDTO.setStoreRefNo(lblStoreRefNo.getText().toString());
        inboundDTO.setDockLocation(etDock.getText().toString());
        inboundDTO.setPalletNo("");
        inboundDTO.setVehicleNumber(lblVehicleNo.getText().toString());
        inboundDTO.setAccountID(accountID);
        message.setEntityObject(inboundDTO);


        Call<String> call = null;
        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            // if (NetworkUtils.isInternetAvailable()) {
            // Calling the Interface method
            ProgressDialogUtils.showProgressDialog("Please Wait");
            call = apiService.PalletComplete(message);
            // } else {
            // DialogUtils.showAlertDialog(getActivity(), "Please enable internet");
            // return;
            // }

        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_01", getActivity());
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
                                        etStoreRefNo.setText("");
                                        ProgressDialogUtils.closeProgressDialog();
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.white));
                                        ivScanPallet.setImageResource(R.drawable.warning_img);
                                        Common.setIsPopupActive(false);
                                        return;
                                    }
                                } else {
                                    LinkedTreeMap<String, String> _lSuggestedGate = new LinkedTreeMap<String, String>();
                                    _lSuggestedGate = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _lSuggestedGate.entrySet()) {
                                        if (entry.getKey().equals("AssociatedGateOfAccess")) {
                                            DialogUtils.showAlertDialog(getActivity(), entry.getValue());
                                        }
                                        ProgressDialogUtils.closeProgressDialog();
                                        Common.setIsPopupActive(false);
                                        etStoreRefNo.setText("");
                                        cvScanPallet.setCardBackgroundColor(getResources().getColor(R.color.palletColor));
                                        ivScanPallet.setImageResource(R.drawable.fullscreen_img);
                                        // Updating Pallet Here
                                    }
                                }
                            } else {
                                ProgressDialogUtils.closeProgressDialog();
                                common.showUserDefinedAlertType(errorMessages.EMC_0020, getActivity(), getContext(), "Error");
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
                    //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                    ProgressDialogUtils.closeProgressDialog();
                    common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                }
            });
        } catch (Exception ex) {
            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "004_03", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
        }
    }

    public void ValidateRSNAndReceive() {
        try {
            Common.setIsPopupActive(true);
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inventory, getContext());
            InventoryDTO inventoryDTO = new InventoryDTO();
            inventoryDTO.setRSN(Materialcode);
            inventoryDTO.setReferenceDocumentNumber(lblStoreRefNo.getText().toString());
            inventoryDTO.setLocationCode(etDock.getText().toString());
            //inventoryDTO.setContainerCode(etStoreRefNo.getText().toString());
            inventoryDTO.setContainerCode("");
            inventoryDTO.setVehicleNumber(lblVehicleNo.getText().toString());
            inventoryDTO.setSLOC(storageloc.toString());
            inventoryDTO.setTenantID(tenantID);
            inventoryDTO.setMOP(etQty.getText().toString());
            inventoryDTO.setMRP(etMRP.getText().toString());
            inventoryDTO.setAccountID(accountID);
            inventoryDTO.setWareHouseID(wareHouseID);
            inventoryDTO.setPOHeaderID(POHeaderID);
            inventoryDTO.setColor(color);
            final String Qty = "1";
            inventoryDTO.setQuantity(Qty);
            if (IsuserConfirmed) {
                inventoryDTO.setUserConfirmedExcessTransaction(true);
                if (etQty.getText().toString() != null)
                    inventoryDTO.setQuantity(etQty.getText().toString());
            }
            if (IsuserConfirmedRedo) {
                inventoryDTO.setUserConfirmReDo(true);
                if (etQty.getText().toString() != null && !(etQty.getText().toString().equals("")))
                    inventoryDTO.setQuantity(etQty.getText().toString());
            }
            inventoryDTO.setUserId(Integer.parseInt(userId));
            message.setEntityObject(inventoryDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.ValidateRSNAndReceive(message);
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
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IB_BL_011")) {

                                            etMRP.setText(MRP);
                                            etQty.setText(MOP);
                                            etMfgDate.setText(MfgDate);
                                            IsuserConfirmed = false;
                                        }
                                        if (owmsExceptionMessage.getWMSExceptionCode().equals("EMC_IB_BL_012")) {
                                            IsuserConfirmed = true;
                                            IsuserConfirmedRedo = false;
                                        }
                                        if (owmsExceptionMessage.isShowUserConfirmDialogue()) {
                                            DialogUtils.showConfirmDialog(getActivity(), "Confirm", owmsExceptionMessage.getWMSMessage(), new DialogInterface.OnClickListener() {

                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {

                                                    switch (which) {
                                                        case DialogInterface.BUTTON_POSITIVE:
                                                            if (IsuserConfirmed) {
                                                                ValidateRSNAndReceive();
                                                            }
                                                            Common.setIsPopupActive(false);
                                                            break;

                                                        case DialogInterface.BUTTON_NEGATIVE:
                                                            //ClearFields();
                                                            IsuserConfirmed = false;
                                                            IsuserConfirmedRedo = false;
                                                            Common.setIsPopupActive(false);
                                                            break;
                                                    }

                                                }
                                            });
                                            Common.setIsPopupActive(true);
                                            ProgressDialogUtils.closeProgressDialog();
                                            return;
                                        } else {
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                            return;
                                        }
                                    } else {
                                        List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                        _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                        List<InventoryDTO> lstInventory = new ArrayList<InventoryDTO>();
                                        List<String> lstColorCodes = new ArrayList<String>();
                                        InventoryDTO inventorydto = null;


                                        for (int i = 0; i < _lInventory.size(); i++) {

                                            inventorydto = new InventoryDTO(_lInventory.get(i).entrySet());
                                            lstInventory.add(inventorydto);
                                            for (int j = 0; j < lstInventory.get(i).getColorCodes().size(); j++) {
                                                lstColorCodes.add(lstInventory.get(i).getColorCodes().get(j).getColourName());
                                                if (lstInventory.get(i).getColorCodes().get(j).getColourName().equals("AST")) {
                                                    IsAssorted = true;
                                                }
                                            }

                                        }

                                        if (inventorydto.isFinishedGoods()) {
                                            etQty.setEnabled(false);
                                        } else {
                                            etQty.setEnabled(true);
                                        }
                                        MOP = inventorydto.getMOP();
                                        MRP = inventorydto.getMRP();
                                        MfgDate = inventorydto.getMonthOfMfg() + "/" + inventorydto.getYearOfMfg();
                                        etQty.setText(inventorydto.getMOP());
                                        etMRP.setText(inventorydto.getMRP());
                                        etMfgDate.setText(inventorydto.getMonthOfMfg() + "/" + inventorydto.getYearOfMfg());
                                        etMfgDate.setEnabled(false);
                                        etSerial.setEnabled(false);
                                        etQty.setEnabled(false);

                                        if (IsAssorted) {
                                            // added on 20/09/2018 by prasanna
                                            ArrayAdapter arrayAdapter1 = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstcolor);
                                            spinnerSelectColor.setAdapter(arrayAdapter1);
                                            int spinnerPosition = arrayAdapter1.getPosition("AST");
                                            //set the default according to value
                                            spinnerSelectColor.setSelection(spinnerPosition);
                                            //spinnerSelectColor.setEnabled(true);    // Commented by Padmaja as per Kashyaps req on 24/05/2019
                                        } else {
                                            ArrayAdapter colorcodes = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstColorCodes);
                                            spinnerSelectColor.setAdapter(colorcodes);
                                            spinnerSelectColor.setEnabled(false);
                                        }

                                        ProgressDialogUtils.closeProgressDialog();

                                        lblPalletQty.setText(inventorydto.getBoxCount());
                                        lblInboundQty.setText(inventorydto.getDocumentProcessedQuantity() + "/" + inventorydto.getDocumentQuantity());
                                        lblDesc.setText(inventorydto.getMaterialShortDescription());
                                        //lblVehicleQty.setText(VehicleReceivedQty+"/"+VehicleInventoryQty);
                                        cvScanSku.setCardBackgroundColor(Color.WHITE);
                                        ivScanSku.setImageResource(R.drawable.check);
                                        Common.setIsPopupActive(false);

                                    }

                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showUserDefinedAlertType(errorMessages.EMC_0021, getActivity(), getContext(), "Error");
                                }
                                IsuserConfirmedRedo = false;

                                IsuserConfirmed = false;
                            }
                        } catch (Exception ex) {
                            try {
                                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_02", getActivity());
                                Common.setIsPopupActive(false);
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
                    ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "003_03", getActivity());
                    Common.setIsPopupActive(false);
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
            Common.setIsPopupActive(false);
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
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
            // notifications while paused.
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_goodsIn));
    }

    //Barcode scanner API
    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (barcodeReader != null) {
            // unregister barcode event listener honeywell
            barcodeReader.removeBarcodeListener((BarcodeReader.BarcodeListener) this);

            // unregister trigger state change listener
            barcodeReader.removeTriggerListener((BarcodeReader.TriggerListener) this);
        }

        Intent RTintent = new Intent("sw.reader.decode.require");
        RTintent.putExtra("Enable", false);
        getActivity().sendBroadcast(RTintent);
        getActivity().unregisterReceiver(this.myDataReceiver);
        super.onDestroyView();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        storageloc = spinnerSelectSloc.getSelectedItem().toString();

        if (spinnerSelectColor.getSelectedItem() != null && spinnerSelectColor.getSelectedItem() != "") {
            color = spinnerSelectColor.getSelectedItem().toString();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }


    @Override
    public boolean onLongClick(View view) {
        return false;
    }
}