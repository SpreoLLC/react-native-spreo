package com.reactlibrary;

import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.mlins.aStar.FloorNavigationPath;
import com.mlins.aStar.NavigationPath;
import com.mlins.dualmap.RouteCalculationHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;




public class SpreoNewFloorAdapter extends RecyclerView.Adapter<SpreoNewFloorAdapter.ViewHolder> {
    List<SpreoNewFloorObject> allItems;
    int selectedFloorId;
    int originFroorId;
    int destinationFloorId;
    boolean isExpanded;
    List<SpreoNewFloorObject> dispayItemList;
    ItemClickListener listener;
    List<Integer> extraNavigationFloors = new ArrayList<>();
    boolean expandAble = true;
    View.OnClickListener holderClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            if (expandAble) {
                if (isExpanded) {
                    int position = (Integer) v.getTag();
                    setSelectedFloorId(getItem(position).floorId);
                    listener.onItemClick(SpreoNewFloorAdapter.this, position);
                }
                setExpanded(!isExpanded);
            } else {
                int position = (Integer) v.getTag();
                setSelectedFloorId(getItem(position).floorId);
                listener.onItemClick(SpreoNewFloorAdapter.this, position);
            }

        }
    };

    SpreoNewFloorAdapter(List<SpreoNewFloorObject> items, int selectedFloorId, int originFloorId, int destinationFloorId, boolean isExpanded, boolean expandAble, ItemClickListener listener) {

        this.allItems = new ArrayList<>(items);
        Collections.sort(allItems, new Comparator<SpreoNewFloorObject>() {
            @Override
            public int compare(SpreoNewFloorObject o1, SpreoNewFloorObject o2) {
                return o2.floorId - o1.floorId;
            }
        });
        this.selectedFloorId = selectedFloorId;
        this.originFroorId = originFloorId;
        this.destinationFloorId = destinationFloorId;
        HashMap<String, NavigationPath> navmap = RouteCalculationHelper.getInstance().getIndoorNavPaths();
        for (NavigationPath facilitypath : navmap.values()) {
            for (FloorNavigationPath floornav : facilitypath.getFullPath()) {
                if (!extraNavigationFloors.contains((int)floornav.getZ())) {
                    extraNavigationFloors.add((int)floornav.getZ());
                }
            }
        }
        this.dispayItemList = new ArrayList<>(allItems);
        setExpanded(isExpanded);
        this.listener = listener;
        this.expandAble = expandAble;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SpreoNewFloorAdapter.ViewHolder(LayoutInflater.from(parent.getContext()), parent, holderClickListener);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        SpreoNewFloorObject item = getItem(position);
        holder.setFloorText(item.floorText);
        holder.setSelected(item.floorId == selectedFloorId);
        holder.setShowOriginDestination(item.floorId == originFroorId, item.floorId == destinationFloorId);
        holder.setPosition(position);
    }

    @Override
    public int getItemCount() {
        return dispayItemList.size();
    }


    public void setSelectedFloorId(int selectedFloorId) {
        this.selectedFloorId = selectedFloorId;
        notifyDataSetChanged();
    }

    public void setOriginFroorId(int originFroorId) {
        this.originFroorId = originFroorId;
        notifyDataSetChanged();
    }

    public void setDestinationFloorId(int destinationFloorId) {
        this.destinationFloorId = destinationFloorId;
        notifyDataSetChanged();
    }

    public void setExpanded(boolean expanded) {
        isExpanded = expanded;
        dispayItemList.clear();
        if (expanded) {
            dispayItemList.addAll(allItems);
        } else {
            for (SpreoNewFloorObject item : allItems) {
                if (item.floorId == originFroorId || item.floorId == destinationFloorId
                        || item.floorId == selectedFloorId || extraNavigationFloors.contains(item.floorId)) {
                    dispayItemList.add(item);
                }
            }
        }
        notifyDataSetChanged();
    }

    public SpreoNewFloorObject getItem(int position) {
        return dispayItemList.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private SpreoCustomTextView floorName;
        private ImageView image;

        public ViewHolder(LayoutInflater inflater, ViewGroup parent, View.OnClickListener listener) {
            super(inflater.inflate(R.layout.spreo_item_floor, parent, false));
            floorName = (SpreoCustomTextView) itemView.findViewById(R.id.fl_item_tv);
            image = (ImageView) itemView.findViewById(R.id.fl_item_image);
            itemView.setOnClickListener(listener);
        }

        public void setSelected(boolean isSelected) {
            floorName.setBackgroundResource(isSelected ? R.drawable.floor_item_selected : R.drawable.floor_item);
            floorName.setTextColor(isSelected ? Color.parseColor("#45494f"):
                    ContextCompat.getColor(itemView.getContext(), android.R.color.white));
        }

        public void setFloorText(CharSequence text) {
            floorName.setText(text);
        }

        void setPosition(int position) {
            itemView.setTag(position);
        }

        public void setShowOriginDestination(boolean showOrigin, boolean showDestination) {
            if (showDestination || showOrigin) {
                image.setImageResource(showDestination ? R.drawable.spreo_end_point : R.drawable.spreo_start_point);
            } else {
                image.setImageBitmap(null);
            }
        }

    }

    interface ItemClickListener {

        void onItemClick(RecyclerView.Adapter adapter, int position);

    }
}
