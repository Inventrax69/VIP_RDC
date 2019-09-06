package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class LSortListInventory {

    @SerializedName("MaterialMasterID")
    @Expose
    private double materialMasterID;
    @SerializedName("RequirementQuantity")
    @Expose
    private double requirementQuantity;
    @SerializedName("PicklistQuantity")
    @Expose
    private double picklistQuantity;
    @SerializedName("PickedQuantity")
    @Expose
    private double pickedQuantity;
    @SerializedName("DockLocation")
    @Expose
    private String dockLocation;
    @SerializedName("DockID")
    @Expose
    private double dockID;
    @SerializedName("MaterialCode")
    @Expose
    private String materialCode;
    @SerializedName("PickListHeaderID")
    @Expose
    private double pickListHeaderID;
    @SerializedName("PickListRefNo")
    @Expose
    private String pickListRefNo;
    @SerializedName("CustomerCode")
    @Expose
    private String customerCode;
    @SerializedName("MaterialDescription")
    @Expose
    private String materialDescription;
    @SerializedName("PickPrioritySeq")
    @Expose
    private double pickPrioritySeq;
    @SerializedName("TripSheetRefNo")
    @Expose
    private String tripSheetRefNo;


    public LSortListInventory(Set<? extends Map.Entry<?, ?>> entries) {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "MaterialMasterID":
                    if (entry.getValue() != null) {
                        this.setMaterialMasterID(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case "RequirementQuantity":
                    if (entry.getValue() != null) {
                        this.setRequirementQuantity(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case "PicklistQuantity":
                    if (entry.getValue() != null) {
                        this.setPicklistQuantity(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;

                    case "PickedQuantity":
                    if (entry.getValue() != null) {
                        this.setPickedQuantity(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                    case "DockLocation":
                    if (entry.getValue() != null) {
                        this.setDockLocation(entry.getValue().toString());
                    }
                    break;
                    case "DockID":
                    if (entry.getValue() != null) {
                        this.setDockID(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                    case "MaterialCode":
                    if (entry.getValue() != null) {
                        this.setMaterialCode(entry.getValue().toString());
                    }
                    break;
                    case "PickListHeaderID":
                    if (entry.getValue() != null) {
                        this.setPickListHeaderID(Double.parseDouble(entry.getValue().toString()));
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
                        this.setPickPrioritySeq(Double.parseDouble(entry.getValue().toString()));
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


    public double getMaterialMasterID() {
        return materialMasterID;
    }

    public void setMaterialMasterID(double materialMasterID) {
        this.materialMasterID = materialMasterID;
    }

    public double getRequirementQuantity() {
        return requirementQuantity;
    }

    public void setRequirementQuantity(double requirementQuantity) {
        this.requirementQuantity = requirementQuantity;
    }

    public double getPicklistQuantity() {
        return picklistQuantity;
    }

    public void setPicklistQuantity(double picklistQuantity) {
        this.picklistQuantity = picklistQuantity;
    }

    public double getPickedQuantity() {
        return pickedQuantity;
    }

    public void setPickedQuantity(double pickedQuantity) {
        this.pickedQuantity = pickedQuantity;
    }

    public String getDockLocation() {
        return dockLocation;
    }

    public void setDockLocation(String dockLocation) {
        this.dockLocation = dockLocation;
    }

    public double getDockID() {
        return dockID;
    }

    public void setDockID(double dockID) {
        this.dockID = dockID;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public double getPickListHeaderID() {
        return pickListHeaderID;
    }

    public void setPickListHeaderID(double pickListHeaderID) {
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

    public double getPickPrioritySeq() {
        return pickPrioritySeq;
    }

    public void setPickPrioritySeq(double pickPrioritySeq) {
        this.pickPrioritySeq = pickPrioritySeq;
    }

    public String getTripSheetRefNo() {
        return tripSheetRefNo;
    }

    public void setTripSheetRefNo(String tripSheetRefNo) {
        this.tripSheetRefNo = tripSheetRefNo;
    }
}