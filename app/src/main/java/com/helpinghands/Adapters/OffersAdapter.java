package com.helpinghands.Adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.helpinghands.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class OffersAdapter extends RecyclerView.Adapter<OffersAdapter.OffersViewHolder> {

    ArrayList<DataSnapshot> mOfferList = new ArrayList<>();

    public OffersAdapter(ArrayList<DataSnapshot> offerList) {
        mOfferList = offerList;
    }

    @NonNull
    @Override
    public OffersViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View view = inflater.inflate(R.layout.layout_single_offer, parent, false);

        return new OffersViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OffersViewHolder holder, int position) {

        try {
            String books = mOfferList.get(position).child("books").getValue().toString();
            String clothes = mOfferList.get(position).child("clothes").getValue().toString();
            String imageurl = mOfferList.get(position).child("imageURL").getValue().toString();

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
            holder.mEmailTextView.setText("By: " + mOfferList.get(position).child("email").getValue().toString());

            if (imageurl.equals("NaN"))
                holder.mPreviewImageView.setVisibility(View.GONE);
            else
                Picasso.get().load(imageurl).fit().centerCrop().into(holder.mPreviewImageView);

        } catch (Exception e) {
            Log.d("Adapter Log", "onBindViewHolder: " + e);
        }
    }

    @Override
    public int getItemCount() {
        return mOfferList.size();
    }


    public class OffersViewHolder extends RecyclerView.ViewHolder {

        TextView mBooksTextView, mClothesTextView, mEmailTextView;
        ImageView mPreviewImageView;

        public OffersViewHolder(@NonNull View itemView) {
            super(itemView);
            mBooksTextView = itemView.findViewById(R.id.books_textview);
            mClothesTextView = itemView.findViewById(R.id.clothes_textview);
            mEmailTextView = itemView.findViewById(R.id.email_textview);
            mPreviewImageView = itemView.findViewById(R.id.preview_imageView);
        }
    }
}
