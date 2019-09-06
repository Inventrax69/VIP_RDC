package com.example.karthikm.merlinwmscipher_vip_rdc.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InventoryDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;

import java.util.List;

public class LiveStockAdapter extends  RecyclerView.Adapter{

    private List<InventoryDTO> liveStockList;

        Context context;
        public LiveStockAdapter(Context context, List<InventoryDTO> list) {
            this.context = context;
            this.liveStockList = list;
        }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView tvMCode,tvDesc,tvMop,tvMrp,tvLocationCode,tvSerialNo,txtColor;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            tvMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            tvDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            tvMop = (TextView) itemView.findViewById(R.id.txtMop);
            tvMrp = (TextView) itemView.findViewById(R.id.txtMrp);
            tvLocationCode = (TextView) itemView.findViewById(R.id.txtLocationCode);
            tvSerialNo = (TextView) itemView.findViewById(R.id.txtSerialNo);
            txtColor = (TextView) itemView.findViewById(R.id.txtColor);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            // infalte the item Layout
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.livestock_row_rsn, parent, false);

            // set the view's size, margins, paddings and layout parameters
            return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        InventoryDTO inventoryDTO = (InventoryDTO) liveStockList.get(position);

        // set the data in items
        ((MyViewHolder) holder).tvMCode.setText(inventoryDTO.getMaterialCode());
        ((MyViewHolder) holder).tvLocationCode.setText(inventoryDTO.getLocationCode());
        ((MyViewHolder) holder).tvMrp.setText("MRP: " + inventoryDTO.getMRP());
        ((MyViewHolder) holder).tvMop.setText(inventoryDTO.getMOP());
        ((MyViewHolder) holder).tvDesc.setText(inventoryDTO.getMaterialShortDescription());
        ((MyViewHolder) holder).tvSerialNo.setText(inventoryDTO.getRSN());
        ((MyViewHolder) holder).txtColor.setText(inventoryDTO.getColor());


    }


    @Override
    public int getItemCount() {
            return liveStockList.size();
        }

}
