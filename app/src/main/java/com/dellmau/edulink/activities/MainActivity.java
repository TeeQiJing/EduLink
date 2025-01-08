package com.dellmau.edulink.activities;


import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;  // Import Fragment class

import android.os.Bundle;
import android.util.Log;

import com.dellmau.edulink.R;
import com.dellmau.edulink.databinding.ActivityMainBinding;
import com.dellmau.edulink.fragments.ChatFragment;
import com.dellmau.edulink.fragments.CommunityFragment;
import com.dellmau.edulink.fragments.CompanyFragment;
import com.dellmau.edulink.fragments.ContentFragment;
import com.dellmau.edulink.fragments.CourseOutlineFragment;
import com.dellmau.edulink.fragments.LeaderboardFragment;
import com.dellmau.edulink.fragments.LearnFragment;
import com.dellmau.edulink.fragments.LessonFragment;
import com.dellmau.edulink.fragments.ProfileFragment;
import com.dellmau.edulink.fragments.RadarFragment;
import com.dellmau.edulink.models.PopularLessonCard;

public class MainActivity extends AppCompatActivity {

    // Declare binding variable with the correct type
    private ActivityMainBinding binding;

    private ProfileFragment profileFragment;
    private RadarFragment radarFragment;
    private LearnFragment learnFragment;
    private CommunityFragment communityFragment;
    private LeaderboardFragment leaderboardFragment;
    private ChatFragment chatFragment;
    private LessonFragment lessonFragment;
    private ContentFragment contentFragment;
    private CompanyFragment companyFragment;
    private CourseOutlineFragment courseOutlineFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Initialize binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize fragments
        companyFragment = new CompanyFragment();
        profileFragment = new ProfileFragment();
        learnFragment = new LearnFragment();
//        ChapterFragment chapterFragment = new ChapterFragment();
        lessonFragment = new LessonFragment();
        communityFragment = new CommunityFragment();
        leaderboardFragment = new LeaderboardFragment();
        chatFragment = new ChatFragment();
        contentFragment = new ContentFragment();
        radarFragment = new RadarFragment();
        courseOutlineFragment = new CourseOutlineFragment();

        // Load the default fragment
        loadFragment(courseOutlineFragment);


        // Handle BottomNavigation item clicks
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d("Navigation", "Selected item ID: " + itemId); // Add this line to debug
            if (itemId == R.id.nav_learn) {
                loadFragment(companyFragment);
                return true;
            } else if (itemId == R.id.nav_community) {
                loadFragment(communityFragment);
                return true;
            } else if (itemId == R.id.nav_leaderboard) {
                loadFragment(leaderboardFragment);
                return true;
            }  else if (itemId == R.id.nav_profile) {
                loadFragment(profileFragment);
                return true;
            }

            return false;
        });
    }

    // Method to load fragments dynamically
    public void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
