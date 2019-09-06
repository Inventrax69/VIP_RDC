package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.example.karthikm.merlinwmscipher_vip_rdc.common.constants.EndpointConstants;
import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by karthik.m on 06/15/2018.
 */

public class InventoryDTO {
    @SerializedName("ScanType")
    private EndpointConstants.ScanType scanType;
    @SerializedName("MaterialCode")
    private String materialCode;
    @SerializedName("RSN")
    private String RSN;
    @SerializedName("LocationCode")
    private String locationCode;
    @SerializedName("ContainerCode")
    private String containerCode;
    @SerializedName("ReferenceDocumentNumber")
    private String referenceDocumentNumber;
    @SerializedName("VehicleNumber")
    private String vehicleNumber;
    @SerializedName("OBDNumber")
    private String OBDNumber;
    @SerializedName("OutboundID")
    private int outboundID;
    @SerializedName("MaterialMasterID")
    private int materialMasterID;
    @SerializedName("VehicleID")
    private int vehicleID;
    @SerializedName("ReferenceDocumentID")
    private int referenceDocumentID;
    @SerializedName("ContainerID")
    private int containerID;
    @SerializedName("LocationID")
    private int locationID;
    @SerializedName("MonthOfMfg")
    private String monthOfMfg;
    @SerializedName("YearOfMfg")
    private String yearOfMfg;
    @SerializedName("MaterialTransactionID")
    private int materialTransactionID;
    @SerializedName("Quantity")
    private String Quantity;
    @SerializedName("UserId")
    private int UserId;
    @SerializedName("ColorCodes")
    private List<ColorDTO> ColorCodes;
    @SerializedName("MOP")
    private String MOP;
    @SerializedName("MRP")
    private String MRP;
    @SerializedName("IsFinishedGoods")
    private boolean isFinishedGoods;
    @SerializedName("IsRawMaterial")
    private boolean isRawMaterial;
    @SerializedName("IsConsumables")
    private boolean isConsumables;
    @SerializedName("SLOC")
    private String SLOC;
    @SerializedName("Color")
    private String color;
    @SerializedName("Result")
    private String Result;
    @SerializedName("DocumentProcessedQuantity")
    private String DocumentProcessedQuantity;
    @SerializedName("DocumentQuantity")
    private String DocumentQuantity;
    @SerializedName("MaterialShortDescription")
    private String MaterialShortDescription;
    @SerializedName("IsReceived")
    private boolean IsReceived;
    @SerializedName("UserConfirmedExcessTransaction")
    private boolean UserConfirmedExcessTransaction;
    @SerializedName("UserConfirmReDo")
    private boolean UserConfirmReDo;
    @SerializedName("SuggestionID")
    private String SuggestionID;
    @SerializedName("BatchNumber")
    private String batchNumber;
    @SerializedName("OldMRP")
    private String oldMRP;
    @SerializedName("ToLocationCode")
    private String ToLocationCode;
    @SerializedName("NewRSN")
    private String NewRSN;
    @SerializedName("IsMaterialParent")
    private Boolean isMaterialParent;
    @SerializedName("BoxCount")
    private String BoxCount;
    @SerializedName("TotalScannedQty")
    private String TotalScannedQty;
    @SerializedName("SourceType")
    private String sourceType;
    @SerializedName("MaterialMomentType")
    private String materialMomentType;
    @SerializedName("TransactionDate")
    private String TransactionDate;
    @SerializedName("TransactionType")
    private String TransactionType;
    @SerializedName("TransactionStatus")
    private String TransactionStatus;
    @SerializedName("Inout")
    private String Inout;
    @SerializedName("AvailQty")
    private String AvailQty;
    @SerializedName("TenantID")
    private String TenantID;

    @SerializedName("WareHouseID")
    private String WareHouseID;

    @SerializedName("POHeaderID")
    private String POHeaderID;
    @SerializedName("AccountID")
    private String AccountID;
    @SerializedName("JobOrderTypeID")
    private Double jobOrderTypeID;

    @SerializedName("InvoiceQuantity")
    private String InvoiceQuantity;


    @SerializedName("StoreRefNo")
    private String storeRefNo;

    @SerializedName("InboundID")
    private String InboundID;


    public String getInboundID() {
        return InboundID;
    }

    public void setInboundID(String inboundID) {
        InboundID = inboundID;
    }


    public String getStoreRefNo() {
        return storeRefNo;
    }

    public void setStoreRefNo(String storeRefNo) {
        this.storeRefNo = storeRefNo;
    }

    public InventoryDTO() {

    }

    public InventoryDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "ColorCodes":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> ColortreemapList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<ColorDTO> lstColor = new ArrayList<ColorDTO>();
                        for (int i = 0; i < ColortreemapList.size(); i++) {
                            ColorDTO dto = new ColorDTO(ColortreemapList.get(i).entrySet());
                            lstColor.add(dto);
                            //Log.d("Message", core.getEntityObject().toString());
                        }
                        this.setColorCodes(lstColor);
                    }
                    break;
                case "RSN":
                    if (entry.getValue() != null) {
                        this.setRSN(entry.getValue().toString());
                    }
                    break;
                case "YearOfMfg":
                    if (entry.getValue() != null) {
                        this.setYearOfMfg(entry.getValue().toString());
                    }
                    break;
                case "MonthOfMfg":
                    if (entry.getValue() != null) {
                        this.setMonthOfMfg(entry.getValue().toString());
                    }
                    break;
                case "MaterialCode":
                    if (entry.getValue() != null) {
                        this.setMaterialCode(entry.getValue().toString());
                    }
                    break;
                case "MOP":
                    if (entry.getValue() != null) {
                        this.setMOP(entry.getValue().toString());
                    }
                    break;
                case "MRP":
                    if (entry.getValue() != null) {
                        this.setMRP(entry.getValue().toString());
                    }
                    break;
                case "ContainerCode":
                    if (entry.getValue() != null) {
                        this.setContainerCode(entry.getValue().toString());
                    }
                    break;
                case "LocationCode":
                    if (entry.getValue() != null) {
                        this.setLocationCode(entry.getValue().toString());
                    }
                    break;

                case "TenantID":
                    if (entry.getValue() != null) {
                        this.setTenantID(entry.getValue().toString());
                    }
                    break;
                case "WareHouseID":
                    if (entry.getValue() != null) {
                        this.setWareHouseID(entry.getValue().toString());
                    }
                    break;
                case "Quantity":
                    if (entry.getValue() != null) {
                        this.setQuantity(entry.getValue().toString());
                    }
                    break;
                case "DocumentProcessedQuantity":
                    if (entry.getValue() != null) {
                        this.setDocumentProcessedQuantity(entry.getValue().toString());
                    }
                    break;
                case "Result":
                    if (entry.getValue() != null) {
                        this.setResult(entry.getValue().toString());
                    }
                    break;
                case "DocumentQuantity":
                    if (entry.getValue() != null) {
                        this.setDocumentQuantity(entry.getValue().toString());
                    }
                    break;
                case "MaterialShortDescription":
                    if (entry.getValue() != null) {
                        this.setMaterialShortDescription(entry.getValue().toString());
                    }
                    break;
                case "IsReceived":
                    if (entry.getValue() != null) {
                        this.setReceived(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "UserConfirmedExcessTransaction":
                    if (entry.getValue() != null) {
                        this.setUserConfirmedExcessTransaction(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "Color":
                    if (entry.getValue() != null) {
                        this.setColor(entry.getValue().toString());
                    }
                    break;
                case "UserConfirmReDo":
                    if (entry.getValue() != null) {
                        this.setUserConfirmReDo(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "BatchNumber":
                    if (entry.getValue() != null) {
                        this.setBatchNumber(entry.getValue().toString());
                    }
                    break;
                case "OBDNumber":
                    if (entry.getValue() != null) {
                        this.setOBDNumber(entry.getValue().toString());
                    }
                    break;


                case "IsConsumed":
                    if (entry.getValue() != null) {
                        this.setConsumables(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "IsRawMaterial":
                    if (entry.getValue() != null) {
                        this.setRawMaterial(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "IsFinishedGoods":
                    if (entry.getValue() != null) {
                        this.setFinishedGoods(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "ReferenceDocumentNumber":
                    if (entry.getValue() != null) {
                        this.setReferenceDocumentNumber(entry.getValue().toString());
                    }
                    break;
                case "SuggestionID":
                    if (entry.getValue() != null) {
                        this.setSuggestionID(entry.getValue().toString());
                    }
                    break;
                case "SLOC":
                    if (entry.getValue() != null) {
                        this.setSLOC(entry.getValue().toString());
                    }
                    break;
                case "OldMRP":
                    if (entry.getValue() != null) {
                        this.setOldMRP(entry.getValue().toString());
                    }
                    break;
                case "ToLocationCode":
                    if (entry.getValue() != null) {
                        this.setToLocationCode(entry.getValue().toString());
                    }
                    break;
                case "NewRSN":
                    if (entry.getValue() != null) {
                        this.setNewRSN(entry.getValue().toString());
                    }
                    break;
                case "IsMaterialParent":
                    if (entry.getValue() != null) {
                        this.setMaterialParent(Boolean.parseBoolean(entry.getValue().toString()));
                    }
                    break;
                case "BoxCount":
                    if (entry.getValue() != null) {
                        this.setBoxCount(entry.getValue().toString());
                    }
                    break;
                case "TotalScannedQty":
                    if (entry.getValue() != null) {
                        this.setTotalScannedQty(entry.getValue().toString());
                    }
                    break;
                case "SourceType":
                    if (entry.getValue() != null) {
                        this.setSourceType(entry.getValue().toString());
                    }
                    break;
                case "MaterialMomentType":
                    if (entry.getValue() != null) {
                        this.setMaterialMomentType(entry.getValue().toString());
                    }
                    break;
                case "TransactionDate":
                    if (entry.getValue() != null) {
                        this.setTransactionDate(entry.getValue().toString());
                    }
                    break;
                case "TransactionType":
                    if (entry.getValue() != null) {
                        this.setTransactionType(entry.getValue().toString());
                    }
                    break;
                case "TransactionStatus":
                    if (entry.getValue() != null) {
                        this.setTransactionStatus(entry.getValue().toString());
                    }
                    break;
                case "Inout":
                    if (entry.getValue() != null) {
                        this.setInout(entry.getValue().toString());
                    }
                    break;
                case "AvailQty":
                    if (entry.getValue() != null) {
                        this.setAvailQty(entry.getValue().toString());
                    }
                    break;

                case "AccountID":
                    if (entry.getValue() != null) {
                        this.setAccountID(entry.getValue().toString());
                    }
                    break;
                case "POHeaderID":
                    if (entry.getValue() != null) {
                        this.setPOHeaderID(entry.getValue().toString());
                    }
                    break;

                case "JobOrderTypeID":
                    if (entry.getValue() != null) {
                        this.setJobOrderTypeID(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;
                case "InvoiceQuantity":
                    if (entry.getValue() != null) {
                        this.setInvoiceQuantity(entry.getValue().toString());
                    }
                    break;
                case "InboundID":
                    if (entry.getValue() != null) {
                        this.setInboundID(entry.getValue().toString());
                    }
                    break;
                case "StoreRefNo":
                    if (entry.getValue() != null) {
                        this.setStoreRefNo(entry.getValue().toString());
                    }
                    break;
            }
        }
    }


    public String getInvoiceQuantity() {
        return InvoiceQuantity;
    }

    public void setInvoiceQuantity(String invoiceQuantity) {
        InvoiceQuantity = invoiceQuantity;
    }


    public String getTransactionType() {
        return TransactionType;
    }

    public void setTransactionType(String transactionType) {
        TransactionType = transactionType;
    }

    public String getTransactionStatus() {
        return TransactionStatus;
    }

    public void setTransactionStatus(String transactionStatus) {
        TransactionStatus = transactionStatus;
    }

    public String getInout() {
        return Inout;
    }

    public void setInout(String inout) {
        Inout = inout;
    }

    public String getAvailQty() {
        return AvailQty;
    }

    public void setAvailQty(String availQty) {
        AvailQty = availQty;
    }

    public String getTransactionDate() {
        return TransactionDate;
    }

    public void setTransactionDate(String transactionDate) {
        TransactionDate = transactionDate;
    }

    public String getMaterialMomentType() {
        return materialMomentType;
    }

    public void setMaterialMomentType(String materialMomentType) {
        this.materialMomentType = materialMomentType;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getTotalScannedQty() {
        return TotalScannedQty;
    }

    public void setTotalScannedQty(String totalScannedQty) {
        TotalScannedQty = totalScannedQty;
    }

    public String getBoxCount() {
        return BoxCount;
    }

    public void setBoxCount(String boxCount) {
        BoxCount = boxCount;
    }

    public Boolean getMaterialParent() {
        return isMaterialParent;
    }

    public void setMaterialParent(Boolean materialParent) {
        isMaterialParent = materialParent;
    }

    public String getNewRSN() {
        return NewRSN;
    }

    public void setNewRSN(String newRSN) {
        NewRSN = newRSN;
    }

    public String getToLocationCode() {
        return ToLocationCode;
    }

    public void setToLocationCode(String toLocationCode) {
        ToLocationCode = toLocationCode;
    }

    public String getOldMRP() {
        return oldMRP;
    }

    public void setOldMRP(String oldMRP) {
        this.oldMRP = oldMRP;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public String getSuggestionID() {
        return SuggestionID;
    }

    public void setSuggestionID(String suggestionID) {
        SuggestionID = suggestionID;
    }

    public boolean isUserConfirmReDo() {
        return UserConfirmReDo;
    }

    public void setUserConfirmReDo(boolean userConfirmReDo) {
        UserConfirmReDo = userConfirmReDo;
    }

    public boolean isUserConfirmedExcessTransaction() {
        return UserConfirmedExcessTransaction;
    }

    public void setUserConfirmedExcessTransaction(boolean userConfirmedExcessTransaction) {
        UserConfirmedExcessTransaction = userConfirmedExcessTransaction;
    }

    public boolean isReceived() {
        return IsReceived;
    }

    public void setReceived(boolean received) {
        IsReceived = received;
    }

    public String getMaterialShortDescription() {
        return MaterialShortDescription;
    }

    public void setMaterialShortDescription(String materialShortDescription) {
        MaterialShortDescription = materialShortDescription;
    }

    public String getDocumentQuantity() {
        return DocumentQuantity;
    }

    public void setDocumentQuantity(String documentQuantity) {
        DocumentQuantity = documentQuantity;
    }

    public List<ColorDTO> getColorCodes() {
        return ColorCodes;
    }

    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public String getMOP() {
        return MOP;
    }

    public void setMOP(String MOP) {
        this.MOP = MOP;
    }

    public void setColorCodes(List<ColorDTO> colorCodes) {
        ColorCodes = colorCodes;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public String getDocumentProcessedQuantity() {
        return DocumentProcessedQuantity;
    }

    public void setDocumentProcessedQuantity(String documentProcessedQuantity) {
        DocumentProcessedQuantity = documentProcessedQuantity;
    }

    public EndpointConstants.ScanType getScanType() {
        return scanType;
    }

    public void setScanType(EndpointConstants.ScanType scanType) {
        this.scanType = scanType;
    }

    public String getMaterialCode() {
        return materialCode;
    }

    public void setMaterialCode(String materialCode) {
        this.materialCode = materialCode;
    }

    public String getRSN() {
        return RSN;
    }

    public void setRSN(String RSN) {
        this.RSN = RSN;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        this.locationCode = locationCode;
    }

    public String getContainerCode() {
        return containerCode;
    }

    public void setContainerCode(String containerCode) {
        this.containerCode = containerCode;
    }

    public String getReferenceDocumentNumber() {
        return referenceDocumentNumber;
    }

    public void setReferenceDocumentNumber(String referenceDocumentNumber) {
        this.referenceDocumentNumber = referenceDocumentNumber;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getOBDNumber() {
        return OBDNumber;
    }

    public void setOBDNumber(String OBDNumber) {
        this.OBDNumber = OBDNumber;
    }

    public int getOutboundID() {
        return outboundID;
    }

    public void setOutboundID(int outboundID) {
        this.outboundID = outboundID;
    }

    public int getMaterialMasterID() {
        return materialMasterID;
    }

    public void setMaterialMasterID(int materialMasterID) {
        this.materialMasterID = materialMasterID;
    }

    public int getVehicleID() {
        return vehicleID;
    }

    public void setVehicleID(int vehicleID) {
        this.vehicleID = vehicleID;
    }

    public int getReferenceDocumentID() {
        return referenceDocumentID;
    }

    public void setReferenceDocumentID(int referenceDocumentID) {
        this.referenceDocumentID = referenceDocumentID;
    }

    public String getQuantity() {
        return Quantity;
    }

    public void setQuantity(String quantity) {
        Quantity = quantity;
    }

    public int getContainerID() {
        return containerID;
    }

    public void setContainerID(int containerID) {
        this.containerID = containerID;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public String getMonthOfMfg() {
        return monthOfMfg;
    }

    public void setMonthOfMfg(String monthOfMfg) {
        this.monthOfMfg = monthOfMfg;
    }

    public String getYearOfMfg() {
        return yearOfMfg;
    }

    public void setYearOfMfg(String yearOfMfg) {
        this.yearOfMfg = yearOfMfg;
    }

    public int getMaterialTransactionID() {
        return materialTransactionID;
    }

    public void setMaterialTransactionID(int materialTransactionID) {
        this.materialTransactionID = materialTransactionID;
    }

    public boolean isFinishedGoods() {
        return isFinishedGoods;
    }

    public void setFinishedGoods(boolean finishedGoods) {
        isFinishedGoods = finishedGoods;
    }

    public boolean isRawMaterial() {
        return isRawMaterial;
    }

    public void setRawMaterial(boolean rawMaterial) {
        isRawMaterial = rawMaterial;
    }

    public boolean isConsumables() {
        return isConsumables;
    }

    public void setConsumables(boolean consumables) {
        isConsumables = consumables;

    }

    public String getSLOC() {
        return SLOC;
    }

    public void setSLOC(String SLOC) {
        this.SLOC = SLOC;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getResult() {
        return Result;
    }

    public void setResult(String result) {
        Result = result;
    }


    public String getTenantID() {
        return TenantID;
    }

    public void setTenantID(String tenantID) {
        TenantID = tenantID;
    }

    public String getWareHouseID() {
        return WareHouseID;
    }

    public void setWareHouseID(String wareHouseID) {
        WareHouseID = wareHouseID;
    }

    public String getPOHeaderID() {
        return POHeaderID;
    }

    public void setPOHeaderID(String POHeaderID) {
        this.POHeaderID = POHeaderID;
    }

    public String getAccountID() {
        return AccountID;
    }

    public void setAccountID(String accountID) {
        AccountID = accountID;
    }

    public Double getJobOrderTypeID() {
        return jobOrderTypeID;
    }

    public void setJobOrderTypeID(Double jobOrderTypeID) {
        this.jobOrderTypeID = jobOrderTypeID;
    }
}