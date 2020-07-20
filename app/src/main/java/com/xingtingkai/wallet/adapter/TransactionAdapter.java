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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewName;
        private final TextView textViewValue;
        private final TextView textView_date;
        private final TextView textView_dayOfDate;
        private final TextView textView_totalAmount;

        private TransactionViewHolder(View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textView_name);
            textViewValue = itemView.findViewById(R.id.textView_value);
            textView_date = itemView.findViewById(R.id.textView_date);
            textView_dayOfDate = itemView.findViewById(R.id.textView_day_of_date);
            textView_totalAmount = itemView.findViewById(R.id.textView_totalAmount);

            // on click
            itemView.setOnClickListener((View v) -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void showHeader(String date, String day, String totalAmount) {

            if (textView_date != null) {
                textView_date.setText(date);
                textView_date.setVisibility(View.VISIBLE);
            }

            if (textView_totalAmount != null) {
                textView_totalAmount.setText(totalAmount);
                textView_totalAmount.setVisibility(View.VISIBLE);
            }

            if (textView_dayOfDate != null) {
                textView_dayOfDate.setText(day);
                textView_dayOfDate.setVisibility(View.VISIBLE);
            }
        }

        /*
        don't remove view otherwise it will cause problems causing it to be null
        changing view to INVISIBLE still takes up space, so GONE is the solution
         */
        public void removeHeader() {

            textView_date.setVisibility(View.GONE);
            textView_totalAmount.setVisibility(View.GONE);
            textView_dayOfDate.setVisibility(View.GONE);
        }
    }

    private final LayoutInflater mInflater;
    private OnItemClickListener listener;

    private List<Transaction> transactions;

    private boolean isFirst;

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
                    oldItem.getInstant().equals(newItem.getInstant()) &&
                    oldItem.getZoneId().equals(newItem.getZoneId()) &&
                    oldItem.isRepeat() == newItem.isRepeat() &&
                    oldItem.isExpenseTransaction() == newItem.isExpenseTransaction();
        }
    };

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_transaction, parent, false);
        return new TransactionViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction transaction = getItem(position);
        holder.textViewName.setText(transaction.getName());
        String value = mInflater.getContext().getString(R.string.single_string_param, (int) transaction.getValue() + "");
        holder.textViewValue.setText(value);

        if (!transaction.isExpenseTransaction()) {
            holder.textViewName.setTypeface(holder.textViewName.getTypeface(), Typeface.BOLD);
            holder.textViewValue.setTypeface(holder.textViewValue.getTypeface(), Typeface.BOLD);
        } else {
            holder.textViewName.setTypeface(holder.textViewName.getTypeface(), Typeface.NORMAL);
            holder.textViewValue.setTypeface(holder.textViewValue.getTypeface(), Typeface.NORMAL);
        }

        updateHeader(transaction, holder);
    }

    public interface OnItemClickListener {
        void onItemClick(Transaction transaction);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void passTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    private void updateHeader(Transaction transaction, TransactionViewHolder holder) {

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(transaction.getInstant(), transaction.getZoneId());

        DateTimeFormatter formatterDate = DateTimeFormatter.ofPattern("dd");
        String formattedDate = formatterDate.format(zonedDateTime);

        DateTimeFormatter formatterDay = DateTimeFormatter.ofPattern("EEE");
        String formattedDay = formatterDay.format(zonedDateTime);

        this.isFirst = false;

        if (transactions != null || !transactions.isEmpty()) {

            String totalAmount = calculateTotalAmountInADay(transaction);

            if (this.isFirst) {
                holder.showHeader(formattedDate, formattedDay, totalAmount);
            }
        }

        if (!this.isFirst) {
            holder.removeHeader();
        }
    }

    private String calculateTotalAmountInADay(Transaction transaction) {

        ZonedDateTime zonedDateTime = ZonedDateTime.ofInstant(transaction.getInstant(), transaction.getZoneId());

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE;

        String formattedDate = formatter.format(zonedDateTime);

        double totalValue = 0;

        int count = 0;

        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < transactions.size(); i++) {

            Transaction tempTransaction = transactions.get(i);

            zonedDateTime = ZonedDateTime.ofInstant(tempTransaction.getInstant(), tempTransaction.getZoneId());

//                if (formatter.format(transactions.get(i).getDate()).equals(formattedDate)) {
            if (formatter.format(zonedDateTime).equals(formattedDate)) {

                // only count expenses, don't minus income from expenses
                if (tempTransaction.isExpenseTransaction()) {
                    totalValue += tempTransaction.getValue();
                }

                if (count == 0 && tempTransaction.getTransactionId() == transaction.getTransactionId()) {
                    this.isFirst = true;
                }
                count++;

                if (i + 1 == transactions.size() ||
                        (i + 1 < transactions.size() && !formatter.format(
                                ZonedDateTime.ofInstant(transactions.get(i + 1).getInstant(), transactions.get(i + 1).getZoneId()))
                                .equals(formattedDate))) {
                    break;
                }
            }
        }

        // round up to 2.d.p
        BigDecimal totalValueBd = new BigDecimal(totalValue).setScale(2, RoundingMode.HALF_UP);

        stringBuilder.append(totalValueBd.toBigInteger());

        return stringBuilder.toString();
    }
}
