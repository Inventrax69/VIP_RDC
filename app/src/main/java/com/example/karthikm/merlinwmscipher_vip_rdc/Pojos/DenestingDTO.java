package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DenestingDTO {

    @SerializedName("UserID")
    private String userID;
    @SerializedName("JobList")
    private List<JobDTO> jobList;
    @SerializedName("PutawayLocation")
    private String putawayLocation;
    @SerializedName("PutawayPallet")
    private String putawayPallet;
    @SerializedName("SerialNumber")
    private String serialNumber;
    @SerializedName("Mop")
    private String Mop;
    @SerializedName("Mrp")
    private String Mrp;
    @SerializedName("BatchNumber")
    private String batchNumber;



    public DenestingDTO() {

    }

    public DenestingDTO(Set<? extends Map.Entry<?, ?>> entries) {

        for (Map.Entry<?, ?> entry : entries) {
            switch (entry.getKey().toString()) {
                case "UserID":
                    if (entry.getValue() != null) {
                        this.setUserID(entry.getValue().toString());
                    }
                    break;
                case "JobList":
                    if (entry.getValue() != null) {
                        List<LinkedTreeMap<?, ?>> jobList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                        List<JobDTO> lstJobList = new ArrayList<JobDTO>();
                        for (int i = 0; i < jobList.size(); i++) {
                            JobDTO dto = new JobDTO(jobList.get(i).entrySet());
                            lstJobList.add(dto);
                            //Log.d("Message", core.getEntityObject().toString());
                        }

                        this.setJobList(lstJobList);
                    }
                    break;
                case "PutawayLocation":
                    if (entry.getValue() != null) {
                        this.setPutawayLocation(entry.getValue().toString());
                    }
                    break;
                case "PutawayPallet":
                    if (entry.getValue() != null) {
                        this.setPutawayPallet(entry.getValue().toString());
                    }
                    break;
                case "SerialNumber":
                    if (entry.getValue() != null) {
                        this.setSerialNumber(entry.getValue().toString());
                    }
                    break;
                case "BatchNumber":
                    if (entry.getValue() != null) {
                        this.setBatchNumber(entry.getValue().toString());
                    }
                    break;
                case "Mrp":
                    if (entry.getValue() != null) {
                        this.setMrp(entry.getValue().toString());
                    }
                    break;
                case "Mop":
                    if (entry.getValue() != null) {
                        this.setMop(entry.getValue().toString());
                    }
                    break;

            }

        }
    }


    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public List<JobDTO> getJobList() {
        return jobList;
    }

    public void setJobList(List<JobDTO> jobList) {
        this.jobList = jobList;
    }

    public String getPutawayLocation() {
        return putawayLocation;
    }

    public void setPutawayLocation(String putawayLocation) {
        this.putawayLocation = putawayLocation;
    }

    public String getPutawayPallet() {
        return putawayPallet;
    }

    public void setPutawayPallet(String putawayPallet) {
        this.putawayPallet = putawayPallet;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getMop() {
        return Mop;
    }

    public void setMop(String mop) {
        Mop = mop;
    }

    public String getMrp() {
        return Mrp;
    }

    public void setMrp(String mrp) {
        Mrp = mrp;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }
}
