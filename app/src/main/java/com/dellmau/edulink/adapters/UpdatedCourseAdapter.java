package com.dellmau.edulink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UpdatedCourseAdapter extends RecyclerView.Adapter<UpdatedCourseAdapter.ViewHolder> {

    private List<String> updatedCourseList;

    public UpdatedCourseAdapter(List<String> updatedCourseList) {
        this.updatedCourseList = updatedCourseList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textView.setText(updatedCourseList.get(position));
    }

    @Override
    public int getItemCount() {
        return updatedCourseList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
