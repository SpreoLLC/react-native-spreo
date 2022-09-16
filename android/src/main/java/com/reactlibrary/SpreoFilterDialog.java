package com.reactlibrary;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.spreo.sdk.poi.PoiCategory;

import java.util.ArrayList;
import java.util.List;


public class SpreoFilterDialog {
    private Dialog mDialog;
    private LinearLayout closeButton;
    private Context ctx = null;
    private ListView filterListView;
    private static final String ALL = "All";
    private MapFilterListener mapFilterListener = null;

    public SpreoFilterDialog() {

    }

    public void showMapFilter(Context context, MapFilterListener mapFilterListener) {
        ctx = context;
        this.mapFilterListener = mapFilterListener;
        mDialog = new Dialog(ctx);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.spreo_filter_dialog);
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        closeButton = (LinearLayout) mDialog.findViewById(R.id.closeButton);
        filterListView = (ListView) mDialog.findViewById(R.id.filterListView);
        closeButton.setOnClickListener(closeButtonListener);

        createMapFilter();

        mDialog.show();
    }

    public void createMapFilter() {
        List<PoiCategory> categories = SpreoSearchDataHolder.getInstance().getFilterCategories();
        List<SpreoMapFilterItem> mapFiltersDrawerItemsList = new ArrayList<SpreoMapFilterItem>();
        List<PoiCategory> visiblecategories = SpreoSearchDataHolder.getInstance().getVisibleCategories();
        mapFiltersDrawerItemsList.add(new SpreoMapFilterItem(ALL, categories.size() == visiblecategories.size()));
        for (PoiCategory poiCategory : categories) {
            if (poiCategory != null) {
                String type = poiCategory.getPoitype();
                Boolean isInMapFilter = poiCategory.getShowInMapFilter();
                if (!type.isEmpty() && isInMapFilter) {
                    mapFiltersDrawerItemsList.add(new SpreoMapFilterItem(type, visiblecategories.contains(poiCategory)));
                }
            }
        }
        SpreoMapFilterItem[] filterDrawerItem = mapFiltersDrawerItemsList.toArray(new SpreoMapFilterItem[mapFiltersDrawerItemsList.size()]);
        SpreoMapFilterAdapter mapfilterdapter = new SpreoMapFilterAdapter(ctx, R.layout.spreo_filter_item, filterDrawerItem);
        filterListView.setAdapter(mapfilterdapter);
    }

    private View.OnClickListener closeButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }

            if (mapFilterListener != null) {
                try {
                    mapFilterListener.mapFilterApplied();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    };
}
