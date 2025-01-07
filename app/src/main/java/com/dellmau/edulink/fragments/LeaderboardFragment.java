package com.dellmau.edulink.fragments;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.dellmau.edulink.R;
import com.dellmau.edulink.adapters.LeaderboardAdapter;
import com.dellmau.edulink.models.LeaderboardCard;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;

public class LeaderboardFragment extends Fragment {


    public LeaderboardFragment() {
        // Required empty public constructor
    }

    ArrayList<LeaderboardCard> testingModelArrayList=new ArrayList<>();
    FirebaseAuth fAuth;
    FirebaseFirestore db;
    ArrayList<String> nameList=new ArrayList<>();
    ArrayList<String> markList=new ArrayList<>();
    ArrayList<Bitmap> avatarList=new ArrayList<>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth=FirebaseAuth.getInstance();
        db=FirebaseFirestore.getInstance();
    }
    private void setUpTestingmodel(final View view){
        db.collection("users").get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                testingModelArrayList.clear();
                nameList.clear();
                markList.clear();
                avatarList.clear();
                for(QueryDocumentSnapshot document: task.getResult()){
                    String username=document.getString("username");
                    String mark=String.valueOf(document.getLong("xp"));
                    nameList.add(username);
                    markList.add(mark);
                    if (document.getString("avatar") != null) {
                        String base64Image = document.getString("avatar");
                        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        avatarList.add(decodedByte);
                    }else{
                        avatarList.add(null);
                    }
                }
                for(int i=0;i<nameList.size();i++){
                    testingModelArrayList.add(new LeaderboardCard(nameList.get(i),markList.get(i),avatarList.get(i)));
                }
                Collections.sort(testingModelArrayList);
                updateUI(view);
            }else{
                Log.e("FireStore","Error getting documents: ",task.getException());
            }
        });
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_leaderboard,container,false);
        setUpTestingmodel(view);
        return view;
    }

    public void updateUI(View view){
        TextView firstName=view.findViewById(R.id.firstName);
        TextView firstMark=view.findViewById(R.id.firstMark);
        TextView secondName=view.findViewById(R.id.secondName);
        TextView secondMark=view.findViewById(R.id.secondMark);
        TextView thirdName=view.findViewById(R.id.thirdName);
        TextView thirdMark=view.findViewById(R.id.thirdMark);
        ImageView first_avatar=view.findViewById(R.id.first_avatar);
        ImageView second_avatar=view.findViewById(R.id.second_avatar);
        ImageView third_avatar=view.findViewById(R.id.third_avatar);
        TextView []name={firstName,secondName,thirdName};
        TextView []mark={firstMark,secondMark,thirdMark};
        ImageView []avatar={first_avatar,second_avatar,third_avatar};
        for(int i=0;i<testingModelArrayList.size()&&i<3;i++) {
            name[i].setText(testingModelArrayList.get(i).getTesting_name());
            mark[i].setText(testingModelArrayList.get(i).getTesting_mark()+" pts");
            if(testingModelArrayList.get(i).getTesting_avatar()!=null) avatar[i].setImageBitmap(testingModelArrayList.get(i).getTesting_avatar());
            else avatar[i].setImageResource(R.drawable.ic_profile);
        }
        RecyclerView recyclerView=view.findViewById(R.id.test_recycler);
        LeaderboardAdapter adapter=new LeaderboardAdapter(requireContext(),testingModelArrayList);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
    }

}
