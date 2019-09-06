package com.example.karthikm.merlinwmscipher_vip_rdc.Fragments;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cipherlab.barcode.GeneralString;
import com.example.karthikm.merlinwmscipher_vip_rdc.Activities.MainActivity;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.ColorDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InboundDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.LSortListInventory;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.OutbountDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.SortListDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.SearchableSpinner.SearchableSpinner;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
import com.example.karthikm.merlinwmscipher_vip_rdc.adapters.SortAdp;
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
 * Created by Prasanna.ch on 05/08/2018
 */

public class SortingFragment extends Fragment implements View.OnClickListener, BarcodeReader.TriggerListener, BarcodeReader.BarcodeListener, AdapterView.OnItemSelectedListener, View.OnLongClickListener {
    private static final String classCode = "API_FRAG_SORTING";
    private View rootView;

    private CardView cvScanRSN;
    private ImageView ivScanRSn, iv_link;

    private SearchableSpinner spinnerPicklist;
    private Button btnGo, btnClose, btnCloseTwo, btnClosethree;

    DialogUtils dialogUtils;

    FragmentUtils fragmentUtils;
    private Common common = null;
    String scanner = null;
    String getScanner = null;
    private IntentFilter filter;
    private ScanValidator scanValidator;
    private Gson gson;
    private LinearLayoutManager linearLayoutManager;
    private WMSCoreMessage core;
    private String Materialcode = null, VehicleReceivedQty = null, VehicleInventoryQty = null;
    String userId = null;
    //For Honey well barcode
    private static BarcodeReader barcodeReader;
    private AidcManager manager;
    String pickListNo = null, color = null, DefaultSLoc = null, tripSheetNumber = "";
    List<List<ColorDTO>> lstColorData;
    ArrayList<String> sloc;
    ArrayList<String> lstcolor;
    SoundUtils soundUtils = null;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    boolean IsuserConfirmed = false, IsuserConfirmedRedo = false, IsAssorted = false;
    ToneGenerator toneGenerator;
    private ErrorMessages errorMessages;
    LinearLayout ll_pickScreenOne, ll_pickScreenTwo, ll_list;
    RecyclerView list;
    TextView txt_rsn, txt_loadPriaority, txt_dock, txt_picList, txt_dockNo;
    EditText etCustomer, etPickList_Tripsheet;

    String prioritySeq = "";

    private final BroadcastReceiver myDataReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            scanner = intent.getStringExtra(GeneralString.BcReaderData);  // Scanned Barcode info
            ProcessScannedinfo(scanner.trim().toString());
        }
    };

    public SortingFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_sorting, container, false);
        barcodeReader = MainActivity.getBarcodeObject();
        loadFormControls();
        return rootView;
    }

    // Form controls
    private void loadFormControls() {
        exceptionLoggerUtils = new ExceptionLoggerUtils();
        ll_pickScreenOne = (LinearLayout) rootView.findViewById(R.id.ll_pickScreenOne);
        ll_pickScreenTwo = (LinearLayout) rootView.findViewById(R.id.ll_pickScreenTwo);
        ll_list = (LinearLayout) rootView.findViewById(R.id.ll_list);

        list = (RecyclerView) rootView.findViewById(R.id.list);
        list.setHasFixedSize(true);

        linearLayoutManager = new LinearLayoutManager(getContext());

        // use a linear layout manager
        list.setLayoutManager(linearLayoutManager);

        cvScanRSN = (CardView) rootView.findViewById(R.id.cvScanRSN);
        ivScanRSn = (ImageView) rootView.findViewById(R.id.ivScanRSn);
        iv_link = (ImageView) rootView.findViewById(R.id.iv_link);

        txt_rsn = (TextView) rootView.findViewById(R.id.txt_rsn);
        txt_picList = (TextView) rootView.findViewById(R.id.txt_picList);
        txt_dockNo = (TextView) rootView.findViewById(R.id.txt_dockNo);
        txt_loadPriaority = (TextView) rootView.findViewById(R.id.txt_loadPriaority);
        txt_dock = (TextView) rootView.findViewById(R.id.txt_dock);

        etPickList_Tripsheet = (EditText) rootView.findViewById(R.id.etPickList_Tripsheet);
        etCustomer = (EditText) rootView.findViewById(R.id.etCustomer);

        spinnerPicklist = (SearchableSpinner) rootView.findViewById(R.id.spinnerPicklist);
        spinnerPicklist.setOnItemSelectedListener(this);

        btnGo = (Button) rootView.findViewById(R.id.btnGo);
        btnClose = (Button) rootView.findViewById(R.id.btnClose);
        btnCloseTwo = (Button) rootView.findViewById(R.id.btnCloseTwo);
        btnClosethree = (Button) rootView.findViewById(R.id.btnClosethree);


        SharedPreferences sp = getActivity().getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");

        btnGo.setOnClickListener(this);
        btnClose.setOnClickListener(this);
        btnCloseTwo.setOnClickListener(this);
        btnClosethree.setOnClickListener(this);

        iv_link.setOnClickListener(this);

        soundUtils = new SoundUtils();
        toneGenerator = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);

        common = new Common();
        errorMessages = new ErrorMessages();

        //DialogUtils.showAlertDialog(getActivity(),VehicleInventoryQty+"/"+VehicleReceivedQty);
        if (getArguments() != null) {

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


        getConsPLAndTSList();


    }

    //button Clicks
    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.btnGo:

                if (tripSheetNumber.equalsIgnoreCase("Select")) {

                } else {
                    GetSortList("tripSheetNumber", tripSheetNumber.split("[-]", 2)[0].trim());
                }

                break;

            case R.id.btnClose:
                FragmentUtils.replaceFragmentWithBackStack(getActivity(), R.id.container_body, new HomeFragment());
                break;

            case R.id.btnCloseTwo:

                ll_pickScreenOne.setVisibility(View.VISIBLE);
                ll_pickScreenTwo.setVisibility(View.GONE);

                break;
            case R.id.btnClosethree:

                ll_pickScreenOne.setVisibility(View.VISIBLE);
                ll_list.setVisibility(View.GONE);

                break;


            case R.id.iv_link:

                GetSortList("tripSheetNumber", etPickList_Tripsheet.getText().toString().split("[-]", 2)[0].trim());


                break;

            default:
                break;
        }
    }


    public void getConsPLAndTSList() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Outbound, getContext());
            OutbountDTO outbountDTO = new OutbountDTO();
            outbountDTO.setUserId(userId);
            message.setEntityObject(outbountDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                ProgressDialogUtils.showProgressDialog("Please Wait");
                call = apiService.GetConsPLAndTTSList(message);
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
                                        }

                                        for (int i = 0; i < lstDto.size(); i++) {
                                            lstPickRefNo = lstDto.get(i).getPickRefNo();
                                        }


                                        if (lstPickRefNo == null) {
                                            ProgressDialogUtils.closeProgressDialog();
                                            common.showUserDefinedAlertType(errorMessages.EMC_0035, getActivity(), getContext(), "Error");
                                        } else {
                                            lstPickRefNo.add(0, "Select");

                                            ProgressDialogUtils.closeProgressDialog();
                                            ArrayAdapter arrayAdapterPickList = new ArrayAdapter(getActivity(), R.layout.support_simple_spinner_dropdown_item, lstPickRefNo);
                                            spinnerPicklist.setAdapter(arrayAdapterPickList);
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


    private void GetSortList(final String type, final String PickOrRsn) {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.SortListDTO, getContext());
            final SortListDTO sortListDTO = new SortListDTO();
            sortListDTO.setUserID(userId);
            if (type.equalsIgnoreCase("tripSheetNumber")) {
                sortListDTO.setTripSheetRefNo(PickOrRsn);  // "PL190202014"
            } else {
                sortListDTO.setSerialNumber(PickOrRsn);
            }
            message.setEntityObject(sortListDTO);
            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);
            try {
                //Checking for Internet Connectivity
                // if (NetworkUtils.isInternetAvailable()) {
                // Calling the Interface method
                call = apiService.GetSortList(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
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
                                //List<LinkedTreeMap<?, ?>> _lPickRefNo = new ArrayList<LinkedTreeMap<?, ?>>();
                                LinkedTreeMap<?, ?> userMap = (LinkedTreeMap<?, ?>) core.getEntityObject();
                                // userMap = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                if (userMap.size() > 0) {

                                    SortListDTO lstDtolstDto = null;
                                    List<SortListDTO> lstDto = new ArrayList<SortListDTO>();


                                    List<LSortListInventory> pickList = new ArrayList<LSortListInventory>();
                                    for (int i = 0; i < userMap.size(); i++) {
                                        lstDtolstDto = new SortListDTO(userMap.entrySet());
                                        lstDto.add(lstDtolstDto);
                                    }

                                    for (int i = 0; i < lstDto.size(); i++) {
                                        pickList = lstDto.get(i).getLSortListInventory();
                                    }

                                    if (pickList.size() > 0) {

                                        if (type.equalsIgnoreCase("rsn")) {


                                            ll_pickScreenOne.setVisibility(View.GONE);
                                            ll_pickScreenTwo.setVisibility(View.VISIBLE);

                                            txt_rsn.setText(PickOrRsn);
                                            txt_dock.setText(pickList.get(0).getDockLocation());
                                            etCustomer.setText(pickList.get(0).getCustomerCode());
                                            etPickList_Tripsheet.setText(pickList.get(0).getTripSheetRefNo());
                                            txt_loadPriaority.setText(String.valueOf(pickList.get(0).getPickPrioritySeq()));

                                        } else if (type.equalsIgnoreCase("tripSheetNumber")) {

                                            SortAdp sortAdp = new SortAdp(getContext(), pickList);
                                            list.setAdapter(sortAdp);

                                            txt_dockNo.setText(pickList.get(0).getDockLocation());
                                            txt_picList.setText(pickList.get(0).getTripSheetRefNo());

                                            ll_list.setVisibility(View.VISIBLE);
                                            ll_pickScreenOne.setVisibility(View.GONE);
                                            ll_pickScreenTwo.setVisibility(View.GONE);

                                        } else {

                                        }
                                    } else {
                                        common.showUserDefinedAlertType("Trip sheet is empty", getActivity(), getContext(), "Warning");
                                    }


                                    ProgressDialogUtils.closeProgressDialog();
                                } else {
                                    ProgressDialogUtils.closeProgressDialog();
                                    //common.showUserDefinedAlertType(errorMessages.EMC_0028, getActivity(), getContext(), "Error");

                                    return;
                                }
                                ProgressDialogUtils.closeProgressDialog();

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
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
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


    @Override
    public void onBarcodeEvent(final BarcodeReadEvent barcodeReadEvent) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // update UI to reflect the data
                // List<String> list = new ArrayList<String>();
                // list.add("Barcode data: " + barcodeReadEvent.getBarcodeData());
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
        if (scannedData != null && !common.isPopupActive()) {
            if (scanValidator.IsPalletScanned(scannedData)) {
                ValidateLocationCode();
            } else if (ScanValidator.IsItemScanned(scannedData)) {
                GetSortList("rsn", scannedData);
            } else if (scanValidator.IsDockScanned(scannedData)) {

            }
        } else {

        }

    }

    public void ValidateLocationCode() {
        try {

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Inbound, getContext());
            InboundDTO inboundDTO = new InboundDTO();
            //  inboundDTO.setDockLocation();
            message.setEntityObject(inboundDTO);

            Call<String> call = null;
            ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

            try {
                //Checking for Internet Connectivity
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
                                        common.showAlertType(owmsExceptionMessage, getActivity(), getContext());
                                        return;
                                    }
                                } else {
                                    List<LinkedTreeMap<?, ?>> _lInventory = new ArrayList<LinkedTreeMap<?, ?>>();
                                    _lInventory = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();
                                    if (_lInventory.size() > 0) {
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
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        ProgressDialogUtils.closeProgressDialog();
                        common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
                    }
                });
            } catch (Exception ex) {
                try {
                    exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                    logException();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ProgressDialogUtils.closeProgressDialog();
                common.showUserDefinedAlertType(errorMessages.EMC_0001, getActivity(), getContext(), "Error");
            }
        } catch (Exception ex) {
            try {
                exceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_02", getActivity());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            common.showUserDefinedAlertType(errorMessages.EMC_0002, getActivity(), getContext(), "Error");
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
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.title_activity_sorting));
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
        tripSheetNumber = spinnerPicklist.getSelectedItem().toString();


       /* ll_pickScreenTwo.setVisibility(View.GONE);
        ll_list.setVisibility(View.VISIBLE);
*/

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public boolean onLongClick(View view) {
        return false;
    }


}