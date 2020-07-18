package com.studypartner.adapters;

import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.navigation.NavigationView;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import com.studypartner.fragments.BasicNotesFragment;
import com.studypartner.models.FileItem;

import java.io.File;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
	
	private Context context;
	private ArrayList<FileItem> list;
	MainActivity activity;
	
	public NotesAdapter(Context context,MainActivity activity, ArrayList<FileItem> list) {
		this.context = context;
		this.list = list;
		this.activity=activity;
	}
	
	@NonNull
	@Override
	public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View itemView = inflater.inflate(R.layout.simple_list_item_1, parent, false);
		return new NotesViewHolder(itemView);
	}
	
	@Override
	public void onBindViewHolder(@NonNull NotesViewHolder holder, final int position) {
		holder.folderName.setText(list.get(position).getName());
		holder.deleteButton.setText("Delete");
		holder.deleteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				File file = new File(list.get(position).getPath());
				list.remove(position);
				file.delete();
				notifyItemRemoved(position);
			}
		});
		holder.openButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if(list.get(position).getType().equals("Folder")) {
					FileItem FileDes=new FileItem(list.get(position).getPath(),list.get(position).getName(),"Folder");
					Bundle bundle=new Bundle();
					bundle.putString("FileDes", FileDes.toString());
					activity.mNavController.navigate(R.id.basicNotesFragment,bundle);
				}
				else
				{}
			}
		});
	}
	
	@Override
	public int getItemCount() {
		return list.size();
	}
	
	static class NotesViewHolder extends RecyclerView.ViewHolder {
		
		TextView folderName;
		Button deleteButton;
		Button openButton;
		
		public NotesViewHolder(View view) {
			super(view);
			folderName = view.findViewById(R.id.folder_name);
			deleteButton = view.findViewById(R.id.delete_button);
			openButton=view.findViewById(R.id.open_button);
		}
	}
}
