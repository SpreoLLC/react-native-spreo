package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mlins.project.Campus;
import com.mlins.project.ProjectConf;
import com.mlins.utils.FacilityConf;
import com.spreo.nav.enums.LocationMode;
import com.spreo.nav.interfaces.IPoi;
import com.spreo.sdk.data.SpreoDataProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class SpreoSearchListAdapter extends ArrayAdapter<IPoi> {
    Context context;
    List<IPoi> pois;
    IPoi firstpoi, lastpoi;
//    SearchFragment parent = null;
    private Filter filter;
    SpreoSearchItem type = null;
    private SpreoSearchClickListener searchClickListener = null;
    private SpreoSearchFilterListener searchFilterListener = null;


    public SpreoSearchListAdapter(Context context, int resource, List<IPoi> objects, SpreoSearchItem type) {
        super(context, resource, objects);
        this.context = context;
        pois = objects;
        this.type = type;
    }

    public void setSearchClickListener(SpreoSearchClickListener listener) {
        searchClickListener = listener;
    }

//    public void setSearchFragment(SearchFragment frag) {
//        parent = frag;
//    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        IPoi currentPoi = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            // if (position == 0 && po+++++++++++++++++*+9+++++++++++is.size() > 1) {
            // convertView = mInflater.inflate(R.layout.spreo_search_list_item, null);
            // try {
            // convertView .setBackgroundResource(R.drawable.layout_bg_selector_top);
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            // } else if (position == pois.size() - 1 && pois.size() > 1) {
            // convertView = mInflater.inflate(R.layout.spreo_search_list_item, null);
            // try {
            // convertView.setBackgroundResource(R.drawable.layout_bg_selector_bottom);
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            // } else if (pois.size() == 1){
            // convertView = mInflater.inflate(R.layout.spreo_search_list_item, null);
            // try {
            // convertView.setBackgroundResource(R.drawable.layout_bg_selector_single);
            // } catch (Exception e) {
            // e.printStackTrace();
            // }
            // } else
            if (pois.size() == 1) {
                convertView = mInflater.inflate(R.layout.spreo_search_list_item, null);
                try {
                    convertView.setBackgroundResource(R.drawable.layout_bg_selector_single);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else
                convertView = mInflater.inflate(R.layout.spreo_search_list_item, null);
            holder = new ViewHolder();
            holder.poiIcon = (ImageView) convertView.findViewById(R.id.poiIcon);
            holder.poiText = (TextView) convertView.findViewById(R.id.poiText);
            holder.poiFacility = (TextView) convertView.findViewById(R.id.poiFacility);
            holder.goBtn = (Button) convertView.findViewById(R.id.goBtn);
            holder.mapIcon = (ImageView) convertView.findViewById(R.id.mapIcon);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        Bitmap iconbm = null; // ResourceDownloader.getInstance().getLocalBitmap(rowItem.getPoiuri());
        if (type == SpreoSearchItem.POI) {
            iconbm = BitmapFactory.decodeResource(context.getResources(), R.drawable.list_item_poi);
        } else if (type == SpreoSearchItem.HISTORY) {
            iconbm = BitmapFactory.decodeResource(context.getResources(), R.drawable.search_history);
        } else if (type == SpreoSearchItem.FAVORITE) {
            iconbm = BitmapFactory.decodeResource(context.getResources(), R.drawable.list_item_favorite);
        }
        holder.poiIcon.setImageBitmap(iconbm);
        holder.poiText.setText(currentPoi.getpoiDescription());

        setBaseStateForPoiFacilityView(holder);
        setFloorAndFacilityIdInViewIfPossible(currentPoi, holder);

        holder.goBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchClickListener != null) {
                    try {
                        searchClickListener.onGoClickListener(getItem(position));
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        });

        holder.mapIcon.setImageResource(R.drawable.store_cell_indicator);

        return convertView;
    }

    @Override
    public IPoi getItem(int position) {
        return pois.get(position);
    }

    @Override
    public Filter getFilter() {
        if (filter == null)
            filter = new AppFilter<IPoi>(pois);
        return filter;
    }

    private void setBaseStateForPoiFacilityView(@NonNull ViewHolder holder){

        holder.poiFacility.setText("");
        holder.poiFacility.setVisibility(View.VISIBLE);
    }

    private void setFloorAndFacilityIdInViewIfPossible(IPoi poi, ViewHolder holder) {

        String floorTitle = SpreoDataProvider.getFloorTitle(poi.getCampusID(), poi.getFacilityID(), (int) poi.getZ());

        String facilityAndFloor = getFacilityName(poi.getFacilityID()) + ", Floor " + floorTitle;
        String poiType = poi.getPoiNavigationType();
        if (poi.getLocation().getLocationType() == LocationMode.INDOOR_MODE)
            holder.poiFacility.setText(facilityAndFloor);
        else
            holder.poiFacility.setVisibility(View.GONE);

    }

    private String getFacilityName(String id) {
        String result = id;
        try {
            Campus campus = ProjectConf.getInstance().getSelectedCampus();
            if (campus != null) {
                result = campus.getFacilityConf(id).getName();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }


    @NonNull
    private String getFormatterFloor(@NonNull String floor) {

        StringBuilder builder = new StringBuilder();

        if (floor.length() >= 2) {
            builder.append(" - ")
                    .append(floor.substring(1, floor.length()));
        } else{
            builder.append(" - ")
                    .append(floor);
        }

        return  builder.toString();
    }

    public void setSearchFilterListener(SpreoSearchFilterListener searchFilterListener) {
        this.searchFilterListener = searchFilterListener;
    }

    private class ViewHolder {
        ImageView poiIcon;
        TextView poiText;
        TextView poiFacility;
        ImageView mapIcon;
        Button goBtn;
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
                    IPoi poi = (IPoi) object;
                    if (poi.getKeyWordsAsString().toLowerCase().contains(filterSeq) || poi.getpoiDescription().toLowerCase().contains(filterSeq)) {
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
        protected void publishResults(CharSequence constraint, FilterResults results) {
            // NOTE: this function is *always* called from the UI thread.
            ArrayList<T> filtered = (ArrayList<T>) results.values;
            notifyDataSetChanged();
            clear();
            for (int i = 0, l = filtered.size(); i < l; i++)
                pois.add((IPoi) filtered.get(i));
            notifyDataSetInvalidated();
            if (searchFilterListener != null) {
                try {
                    searchFilterListener.onFilterResultPublished();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    private String getFloorText(IPoi citem) {
        String result = "";
        if (!citem.getPoiNavigationType().equals("external")) {
            Campus ccampus = ProjectConf.getInstance().getSelectedCampus();
            FacilityConf cfacility = null;
            if (ccampus != null) {
                Map<String, FacilityConf> fmap = ccampus.getFacilitiesConfMap();
                int z = (int) citem.getZ();
                String fid = citem.getFacilityID();
                if (fid != null && !fid.equals("unknown")) {
                    cfacility = fmap.get(fid);
                    if (cfacility != null) {
                        String floortitle = cfacility.getFloorTitle(z);
                        if (floortitle != null) {
                            result = floortitle;
                        }
                    }
                }
            }
        }
        return result;
    }
}
