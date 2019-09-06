package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Set;

/**
 * Created by karthik.m on 09/24/2018.
 */

public class SkipReasonDTO {
    @SerializedName("SkipReason")
    private String skipReason;
    @SerializedName("SkipReasonID")
    private String skipReasonID;
    @SerializedName("UserId")
    private String userId;
    @SerializedName("SuggestionID")
    private String SuggestionID;
    @SerializedName("SelectedPickRefNumber")
    private String selectedPickRefNumber;
    public SkipReasonDTO(){

    }

    public SkipReasonDTO(Set<? extends Map.Entry<?, ?>> entries) {
        for(Map.Entry<?, ?> entry : entries) {

            switch (entry.getKey().toString()) {

                case "SkipReason":
                    if (entry.getValue() != null) {
                        this.setSkipReason(entry.getValue().toString());
                    }
                    break;
                case "SkipReasonID":
                    if (entry.getValue() != null) {
                        this.setSkipReasonID(entry.getValue().toString());
                    }
                    break;
                case "UserId":
                    if (entry.getValue() != null) {
                        this.setUserId(entry.getValue().toString());
                    }
                    break;
                case "SuggestionID":
                    if (entry.getValue() != null) {
                        this.setSuggestionID(entry.getValue().toString());
                    }
                    break;
            }
        }
    }

    public String getSelectedPickRefNumber() {
        return selectedPickRefNumber;
    }

    public void setSelectedPickRefNumber(String selectedPickRefNumber) {
        this.selectedPickRefNumber = selectedPickRefNumber;
    }

    public String getSuggestionID() {
        return SuggestionID;
    }

    public void setSuggestionID(String suggestionID) {
        SuggestionID = suggestionID;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSkipReason() {
        return skipReason;
    }

    public void setSkipReason(String skipReason) {
        this.skipReason = skipReason;
    }

    public String getSkipReasonID() {
        return skipReasonID;
    }

    public void setSkipReasonID(String skipReasonID) {
        this.skipReasonID = skipReasonID;
    }
}
