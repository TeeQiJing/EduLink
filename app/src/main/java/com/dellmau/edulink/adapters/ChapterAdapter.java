

package com.dellmau.edulink.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dellmau.edulink.R;
import com.dellmau.edulink.models.Chapter;
import com.dellmau.edulink.models.Quiz;

import java.util.List;
import java.util.Map;



public class ChapterAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private final List<Object> contentList; // List containing Chapter and Quiz objects
    private final OnChapterClickListener chapterClickListener;

    // Constants for view types
    private static final int TYPE_CHAPTER = 0;
    private static final int TYPE_QUIZ = 1;

    // Listener interface for handling item click events
    public interface OnChapterClickListener {
        void onChapterClick(Object chapterOrQuiz);
    }

    // Constructor to initialize context, content list, and click listener
    private Map<String, Boolean> completionStateMap;

    public ChapterAdapter(Context context, List<Object> contentList, Map<String, Boolean> completionStateMap, OnChapterClickListener listener) {
        this.context = context;
        this.contentList = contentList;
        this.completionStateMap = completionStateMap;
        this.chapterClickListener = listener;
    }


    @Override
    public int getItemViewType(int position) {
        // Determine the item type (Chapter or Quiz) based on the object's class
        if (contentList.get(position) instanceof Chapter) {
            return TYPE_CHAPTER;
        } else if (contentList.get(position) instanceof Quiz) {
            return TYPE_QUIZ;
        }
        return -1; // Invalid type
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_CHAPTER) {
            // Inflate layout for Chapter items
            View view = inflater.inflate(R.layout.item_chapter, parent, false);
            return new ChapterViewHolder(view);
        } else { // TYPE_QUIZ
            // Inflate layout for Quiz items
            View view = inflater.inflate(R.layout.item_chapter, parent, false); // Separate layout for quizzes
            return new QuizViewHolder(view);
        }
    }

@Override
public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
    Object item = contentList.get(position);
    boolean isCompleted = false;

    if (item instanceof Chapter) {
        Chapter chapter = (Chapter) item;
        isCompleted = completionStateMap.getOrDefault(chapter.getId(), false);
        ((ChapterViewHolder) holder).bind(chapter, isCompleted);
    } else if (item instanceof Quiz) {
        Quiz quiz = (Quiz) item;
        isCompleted = completionStateMap.getOrDefault(quiz.getId(), false);
        ((QuizViewHolder) holder).bind(quiz, isCompleted);
    }

    holder.itemView.setOnClickListener(v -> chapterClickListener.onChapterClick(item));
}


    @Override
    public int getItemCount() {
        return contentList.size(); // Total number of items in the list
    }

    // ViewHolder class for Chapter items
    public static class ChapterViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageView icon;

        private FrameLayout chapterIconContainer;

        public ChapterViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.chapterTitle);
            icon = itemView.findViewById(R.id.chapterIcon);
            chapterIconContainer = itemView.findViewById(R.id.chapterIconContainer);
        }

        public void bind(Chapter chapter, boolean isCompleted) {
            title.setText(chapter.getTitle());
            // Set background of the container based on completion status
            chapterIconContainer.setBackgroundResource(isCompleted ? R.drawable.circle_background : R.drawable.circle_background_inactive);
            icon.setImageResource(R.drawable.ic_word); // The icon remains unchanged, only the background of the container changes
        }
    }

    // ViewHolder class for Quiz items
    public static class QuizViewHolder extends RecyclerView.ViewHolder {
        private final TextView title;
        private final ImageView icon;
        private final FrameLayout chapterIconContainer;

        public QuizViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.chapterTitle);
            icon = itemView.findViewById(R.id.chapterIcon);
            chapterIconContainer = itemView.findViewById(R.id.chapterIconContainer);
        }

        public void bind(Quiz quiz, boolean isCompleted) {
            title.setText(quiz.getTitle());
            // Set background of the container based on completion status
            chapterIconContainer.setBackgroundResource(isCompleted ? R.drawable.circle_background : R.drawable.circle_background_inactive);
            icon.setImageResource(R.drawable.ic_bulb); // The icon remains unchanged, only the background of the container changes
        }
    }
}


