package com.studypartner.adapters;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.studypartner.R;
import com.studypartner.models.FileItem;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
	private static final String TAG = "NotesAdapter";
	
	public interface NotesClickListener {
		void onClick(View view, int position);
		void onLongClick(View view, int position);
	}
	
	private Context mContext;
	private ArrayList<FileItem> mFileItems;
	
	public NotesAdapter(Context context, ArrayList<FileItem> fileItems) {
		mContext = context;
		mFileItems = fileItems;
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
	}
	
	@Override
	public int getItemCount() {
		return mFileItems.size();
	}
	
	static class NotesViewHolder extends RecyclerView.ViewHolder {
		
		TextView fileName;
		
		public NotesViewHolder(View view) {
			super(view);
			
			fileName = view.findViewById(R.id.file_name);
		}
	}
	
	public static class NotesItemTouchListener implements RecyclerView.OnItemTouchListener {
		private NotesClickListener mNotesClickListener;
		private GestureDetector mGestureDetector;
		
		public NotesItemTouchListener(Context context, final RecyclerView recyclerView, final NotesClickListener notesClickListener) {
			this.mNotesClickListener = notesClickListener;
			this.mGestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
				@Override
				public boolean onSingleTapUp(MotionEvent e) {
					return true;
				}
				
				@Override
				public void onLongPress(MotionEvent e) {
					View child = recyclerView.findChildViewUnder(e.getX(), e.getY());
					if (child != null && notesClickListener != null) {
						notesClickListener.onLongClick(child, recyclerView.getChildAdapterPosition(child));
					}
				}
			});
		}
		
		@Override
		public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
			View child = rv.findChildViewUnder(e.getX(), e.getY());
			if (child != null && mNotesClickListener != null && mGestureDetector.onTouchEvent(e)) {
				mNotesClickListener.onClick(child, rv.getChildAdapterPosition(child));
			}
			
			return false;
		}
		
		@Override
		public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
		
		}
		
		@Override
		public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
		
		}
	}
}
