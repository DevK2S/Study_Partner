package com.studypartner.adapters;

import android.content.Context;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.studypartner.R;
import com.studypartner.models.FileItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
	private static final String TAG = "NotesAdapter";
	
	public interface NotesClickListener {
		void onClick(int position);
		void onLongClick(int position);
	}
	
	private Context mContext;
	private ArrayList<FileItem> mFileItems;
	private SparseBooleanArray selectedItems;
	private NotesClickListener listener;
	
	private static int currentSelectedIndex = -1;
	
	public NotesAdapter(Context context, ArrayList<FileItem> fileItems, NotesClickListener listener) {
		this.mContext = context;
		this.mFileItems = fileItems;
		this.listener = listener;
		selectedItems = new SparseBooleanArray();
	}
	
	public void setFileItems(ArrayList<FileItem> fileItems) {
		mFileItems = fileItems;
	}
	
	@NonNull
	@Override
	public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.notes_item, parent, false);
		return new NotesAdapter.NotesViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull NotesViewHolder holder, final int position) {
		Log.d(TAG, "onBindViewHolder: starts");
		
		FileItem fileItem = mFileItems.get(position);
		holder.fileName.setText(fileItem.getName());
		
		if (fileItem.getType() != FileItem.FileType.FILE_TYPE_FOLDER) {
			Picasso.get()
					.load(fileItem.getPath())
					.error(R.drawable.image_error_icon)
					.into(holder.fileImage);
		}
		
		holder.itemView.setActivated(selectedItems.get(position, false));
		applyClickEvents(holder, position);
		
	}
	
	private void applyClickEvents(NotesViewHolder holder, final int position) {
		holder.fileLayout.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				listener.onClick(position);
			}
		});
		
		holder.fileLayout.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View view) {
				listener.onLongClick(position);
				view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
				return true;
			}
		});
	}
	
	public void toggleSelection(int pos) {
		currentSelectedIndex = pos;
		if (selectedItems.get(pos, false)) {
			selectedItems.delete(pos);
		} else {
			selectedItems.put(pos, true);
		}
		notifyItemChanged(pos);
	}
	
	public void selectAll() {
		for (int i = 0; i < getItemCount(); i++)
			selectedItems.put(i, true);
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
	
	public void removeData(int position) {
		mFileItems.remove(position);
		resetCurrentIndex();
	}
	
	private void resetCurrentIndex() {
		currentSelectedIndex = -1;
	}
	
	@Override
	public int getItemCount() {
		return mFileItems.size();
	}
	
	public class NotesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
		
		TextView fileName;
		ImageView fileImage;
		CardView fileLayout;
		
		public NotesViewHolder(View view) {
			super(view);
			
			fileName = view.findViewById(R.id.file_name);
			fileImage = view.findViewById(R.id.file_image);
			fileLayout = view.findViewById(R.id.file_layout);
			
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
