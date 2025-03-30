package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.helpers.DatabaseHelper;
import com.example.myapplication.models.UserTransaction;
import com.example.myapplication.views.fragments.Handshake;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class UserTransactionsAdapter extends RecyclerView.Adapter<UserTransactionsAdapter.ViewHolder> {

    private List<UserTransaction> userTransactions;
    private boolean isCompleted;
    private DatabaseHelper databaseHelper;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private Handshake handshakeFragment;

    public UserTransactionsAdapter(List<UserTransaction> userTransactions, boolean isCompleted, Handshake handshakeFragment) {
        this.userTransactions = userTransactions;
        this.isCompleted = isCompleted;
        this.databaseHelper = new DatabaseHelper();
        this.mAuth = FirebaseAuth.getInstance();
        this.user = mAuth.getCurrentUser();
        this.handshakeFragment = handshakeFragment;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserTransaction transaction = userTransactions.get(position);
        if(user != null && user.getDisplayName() != null && user.getDisplayName().equals(transaction.getSender())) {
            holder.recipient.setText(transaction.getReceiver());
        } else {
            holder.recipient.setText(transaction.getSender());
        }
        holder.amount.setText(String.valueOf(transaction.getAmount()));
        holder.type.setText(transaction.getType());
        holder.date.setText(transaction.getDate());
        holder.account.setText(transaction.getAccount());
        holder.status.setText(transaction.getStatus());
        holder.note.setText(transaction.getNote());

        if (transaction.getStatus().equals("Pending")) {
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
            holder.settleButton.setVisibility(View.VISIBLE);
        } else if (transaction.getStatus().equals("Requested")) {
            holder.acceptButton.setVisibility(View.VISIBLE);
            holder.rejectButton.setVisibility(View.VISIBLE);
            holder.settleButton.setVisibility(View.GONE);
        } else if (transaction.getStatus().equals("SettlementRequested")) {
            if(user.equals(transaction.getSender())) {
                holder.acceptButton.setVisibility(View.GONE);
                holder.rejectButton.setVisibility(View.GONE);
                holder.settleButton.setVisibility(View.GONE);
                holder.deleteButton.setVisibility(View.VISIBLE);
            } else {
                holder.acceptButton.setVisibility(View.VISIBLE);
                holder.rejectButton.setVisibility(View.VISIBLE);
                holder.deleteButton.setVisibility(View.GONE);
                holder.settleButton.setVisibility(View.GONE);
            }
        } else if(transaction.getStatus().equals("Sent")) {
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
            holder.settleButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.VISIBLE);
        } else {
            holder.acceptButton.setVisibility(View.GONE);
            holder.rejectButton.setVisibility(View.GONE);
            holder.settleButton.setVisibility(View.GONE);
            holder.deleteButton.setVisibility(View.GONE);
        }

        holder.acceptButton.setOnClickListener(v -> {
            if (transaction.getStatus().equals("Requested")) {
                transaction.setStatus("Pending");
                updateUserAccountBalances(transaction);
                databaseHelper.updateTransactionStatus(transaction.getId(), transaction.getSender(), "Pending");
                databaseHelper.updateTransactionStatus(transaction.getId(), transaction.getReceiver(), "Pending");
            }
            notifyItemChanged(position);
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (transaction.getStatus().equals("SettlementRequested")) {
                transaction.setStatus("Pending");
                databaseHelper.updateTransactionStatus(transaction.getId(), transaction.getSender(), "Pending");
                databaseHelper.updateTransactionStatus(transaction.getId(), transaction.getReceiver(), "Pending");
            } else {
                String transactionId = transaction.getId();
                String sender = transaction.getSender();
                String recipient = transaction.getReceiver();
                databaseHelper.removeHandshakeTransaction(transactionId, sender);
                databaseHelper.removeHandshakeTransaction(transactionId, recipient);
                userTransactions.remove(position);
                notifyItemRemoved(position);
            }
        });

        holder.settleButton.setOnClickListener(v -> {
            handshakeFragment.showSettlementDialog(transaction);
        });

        holder.deleteButton.setOnClickListener(v -> {
            String transactionId = transaction.getId();
            String sender = transaction.getSender();
            String recipient = transaction.getReceiver();

            // Remove the transaction from the sender's and recipient's handshake transactions
            databaseHelper.removeHandshakeTransaction(transactionId, sender);
            databaseHelper.removeHandshakeTransaction(transactionId, recipient);

            userTransactions.remove(position);
            notifyItemRemoved(position);
            // Send update notification
        });
    }

    private void updateUserAccountBalances(UserTransaction transaction) {
        String userId = mAuth.getCurrentUser().getDisplayName();

        if (transaction.getType().equals("outgoing")) {
            databaseHelper.updateUserPayableAmount(userId, transaction.getAmount());
        } else if (transaction.getType().equals("incoming")) {
            databaseHelper.updateUserReceivableAmount(userId, transaction.getAmount());
        }
    }

    @Override
    public int getItemCount() {
        return userTransactions.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView recipient, amount, type, date, account, status, note;
        Button acceptButton, rejectButton, settleButton, deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            recipient = itemView.findViewById(R.id.Recipient);
            note = itemView.findViewById(R.id.transactionNote);
            amount = itemView.findViewById(R.id.amount);
            type = itemView.findViewById(R.id.type);
            date = itemView.findViewById(R.id.date);
            account = itemView.findViewById(R.id.account);
            status = itemView.findViewById(R.id.status);
            note = itemView.findViewById(R.id.transactionNote);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            settleButton = itemView.findViewById(R.id.settleButton);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}