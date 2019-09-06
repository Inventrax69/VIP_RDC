package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

public class JobDTO {
    @SerializedName("JobRefNumber")
    private String jobRefNumber;
    @SerializedName("ParentSKU ")
    private String parentSKU;
    @SerializedName("ReceiveExpectedQuantity")
    private String receiveExpectedQuantity;
    @SerializedName("ReceivedQuantity")
    private String receivedQuantity;
    @SerializedName("ConsumedQuantity")
    private String consumedQuantity;
    @SerializedName("ParentSKUDesc")
    private String parentSKUDesc;
    @SerializedName("JobOrderTypeID")
    private Double jobOrderTypeID;

    public JobDTO(Set<? extends Map.Entry<?, ?>> entries) {

        for (Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "JobRefNumber":
                    if (entry.getValue() != null) {
                        this.setJobRefNumber(entry.getValue().toString());
                    }
                    break;

                case "ParentSKU":
                    if (entry.getValue() != null) {
                        this.setParentSKU(entry.getValue().toString());
                    }
                    break;
                case "ReceiveExpectedQuantity":
                    if (entry.getValue() != null) {
                        this.setReceiveExpectedQuantity(entry.getValue().toString());
                    }
                    break;
                case "ReceivedQuantity":
                    if (entry.getValue() != null) {
                        this.setReceivedQuantity(entry.getValue().toString());
                    }
                    break;
                case "ConsumedQuantity":
                    if (entry.getValue() != null) {
                        this.setConsumedQuantity(entry.getValue().toString());
                    }
                    break;
                case "ParentSKUDesc":
                    if (entry.getValue() != null) {
                        this.setParentSKUDesc(entry.getValue().toString());
                    }
                    break;
                case "JobOrderTypeID":
                    if (entry.getValue() != null) {
                        this.setJobOrderTypeID(Double.parseDouble(entry.getValue().toString()));
                    }
                    break;

            }
        }
    }


    public String getJobRefNumber() {
        return jobRefNumber;
    }

    public void setJobRefNumber(String jobRefNumber) {
        this.jobRefNumber = jobRefNumber;
    }

    public String getParentSKU() {
        return parentSKU;
    }

    public void setParentSKU(String parentSKU) {
        this.parentSKU = parentSKU;
    }

    public String getReceiveExpectedQuantity() {
        return receiveExpectedQuantity;
    }

    public void setReceiveExpectedQuantity(String receiveExpectedQuantity) {
        this.receiveExpectedQuantity = receiveExpectedQuantity;
    }

    public String getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(String receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    public String getConsumedQuantity() {
        return consumedQuantity;
    }

    public void setConsumedQuantity(String consumedQuantity) {
        this.consumedQuantity = consumedQuantity;
    }

    public String getParentSKUDesc() {
        return parentSKUDesc;
    }

    public void setParentSKUDesc(String parentSKUDesc) {
        this.parentSKUDesc = parentSKUDesc;
    }

    public Double getJobOrderTypeID() {
        return jobOrderTypeID;
    }

    public void setJobOrderTypeID(Double jobOrderTypeID) {
        this.jobOrderTypeID = jobOrderTypeID;
    }
}