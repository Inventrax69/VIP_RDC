package com.example.karthikm.merlinwmscipher_vip_rdc.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.LSortListInventory;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;

import java.util.List;

public class SortAdp extends RecyclerView.Adapter{


    private List<LSortListInventory> lSortListInventories;

    Context context;
    public SortAdp(Context context, List<LSortListInventory> list) {
        this.context = context;
        this.lSortListInventories = list;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        //TextView lblColor,tvMCode,tvDesc,tvLocationCode,tvQty,lblSource,lblMomentType,lblTransDate,lblAvailableQty,lblTransQty,lblStatus,tvAvailableQty;// init the item view's

        TextView tvCount, txtCustomer,mCode, qty, description;
        LinearLayout my_linear_layout;


        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            tvCount = (TextView) itemView.findViewById(R.id.tvCount);
            txtCustomer = (TextView) itemView.findViewById(R.id.txtCustomer);
            mCode = (TextView) itemView.findViewById(R.id.mCode);
            qty = (TextView) itemView.findViewById(R.id.qty);
            description = (TextView) itemView.findViewById(R.id.description);

        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new SortAdp.MyViewHolder(v);
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        LSortListInventory lSortListInventory = (LSortListInventory) lSortListInventories.get(position);

        /*if (((SortAdp.MyViewHolder) holder).tvCount.getText().toString().equalsIgnoreCase(String.valueOf(lSortListInventory.getPickPrioritySeq()).split("[.]")[0].trim())) {

            //((SortAdp.MyViewHolder) holder).tvCount.setText(String.valueOf(lSortListInventory.getPickPrioritySeq()));
            ((SortAdp.MyViewHolder) holder).txtCustomer.setVisibility(View.GONE);
            ((SortAdp.MyViewHolder) holder).tvCount.setBackgroundColor(R.color.skuColor);


        } else {

            ((SortAdp.MyViewHolder) holder).txtCustomer.setVisibility(View.VISIBLE);
            ((SortAdp.MyViewHolder) holder).tvCount.setBackgroundColor(R.color.skuColor);
        }*/


        ((MyViewHolder) holder).tvCount.getText();


        ((SortAdp.MyViewHolder) holder).tvCount.setText(String.valueOf(lSortListInventory.getPickPrioritySeq()).split("[.]")[0].trim());
        ((SortAdp.MyViewHolder) holder).txtCustomer.setText(lSortListInventory.getCustomerCode());
        ((SortAdp.MyViewHolder) holder).mCode.setText(lSortListInventory.getMaterialCode());
        ((SortAdp.MyViewHolder) holder).qty.setText(String.valueOf(lSortListInventory.getPicklistQuantity()).split("[.]")[0].trim());
        ((SortAdp.MyViewHolder) holder).description.setText(lSortListInventory.getMaterialDescription());
    }


    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return lSortListInventories.size();
    }

}
