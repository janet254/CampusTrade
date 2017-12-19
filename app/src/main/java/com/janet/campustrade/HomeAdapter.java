package com.janet.campustrade;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

/**
 * Created by Janet on 18/11/2017.
 */

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.MyViewHolder>{
    private List<Item> itemList;
    private Context mContext;

    public class  MyViewHolder extends RecyclerView.ViewHolder{
        public TextView name, description;
        public TextView price;
        public ImageView image;

        public MyViewHolder(View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.itemImage);
            name = (TextView) itemView.findViewById(R.id.itemName);
            description =(TextView) itemView.findViewById(R.id.itemDescription);
            price = (TextView) itemView.findViewById(R.id.price);
        }
    }

    //constructor for home adapter
    public HomeAdapter (Context context, List<Item> itemList){
        this.itemList = itemList;
        mContext = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.home_layout, parent, false);
        return  new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Item item = itemList.get(position);
        Glide.with(mContext).load(item.getImage()).into(holder.image);
        holder.name.setText(item.getName());
        holder.description.setText(item.getDescription());
        holder.price.setText("Ksh." +item.getPrice());
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

}
