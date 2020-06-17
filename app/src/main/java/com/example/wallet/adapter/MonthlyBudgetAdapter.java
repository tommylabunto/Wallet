package com.example.wallet.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.R;
import com.example.wallet.db.entity.MonthlyBudget;

import java.time.Month;
import java.util.Calendar;

public class MonthlyBudgetAdapter extends ListAdapter<MonthlyBudget, MonthlyBudgetAdapter.MonthlyBudgetViewHolder> {

    class MonthlyBudgetViewHolder extends RecyclerView.ViewHolder {
        private final Calendar calendar = Calendar.getInstance();
        private final TextView textViewMonth;
        private final TextView textViewBudget;

        private MonthlyBudgetViewHolder(View itemView) {
            super(itemView);
            textViewMonth = itemView.findViewById(R.id.textView_month);
            textViewBudget = itemView.findViewById(R.id.textView_budget);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(getItem(position));
                    }
                }
            });
        }
    }

    private final LayoutInflater mInflater;
    private MonthlyBudgetAdapter.OnItemClickListener listener;

    public MonthlyBudgetAdapter(Context context) {
        super(DIFF_CALLBACK);
        mInflater = LayoutInflater.from(context);
    }

    private static final DiffUtil.ItemCallback<MonthlyBudget> DIFF_CALLBACK = new DiffUtil.ItemCallback<MonthlyBudget>() {
        @Override
        public boolean areItemsTheSame(MonthlyBudget oldItem, MonthlyBudget newItem) {
            return oldItem.getMonthlyBudgetId() == newItem.getMonthlyBudgetId();
        }

        @Override
        public boolean areContentsTheSame(MonthlyBudget oldItem, MonthlyBudget newItem) {
            return oldItem.getBudget() == newItem.getBudget() &&
                    oldItem.getYear() == newItem.getYear() &&
                    oldItem.getMonth() == newItem.getMonth();
        }
    };

    @Override
    public MonthlyBudgetViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_monthly_budget_item, parent, false);
        MonthlyBudgetViewHolder holder = new MonthlyBudgetViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(MonthlyBudgetViewHolder holder, int position) {
        MonthlyBudget monthlyBudget = getItem(position);

        holder.textViewBudget.setText(monthlyBudget.getBudget() + "");

        // month uses 1 (jan) to 12 (dec)
        Month month = Month.of(monthlyBudget.getMonth() + 1);
        Log.d("month",month.getValue() + "");
        // originally is all caps
        String monthString = month.name().substring(0,1) + month.name().substring(1).toLowerCase();
        holder.textViewMonth.setText(monthString);

        int tempMonth = holder.calendar.get(Calendar.MONTH) + 1;
        Log.d("tempMonth",tempMonth + "");

        // bold same month
        if (tempMonth == month.getValue()) {
            Log.d("same month",tempMonth + "");
            holder.textViewMonth.setTypeface(holder.textViewMonth.getTypeface(), Typeface.BOLD);
            holder.textViewBudget.setTypeface(holder.textViewBudget.getTypeface(), Typeface.BOLD);
        } else {
            holder.textViewMonth.setTypeface(holder.textViewMonth.getTypeface(), Typeface.ITALIC);
            holder.textViewBudget.setTypeface(holder.textViewBudget.getTypeface(), Typeface.ITALIC);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(MonthlyBudget monthlyBudget);
    }

    public void setOnItemClickListener(MonthlyBudgetAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
