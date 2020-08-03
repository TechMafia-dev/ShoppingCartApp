package com.justforfun.phoneverification;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class ListingAdapter extends RecyclerView.Adapter<ListingAdapter.MyViewHolder> {

    Context context;
    HashMap<String, Items> items;

    public ListingAdapter(Context c, HashMap<String, Items> i){
        context = c;
        items = i;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.listing_item, parent, false));
    }

    int count;
    String key;

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, final int position) {
        key = items.keySet().toArray()[position].toString();
        holder.item_name.setText(items.get(key).getItemName());
        holder.item_type.setText(items.get(key).getItemType());
        if (items.get(key).getItemAvialable()){
            holder.isAvialable1.setText("Avialable");
            holder.isAvialable2.setText("");
        } else {
            holder.isAvialable1.setText("");
            holder.isAvialable2.setText("Item out of stock!!!");
        }
        holder.item_cost.setText(String.valueOf(items.get(key).getItemCost()));
        holder.item_ID.setText(items.get(key).getItemID());

        StorageReference mStorageRef = FirebaseStorage.getInstance().getReference();
        StorageReference fileRef = mStorageRef.child("storelistings/"+ items.get(key).getItemID() +".jpg");
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(holder.item_image);
            }
        });

        holder.item_count.setText(String.valueOf(items.get(key).getItemCount()));

        holder.increase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = Integer.valueOf(holder.item_count.getText().toString());
                count++;
                holder.item_count.setText(String.valueOf(count));
                //for item count
                Intent intent = new Intent("count_value_changed");
                intent.putExtra("id" , String.valueOf(holder.item_ID.getText().toString()));
                intent.putExtra("name", String.valueOf(holder.item_name.getText().toString()));
                intent.putExtra("cost", String.valueOf(holder.item_cost.getText().toString()));
                intent.putExtra("quantity", String.valueOf(holder.item_count.getText().toString()));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
        holder.decrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                count = Integer.valueOf(holder.item_count.getText().toString());
                if (count > 0)
                    count--;
                holder.item_count.setText(String.valueOf(count));
                //for item count
                Intent intent = new Intent("count_value_changed");
                intent.putExtra("id" , String.valueOf(holder.item_ID.getText().toString()));
                intent.putExtra("name", String.valueOf(holder.item_name.getText().toString()));
                intent.putExtra("cost", String.valueOf(holder.item_cost.getText().toString()));
                intent.putExtra("quantity", String.valueOf(holder.item_count.getText().toString()));
                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView item_name, item_type, isAvialable1, isAvialable2, item_cost, item_ID, item_count;
        ImageView item_image;
        Button decrease, increase;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            item_name = itemView.findViewById(R.id.item_header);
            item_type = itemView.findViewById(R.id.itemType);
            isAvialable1 = itemView.findViewById(R.id.item_isAvialable1);
            isAvialable2 = itemView.findViewById(R.id.item_isAvialable2);
            item_cost = itemView.findViewById(R.id.item_cost);
            item_ID = itemView.findViewById(R.id.item_ID);
            item_count = itemView.findViewById(R.id.item_count);
            item_image = itemView.findViewById(R.id.item_image);
            increase = itemView.findViewById(R.id.item_count_increase);
            decrease = itemView.findViewById(R.id.item_count_decrease);
        }
    }
}
