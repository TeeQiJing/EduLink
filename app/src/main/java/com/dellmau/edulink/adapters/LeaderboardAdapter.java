package com.dellmau.edulink.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dellmau.edulink.R;
import com.dellmau.edulink.models.LeaderboardCard;

import java.util.ArrayList;

public class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.MyViewHolder> {
    Context context;
    ArrayList<LeaderboardCard> testingModelArrayList;
    public LeaderboardAdapter(Context context, ArrayList<LeaderboardCard> testingModelArrayList){
        this.context=context;
        this.testingModelArrayList=testingModelArrayList;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view=inflater.inflate(R.layout.leaderboard_card,parent,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        int actualPosition=position+3;
        if(actualPosition<testingModelArrayList.size()) {
            holder.test_name.setText(testingModelArrayList.get(actualPosition).getTesting_name());
            holder.test_mark.setText(testingModelArrayList.get(actualPosition).getTesting_mark()+" pts");
            int rankingShow=position+4;
            holder.ranking.setText(String.valueOf(rankingShow));
            if(testingModelArrayList.get(actualPosition).getTesting_avatar()!=null) holder.test_avatar.setImageBitmap(testingModelArrayList.get(actualPosition).getTesting_avatar());
            else holder.test_avatar.setImageResource(R.drawable.ic_profile);
        }
    }

    @Override
    public int getItemCount() {
        return Math.min(97, Math.max(0, testingModelArrayList.size() - 3));
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{
        TextView test_name,test_mark,ranking;
        ImageView test_avatar;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            test_name=itemView.findViewById(R.id.test_name);
            test_mark=itemView.findViewById(R.id.test_mark);
            ranking=itemView.findViewById(R.id.ranking);
            test_avatar=itemView.findViewById(R.id.user_avatar);
        }
    }
}
