package com.example.luxed.karatefrontend.Adapters;

import com.example.luxed.karatefrontend.Entities.Account;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.luxed.karatefrontend.R;

import java.security.AccessControlContext;
import java.util.ArrayList;
import java.util.List;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.MyViewHolder> {
    private ArrayList<Account> accounts;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView info;

        public MyViewHolder(View view) {
            super(view);

            image = view.findViewById(R.id.imgAccountImage);
            info = view.findViewById(R.id.tvAccountInfos);
        }
    }

    public AccountAdapter(ArrayList<Account> accounts) {
        this.accounts = accounts;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.account_list_row, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Log.i("RecyclerView", "onBindViewHolder: " + position);
        Account acc = accounts.get(position);

        if (acc != null) {
            String imgData = acc.getAvatar().substring(acc.getAvatar().indexOf(',') + 1);

            byte[] decodedString = Base64.decode(imgData.getBytes(), Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            holder.image.setImageBitmap(decodedByte);
            holder.info.setText(acc.getFullName() + ", " + acc.getGroupe() + ", " + acc.getGroupe());
        }
    }

    @Override
    public int getItemCount() {
        return accounts.size();
    }
}
