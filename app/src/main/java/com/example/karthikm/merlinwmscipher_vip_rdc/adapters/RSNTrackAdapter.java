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

public class RSNTrackAdapter extends   RecyclerView.Adapter{


    private List<InventoryDTO> liveStockList;

    Context context;
    public RSNTrackAdapter(Context context, List<InventoryDTO> list) {
        this.context = context;
        this.liveStockList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView lblColor,tvMCode,tvDesc,tvLocationCode,tvQty,lblSource,lblMomentType,lblTransDate,lblAvailableQty,lblTransQty,lblStatus,tvAvailableQty;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            lblColor = (TextView) itemView.findViewById(R.id.lblColor);
            tvMCode = (TextView) itemView.findViewById(R.id.txtMCode);
            tvDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            tvQty = (TextView) itemView.findViewById(R.id.txtQty);
            lblSource = (TextView) itemView.findViewById(R.id.lblSource);
            tvLocationCode = (TextView) itemView.findViewById(R.id.txtLocationCode);
            lblMomentType = (TextView) itemView.findViewById(R.id.lblMomentType);
            lblTransDate = (TextView) itemView.findViewById(R.id.lblTransDate);
            lblAvailableQty=(TextView)itemView.findViewById(R.id.lblAvailableQty);
            lblTransQty= (TextView)itemView.findViewById(R.id.lblTransQty);
            lblStatus= (TextView)itemView.findViewById(R.id.lblStatus);
            tvAvailableQty= (TextView)itemView.findViewById(R.id.tvAvailableQty);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rsn_tracking_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        InventoryDTO inventoryDTO = (InventoryDTO) liveStockList.get(position);

        ((MyViewHolder) holder).tvMCode.setText(inventoryDTO.getMaterialCode());
        ((MyViewHolder) holder).tvDesc.setText(inventoryDTO.getMaterialShortDescription());
        ((MyViewHolder) holder).lblSource.setText(inventoryDTO.getSourceType());
        ((MyViewHolder) holder).lblMomentType.setText(inventoryDTO.getMaterialMomentType());
        ((MyViewHolder) holder).lblTransDate.setText(inventoryDTO.getTransactionDate());
        ((MyViewHolder) holder).tvLocationCode.setText(inventoryDTO.getLocationCode());
        ((MyViewHolder) holder).lblSource.setText(inventoryDTO.getSourceType());
        ((MyViewHolder) holder).lblTransQty.setText(inventoryDTO.getQuantity());

        ((MyViewHolder) holder).lblStatus.setText(inventoryDTO.getTransactionStatus());

        if (inventoryDTO.getInout().equals("In")){
            // set the data in items
            ((MyViewHolder) holder).tvAvailableQty.setVisibility(View.VISIBLE);
            ((MyViewHolder) holder).lblAvailableQty.setText(inventoryDTO.getAvailQty());
            ((RSNTrackAdapter.MyViewHolder) holder).lblColor.setBackgroundResource(R.color.green);
            return;
        }
        else {
            // set the data in items
            ((MyViewHolder) holder).tvAvailableQty.setVisibility(View.GONE);
            ((RSNTrackAdapter.MyViewHolder) holder).lblColor.setBackgroundResource(R.color.red);
        }

    }


    @Override
    public int getItemCount() {
        return liveStockList.size();
    }

}
