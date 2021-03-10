package com.srinivasan.photovault;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.List;

public class CyclerAdapter extends RecyclerView.Adapter<CyclerAdapter.ViewHolder> {

    private OnItemClickListener mListener;

    private Context context;
    private List<PhotoInfoModel> photoInfoModelList;

    public CyclerAdapter(Context context,List<PhotoInfoModel> photoInfoModelList){
        this.context = context;
        this.photoInfoModelList = photoInfoModelList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(context).inflate(R.layout.card_item,parent,false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        PhotoInfoModel photoInfoModel = photoInfoModelList.get(position);
        holder.cyclerTxt.setText(photoInfoModel.getPhotoName());
        if (photoInfoModel.getPhotoUrl() != null && !photoInfoModel.getPhotoUrl().isEmpty()){
            /*Picasso.get().load(photoInfoModel.getPhotoUrl())
                    .placeholder(R.drawable.ic_android)
                    //.error(R.drawable.ic_download_cloud)
                    .fit()
                    .centerCrop()
                    .into(holder.cyclerImg);*/
            Glide.with(context)
                    .load(photoInfoModel.getPhotoUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_android)
                    .into(holder.cyclerImg);
        }else {
            holder.cyclerImg.setImageDrawable(ContextCompat.getDrawable(this.context,R.drawable.ic_upload_cloud));
        }

    }

    @Override
    public int getItemCount() {
        return photoInfoModelList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,OnItemClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {

        public TextView cyclerTxt;
        public ImageView cyclerImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            cyclerTxt = itemView.findViewById(R.id.recycle_txt);
            cyclerImg = itemView.findViewById(R.id.recycle_img);

            itemView.setOnClickListener(this);
            itemView.setOnCreateContextMenuListener(this);
        }

        @Override
        public void onClick(View v) {
            if (mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    mListener.onItemClick(position);
                }
            }
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            menu.setHeaderTitle("Options");
            menu.setHeaderIcon(R.drawable.ic_info);
            MenuItem open = menu.add(Menu.NONE,1,1,"Open");
            MenuItem delete = menu.add(Menu.NONE,2,2,"Delete");

            open.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            if (mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    switch (item.getItemId()){
                        case 1:
                            mListener.onOpenClick(position);
                            return true;
                        case 2:
                            mListener.onDeleteClick(position);
                            return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void onItemClick(int position) {

        }

        @Override
        public void onDeleteClick(int position) {

        }

        @Override
        public void onOpenClick(int position) {

        }
    }

    public interface OnItemClickListener{
        void onItemClick(int position);
        void onDeleteClick(int position);
        void onOpenClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mListener = listener;
    }

}
