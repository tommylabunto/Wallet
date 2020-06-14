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

import java.util.List;

public class RepeatTransactionAdapter extends ListAdapter<Transaction, RepeatTransactionAdapter.RepeatTransactionViewHolder> {

    class RepeatTransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView wordItemView;
        private final TextView textViewValue;

        private RepeatTransactionViewHolder(View itemView) {
            super(itemView);
            wordItemView = itemView.findViewById(R.id.textView);
            textViewValue = itemView.findViewById(R.id.textView_value);

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
    private RepeatTransactionAdapter.OnItemClickListener listener;

    private List<Transaction> transactions;

    public RepeatTransactionAdapter(Context context) {
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
                    oldItem.getFrequency() == newItem.getFrequency() &&
                    oldItem.getNumOfRepeat() == newItem.getNumOfRepeat() &&
                    oldItem.isRepeat() == newItem.isRepeat() &&
                    oldItem.isExpenseTransaction() == newItem.isExpenseTransaction();
        }
    };

    @Override
    public RepeatTransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_repeat_item, parent, false);
        RepeatTransactionViewHolder holder = new RepeatTransactionViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(RepeatTransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.wordItemView.setText(transaction.getName());
        holder.textViewValue.setText(transaction.getValue() + "");
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public void setOnItemClickListener(RepeatTransactionAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
