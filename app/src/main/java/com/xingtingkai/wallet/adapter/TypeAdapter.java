package com.xingtingkai.wallet.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.xingtingkai.wallet.R;
import com.xingtingkai.wallet.db.entity.Type;

public class TypeAdapter extends ListAdapter<Type, TypeAdapter.TypeViewHolder> {

    class TypeViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewType;

        private TypeViewHolder(View itemView) {
            super(itemView);
            textViewType = itemView.findViewById(R.id.textView_type);

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
    private TypeAdapter.OnItemClickListener listener;

    public TypeAdapter(Context context) {
        super(DIFF_CALLBACK);
        mInflater = LayoutInflater.from(context);
    }

    private static final DiffUtil.ItemCallback<Type> DIFF_CALLBACK = new DiffUtil.ItemCallback<Type>() {
        @Override
        public boolean areItemsTheSame(Type oldItem, Type newItem) {
            return oldItem.getTypeId() == newItem.getTypeId();
        }

        @Override
        public boolean areContentsTheSame(Type oldItem, Type newItem) {
            return oldItem.getName().equals(newItem.getName());
        }
    };

    @Override
    public TypeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = mInflater.inflate(R.layout.recyclerview_type_item, parent, false);
        TypeViewHolder holder = new TypeViewHolder(itemView);
        return holder;
    }

    @Override
    public void onBindViewHolder(TypeViewHolder holder, int position) {
        Type type = getItem(position);
        holder.textViewType.setText(type.getName());
    }

    public interface OnItemClickListener {
        void onItemClick(Type type);
    }

    public void setOnItemClickListener(TypeAdapter.OnItemClickListener listener) {
        this.listener = listener;
    }
}
