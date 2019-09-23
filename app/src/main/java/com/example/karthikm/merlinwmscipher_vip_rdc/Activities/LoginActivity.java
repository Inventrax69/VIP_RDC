package com.example.karthikm.merlinwmscipher_vip_rdc.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.LoginUserDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreAuthentication;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
import com.example.karthikm.merlinwmscipher_vip_rdc.application.AbstractApplication;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.Common;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.EndpointConstants;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.ErrorMessages;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.ServiceURL;
import com.example.karthikm.merlinwmscipher_vip_rdc.interfaces.ApiInterface;
import com.example.karthikm.merlinwmscipher_vip_rdc.login.LoginPresenter;
import com.example.karthikm.merlinwmscipher_vip_rdc.login.LoginPresenterImpl;
import com.example.karthikm.merlinwmscipher_vip_rdc.login.LoginView;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.AndroidUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.DateUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.DialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ExceptionLoggerUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.NetworkUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.ProgressDialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.SharedPreferencesUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.SoundUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Prasanna ch on 05/07/2018.
 */

public class LoginActivity extends AppCompatActivity implements LoginView {

    private static final String classCode = "WMSCore_Android_Activity_001";
    private EditText inputUserId, inputPassword;
    private TextInputLayout inputLayoutUserId, inputLayoutPassword;
    private Button btnLogin;
    private LoginPresenter loginPresenter;
    private CheckBox chkRememberPassword;
    private ProgressDialogUtils progressDialogUtils;
    private SharedPreferencesUtils sharedPreferencesUtils;
    private Gson gson;
    private WMSCoreMessage core;
    private Common common;
    private SoundUtils soundUtils;
    private ExceptionLoggerUtils exceptionLoggerUtils;
    private ErrorMessages errorMessages;
    private TextView txtVersion, txtReleaseDate;
    RestService restService;
    public static final int MULTIPLE_PERMISSIONS = 10;
    // if the android mobile version is greater than 6.0 we are giving the following permissions
    String[] permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.GET_ACCOUNTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE, Manifest.permission.WRITE_CONTACTS, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET, Manifest.permission.WAKE_LOCK, Manifest.permission.VIBRATE, Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_NETWORK_STATE, Manifest.permission.ACCESS_LOCATION_EXTRA_COMMANDS, Manifest.permission.READ_PHONE_STATE};
    ImageView settings;
    String serviceUrlString = null;

    ServiceURL serviceURL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        requestforpermissions(permissions);
        //versioncontrol();
        loadFormControls();

        loginPresenter = new LoginPresenterImpl(this);
    }

    //Loading all the form controls
    private void loadFormControls() {

        try {

            common = new Common();

            inputUserId = (EditText) findViewById(R.id.etUsername);

            errorMessages = new ErrorMessages();
            serviceURL = new ServiceURL();
            inputPassword = (EditText) findViewById(R.id.etPass);

            exceptionLoggerUtils = new ExceptionLoggerUtils();
            restService = new RestService();
            chkRememberPassword = (CheckBox) findViewById(R.id.cbRememberMe);
            soundUtils = new SoundUtils();
            inputUserId.addTextChangedListener(new LoginViewTextWatcher(inputUserId));
            inputPassword.addTextChangedListener(new LoginViewTextWatcher(inputPassword));
            gson = new GsonBuilder().create();
            btnLogin = (Button) findViewById(R.id.btnLogin);
            core = new WMSCoreMessage();
            txtReleaseDate = (TextView) findViewById(R.id.txtDate);
            txtVersion = (TextView) findViewById(R.id.txtVersionName);
            txtVersion.setText("Version:" + " " + AndroidUtils.getVersionName().toString());
            txtReleaseDate.setText("Release Date:" + " " + "18-09-2019");
            SharedPreferences sp = this.getSharedPreferences("SettingsActivity", Context.MODE_PRIVATE);
            serviceUrlString = sp.getString("url", "");

            ServiceURL.setServiceUrl(serviceUrlString);

            try {
                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!inputUserId.getText().toString().isEmpty() && !inputPassword.getText().toString().isEmpty()) {
                            //materialDialogUtil.showErrorDialog(LoginActivity.this,"Failed Failed Failed Failed Failed Failed Failed Failed Failed Failed Failed Failed");
                            if (submitForm()) {
                                // Checking Internet Connection

                                if (serviceUrlString != null && serviceUrlString != "") {

                                    /*Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                                    startActivity(intent);*/

                                    validateUserSession();
                                } else {
                                    DialogUtils.showAlertDialog(LoginActivity.this, "Configure Url");
                                }

                                //If User Clicks on remember me username,Password is stored in Shared preferences
                                if (chkRememberPassword.isChecked()) {
                                    sharedPreferencesUtils.savePreference("userId", inputUserId.getText().toString().trim());
                                    sharedPreferencesUtils.savePreference("password", inputPassword.getText().toString().trim());
                                    sharedPreferencesUtils.savePreference("isRememberPasswordChecked", true);
                                } else {
                                    sharedPreferencesUtils.removePreferences("userId");
                                    sharedPreferencesUtils.removePreferences("password");
                                    sharedPreferencesUtils.removePreferences("isRememberPasswordChecked");
                                }
                            } else {

                                //Toast.makeText(getApplicationContext(),"Enter credentials",Toast.LENGTH_LONG).show();
                            }
                        } else {
                            DialogUtils.showAlertDialog(LoginActivity.this, "Enter User Id and Password");
                        }
                    }
                });
            } catch (Exception ex) {
                Log.d("", "");
            }
            settings = (ImageView) findViewById(R.id.ivSettings);
            try {
                settings.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Intent intent = new Intent(LoginActivity.this, SettingsActivity.class);
                        startActivity(intent);

                    }
                });
            } catch (Exception ex) {

            }
            progressDialogUtils = new ProgressDialogUtils(this);

            sharedPreferencesUtils = new SharedPreferencesUtils("LoginActivity", getApplicationContext());

            /*if (sharedPreferencesUtils.loadPreferenceAsBoolean("isRememberPasswordChecked",false)){
                inputUserId.setText(sharedPreferencesUtils.loadPreference("userId",""));
                inputPassword.setText(sharedPreferencesUtils.loadPreference("password",""));
                chkRememberPassword.setChecked(true);
            }else {
                inputUserId.setText(sharedPreferencesUtils.loadPreference("userId",""));
                inputPassword.setText(sharedPreferencesUtils.loadPreference("password",""));
                sharedPreferencesUtils.loadPreferenceAsBoolean("isRememberPasswordChecked",true);
            }*/
            AbstractApplication.CONTEXT = getApplicationContext();
        } catch (Exception ex) {

            DialogUtils.showAlertDialog(this, "Error while initializing controls");
            return;

        }
    }

    @Override
    protected void onDestroy() {
        loginPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void showProgress() {
        ProgressDialogUtils.showProgressDialog("Please Wait ...");
    }

    @Override
    public void hideProgress() {
        ProgressDialogUtils.closeProgressDialog();
    }

    @Override
    public void setUsernameError() {
        //inputLayoutUserId.setError(getString(R.string.));
        //requestFocus(inputUserId);
    }

    @Override
    public void setPasswordError() {
        //  inputLayoutPassword.setError(getString(R.string.err_msg_password));
        // requestFocus(inputPassword);
    }

    @Override
    public void showLoginError(String message) {
        DialogUtils.showAlertDialog(this, message);
        return;
    }

    @Override
    public void navigateToHome() {

        // sharedPreferencesUtils.savePreference("login_status", true);

        showProgress();
        hideProgress();

        this.startActivity(new Intent(this, MainActivity.class));
        //  this.finish();
    }

    /**
     * Validating form
     */
    private boolean submitForm() {

        String userId = inputUserId.getText().toString().trim();

        if (userId.isEmpty() || !isValidUserId(userId)) {
            inputLayoutUserId.setError(getString(R.string.userHint));
            inputLayoutUserId.setErrorEnabled(true);
            return false;
        }
        if (inputPassword.getText().toString().trim().isEmpty()) {
            inputLayoutPassword.setError(getString(R.string.passHint));
            inputLayoutPassword.setErrorEnabled(true);
            return false;
        }

            /*if (NetworkUtils.isInternetAvailable()){

                loginPresenter.validateCredentials(inputUserId.getText().toString(), inputPassword.getText().toString(),chkRememberPassword.isChecked());

            }else {
                DialogUtils.showAlertDialog(this,"Please enable internet");
                return ;

            }*/

        return true;
    }

    //Validating the User credentials and Calling the API method
    public void validateUserSession() {
        if (NetworkUtils.isInternetAvailable(this)) {
        } else {
            DialogUtils.showAlertDialog(this, errorMessages.EMC_0025);
            // soundUtils.alertSuccess(LoginActivity.this,getBaseContext());
            return;
        }

        WMSCoreMessage message = new WMSCoreMessage();
        WMSCoreAuthentication token = new WMSCoreAuthentication();
        token.setAuthKey(AndroidUtils.getDeviceSerialNumber().toString());
        token.setUserId("1");
        token.setAuthValue("");
        token.setLoginTimeStamp(DateUtils.getTimeStamp().toString());
        token.setAuthToken("");
        token.setRequestNumber(1);
        message.setType(EndpointConstants.LoginUserDTO);
        message.setAuthToken(token);
        LoginUserDTO loginUserDTO = new LoginUserDTO();
        loginUserDTO.setMailID(inputUserId.getText().toString());
        loginUserDTO.setPasswordEncrypted(inputPassword.getText().toString());
       /* loginUserDTO.setClientMAC(AndroidUtils.getMacAddress(this).toString());
        loginUserDTO.setSessionIdentifier(AndroidUtils.getIPAddress(true));
        loginUserDTO.setCookieIdentifier(AndroidUtils.getIMEINumber(this).toString());*/
        message.setEntityObject(loginUserDTO);


        Call<String> call = null;
        ApiInterface apiService = RestService.getClient().create(ApiInterface.class);

        try {
            //Checking for Internet Connectivity
            if (NetworkUtils.getConnectivityStatusAsBoolean(getApplicationContext())) {
                // Calling the Interface method

                call = apiService.UserLogin(message);
                ProgressDialogUtils.showProgressDialog("Please Wait");
            } else {
                DialogUtils.showAlertDialog(LoginActivity.this, "Please enable internet");
                return;
            }

        } catch (Exception ex) {

            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001_01", getApplicationContext());
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
                    //checking the Entity obeject is not null
                    if (response.body() != null) {


                        core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                        if (core.getEntityObject() != null) {

                            if ((core.getType().toString().equals("Exception"))) {
                                List<LinkedTreeMap<?, ?>> _lExceptions = new ArrayList<LinkedTreeMap<?, ?>>();
                                _lExceptions = (List<LinkedTreeMap<?, ?>>) core.getEntityObject();

                                WMSExceptionMessage owmsExceptionMessage = null;
                                for (int i = 0; i < _lExceptions.size(); i++) {

                                    owmsExceptionMessage = new WMSExceptionMessage(_lExceptions.get(i).entrySet());
                                    ProgressDialogUtils.closeProgressDialog();
                                    common.showAlertType(owmsExceptionMessage, LoginActivity.this, getApplicationContext());
                                    return;

                                }
                            } else {

                                ProgressDialogUtils.closeProgressDialog();

                                LinkedTreeMap<String, String> _lstLogindetails = new LinkedTreeMap<String, String>();
                                _lstLogindetails = (LinkedTreeMap<String, String>) core.getEntityObject();
                                if (_lstLogindetails != null) {
                                    for (Map.Entry<String, String> entry : _lstLogindetails.entrySet()) {
                                        if (entry.getKey().equals("UserID")) {

                                            sharedPreferencesUtils.savePreference("RefUserId", entry.getValue().toString());

                                        }
                                        if (entry.getKey().equals("UserName")) {

                                            sharedPreferencesUtils.savePreference("UserName", entry.getValue().toString());

                                        }
                                        if (entry.getKey().equals("UserRole")) {

                                            sharedPreferencesUtils.savePreference("RefUserRollId", entry.getValue().toString());
                                            Log.d("RefUserRollId", entry.getValue());

                                        }
                                        if (entry.getKey().equals("TenantType")) {

                                            sharedPreferencesUtils.savePreference("TenantType", entry.getValue().toString());
                                            Log.d("TenantType", entry.getValue());

                                        }
                                    }

                                    Intent i = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(i);

                                }

                            }

                        } else {
                            ProgressDialogUtils.closeProgressDialog();
                            DialogUtils.showAlertDialog(LoginActivity.this, errorMessages.EMC_0002);
                            //DialogUtils.showAlertDialog(LoginActivity.this,"Network Error");
                        }

                    } else {
                        ProgressDialogUtils.closeProgressDialog();
                        DialogUtils.showAlertDialog(LoginActivity.this, errorMessages.EMC_0005);
                    }
                }

                // response object fails
                @Override
                public void onFailure(Call<String> call, Throwable throwable) {
                    //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                    ProgressDialogUtils.closeProgressDialog();
                    DialogUtils.showAlertDialog(LoginActivity.this, errorMessages.EMC_0001);
                    // soundUtils.alertConfirm(LoginActivity.this,getBaseContext());
                }
            });
        } catch (Exception ex) {

            try {
                ExceptionLoggerUtils.createExceptionLog(ex.toString(), classCode, "001", getApplicationContext());
                logException();
            } catch (IOException e) {
                e.printStackTrace();
            }
            ProgressDialogUtils.closeProgressDialog();
            DialogUtils.showAlertDialog(LoginActivity.this, errorMessages.EMC_0003);
        }
    }

    // sending exception to the database
    public void logException() {
        try {

            String textFromFile = ExceptionLoggerUtils.readFromFile(LoginActivity.this);

            WMSCoreMessage message = new WMSCoreMessage();
            message = common.SetAuthentication(EndpointConstants.Exception, this);
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
                Log.d("Message", ex.toString());
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
                                    common.showAlertType(owmsExceptionMessage, LoginActivity.this, getApplicationContext());
                                    return;
                                }
                            } else {
                                LinkedTreeMap<String, String> _lResultvalue = new LinkedTreeMap<String, String>();
                                _lResultvalue = (LinkedTreeMap<String, String>) core.getEntityObject();
                                for (Map.Entry<String, String> entry : _lResultvalue.entrySet())
                                    if (entry.getKey().equals("Result")) {
                                        String Result = entry.getValue();
                                        if (Result.equals("0")) {

                                            return;
                                        } else {
                                            exceptionLoggerUtils.deleteFile(LoginActivity.this);
                                            return;
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


                            Log.d("Message", core.getEntityObject().toString());
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();
                        DialogUtils.showAlertDialog(LoginActivity.this, "Not valid credentials");
                        return;
                    }
                });
            } catch (Exception ex) {
                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {
            DialogUtils.showAlertDialog(LoginActivity.this, "Please check the values");
            return;
        }
    }

    public void checkUserSession() {

        try {
            WMSCoreMessage message = new WMSCoreMessage();
            WMSCoreAuthentication token = new WMSCoreAuthentication();
            token.setAuthKey(AndroidUtils.getDeviceSerialNumber().toString());
            token.setUserId("1");
            token.setAuthValue("");
            token.setLoginTimeStamp(DateUtils.getTimeStamp().toString());
            token.setAuthToken("");
            token.setRequestNumber(1);
            message.setType(EndpointConstants.LoginUserDTO);
            message.setAuthToken(token);

            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {

                call = apiService.Checktest(message);


            } catch (Exception ex) {
                DialogUtils.showAlertDialog(this, "Network Error");

            }
            try {
                //Getting response from the method
                call.enqueue(new Callback<String>() {

                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {

                        try {
                            core = new WMSCoreMessage();
                            gson = new GsonBuilder().create();
                            core = gson.fromJson(response.body().toString(), WMSCoreMessage.class);

                            if (response.body() != null) {

                                if (core.getEntityObject() != null) {
                                    LinkedTreeMap<String, String> _isLoggedIn = new LinkedTreeMap<String, String>();
                                    _isLoggedIn = (LinkedTreeMap<String, String>) core.getEntityObject();
                                    for (Map.Entry<String, String> entry : _isLoggedIn.entrySet()) {
                                        if (entry.getKey().equals("IsLoggedIn")) {
                                            String loginStatues = entry.getValue();
                                            if (loginStatues.equals("true")) {

                                                validateUserSession();

                                                return;
                                            } else {                                                             // on valid pallet


                                                return;
                                            }
                                        }
                                    }
                                }
                            }

                            if (response.body() != null) {
                                validateUserSession();
                            }

                        } catch (Exception ex) {

                        }
                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();

                        DialogUtils.showAlertDialog(LoginActivity.this, "Network Error");
                    }
                });
            } catch (Exception ex) {
                //Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
                DialogUtils.showAlertDialog(LoginActivity.this, "Network error");
            }
        } catch (Exception ex) {

        }
    }

    private static boolean isValidUserId(String userId) {
        return !TextUtils.isEmpty(userId);
    }

    private boolean validateUserId() {
        String userId = inputUserId.getText().toString().trim();
        if (userId.isEmpty() || !isValidUserId(userId)) {
            // inputLayoutUserId.setError(getString(R.string.err_msg_user_id));
            inputLayoutUserId.setErrorEnabled(false);
            return false;
        } else {
            return true;
        }
    }

    private boolean validatePassword() {
        if (inputPassword.getText().toString().trim().isEmpty()) {
            // inputLayoutPassword.setError(getString(R.string.err_msg_password));
            inputLayoutPassword.setErrorEnabled(false);
            return false;
        } else {
            return true;
        }
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class LoginViewTextWatcher implements TextWatcher {

        private View view;

        private LoginViewTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.etUsername:
                    //validateUserId();
                    break;
                case R.id.etPass:
                    //validatePassword();
                    break;
            }
        }
    }

    private void versioncontrol() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // Do something for lollipop and above versions
        } else {
            // Toast.makeText(getApplicationContext(), "Android version is not supported.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void requestforpermissions(String[] permissions) {
        if (checkPermissions()) {
        }
        //  permissions  granted.
    }

    private boolean checkPermissions() {
        int result;
        List<String> listPermissionsNeeded = new ArrayList<>();
        for (String p : permissions) {
            result = ContextCompat.checkSelfPermission(LoginActivity.this, p);
            if (result != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p);
            }
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    loadFormControls();
                    // permissions granted.
                } else {
                    String permission = "";
                    for (String per : permissions) {
                        permission += "\n" + per;

                    }
                    // permissions list of don't granted permission
                }
                return;
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        /*try {

            EnterpriseDeviceManager enterpriseDeviceManager = (EnterpriseDeviceManager)getSystemService(EnterpriseDeviceManager.ENTERPRISE_POLICY_SERVICE);

            RestrictionPolicy restrictionPolicy = enterpriseDeviceManager.getRestrictionPolicy();

            restrictionPolicy.allowSettingsChanges(false);

        }catch (Exception ex){

        }*/

        //android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 255);

    }

    @Override
    protected void onPause() {
        super.onPause();
        // android.provider.Settings.System.putInt(getContentResolver(), android.provider.Settings.System.SCREEN_BRIGHTNESS, 25);
    }
}
