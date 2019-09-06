package com.example.karthikm.merlinwmscipher_vip_rdc.adapters;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.karthikm.merlinwmscipher_vip_rdc.Fragments.AvailableStockList;
import com.example.karthikm.merlinwmscipher_vip_rdc.Pojos.InventoryDTO;
import com.example.karthikm.merlinwmscipher_vip_rdc.R;
import com.example.karthikm.merlinwmscipher_vip_rdc.util.FragmentUtils;

import java.util.List;

public class DeNestingAdapter extends RecyclerView.Adapter {

    private List<InventoryDTO> denestingList;
    private FragmentActivity fragmentActivity;
    Context context;
    String jobOrderTypeId;


    public DeNestingAdapter(Context context, List<InventoryDTO> list, FragmentActivity fragmentActivity, String jobOrderTypeId) {
        this.context = context;
        this.denestingList = list;
        this.fragmentActivity = fragmentActivity;
        this.jobOrderTypeId = jobOrderTypeId;

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView txtSKU, txtDesc, txtQty, lblColor;// init the item view's

        public MyViewHolder(View itemView) {

            super(itemView);
            // get the reference of item view's
            txtSKU = (TextView) itemView.findViewById(R.id.txtSKU);
            txtDesc = (TextView) itemView.findViewById(R.id.txtDesc);
            txtQty = (TextView) itemView.findViewById(R.id.txtQty);
            lblColor = (TextView) itemView.findViewById(R.id.lblColor);
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // infalte the item Layout
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.denesting_list_row, parent, false);

        // set the view's size, margins, paddings and layout parameters
        return new DeNestingAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final InventoryDTO inventoryDTO = (InventoryDTO) denestingList.get(position);

        if (jobOrderTypeId.equals("2.0")) {

            // job order type ID = 2.0 ----> DeNesting
            // job order type ID = 1.0 ----> Nesting

            if (inventoryDTO.getMaterialParent()) {
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context c = v.getContext();
                        Bundle bundle = new Bundle();
                        bundle.putString("MaterialCode", inventoryDTO.getMaterialCode());
                        bundle.putString("JobOrdeNo", inventoryDTO.getReferenceDocumentNumber());
                        bundle.putString("jobOrderTypeId", jobOrderTypeId);
                        AvailableStockList availbleStockFragment = new AvailableStockList();
                        availbleStockFragment.setArguments(bundle);
                        FragmentUtils.replaceFragmentWithBackStack(fragmentActivity, R.id.container_body, availbleStockFragment);
                    }
                });

                // set the data in items
                ((DeNestingAdapter.MyViewHolder) holder).txtSKU.setText(inventoryDTO.getMaterialCode());
                ((DeNestingAdapter.MyViewHolder) holder).txtDesc.setText(inventoryDTO.getMaterialShortDescription());
                ((DeNestingAdapter.MyViewHolder) holder).txtQty.setTextColor(ContextCompat.getColor(context, R.color.parentColor));
                ((DeNestingAdapter.MyViewHolder) holder).txtQty.setText(inventoryDTO.getDocumentProcessedQuantity() + "/" + inventoryDTO.getDocumentQuantity());
                ((DeNestingAdapter.MyViewHolder) holder).lblColor.setBackgroundResource(R.color.parentColor);
                return;
            }

            // set the data in items
            ((DeNestingAdapter.MyViewHolder) holder).txtSKU.setText(inventoryDTO.getMaterialCode());
            ((DeNestingAdapter.MyViewHolder) holder).txtDesc.setText(inventoryDTO.getMaterialShortDescription());
            ((DeNestingAdapter.MyViewHolder) holder).txtQty.setTextColor(ContextCompat.getColor(context, R.color.childColor));
            ((DeNestingAdapter.MyViewHolder) holder).txtQty.setText(inventoryDTO.getDocumentProcessedQuantity() + "/" + inventoryDTO.getDocumentQuantity());
            ((DeNestingAdapter.MyViewHolder) holder).lblColor.setBackgroundResource(R.color.childColor);

        } else {
            if (!inventoryDTO.getMaterialParent()) {

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Context c = v.getContext();
                        Bundle bundle = new Bundle();
                        bundle.putString("MaterialCode", inventoryDTO.getMaterialCode());
                        bundle.putString("JobOrdeNo", inventoryDTO.getReferenceDocumentNumber());
                        bundle.putString("jobOrderTypeId", jobOrderTypeId);
                        AvailableStockList availbleStockFragment = new AvailableStockList();
                        availbleStockFragment.setArguments(bundle);
                        FragmentUtils.replaceFragmentWithBackStack(fragmentActivity, R.id.container_body, availbleStockFragment);
                    }
                });

                // set the data in items
                ((DeNestingAdapter.MyViewHolder) holder).txtSKU.setText(inventoryDTO.getMaterialCode());
                ((DeNestingAdapter.MyViewHolder) holder).txtDesc.setText(inventoryDTO.getMaterialShortDescription());
                ((DeNestingAdapter.MyViewHolder) holder).txtQty.setTextColor(ContextCompat.getColor(context, R.color.childColor));
                ((DeNestingAdapter.MyViewHolder) holder).txtQty.setText(inventoryDTO.getDocumentProcessedQuantity() + "/" + inventoryDTO.getDocumentQuantity());
                ((DeNestingAdapter.MyViewHolder) holder).lblColor.setBackgroundResource(R.color.childColor);


                return;
            }

            // set the data in items
            ((DeNestingAdapter.MyViewHolder) holder).txtSKU.setText(inventoryDTO.getMaterialCode());
            ((DeNestingAdapter.MyViewHolder) holder).txtDesc.setText(inventoryDTO.getMaterialShortDescription());
            ((DeNestingAdapter.MyViewHolder) holder).txtQty.setTextColor(ContextCompat.getColor(context, R.color.parentColor));
            ((DeNestingAdapter.MyViewHolder) holder).txtQty.setText(inventoryDTO.getDocumentProcessedQuantity() + "/" + inventoryDTO.getDocumentQuantity());
            ((DeNestingAdapter.MyViewHolder) holder).lblColor.setBackgroundResource(R.color.parentColor);


        }

    }

    @Override
    public int getItemCount() {
        return denestingList.size();
    }

}