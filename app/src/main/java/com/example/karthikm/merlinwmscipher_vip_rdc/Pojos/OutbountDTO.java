package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by karthik.m on 07/04/2018.
 */

public class OutbountDTO {
    @SerializedName("OutboundID")
    private String outboundID;
    @SerializedName("PickRefNo")
    private List<String> pickRefNo;
    @SerializedName("MRP")
    private Double MRP;
    @SerializedName("MOP")
    private String MOP;
    @SerializedName("SKU")
    private String SKU;
    @SerializedName("SerialNo")
    private String serialNo;
    @SerializedName("MfgDate")
    private String MfgDate;
    @SerializedName("Location")
    private String location;
    @SerializedName("LoadList")
    private List<LoadDTO> LoadList;
    @SerializedName("PalletNo")
    private String palletNo;
    @SerializedName("UserId")
    private String userId;
    @SerializedName("IsMaterialDamaged")
    private Boolean isMaterialDamaged;
    @SerializedName("IsMaterialNotFound")
    private Boolean isMaterialNotFound;
    @SerializedName("Result")
    private String result;
    @SerializedName("RequiredQty")
    private String requiredQty;
    @SerializedName("PickedQty")
    private String pickedQty;
    @SerializedName("SelectedPickRefNumber")
    private String SelectedPickRefNumber;
    @SerializedName("SelectedLoadSheetNumber")
    private String SelectedLoadSheetNumber;
    @SerializedName("AllowNestedInventoryDispatch")
    private Boolean AllowNestedInventoryDispatch;
    @SerializedName("AllowDispatchOfOLDMRP")
    private Boolean AllowDispatchOfOLDMRP;
    @SerializedName("AllowCrossDocking")
    private Boolean AllowCrossDocking;
    @SerializedName("StrictComplianceToPicking")
    private Boolean StrictComplianceToPicking;
    @SerializedName("AutoReconsileInventoryOnSkip")
    private Boolean AutoReconsileInventoryOnSkip;
    @SerializedName("DockNumber")
    private String dockNumber;
    @SerializedName("SuggestionID")
    private String SuggestionID;
    @SerializedName("RevertQty")
    private String RevertQty;
    @SerializedName("CustomerCode")
    private String CustomerCode;
    @SerializedName("MaterialMasterId")
    private String MaterialMasterId;
    @SerializedName("MaterialDescription")
    private String MaterialDescription;
    @SerializedName("OSkipReasonDTO")
    private SkipReasonDTO oSkipReasonDTO;
    @SerializedName("TripSheetHeaderID")
    private String tripSheetHeaderID;
    @SerializedName("SelectedTripsheetRefNumber")
    private String selectedTripsheetRefNumber;
    @SerializedName("TripSheetRefNo")
    private List<String> TripSheetRefNo;


    public OutbountDTO() {

    }

    public OutbountDTO(Set<? extends Map.Entry<?, ?>> entries) {

        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "OutboundID":
                    if (entry.getValue() != null) {
                        this.setOutboundID(entry.getValue().toString());
                    }
                    break;

                case "PickRefNo":
                    if (entry.getValue() != null) {
                        this.setPickRefNo((List<String>) entry.getValue());
                    }
                    break;

                case "MRP":
                    if (entry.getValue() != null) {
                        this.setMRP(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;

                case "MOP":
                    if (entry.getValue() != null) {
                        this.setMOP(entry.getValue().toString());
                    }
                    break;

                case "SKU":
                    if (entry.getValue() != null) {
                        this.setSKU(entry.getValue().toString());
                    }
                    break;

                case "SerialNo":
                    if (entry.getValue() != null) {
                        this.setSerialNo(entry.getValue().toString());
                    }
                    break;

                case "MfgDate":
                    if (entry.getValue() != null) {
                        this.setMfgDate(entry.getValue().toString());
                    }
                    break;

                case "Location":
                    if (entry.getValue() != null) {
                        this.setLocation(entry.getValue().toString());
                    }
                    break;

                case "PalletNo":
                    if (entry.getValue() != null) {
                        this.setPalletNo(entry.getValue().toString());
                    }
                    break;

                case "DockNumber":
                    if (entry.getValue() != null) {
                        this.setDockNumber(entry.getValue().toString());
                    }
                    break;

                case "RequiredQty":
                    if (entry.getValue() != null) {
                        this.setRequiredQty(entry.getValue().toString());
                    }
                    break;

                case "PickedQty":
                    if (entry.getValue() != null) {
                        this.setPickedQty(entry.getValue().toString());
                    }
                    break;

                case "AllowNestedInventoryDispatch":
                    if (entry.getValue() != null) {
                        this.setAllowNestedInventoryDispatch(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "AllowDispatchOfOLDMRP":
                    if (entry.getValue() != null) {
                        this.setAllowDispatchOfOLDMRP(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "AllowCrossDocking":
                    if (entry.getValue() != null) {
                        this.setAllowCrossDocking(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "StrictComplianceToPicking":
                    if (entry.getValue() != null) {
                        this.setStrictComplianceToPicking(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "AutoReconsileInventoryOnSkip":
                    if (entry.getValue() != null) {
                        this.setAutoReconsileInventoryOnSkip(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;

                case "LoadList":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> treemapList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<LoadDTO> lstLoad = new ArrayList<LoadDTO>();
                        if (treemapList.size() > 0 && treemapList != null) {
                            for (int i = 0; i < treemapList.size(); i++) {
                                LoadDTO dto = new LoadDTO(treemapList.get(i).entrySet());
                                lstLoad.add(dto);
                            }
                        }
                        this.setLoadList(lstLoad);
                    }
                    break;

                case "SuggestionID":
                    if (entry.getValue() != null) {
                        this.setSuggestionID(entry.getValue().toString());
                    }
                    break;

                case "RevertQty":
                    if (entry.getValue() != null) {
                        this.setRevertQty(entry.getValue().toString());
                    }
                    break;

                case "CustomerCode":
                    if (entry.getValue() != null) {
                        this.setCustomerCode(entry.getValue().toString());
                    }
                    break;

                case "MaterialMasterId":
                    if (entry.getValue() != null) {
                        this.setMaterialMasterId(entry.getValue().toString());
                    }
                    break;

                case "MaterialDescription":
                    if (entry.getValue() != null) {
                        this.setMaterialDescription(entry.getValue().toString());
                    }
                    break;

                case "oSkipReasonDTO":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> treemapList = (List<LinkedTreeMap<?, ?>>) entry.getValue();

                        SkipReasonDTO dto = null;
                        if (treemapList.size() > 0 && treemapList != null) {
                            for (int i = 0; i < treemapList.size(); i++) {
                                dto = new SkipReasonDTO(treemapList.get(i).entrySet());

                            }
                        }
                        this.setoSkipReasonDTO(dto);
                    }
                    break;

                case "TripSheetHeaderID":
                    if (entry.getValue() != null) {
                        this.setTripSheetHeaderID(entry.getValue().toString());
                    }
                    break;

                case "SelectedTripsheetRefNumber":
                    if (entry.getValue() != null) {
                        this.setSelectedTripsheetRefNumber(entry.getValue().toString());
                    }
                    break;


                case "TripSheetRefNo":
                    if (entry.getValue() != null) {
                        this.setTripSheetRefNo((List<String>) entry.getValue());
                    }
                    break;
            }
        }
    }

    public SkipReasonDTO getoSkipReasonDTO() {
        return oSkipReasonDTO;
    }

    public void setoSkipReasonDTO(SkipReasonDTO oSkipReasonDTO) {
        this.oSkipReasonDTO = oSkipReasonDTO;
    }

    public String getMaterialDescription() {
        return MaterialDescription;
    }

    public void setMaterialDescription(String materialDescription) {
        MaterialDescription = materialDescription;
    }

    public String getMaterialMasterId() {
        return MaterialMasterId;
    }

    public void setMaterialMasterId(String materialMasterId) {
        MaterialMasterId = materialMasterId;
    }

    public String getCustomerCode() {
        return CustomerCode;
    }

    public void setCustomerCode(String customerCode) {
        CustomerCode = customerCode;
    }

    public String getRevertQty() {
        return RevertQty;
    }

    public void setRevertQty(String revertQty) {
        RevertQty = revertQty;
    }

    public String getSuggestionID() {
        return SuggestionID;
    }

    public void setSuggestionID(String suggestionID) {
        SuggestionID = suggestionID;
    }

    public List<LoadDTO> getLoadList() {
        return LoadList;
    }

    public void setLoadList(List<LoadDTO> loadList) {
        LoadList = loadList;
    }

    public String getDockNumber() {
        return dockNumber;
    }

    public void setDockNumber(String dockNumber) {
        this.dockNumber = dockNumber;
    }

    public Boolean getAllowNestedInventoryDispatch() {
        return AllowNestedInventoryDispatch;
    }

    public void setAllowNestedInventoryDispatch(Boolean allowNestedInventoryDispatch) {
        AllowNestedInventoryDispatch = allowNestedInventoryDispatch;
    }

    public Boolean getAllowDispatchOfOLDMRP() {
        return AllowDispatchOfOLDMRP;
    }

    public void setAllowDispatchOfOLDMRP(Boolean allowDispatchOfOLDMRP) {
        AllowDispatchOfOLDMRP = allowDispatchOfOLDMRP;
    }

    public Boolean getAllowCrossDocking() {
        return AllowCrossDocking;
    }

    public void setAllowCrossDocking(Boolean allowCrossDocking) {
        AllowCrossDocking = allowCrossDocking;
    }

    public Boolean getStrictComplianceToPicking() {
        return StrictComplianceToPicking;
    }

    public void setStrictComplianceToPicking(Boolean strictComplianceToPicking) {
        StrictComplianceToPicking = strictComplianceToPicking;
    }

    public Boolean getAutoReconsileInventoryOnSkip() {
        return AutoReconsileInventoryOnSkip;
    }

    public void setAutoReconsileInventoryOnSkip(Boolean autoReconsileInventoryOnSkip) {
        AutoReconsileInventoryOnSkip = autoReconsileInventoryOnSkip;
    }

    public String getOutboundID() {
        return outboundID;
    }

    public void setOutboundID(String outboundID) {
        this.outboundID = outboundID;
    }

    public List<String> getPickRefNo() {
        return pickRefNo;
    }

    public void setPickRefNo(List<String> pickRefNo) {
        this.pickRefNo = pickRefNo;
    }

    public Double getMRP() {
        return MRP;
    }

    public void setMRP(Double MRP) {
        this.MRP = MRP;
    }

    public String getMOP() {
        return MOP;
    }

    public void setMOP(String MOP) {
        this.MOP = MOP;
    }

    public String getSKU() {
        return SKU;
    }

    public void setSKU(String SKU) {
        this.SKU = SKU;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getMfgDate() {
        return MfgDate;
    }

    public void setMfgDate(String mfgDate) {
        MfgDate = mfgDate;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }


    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Boolean getMaterialDamaged() {
        return isMaterialDamaged;
    }

    public void setMaterialDamaged(Boolean materialDamaged) {
        isMaterialDamaged = materialDamaged;
    }

    public Boolean getMaterialNotFound() {
        return isMaterialNotFound;
    }

    public void setMaterialNotFound(Boolean materialNotFound) {
        isMaterialNotFound = materialNotFound;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getRequiredQty() {
        return requiredQty;
    }

    public void setRequiredQty(String requiredQty) {
        this.requiredQty = requiredQty;
    }

    public String getPickedQty() {
        return pickedQty;
    }

    public void setPickedQty(String pickedQty) {
        this.pickedQty = pickedQty;
    }

    public String getSelectedPickRefNumber() {
        return SelectedPickRefNumber;
    }

    public void setSelectedPickRefNumber(String selectedPickRefNumber) {
        SelectedPickRefNumber = selectedPickRefNumber;
    }


    public String getSelectedLoadSheetNumber() {
        return SelectedLoadSheetNumber;
    }

    public void setSelectedLoadSheetNumber(String selectedLoadSheetNumber) {
        SelectedLoadSheetNumber = selectedLoadSheetNumber;
    }


    public String getTripSheetHeaderID() {
        return tripSheetHeaderID;
    }

    public void setTripSheetHeaderID(String tripSheetHeaderID) {
        this.tripSheetHeaderID = tripSheetHeaderID;
    }

    public List<String> getTripSheetRefNo() {
        return TripSheetRefNo;
    }

    public void setTripSheetRefNo(List<String> tripSheetRefNo) {
        TripSheetRefNo = tripSheetRefNo;
    }

    public String getSelectedTripsheetRefNumber() {
        return selectedTripsheetRefNumber;
    }

    public void setSelectedTripsheetRefNumber(String selectedTripsheetRefNumber) {
        this.selectedTripsheetRefNumber = selectedTripsheetRefNumber;
    }
}