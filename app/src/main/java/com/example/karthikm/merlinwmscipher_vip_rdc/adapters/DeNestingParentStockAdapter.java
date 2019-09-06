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

/**
 * Created by karthik.m on 08/17/2018.
 */

public class DeNestingParentStockAdapter extends  RecyclerView.Adapter {

    private List<InventoryDTO> activeStockList;

    Context context;
    public DeNestingParentStockAdapter(Context context, List<InventoryDTO> list) {
        this.context = context;
        this.activeStockList = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtLocation,txtSKU,txtQty,txtDesc,txtColor;;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtLocation = (TextView) itemView.findViewById(R.id.txtLocation);
            txtSKU = (TextView) itemView.findViewById(R.id.txtSKU);
            txtQty = (TextView) itemView.findViewById(R.id.txtQty);
            txtDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            txtColor = (TextView) itemView.findViewById(R.id.txtColor);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.denesting_activestock_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new DeNestingParentStockAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        InventoryDTO inventoryDTO = (InventoryDTO) activeStockList.get(position);

        // set the data in items
        ((DeNestingParentStockAdapter.MyViewHolder) holder).txtLocation.setText(inventoryDTO.getLocationCode());
        ((DeNestingParentStockAdapter.MyViewHolder) holder).txtSKU.setText(inventoryDTO.getMaterialCode());
        ((DeNestingParentStockAdapter.MyViewHolder) holder).txtDesc.setText(inventoryDTO.getMaterialShortDescription());
        ((DeNestingParentStockAdapter.MyViewHolder) holder).txtQty.setText(inventoryDTO.getQuantity());
        ((DeNestingParentStockAdapter.MyViewHolder) holder).txtColor.setText(inventoryDTO.getColor());

    }


    @Override
    public int getItemCount() {
        return activeStockList.size();
    }
}