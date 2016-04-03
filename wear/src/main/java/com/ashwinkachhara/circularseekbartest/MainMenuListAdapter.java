package com.ashwinkachhara.circularseekbartest;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by ashwin on 4/2/16.
 */
public class MainMenuListAdapter extends WearableListView.Adapter {
    private final Context mContext;
    private final LayoutInflater mInflater;

    // Provide a suitable constructor (depends on the kind of dataset)
    public MainMenuListAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private ImageView imageView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            imageView = (ImageView) itemView.findViewById(R.id.circle);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // Inflate our custom layout for list items
        return new ItemViewHolder(mInflater.inflate(R.layout.mainmenulistview_item, null));
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        ImageView img = itemHolder.imageView;
        // replace text contents
        switch (position){
            case 0:
                img.setImageResource(R.drawable.mainmenuplay);
                break;
            case 1:
                img.setImageResource(R.drawable.mainmenulist);
                break;
            case 2:
                img.setImageResource(R.drawable.mainmenufav);
                break;
        }


        // replace list item's metadata
        holder.itemView.setTag(position);
    }

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return 3;
    }
}
