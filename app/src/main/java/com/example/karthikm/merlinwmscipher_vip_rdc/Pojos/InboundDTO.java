package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * Created by Padmaja.B on 6/11/2018.
 */

public class InboundDTO {
    @SerializedName("WarehouseId")
    private String warehouseId;
    @SerializedName("InboundID")
    private String inboundID;
    @SerializedName("StoreRefNo")
    private String storeRefNo;
    @SerializedName("MRP")
    private Double MRP;
    @SerializedName("MOP")
    private String MOP;
    @SerializedName("SKU ")
    private String SKU;
    @SerializedName("SerialNo")
    private String serialNo;
    @SerializedName("MfgDate")
    private String MfgDate;
    @SerializedName("DockLocation")
    private String dockLocation;
    @SerializedName("PalletNo")
    private String palletNo;
    @SerializedName("UserId")
    private String UserId;
    @SerializedName("VehicleNumber")
    private String vehicleNumber;
    @SerializedName("AssociatedGateOfAccess")
    private String AssociatedGateOfAccess;
    @SerializedName("PutawayLocation")
    private String PutawayLocation;
    @SerializedName("VehicleReceivedQuantity")
    private String VehicleReceivedQuantity;
    @SerializedName("VehicleInventoryQuantity")
    private String VehicleInventoryQuantity;
    @SerializedName("VehicleId")
    private String vehicleId;

    @SerializedName("TenantID")
    private String TenantID;
    @SerializedName("POHeaderID")
    private String POHeaderID;
    @SerializedName("AccountID")
    private String AccountID;

    @SerializedName("IsPalletrequired")
    private String IsPalletrequired;






    public InboundDTO() {
    }


    public InboundDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "WarehouseId":
                    if (entry.getValue() != null) {
                        this.setWarehouseId(entry.getValue().toString());
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
                case "DockLocation":
                    if (entry.getValue() != null) {
                        this.setDockLocation(entry.getValue().toString());
                    }
                    break;
                case "PalletNo":
                    if (entry.getValue() != null) {
                        this.setPalletNo(entry.getValue().toString());
                    }
                    break;
                case "UserId":
                    if (entry.getValue() != null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;
                case "VehicleNumber":
                    if (entry.getValue() != null) {
                        this.setVehicleNumber(entry.getValue().toString());
                    }
                    break;
                case "AssociatedGateOfAccess":
                    if (entry.getValue() != null) {
                        this.setAssociatedGateOfAccess(entry.getValue().toString());
                    }
                    break;
                case "PutawayLocation":
                    if (entry.getValue() != null) {
                        this.setPutawayLocation(entry.getValue().toString());
                    }
                    break;
                case "VehicleReceivedQuantity":
                    if (entry.getValue() != null) {
                        this.setVehicleReceivedQuantity(entry.getValue().toString());
                    }
                    break;
                case "VehicleInventoryQuantity":
                    if (entry.getValue() != null) {
                        this.setVehicleInventoryQuantity(entry.getValue().toString());
                    }
                    break;
                case "VehicleId":
                    if (entry.getValue() != null) {
                        this.setVehicleId(entry.getValue().toString());
                    }
                    break;

                case "TenantID":
                    if (entry.getValue() != null) {
                        this.setTenantID(entry.getValue().toString());
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
                case "IsPalletrequired":
                    if (entry.getValue() != null) {
                        this.setIsPalletrequired(entry.getValue().toString());
                    }
                    break;

            }
        }
    }

    public String getIsPalletrequired() {
        return IsPalletrequired;
    }

    public void setIsPalletrequired(String isPalletrequired) {
        IsPalletrequired = isPalletrequired;
    }

    public String getTenantID() {
        return TenantID;
    }

    public void setTenantID(String tenantID) {
        TenantID = tenantID;
    }

    public String getWarehouseId() {
        return warehouseId;
    }

    public void setWarehouseId(String warehouseId) {
        this.warehouseId = warehouseId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getVehicleReceivedQuantity() {
        return VehicleReceivedQuantity;
    }

    public void setVehicleReceivedQuantity(String vehicleReceivedQuantity) {
        VehicleReceivedQuantity = vehicleReceivedQuantity;
    }

    public String getVehicleInventoryQuantity() {
        return VehicleInventoryQuantity;
    }

    public void setVehicleInventoryQuantity(String vehicleInventoryQuantity) {
        VehicleInventoryQuantity = vehicleInventoryQuantity;
    }


    public String getAssociatedGateOfAccess() {
        return AssociatedGateOfAccess;
    }

    public void setAssociatedGateOfAccess(String associatedGateOfAccess) {
        AssociatedGateOfAccess = associatedGateOfAccess;
    }

    public String getVehicleNumber() {
        return vehicleNumber;
    }

    public void setVehicleNumber(String vehicleNumber) {
        this.vehicleNumber = vehicleNumber;
    }

    public String getInboundID() {
        return inboundID;
    }

    public void setInboundID(String inboundID) {
        this.inboundID = inboundID;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getPutawayLocation() {
        return PutawayLocation;
    }

    public void setPutawayLocation(String putawayLocation) {
        PutawayLocation = putawayLocation;
    }

    public String getStoreRefNo() {
        return storeRefNo;
    }

    public void setStoreRefNo(String storeRefNo) {
        this.storeRefNo = storeRefNo;
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

    public String getDockLocation() {
        return dockLocation;
    }

    public void setDockLocation(String dockLocation) {
        this.dockLocation = dockLocation;
    }

    public String getPalletNo() {
        return palletNo;
    }

    public void setPalletNo(String palletNo) {
        this.palletNo = palletNo;
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
}