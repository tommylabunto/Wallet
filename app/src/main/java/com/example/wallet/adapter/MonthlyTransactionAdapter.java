package com.example.wallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.R;
import com.example.wallet.db.entity.Transaction;

public class MonthlyTransactionAdapter extends ListAdapter<Transaction, MonthlyTransactionAdapter.MonthlyTransactionViewHolder> {

    class MonthlyTransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTypeName;
        private final TextView textViewTypeAmount;

        private MonthlyTransactionViewHolder(View itemView) {
            super(itemView);
            textViewTypeName = itemView.findViewById(R.id.textView_typeName);
            textViewTypeAmount = itemView.findViewById(R.id.textView_amount);

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
                    oldItem.getDate().equals(newItem.getDate()) &&
                    oldItem.isRepeat() == newItem.isRepeat() &&
                    oldItem.isExpenseTransaction() == newItem.isExpenseTransaction();
        }
    };

    @Override
    public MonthlyTransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_monthly_transaction_item, parent, false);
        MonthlyTransactionViewHolder holder = new MonthlyTransactionViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(MonthlyTransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.textViewTypeName.setText(transaction.getTypeName());
        holder.textViewTypeAmount.setText(transaction.getValue() + "");
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}
