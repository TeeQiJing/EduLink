package com.dellmau.edulink.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dellmau.edulink.R;
import com.dellmau.edulink.models.CertCard;

import java.util.ArrayList;

public class CertAdapter extends RecyclerView.Adapter<CertAdapter.CertViewHolder> {
    private Context context;
    private ArrayList<CertCard> certCartArrayList;

    public CertAdapter(Context context, ArrayList<CertCard> certCartArrayList) {
        this.context = context;
        this.certCartArrayList = certCartArrayList;
    }

    @NonNull
    @Override
    public CertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.cert_card,parent,false);
        return new CertAdapter.CertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CertViewHolder holder, int position) {
        if(position<certCartArrayList.size()) {
            holder.date.setText("Developer Certification on "+certCartArrayList.get(position).getDate());
            holder.user_name.setText(certCartArrayList.get(position).getUser_name());
            holder.course_name.setText(certCartArrayList.get(position).getCourse_name());
        }
    }

    @Override
    public int getItemCount() {
        return certCartArrayList.size();
    }

    public static class CertViewHolder extends RecyclerView.ViewHolder{
        TextView date, user_name, course_name;
        public CertViewHolder(@NonNull View itemView) {
            super(itemView);
            date=itemView.findViewById(R.id.date);
            user_name=itemView.findViewById(R.id.name);
            course_name=itemView.findViewById(R.id.course_name);
        }
    }
}
