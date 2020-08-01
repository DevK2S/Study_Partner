package com.studypartner.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.studypartner.R;
import com.studypartner.models.FileItem;
import com.studypartner.utils.FileType;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
	
	public interface NotesClickListener {
		void onClick(int position);
		void onLongClick(int position);
		void onOptionsClick(View view, int position);
	}
	
	private Activity mActivity;
	private ArrayList<FileItem> mFileItems;
	private ArrayList<FileItem> mFileItemsCopy;
	private SparseBooleanArray selectedItems;
	private NotesClickListener listener;
	
	private boolean isOptionsVisible;
	
	public NotesAdapter(Activity activity, ArrayList<FileItem> fileItems, NotesClickListener listener, boolean isOptionVisible) {
		this.mActivity = activity;
		this.mFileItems = fileItems;
		this.mFileItemsCopy = new ArrayList<>();
		mFileItemsCopy.addAll(mFileItems);
		this.listener = listener;
		selectedItems = new SparseBooleanArray();
		this.isOptionsVisible = isOptionVisible;
	}
	
	@NonNull
	@Override
	public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.notes_item, parent, false);
		return new NotesAdapter.NotesViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull final NotesViewHolder holder, final int position) {
		
		final FileItem fileItem = mFileItems.get(position);
		
		holder.fileName.setText(fileItem.getName());
		
		if (fileItem.getType() == FileType.FILE_TYPE_FOLDER) {
			
			if (fileItem.isStarred()) {
				holder.fileImage.setImageResource(R.drawable.folder_starred_icon);
			} else {
				holder.fileImage.setImageResource(R.drawable.folder_icon);
			}
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_IMAGE) {
			
			File image = new File(fileItem.getPath());
			if (image.exists()) {
				
				Glide.with(mActivity.getBaseContext())
						.asBitmap()
						.load(image)
						.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						.into(holder.fileImage);
				
			} else {
				holder.fileImage.setImageResource(R.drawable.image_add_icon_bs);
			}
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_VIDEO) {
			
			File video = new File(fileItem.getPath());
			if (video.exists()) {
				
				Glide.with(mActivity.getBaseContext())
						.asBitmap()
						.load(video)
						.diskCacheStrategy(DiskCacheStrategy.RESOURCE)
						.into(holder.fileImage);
				
			} else {
				holder.fileImage.setImageResource(R.drawable.video_add_icon_bs);
			}
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_AUDIO) {
			
			holder.fileImage.setImageResource(R.drawable.headphone_icon);
			
		} else if (fileItem.getType() == FileType.FILE_TYPE_LINK) {
			
			holder.fileImage.setImageResource(R.drawable.link_icon);
			
		} else {
			if (fileItem.getName().contains(".doc") || fileItem.getName().contains(".docx")) {
				holder.fileImage.setImageResource(R.drawable.doc_icon);
			} else if(fileItem.getName().contains(".pdf")) {
				holder.fileImage.setImageResource(R.drawable.pdf_icon);
			} else if(fileItem.getName().contains(".ppt") || fileItem.getName().contains(".pptx")) {
				holder.fileImage.setImageResource(R.drawable.ppt_icon);
			} else if(fileItem.getName().contains(".xls") || fileItem.getName().contains(".xlsx")) {
				holder.fileImage.setImageResource(R.drawable.excel_icon);
			} else if(fileItem.getName().contains(".zip") || fileItem.getName().contains(".rar")) {
				holder.fileImage.setImageResource(R.drawable.zip_icon);
			} else if(fileItem.getName().contains(".txt")) {
				holder.fileImage.setImageResource(R.drawable.txt_icon);
			} else {
				holder.fileImage.setImageResource(R.drawable.file_icon);
			}
		}
		
		holder.fileLayout.setBackground(mActivity.getDrawable(R.drawable.notes_item_background_odd));
		
		if (selectedItems.get(position, false)) {
			holder.itemView.setActivated(true);
		} else {
			holder.itemView.setActivated(false);
			if (position % 2 == 0) {
				holder.fileLayout.setBackground(mActivity.getDrawable(R.drawable.notes_item_background_even));
			}
		}
		
		if (selectedItems.size() > 0 || !isOptionsVisible) {
			holder.fileOptions.setVisibility(View.GONE);
		} else {
			holder.fileOptions.setVisibility(View.VISIBLE);
		}
		
		applyClickEvents(holder);
		
	}
	
	private void applyClickEvents(final NotesViewHolder holder) {
		holder.fileOptions.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				listener.onOptionsClick(v,holder.getAdapterPosition());
			}
		});
		
		holder.fileLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listener.onClick(holder.getAdapterPosition());
			}
		});
		
		holder.fileLayout.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				listener.onLongClick(holder.getAdapterPosition());
				view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				return true;
			}
		});
	}
	
	public void filter (String query) {
		mFileItems.clear();
		
		if (!query.isEmpty()) {
			for (FileItem item : mFileItemsCopy) {
				if (item.getName().toLowerCase().contains(query.toLowerCase())) {
					mFileItems.add(item);
				}
			}
			
		} else {
			mFileItems.addAll(mFileItemsCopy);
		}
		notifyDataSetChanged();
	}
	
	public void toggleSelection(int pos) {
		int oldSelectedItemSize = selectedItems.size();
		
		if (selectedItems.get(pos, false)) {
			selectedItems.delete(pos);
		} else {
			selectedItems.put(pos, true);
		}
		
		if (selectedItems.size() == 1 && selectedItems.size() > oldSelectedItemSize) {
			notifyDataSetChanged();
		} else {
			notifyItemChanged(pos);
		}
	}
	
	public void selectAll() {
		for (int i = 0; i < getItemCount(); i++) {
			if (!selectedItems.get(i, false)) {
				selectedItems.put(i, true);
			}
		}
		notifyDataSetChanged();
	}
	
	public void clearSelections() {
		selectedItems.clear();
		notifyDataSetChanged();
	}
	
	public int getSelectedItemCount() {
		return selectedItems.size();
	}
	
	public ArrayList<Integer> getSelectedItems() {
		ArrayList<Integer> items = new ArrayList<>(selectedItems.size());
		for (int i = 0; i < selectedItems.size(); i++) {
			items.add(selectedItems.keyAt(i));
		}
		return items;
	}
	
	@Override
	public int getItemCount() {
		return mFileItems.size();
	}
	
	public class NotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		
		TextView fileName;
		ImageView fileImage;
		ImageButton fileOptions;
		CardView fileLayout;
		
		public NotesViewHolder(View view) {
			super(view);
			
			fileName = view.findViewById(R.id.fileName);
			fileImage = view.findViewById(R.id.fileImage);
			fileLayout = view.findViewById(R.id.fileLayout);
			fileOptions = view.findViewById(R.id.fileOptions);
			
			view.setOnClickListener(this);
			view.setOnLongClickListener(this);
		}
		
		@Override
		public void onClick(View v) {
			listener.onClick(getAdapterPosition());
		}
		
		@Override
		public boolean onLongClick(View v) {
			listener.onLongClick(getAdapterPosition());
			v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
			return true;
		}
	}
}