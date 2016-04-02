package com.ashwinkachhara.circularseekbartest;

import android.content.Context;
import android.support.wearable.view.WearableListView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SectionIndexer;
import android.widget.TextView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by ashwin on 2/21/16.
 */
public final class Adapter extends WearableListView.Adapter implements SectionIndexer{
    private ArrayList<String> mDataset;
    private final Context mContext;
    private final LayoutInflater mInflater;

    private HashMap<String,Integer> alphaIndexer;
    private String[] sections;

    // Provide a suitable constructor (depends on the kind of dataset)
    public Adapter(Context context, ArrayList<String> dataset) {
        alphaIndexer = new HashMap<String, Integer>();
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mDataset = dataset;

        for (int i = 0; i < mDataset.size(); i++)
        {
            String s = mDataset.get(i).substring(0, 1).toUpperCase();
            if (!alphaIndexer.containsKey(s))
                alphaIndexer.put(s, i);
        }
        Set<String> sectionLetters = alphaIndexer.keySet();
        ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
        Collections.sort(sectionList);
        sections = new String[sectionList.size()];
        for (int i = 0; i < sectionList.size(); i++)
            sections[i] = sectionList.get(i);
    }

    @Override
    public Object[] getSections() {
        return sections;
    }

    @Override
    public int getPositionForSection(int sectionIndex) {
        return alphaIndexer.get(sections[sectionIndex]);
    }

    @Override
    public int getSectionForPosition(int position) {
        return 0;
    }

    // Provide a reference to the type of views you're using
    public static class ItemViewHolder extends WearableListView.ViewHolder {
        private TextView textView;
        public ItemViewHolder(View itemView) {
            super(itemView);
            // find the text view within the custom item's layout
            textView = (TextView) itemView.findViewById(R.id.name);
        }
    }

    // Create new views for list items
    // (invoked by the WearableListView's layout manager)
    @Override
    public WearableListView.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        // Inflate our custom layout for list items
        return new ItemViewHolder(mInflater.inflate(R.layout.wearablelistview_item, null));
    }

    // Replace the contents of a list item
    // Instead of creating new views, the list tries to recycle existing ones
    // (invoked by the WearableListView's layout manager)
    @Override
    public void onBindViewHolder(WearableListView.ViewHolder holder,
                                 int position) {
        // retrieve the text view
        ItemViewHolder itemHolder = (ItemViewHolder) holder;
        TextView view = itemHolder.textView;
        // replace text contents
        view.setText(mDataset.get(position));
        // replace list item's metadata
        holder.itemView.setTag(position);
    }

    // Return the size of your dataset
    // (invoked by the WearableListView's layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
