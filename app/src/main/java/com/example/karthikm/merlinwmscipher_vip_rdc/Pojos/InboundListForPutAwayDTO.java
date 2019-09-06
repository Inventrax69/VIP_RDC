package com.example.karthikm.merlinwmscipher_vip_rdc.Pojos;

import com.google.gson.annotations.SerializedName;
import com.google.gson.internal.LinkedTreeMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by karthik.m on 06/12/2018.
 */


public class InboundListForPutAwayDTO {

    @SerializedName("UserId")
    private String UserId;

    @SerializedName("MRP")
    private String MRP;

    @SerializedName("InboundList")
    private List<InboundDTO> inboundList;


    public InboundListForPutAwayDTO(Set<? extends Map.Entry<?, ?>> entries)
    {

        for(Map.Entry<?, ?> entry : entries)
        {

           switch(entry.getKey().toString())
           {

               case   "UserId":
                   if(entry.getValue()!=null) {
                       this.setUserId(entry.getValue().toString());
                   }
                   break;
               case   "MRP":
                   if(entry.getValue()!=null) {
                       this.setMRP( entry.getValue().toString());
                   }
                   break;
               case   "InboundList":
                   if(entry.getValue()!=null) {
                       List<LinkedTreeMap<?, ?>> treemapList = (List<LinkedTreeMap<?, ?>>) entry.getValue();
                       List<InboundDTO> lstInbound = new ArrayList<InboundDTO>();
                       for (int i = 0; i < treemapList.size(); i++) {
                           InboundDTO dto = new InboundDTO(treemapList.get(i).entrySet());
                           lstInbound.add(dto);
                       }

                       this.setInboundList(lstInbound);
                   }
                   break;


           }

        }


    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getMRP() {
        return MRP;
    }

    public void setMRP(String MRP) {
        this.MRP = MRP;
    }

    public List<InboundDTO> getInboundList() {
        return inboundList;
    }

    public void setInboundList(List<InboundDTO> inboundList) {
        this.inboundList = inboundList;
    }


}
