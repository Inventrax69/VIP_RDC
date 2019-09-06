package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PickListInventoryDTO {


    @SerializedName("MaterialMasterID")
    private String materialMasterID;

    @SerializedName("RequirementQuantity")
    private String requirementQuantity;

    @SerializedName("PicklistQuantity")
    private String picklistQuantity;

    @SerializedName("PickedQuantity")
    private String pickedQuantity;

    @SerializedName("DockLocation")
    private String dockLocation;

    @SerializedName("DockID")
    private String dockID;

    @SerializedName("MaterialCode")
    private String materialCode;

    @SerializedName("PickListHeaderID")
    private String pickListHeaderID;

    @SerializedName("PickListRefNo")
    private String pickListRefNo;

    @SerializedName("CustomerCode")
    private String customerCode;

    @SerializedName("MaterialDescription")
    private String materialDescription;

    @SerializedName("PickPrioritySeq")
    private String pickPrioritySeq;

    @SerializedName("TripSheetRefNo")
    private String tripSheetRefNo;


    public PickListInventoryDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "MaterialMasterID":
                    if (entry.getValue() != null) {
                        this.setMaterialMasterID(String.valueOf(entry.getValue()));
                    }
                    break;

                case "RequirementQuantity":
                    if (entry.getValue() != null) {
                        this.setRequirementQuantity(entry.getValue().toString());
                    }
                    break;

                case "PicklistQuantity":
                    if (entry.getValue() != null) {
                        this.setPicklistQuantity((entry.getValue().toString()));
                    }
                    break;

                case "PickedQuantity":
                    if (entry.getValue() != null) {
                        this.setPickedQuantity(entry.getValue().toString());
                    }
                    break;

                case "DockLocation":
                    if (entry.getValue() != null) {
                        this.setDockLocation((entry.getValue().toString()));
                    }
                    break;

                case "DockID":
                    if (entry.getValue() != null) {
                        this.setDockID(entry.getValue().toString());
                    }
                    break;

                case "MaterialCode":
                    if (entry.getValue() != null) {
                        this.setMaterialCode(entry.getValue().toString());
                    }
                    break;


                case "PickListHeaderID":
                    if (entry.getValue() != null) {
                        this.setPickListHeaderID(entry.getValue().toString());
                    }
                    break;

                case "PickListRefNo":
                    if (entry.getValue() != null) {
                        this.setPickListRefNo(entry.getValue().toString());
                    }
                    break;


                case "CustomerCode":
                    if (entry.getValue() != null) {
                        this.setCustomerCode(entry.getValue().toString());
                    }
                    break;

                case "MaterialDescription":
                    if (entry.getValue() != null) {
                        this.setMaterialDescription(entry.getValue().toString());
                    }
                    break;
                case "PickPrioritySeq":
                    if (entry.getValue() != null) {
                        this.setPickPrioritySeq(entry.getValue().toString());
                    }
                    break;
                case "TripSheetRefNo":
                    if (entry.getValue() != null) {
                        this.setTripSheetRefNo(entry.getValue().toString());
                    }
                    break;
              

            }
        }
    }

    public String getMaterialMasterID() {
        return materialMasterID;
    }

    public void setMaterialMasterID(String materialMasterID) {
        this.materialMasterID = materialMasterID;
    }

    public String getRequirementQuantity() {
        return requirementQuantity;
    }

    public void setRequirementQuantity(String requirementQuantity) {
        this.requirementQuantity = requirementQuantity;
    }

    public String getPicklistQuantity() {
        return picklistQuantity;
    }

    public void setPicklistQuantity(String picklistQuantity) {
        this.picklistQuantity = picklistQuantity;
    }

    public String getPickedQuantity() {
        return pickedQuantity;
    }

    public void setPickedQuantity(String pickedQuantity) {
        this.pickedQuantity = pickedQuantity;
    }

    public String getDockLocation() {
        return dockLocation;
    }

    public void setDockLocation(String dockLocation) {
        this.dockLocation = dockLocation;
    }

    public String getDockID() {
        return dockID;
    }

    public void setDockID(String dockID) {
        this.dockID = dockID;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getPickListHeaderID() {
        return pickListHeaderID;
    }

    public void setPickListHeaderID(String pickListHeaderID) {
        this.pickListHeaderID = pickListHeaderID;
    }

    public String getPickListRefNo() {
        return pickListRefNo;
    }

    public void setPickListRefNo(String pickListRefNo) {
        this.pickListRefNo = pickListRefNo;
    }

    public String getCustomerCode() {
        return customerCode;
    }

    public void setCustomerCode(String customerCode) {
        this.customerCode = customerCode;
    }

    public String getMaterialDescription() {
        return materialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        this.materialDescription = materialDescription;
    }

    public String getPickPrioritySeq() {
        return pickPrioritySeq;
    }

    public void setPickPrioritySeq(String pickPrioritySeq) {
        this.pickPrioritySeq = pickPrioritySeq;
    }

    public String getTripSheetRefNo() {
        return tripSheetRefNo;
    }

    public void setTripSheetRefNo(String tripSheetRefNo) {
        this.tripSheetRefNo = tripSheetRefNo;
    }
}
