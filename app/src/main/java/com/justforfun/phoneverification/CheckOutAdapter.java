package com.justforfun.phoneverification;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;

public class CheckOutAdapter extends RecyclerView.Adapter<CheckOutAdapter.MyViewHolder> {

    Context context;
    HashMap<String, CheckOutListing> items;
    //HashMap<Integer, String> keys;

    public CheckOutAdapter (Context c, HashMap<String, CheckOutListing> h) {
        context = c;
        items = h;

    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CheckOutAdapter.MyViewHolder(LayoutInflater.from(context).inflate(R.layout.checkout_item_layout, parent, false));
    }

    int Scost;
    int Squan;
    int Stotal;
    String key;


    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        holder.serial.setText(String.valueOf(position + 1));
        key = items.keySet().toArray()[position].toString();
        holder.ID.setText(key);
        holder.name.setText(items.get(key).getName());
        Scost = items.get(key).getCost();
        holder.cost.setText(String.valueOf(Scost));
        Squan = items.get(key).getQuantity();
        holder.unit.setText(String.valueOf(Squan));
        Stotal = Squan*Scost;
        holder.subTotal.setText(String.valueOf(Stotal));
        holder.delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.linearLayout.setBackgroundResource(R.color.grey);
                Intent intent = new Intent("item_deleted");
                intent.putExtra("key" , holder.ID.getText());
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView serial, ID, name, cost, unit, subTotal;
        Button delete_btn;
        LinearLayout linearLayout;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            serial = itemView.findViewById(R.id.serial);
            ID = itemView.findViewById(R.id.ID);
            name = itemView.findViewById(R.id.iName);
            cost = itemView.findViewById(R.id.Cost);
            unit = itemView.findViewById(R.id.Units);
            subTotal = itemView.findViewById(R.id.subTotal);
            delete_btn = itemView.findViewById(R.id.iv_delete);
            linearLayout = itemView.findViewById(R.id.item_layout);
        }
    }
}
