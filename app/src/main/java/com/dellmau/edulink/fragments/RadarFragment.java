package com.dellmau.edulink.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dellmau.edulink.R;
import com.github.mikephil.charting.charts.RadarChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.RadarData;
import com.github.mikephil.charting.data.RadarDataSet;
import com.github.mikephil.charting.data.RadarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class RadarFragment extends Fragment {


    RadarChart radarChart;
    String user_role;
    private SharedPreferences sharedPreferences;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    public RadarFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_radar, container, false);
        sharedPreferences = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE);
        user_role = sharedPreferences.getString("user_role", "");
        radarChart = rootView.findViewById(R.id.radarChart);

        Map<String, Integer> skill_point = new HashMap<>();
        DocumentReference docRef = db.collection(user_role.toLowerCase()).document(mAuth.getCurrentUser().getUid());

        docRef.get().addOnCompleteListener(getActivity(), task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    // Retrieve the specific 'skill_point' field as a Map<String, Integer>
                    Map<String, Object> retrievedSkillPoint = (Map<String, Object>) document.get("skill_point");

                    // Check if 'skill_point' is not null and then populate the 'skill_point' map
                    if (retrievedSkillPoint != null) {
                        // Clear the existing map and put values from the retrieved map
                        skill_point.clear();
                        for (Map.Entry<String, Object> entry : retrievedSkillPoint.entrySet()) {
                            // Cast the value to Integer and put it in the skill_point map
                            skill_point.put(entry.getKey(), ((Long) entry.getValue()).intValue());
                        }

                        // Create data entries for the radar chart
                        ArrayList<RadarEntry> entries = new ArrayList<>();
                        ArrayList<String> labels = new ArrayList<>(); // To store the labels

                        // Loop through the map to populate the radar chart entries and labels
                        int index = 0; // Initialize the index for the X-axis labels
                        for (Map.Entry<String, Integer> entry : skill_point.entrySet()) {
                            // Add each skill's score as a RadarEntry
                            entries.add(new RadarEntry(entry.getValue().floatValue(), index));

                            // Add each skill's name to the labels list
                            labels.add(entry.getKey());

                            index++;
                        }

                        // Create dataset and set properties
                        RadarDataSet dataSet = new RadarDataSet(entries, "Skills");
                        dataSet.setColor(ColorTemplate.COLORFUL_COLORS[4]);
                        dataSet.setDrawFilled(true);
                        dataSet.setLineWidth(3f);  // Make the line a bit thicker
//                        dataSet.setCol
                        dataSet.setDrawHighlightCircleEnabled(true); // Highlight points when tapped
                        dataSet.setDrawValues(true); // Show values at the points

                        // Increase the size of the radar entry points
                        dataSet.setValueTextSize(15f);  // Increase text size for point values
                        dataSet.setValueTextColor(Color.WHITE);  // Make value text color white

                        // Create radar data
                        RadarData data = new RadarData(dataSet);
                        radarChart.setData(data);

                        // Set the labels for each axis (corners of the radar)
                        radarChart.getXAxis().setValueFormatter(new ValueFormatter() {
                            @Override
                            public String getFormattedValue(float value) {
                                // Convert the float 'value' to an integer index and get the corresponding label
                                int index = (int) value;
                                if (index >= 0 && index < labels.size()) {
                                    return labels.get(index);  // Return the label from the list
                                }
                                return "";
                            }
                        });

                        // Customize the chart appearance
                        radarChart.getXAxis().setDrawLabels(true);  // Display axis labels
                        radarChart.getXAxis().setTextSize(10f);  // Set text size for axis labels
                        radarChart.getXAxis().setTextColor(Color.WHITE); // Set text color for axis labels

                        // Customize Y-axis (Scale)
                        radarChart.getYAxis().setEnabled(true); // Enable Y-axis
                        radarChart.getYAxis().setTextColor(Color.WHITE); // Set Y-axis text color to white
                        radarChart.getYAxis().setLabelCount(5, false); // Adjust label count if needed
                        radarChart.getYAxis().setAxisMinimum(0f); // Ensure the Y-axis doesn't show negative values

                        // Customize the grid lines (white)
                        radarChart.getYAxis().setGridColor(Color.WHITE);  // Set grid line color to white
                        radarChart.getXAxis().setGridColor(Color.WHITE);  // Set X-axis grid line color to white



                        // Hide the legend if not needed
                        radarChart.getLegend().setEnabled(false); // Disable the legend
                        radarChart.getYAxis().setEnabled(false); // Disable Y-axis (scale)
                        radarChart.getXAxis().setDrawGridLines(false); // Remove grid lines on the X-axis
                        radarChart.getYAxis().setDrawGridLines(false); // Remove grid lines on the Y-axis


                        // Hide the legend if not needed
                        radarChart.getLegend().setEnabled(false); // Disable the legend

                        // Refresh the chart
                        radarChart.invalidate();
                    }
                } else {
                    Log.d("Firebase", "No such document");
                }
            } else {
                Log.d("Firebase", "Failed to get document: ", task.getException());
            }
        });

        return rootView;
    }








}