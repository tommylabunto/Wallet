package com.xingtingkai.wallet.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.xingtingkai.wallet.R;
import com.xingtingkai.wallet.db.entity.Transaction;

public class MonthlyTransactionAdapter extends ListAdapter<Transaction, MonthlyTransactionAdapter.MonthlyTransactionViewHolder> {

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    class MonthlyTransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTypeName;
        private final TextView textViewTypeAmount;

        private MonthlyTransactionViewHolder(View itemView) {
            super(itemView);
            textViewTypeName = itemView.findViewById(R.id.textView_typeName);
            textViewTypeAmount = itemView.findViewById(R.id.textView_amount);

            // on click
            itemView.setOnClickListener((View v) -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }
    }

    private final LayoutInflater mInflater;
    private MonthlyTransactionAdapter.OnItemClickListener listener;

    public MonthlyTransactionAdapter(Context context) {
        super(DIFF_CALLBACK);
        mInflater = LayoutInflater.from(context);
    }

    private static final DiffUtil.ItemCallback<Transaction> DIFF_CALLBACK = new DiffUtil.ItemCallback<Transaction>() {
        @Override
        public boolean areItemsTheSame(Transaction oldItem, Transaction newItem) {
            return oldItem.getTransactionId() == newItem.getTransactionId();
        }

        @Override
        public boolean areContentsTheSame(Transaction oldItem, Transaction newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getValue() == newItem.getValue() &&
                    oldItem.getTypeName().equals(newItem.getTypeName()) &&
                    oldItem.getInstant().equals(newItem.getInstant()) &&
                    oldItem.getZoneId().equals(newItem.getZoneId()) &&
                    oldItem.isRepeat() == newItem.isRepeat() &&
                    oldItem.isExpenseTransaction() == newItem.isExpenseTransaction();
        }
    };

    @NonNull
    @Override
    public MonthlyTransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_monthly_transaction_item, parent, false);
        return new MonthlyTransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MonthlyTransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.textViewTypeName.setText(transaction.getTypeName());
        String value = mInflater.getContext().getString(R.string.single_string_param, (int) transaction.getValue() + "");
        holder.textViewTypeAmount.setText(value);

        if (!transaction.isExpenseTransaction()) {
            holder.textViewTypeName.setTypeface(holder.textViewTypeName.getTypeface(), Typeface.BOLD);
            holder.textViewTypeAmount.setTypeface(holder.textViewTypeAmount.getTypeface(), Typeface.BOLD);
        } else {
            holder.textViewTypeName.setTypeface(holder.textViewTypeName.getTypeface(), Typeface.NORMAL);
            holder.textViewTypeAmount.setTypeface(holder.textViewTypeAmount.getTypeface(), Typeface.NORMAL);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }
}
