package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.myapplication.R;
import com.example.myapplication.models.Transaction;
import java.util.List;

public class TransactionsAdapter extends RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private OnTransactionLongClickListener longClickListener;

    public TransactionsAdapter(List<Transaction> transactions, OnTransactionLongClickListener longClickListener) {
        this.transactions = transactions;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);
        holder.label.setText(transaction.getCategory());
        holder.amount.setText(String.valueOf(transaction.getAmount()));
        holder.date.setText(transaction.getDate());
        holder.account.setText(transaction.getAccount());

        if ("Expense".equals(transaction.getType())) {
            holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.red));
        } else if ("Income".equals(transaction.getType())) {
            holder.amount.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.green));
        }
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView label, amount, date, account;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.transactionCategory);
            amount = itemView.findViewById(R.id.transactionAmount);
            date = itemView.findViewById(R.id.transactionDate);
            account = itemView.findViewById(R.id.accountLbl);

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onTransactionLongClick(transactions.get(getAdapterPosition()));
                    return true;
                }
                return false;
            });
        }
    }

    public interface OnTransactionLongClickListener {
        void onTransactionLongClick(Transaction transaction);
    }
}