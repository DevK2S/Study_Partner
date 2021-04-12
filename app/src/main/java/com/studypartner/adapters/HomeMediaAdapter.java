package com.studypartner.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.studypartner.R;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class HomeMediaAdapter extends RecyclerView.Adapter<HomeMediaAdapter.HomeMediaViewHolder> {
	
	private final Activity mActivity;
	private final ArrayList<FileItem> mFileItems;
	private final HomeMediaClickListener listener;
	
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
		final FileItem fileItem = mFileItems.get(position);
		
		if (fileItem.getType() == FileType.FILE_TYPE_IMAGE) {
			
			File image = new File(fileItem.getPath());
			if (image.exists()) {

				Glide.with(mActivity.getBaseContext())
						.asBitmap()
						.load(image)
						.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						.into(holder.homeMediaImage);

			} else {
				holder.homeMediaImage.setImageResource(R.drawable.image_add_icon_bs);
			}
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_VIDEO) {
			
			File video = new File(fileItem.getPath());
			if (video.exists()) {
				
				Glide.with(mActivity.getBaseContext())
						.asBitmap()
						.load(video)
						.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						.into(holder.homeMediaImage);
				
			} else {
				holder.homeMediaImage.setImageResource(R.drawable.video_add_icon_bs);
			}
			
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
	
	public interface HomeMediaClickListener {
		void onClick(int position);
	}
	
	public class HomeMediaViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
		
		private final ImageView homeMediaImage;
		
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
