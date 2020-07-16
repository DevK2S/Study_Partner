package com.studypartner.adapters;

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.studypartner.R;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NotesViewHolder> {
    
    private Context context;
    private ArrayList<Pair<String,String> >list;
    
    public NotesAdapter(Context context,ArrayList<Pair<String,String> >list) {
        this.context = context;
        this.list = list;
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
        holder.folderName.setText(list.get(position).second);
        holder.deleteButton.setText("Delete");
        holder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //notifyDataSetChanged();
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, list.size());
                File file = new File(list.get(position).first);
                list.remove(position);
                file.delete();

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
        public NotesViewHolder(View view) {
            super(view);
            folderName = view.findViewById(R.id.folder_name);
            deleteButton=view.findViewById(R.id.delete_button);
        }
    }
}
