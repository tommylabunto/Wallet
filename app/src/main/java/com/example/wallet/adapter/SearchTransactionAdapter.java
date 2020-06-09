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

public class SearchTransactionAdapter extends ListAdapter<Transaction, SearchTransactionAdapter.SearchTransactionViewHolder> {

    class SearchTransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTypeName;
        private final TextView textViewTypeAmount;

        private SearchTransactionViewHolder(View itemView) {
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
    private OnItemClickListener listener;

    public SearchTransactionAdapter(Context context) {
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
    public SearchTransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_search_transaction_item, parent, false);
        SearchTransactionViewHolder holder = new SearchTransactionViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(SearchTransactionViewHolder holder, int position) {
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
