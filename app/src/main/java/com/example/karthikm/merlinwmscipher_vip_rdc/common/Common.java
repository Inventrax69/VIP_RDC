package com.example.karthikm.merlinwmscipher_vip_rdc.common;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.SyncStateContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.OutbountDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreAuthentication;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSCoreMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.WMSExceptionMessage;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.Services.RestService;
import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.EndpointConstants;
import com.example.karthikm.merlinwmscipher_vip_rdc.interfaces.ApiInterface;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.AndroidUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.DateUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.DialogUtils;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.SoundUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.EndpointConstants.LoginUserDTO;

/**
 * Created by Prasanna.ch on 06/14/2018.
 */

public class Common {
    private WMSCoreMessage core;
    private Gson gson;
    String userId = null;

    private SoundUtils soundUtils;
    public static boolean isPopupActive() {
        return isPopupActive;
    }

    public static void setIsPopupActive(boolean isPopupActive) {
        Common.isPopupActive = isPopupActive;
    }

    public static boolean isPopupActive;

    // commom Authentication Object
    public WMSCoreMessage SetAuthentication(EndpointConstants Constant, Context context) {

        SharedPreferences sp = context.getSharedPreferences("LoginActivity", Context.MODE_PRIVATE);
        userId = sp.getString("RefUserId", "");
        WMSCoreMessage message = new WMSCoreMessage();
        WMSCoreAuthentication token = new WMSCoreAuthentication();
        token.setAuthKey(AndroidUtils.getDeviceSerialNumber().toString());
        token.setUserId(userId);
        token.setAuthValue("");
        token.setLoginTimeStamp(DateUtils.getTimeStamp().toString());
        token.setAuthToken("");
        token.setRequestNumber(1);
        message.setType(Constant);
        message.setAuthToken(token);
        return message;

    }

    /*  public WMSCoreMessage SetAuthentication(EndpointConstants Constant)
      {


          WMSCoreMessage message = new WMSCoreMessage();
          WMSCoreAuthentication token = new WMSCoreAuthentication();
          token.setAuthKey(AndroidUtils.getDeviceSerialNumber().toString());
          token.setUserId(userId);
          token.setAuthValue("");
          token.setLoginTimeStamp(DateUtils.getTimeStamp().toString());
          token.setAuthToken("");
          token.setRequestNumber(1);
          message.setType(Constant);
          message.setAuthToken(token);
          return message;

      }*/
    public void CheckInternetconnectivity() {
        try {
            WMSCoreMessage message = new WMSCoreMessage();
            // message= this.SetAuthentication(EndpointConstants.Outbound,);


            Call<String> call = null;
            ApiInterface apiService =
                    RestService.getClient().create(ApiInterface.class);

            try {

                call = apiService.Checktest(message);


            } catch (Exception ex) {
                Log.d("Message", ex.toString());

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


                        } catch (Exception ex) {
                            Log.d("Message", core.getEntityObject().toString());
                        }


                    }

                    // response object fails
                    @Override
                    public void onFailure(Call<String> call, Throwable throwable) {
                        //Toast.makeText(LoginActivity.this, throwable.toString(), Toast.LENGTH_LONG).show();

                    }
                });
            } catch (Exception ex) {
                // Toast.makeText(LoginActivity.this, ex.toString(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception ex) {

        }
    }

   /* public void showAlertType(WMSExceptionMessage wmsExceptionMessage, Activity activity) {

        if (wmsExceptionMessage.isShowAsCriticalError()) {
            DialogUtils.showAlertDialog(activity, "Critical Error", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.link_break);
            setIsPopupActive(false);
            return;
        }
        if (wmsExceptionMessage.isShowAsError()) {
            setIsPopupActive(true);
            DialogUtils.showAlertDialog(activity, "Error", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_NEUTRAL:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });
            setIsPopupActive(false);
            return;
        }
        if (wmsExceptionMessage.isShowAsWarning()) {
            DialogUtils.showAlertDialog(activity, "Warning", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.warning_img);
            setIsPopupActive(false);
            return;
        }
        if (wmsExceptionMessage.isShowAsSuccess()) {
            DialogUtils.showAlertDialog(activity, "Success", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.success);
            setIsPopupActive(false);
            return;
        }


    }*/


    public void showAlertType(WMSExceptionMessage wmsExceptionMessage, Activity activity, Context context) {

        soundUtils = new SoundUtils();
        if (wmsExceptionMessage.isShowAsCriticalError()) {
            setIsPopupActive(true);
            soundUtils.alertCriticalError(activity, context);
            DialogUtils.showAlertDialog(activity, "Critical Error", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.link_break, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (wmsExceptionMessage.isShowAsError()) {
            setIsPopupActive(true);
            soundUtils.alertError(activity, context);
            DialogUtils.showAlertDialog(activity, "Error", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (wmsExceptionMessage.isShowAsWarning()) {
            setIsPopupActive(true);
            soundUtils.alertWarning(activity, context);
            DialogUtils.showAlertDialog(activity, "Warning", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.warning_img,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (wmsExceptionMessage.isShowAsSuccess()) {
            setIsPopupActive(true);
            soundUtils.alertSuccess(activity, context);
            DialogUtils.showAlertDialog(activity, "Success", wmsExceptionMessage.getWMSMessage().toString(), R.drawable.success,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });
            return;
        }

    }




    public void showUserDefinedAlertType(String errorCode, Activity activity, Context context,String alerttype) {

        soundUtils = new SoundUtils();
        if (alerttype.equals("Critical Error")) {
            setIsPopupActive(true);
            soundUtils.alertCriticalError(activity, context);
            DialogUtils.showAlertDialog(activity, "Critical Error", errorCode, R.drawable.link_break, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Error")) {
            setIsPopupActive(true);
            soundUtils.alertError(activity, context);
            DialogUtils.showAlertDialog(activity, "Error", errorCode, R.drawable.cross_circle, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Warning")) {
            setIsPopupActive(true);
            soundUtils.alertWarning(activity, context);
            DialogUtils.showAlertDialog(activity, "Warning", errorCode, R.drawable.warning_img,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });

            return;
        }
        if (alerttype.equals("Success")) {
            setIsPopupActive(true);
            soundUtils.alertSuccess(activity, context);
            DialogUtils.showAlertDialog(activity, "Success", errorCode, R.drawable.success,new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            setIsPopupActive(false);
                            break;
                    }
                }
            });
            return;
        }

    }
}