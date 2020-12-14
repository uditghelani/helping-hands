package com.helpinghands.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.helpinghands.R;

import java.util.ArrayList;

public class RequestsAdapter extends RecyclerView.Adapter<RequestsAdapter.RequestViewHolder> {

    ArrayList<DataSnapshot> mRequestList = new ArrayList<>();

    public RequestsAdapter(ArrayList<DataSnapshot> requestList) {
        mRequestList = requestList;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.layout_single_request, parent, false);

        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {

        try {
            String books = mRequestList.get(position).child("books").getValue().toString();
            String clothes = mRequestList.get(position).child("clothes").getValue().toString();


            if (!books.equals("NaN") && !clothes.equals("NaN")) {
                holder.mBooksTextView.setText("Books " + books);

                holder.mClothesTextView.setText("Clothes " + clothes);
            } else if (!books.equals("NaN")) {
                holder.mBooksTextView.setText("Books " + books);
                holder.mClothesTextView.setVisibility(View.GONE);

            } else if (!clothes.equals("NaN")) {

                holder.mClothesTextView.setText("Clothes " + clothes);
                holder.mBooksTextView.setVisibility(View.GONE);
            }

            holder.mEmailTextView.setText("By: " + mRequestList.get(position).child("email").getValue().toString());

        }catch (Exception e){
            Log.d("Adapter Log", "onBindViewHolder: "+ e);
        }
    }

    @Override
    public int getItemCount() {
        return mRequestList.size();
    }


    public class RequestViewHolder extends RecyclerView.ViewHolder {

        TextView mBooksTextView, mClothesTextView,mEmailTextView;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            mBooksTextView = itemView.findViewById(R.id.books_textview);
            mClothesTextView = itemView.findViewById(R.id.clothes_textview);
            mEmailTextView = itemView.findViewById(R.id.email_textview);
        }
    }
}
