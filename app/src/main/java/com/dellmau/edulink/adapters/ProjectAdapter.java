//package com.dellmau.edulink.adapters;
//
//import android.content.Context;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.dellmau.edulink.R;
//import com.dellmau.edulink.models.Project;
//
//import java.util.List;
//import java.util.Map;
//
//public class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.ProjectViewHolder> {
//
//    private Context context;
//    private List<Project> projectList;
//
//    public ProjectAdapter(Context context, List<Project> projectList) {
//        this.context = context;
//        this.projectList = projectList;
//    }
//
//    @NonNull
//    @Override
//    public ProjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.item_project, parent, false);
//        return new ProjectViewHolder(view);
//    }
//
//    @Override
//    public void onBindViewHolder(@NonNull ProjectViewHolder holder, int position) {
//        Project project = projectList.get(position);
//
//        // Set project title
//        holder.projectTitleTextView.setText(project.getProject_title());
//
//        // Set project description (if available)
//        holder.projectDescTextView.setText(project.getProject_desc() != null ? project.getProject_desc() : "No description available.");
//
//        // Set number of students and educators
//        holder.numStudentEducator.setText("Students: " + project.getNumber_student_required() + " | Educators: " + project.getNumber_educator_required());
//
//        // Set deadline
//        holder.projectDeadline.setText(project.getFormattedDeadline() != null ? "Deadline: " + project.getFormattedDeadline() : "No deadline specified.");
//
//        // Set suggested skills
//        Map<String, Integer> suggestedSkills = project.getSuggested_skills();
//        StringBuilder skillsBuilder = new StringBuilder();
//        if (suggestedSkills != null && !suggestedSkills.isEmpty()) {
//            for (Map.Entry<String, Integer> entry : suggestedSkills.entrySet()) {
//                skillsBuilder.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
//            }
//            holder.skillsTextView.setText(skillsBuilder.toString());
//        } else {
//            holder.skillsTextView.setText("No suggested skills available.");
//        }
//
//        // Display similarity score as percentage
//        holder.similarityScoreTextView.setText("Similarity Score: " + project.getSimilarityScore() + "%");
//    }
//
//    @Override
//    public int getItemCount() {
//        return projectList.size();
//    }
//
//    public static class ProjectViewHolder extends RecyclerView.ViewHolder {
//        TextView projectTitleTextView;
//        TextView skillsTextView;
//        TextView projectDescTextView;
//        TextView numStudentEducator;
//        TextView projectDeadline;
//        TextView similarityScoreTextView;
//
//        public ProjectViewHolder(View itemView) {
//            super(itemView);
//            projectTitleTextView = itemView.findViewById(R.id.projectTitle);
//            skillsTextView = itemView.findViewById(R.id.requiredSkills);
//            projectDescTextView = itemView.findViewById(R.id.projectDescription);
//            numStudentEducator = itemView.findViewById(R.id.numberOfStudentsAndEducators);
//            projectDeadline = itemView.findViewById(R.id.deadline);
//            similarityScoreTextView = itemView.findViewById(R.id.similarityScore);
//        }
//    }
//}
