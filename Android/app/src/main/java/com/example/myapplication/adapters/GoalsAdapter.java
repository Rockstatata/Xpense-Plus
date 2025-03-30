package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Goal;
import java.util.List;

// GoalsAdapter.java
public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {
    private List<Goal> goals;
    private OnGoalLongClickListener longClickListener;

    public GoalsAdapter(List<Goal> goals, OnGoalLongClickListener longClickListener) {
        this.goals = goals;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goals.get(position);
        holder.bind(goal);
        holder.itemView.setOnLongClickListener(v -> {
            longClickListener.onGoalLongClick(goal);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return goals.size();
    }

    public void updateGoals(List<Goal> newGoals) {
        this.goals = newGoals;
        notifyDataSetChanged();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        private TextView titleTextView;
        private TextView targetAmountTextView;
        private TextView currentAmountTextView;
        private TextView endDateTextView;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.goal_name);
            targetAmountTextView = itemView.findViewById(R.id.goal_target);
            currentAmountTextView = itemView.findViewById(R.id.goal_current);
            endDateTextView = itemView.findViewById(R.id.goal_end_date);
        }

        public void bind(Goal goal) {
            titleTextView.setText(goal.getTitle());
            targetAmountTextView.setText(String.valueOf(goal.getTargetAmount()));
            currentAmountTextView.setText(String.valueOf(goal.getCurrentAmount()));
            endDateTextView.setText(goal.getEndDate());
        }
    }

    public interface OnGoalLongClickListener {
        void onGoalLongClick(Goal goal);
    }
}