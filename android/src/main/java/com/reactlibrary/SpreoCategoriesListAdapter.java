package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spreo.sdk.poi.PoiCategory;

import java.util.ArrayList;
import java.util.List;


public class SpreoCategoriesListAdapter extends ArrayAdapter<PoiCategory> {
    Context context;
    List<PoiCategory> categories;
    private Filter filter;

    public SpreoCategoriesListAdapter(Context context, int resource,
                                      List<PoiCategory> objects) {
        super(context, resource, objects);
        this.context = context;
        categories = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        PoiCategory rowItem = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if (position == 0) {
                convertView = mInflater.inflate(R.layout.spreo_category_list_item, null);
                try {
//                    convertView.setBackgroundResource(R.drawable.layout_bg_selector_top);
                } catch (Exception e) {
                }
            } else if (position == categories.size() - 1) {
                convertView = mInflater.inflate(R.layout.spreo_category_list_item, null);
                try {
//                    convertView.setBackgroundResource(R.drawable.layout_bg_selector_bottom);
                } catch (Exception e) {
                }
            } else
                convertView = mInflater.inflate(R.layout.spreo_category_list_item, null);
            holder = new ViewHolder();
            holder.categoryIcon = (ImageView) convertView.findViewById(R.id.poiIcon);
            holder.categoryText = (TextView) convertView.findViewById(R.id.poiText);
            holder.arrowIcon = (ImageView) convertView.findViewById(R.id.mapIcon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

//        Bitmap iconbm = ResourceDownloader.getInstance().getLocalBitmap(rowItem.getPoiuri());
//        holder.categoryIcon.setImageBitmap(iconbm);
        holder.categoryText.setText(rowItem.getPoitype());
        holder.arrowIcon.setImageResource(R.drawable.store_cell_indicator);

        return convertView;
    }

    @Override
    public PoiCategory getItem(int position) {
        return categories.get(position);
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new AppFilter<PoiCategory>(categories);
        return filter;
    }

    private class ViewHolder {
        ImageView categoryIcon;
        TextView categoryText;
        ImageView arrowIcon;
    }

    @SuppressWarnings("unused")
    private class AppFilter<T> extends Filter {

        private ArrayList<T> sourceObjects;

        public AppFilter(List<T> objects) {
            sourceObjects = new ArrayList<T>();
            synchronized (this) {
                sourceObjects.addAll(objects);
            }
        }

        @Override
        protected FilterResults performFiltering(CharSequence chars) {
            String filterSeq = chars.toString().toLowerCase();
            FilterResults result = new FilterResults();
            if (filterSeq != null && filterSeq.length() > 0) {
                ArrayList<T> filter = new ArrayList<T>();

                for (T object : sourceObjects) {
                    // the filtering itself:
                    PoiCategory categories = (PoiCategory) object;
//					PoiCategory.this.
                    if (categories.getPoitype().toLowerCase().contains(filterSeq) || categories.getPoitype().toLowerCase().contains(filterSeq)) {
                        filter.add(object);
                    }
                }
                result.count = filter.size();
                result.values = filter;
            } else {
                // add all objects
                synchronized (this) {
                    result.values = sourceObjects;
                    result.count = sourceObjects.size();
                }
            }
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected void publishResults(CharSequence constraint,
                                      FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.
            ArrayList<T> filtered = (ArrayList<T>) results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = filtered.size(); i < l; i++)
                categories.add((PoiCategory) filtered.get(i));
            notifyDataSetInvalidated();
        }
    }
}
