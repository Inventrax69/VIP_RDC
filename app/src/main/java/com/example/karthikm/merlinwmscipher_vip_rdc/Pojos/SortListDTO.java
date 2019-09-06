package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Padmaja.B on 6/11/2018.
 */

public class SortListDTO {


    @SerializedName("SerialNumber")
    private String serialNumber;

    @SerializedName("PickListRefNo")
    private String pickListRefNo;

    @SerializedName("TripSheetRefNo")
    private String tripSheetRefNo;

    @SerializedName("LSortListInventory")
    private List<LSortListInventory> lSortListInventory = null;

    @SerializedName("UserID")
    private String userID;


    public SortListDTO() {

    }


    public SortListDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "SerialNumber":
                    if (entry.getValue() != null) {
                        this.setSerialNumber(entry.getValue().toString());
                    }
                    break;
                case "PickListRefNo":
                    if (entry.getValue() != null) {
                        this.setPickListRefNo(entry.getValue().toString());
                    }
                    break;

                case "LSortListInventory":


                    if(entry.getValue()!=null) {
                        List<LinkedTreeMap<?, ?>> treemapList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<LSortListInventory> lstInbound = new ArrayList<LSortListInventory>();
                        for (int i = 0; i < treemapList.size(); i++) {
                            LSortListInventory dto = new LSortListInventory(treemapList.get(i).entrySet());
                            lstInbound.add(dto);
                        }
                        this.setLSortListInventory(lstInbound);
                    }



                    /*if (entry.getValue() != null) {
                       // LinkedTreeMap<?, ?> SloctreemapList = LinkedTreeMap<?, ?> entry.getValue();
                        LinkedTreeMap<?, ?> SloctreemapList = (LinkedTreeMap<?, ?>) entry.getValue();
                        List<LSortListInventory> lstSLOC = new ArrayList<LSortListInventory>();
                        for (int i = 0; i < SloctreemapList.size(); i++) {
                            LSortListInventory dto = new LSortListInventory(SloctreemapList.entrySet());
                            lstSLOC.add(dto);
                            //Log.d("Message", core.getEntityObject().toString());
                        }

                        this.setLSortListInventory(lstSLOC);
                    }*/
                    break;

                case "UserID":
                    if (entry.getValue() != null) {
                        this.setUserID(entry.getValue().toString());
                    }
                    break;
            }
        }
    }

    public String getPickListRefNo() {
        return pickListRefNo;
    }

    public void setPickListRefNo(String pickListRefNo) {
        this.pickListRefNo = pickListRefNo;
    }

    public List<LSortListInventory> getlSortListInventory() {
        return lSortListInventory;
    }

    public void setlSortListInventory(List<LSortListInventory> lSortListInventory) {
        this.lSortListInventory = lSortListInventory;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }



    public String getTripSheetRefNo() {
        return tripSheetRefNo;
    }

    public void setTripSheetRefNo(String tripSheetRefNo) {
        this.tripSheetRefNo = tripSheetRefNo;
    }


    public List<LSortListInventory> getLSortListInventory() {
        return lSortListInventory;
    }

    public void setLSortListInventory(List<LSortListInventory> lSortListInventory) {
        this.lSortListInventory = lSortListInventory;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

}