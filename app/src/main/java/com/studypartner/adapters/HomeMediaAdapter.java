package com.studypartner.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.studypartner.R;
import com.studypartner.models.FileItem;

import java.io.File;
import java.util.ArrayList;

public class HomeMediaAdapter extends RecyclerView.Adapter<HomeMediaAdapter.HomeMediaViewHolder> {

    public interface HomeMediaClickListener {
        void onClick(int position);
    }

    private Activity mActivity;
    private ArrayList<FileItem> mFileItems;
    private HomeMediaClickListener listener;

    public HomeMediaAdapter(Activity mActivity, ArrayList<FileItem> mFileItems, HomeMediaClickListener listener) {
        this.mActivity = mActivity;
        this.mFileItems = mFileItems;
        this.listener = listener;
    }


    @NonNull
    @Override
    public HomeMediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.home_media_item, parent, false);
        return new HomeMediaAdapter.HomeMediaViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeMediaViewHolder holder, int position) {
        final FileItem item = mFileItems.get(position);
        File media = new File(item.getPath());
        if (media.exists()) {
            Glide.with(mActivity.getBaseContext())
                    .asBitmap()
                    .load(media)
                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                    .into(holder.homeMediaImage);
        }
        applyClickEvents(holder);

    }

    private void applyClickEvents(final HomeMediaViewHolder holder) {
        holder.homeMediaImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.onClick(holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return mFileItems.size();
    }

    public class HomeMediaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView homeMediaImage;

        public HomeMediaViewHolder(@NonNull View itemView) {
            super(itemView);
            homeMediaImage = itemView.findViewById(R.id.homeMediaImage);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onClick(getAdapterPosition());
        }
    }
}
