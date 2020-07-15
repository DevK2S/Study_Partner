package com.studypartner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.studypartner.R;
import com.studypartner.activities.MainActivity;
import java.util.ArrayList;

public class customRecyclerAdapter extends RecyclerView.Adapter<customRecyclerAdapter.ViewHolder>
{

    Context context;
    ArrayList<String> list;
    public customRecyclerAdapter(Context context, ArrayList<String> list)
    {
        this.context=context;
        this.list=list;
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.folderName.setText(list.get(position));
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView folderName;
        public ViewHolder(View view) {
            super(view);
            folderName = view.findViewById(R.id.text1);
        }
    }
}
