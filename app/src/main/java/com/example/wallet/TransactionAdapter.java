package com.example.wallet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.wallet.db.Transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView wordItemView;

        private TransactionViewHolder(View itemView) {
            super(itemView);
            wordItemView = itemView.findViewById(R.id.textView);

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

        public void showHeader(String text) {
            TextView textView = itemView.findViewById(R.id.textView2);
            textView.setText(text);
        }
    }

    private final LayoutInflater mInflater;
    private OnItemClickListener listener;

    private List<Transaction> transactions;

    public TransactionAdapter(Context context) {
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
                    oldItem.getDate().equals(newItem.getDate());
        }
    };

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_item, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction current = getItem(position);
        holder.wordItemView.setText(current.getName());

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(current.getDate());

        SimpleDateFormat formatter = new SimpleDateFormat("ddMMyyyy");
        String formattedDate = formatter.format(calendar.getTime());

        double totalValue = 0;

        for (int i = 0; i < transactions.size(); i++) {
            if (formatter.format(transactions.get(i).getDate()).equals(formattedDate)) {

                totalValue += transactions.get(i).getValue();

                if (i+1 == transactions.size() || ( i+1 < transactions.size() && !formatter.format(transactions.get(i+1).getDate()).equals(formattedDate))) {
                    holder.showHeader(totalValue + "");
                }
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void showHeader(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
