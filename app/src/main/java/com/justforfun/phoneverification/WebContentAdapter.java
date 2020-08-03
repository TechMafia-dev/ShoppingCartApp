package com.justforfun.phoneverification;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.HashMap;

public class WebContentAdapter extends RecyclerView.Adapter<WebContentAdapter.MyViewHolder> {

    Context context;
    HashMap<String, WebItems> webitems;

    public WebContentAdapter(Context c, HashMap<String, WebItems> l){
        context = c;
        webitems = l;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).inflate(R.layout.web_content_item, parent, false));
    }


    String key;

    @Override
    public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
        key = webitems.keySet().toArray()[position].toString();
        holder.header.setText(webitems.get(key).getHeader());

        Uri uri = Uri.parse(webitems.get(key).getUrl());
        if (webitems.get(key).getIsVideo()) {
            holder.videoView.setVideoURI(uri);
            holder.videoView.seekTo(1);
            MediaController mediaController = new MediaController(context);
            holder.videoView.setMediaController(mediaController);
            mediaController.setAnchorView(holder.videoView);
            holder.videoView.bringToFront();
        }
        else {
            //Picasso.get().load(uri).into(holder.imageView);
            Glide.with(context).load(uri).placeholder(R.drawable.ic_action_play).error(R.drawable.ic_action_play).into(holder.imageView);
            holder.imageView.bringToFront();
        }
        holder.Description.setText(webitems.get(key).getDescription());
    }

    @Override
    public int getItemCount() {
        return webitems.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView header, Description;
        VideoView videoView;
        ImageView imageView;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            header = itemView.findViewById(R.id.content_title);
            videoView = itemView.findViewById(R.id.content_video);
            imageView = itemView.findViewById(R.id.content_image);
            Description = itemView.findViewById(R.id.content_description);
        }
    }

}
