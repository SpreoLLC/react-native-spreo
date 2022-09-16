package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.spreo.sdk.poi.PoiCategory;
import com.spreo.sdk.poi.PoisUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;




public class SpreoMapFilterAdapter extends ArrayAdapter<SpreoMapFilterItem> {
    Context mContext;
    int layoutResourceId;
    SpreoMapFilterItem data[] = null;
    LayoutInflater inflater;
    boolean checkBoxClick = false;
    private HashMap<String, PoiCategory> categoriesTabs = new HashMap<String, PoiCategory>();
    private List<PoiCategory> categories = SpreoSearchDataHolder.getInstance().getFilterCategories();

    public SpreoMapFilterAdapter(Context mContext, int layoutResourceId, SpreoMapFilterItem[] data) {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
        inflater = ((Activity) mContext).getLayoutInflater();
        for (PoiCategory poiCategory : categories) {
            if (poiCategory != null) {
                String type = poiCategory.getPoitype();
                if (!type.isEmpty()) {
                    categoriesTabs.put(type, poiCategory);
                }
            }
        }
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final SpreoMapFilterItem drawerItem = data[position];

        boolean not = false;

        View view = convertView;
        if (view == null) {
            view = inflater.inflate(layoutResourceId, parent, false);
        }

        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBoxItem);
        TextView textViewName = (TextView) view.findViewById(R.id.textViewItem);

        checkBox.setTag(position);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d("pos", "" + position);
                Log.d("b", "" + b);
                data[position].isChecked = b;
                if (b) {
                    if (position == 0) {
                        for (int i = 0; i < data.length; i++) {
                            if (!data[i].isChecked) {
                                data[i].isChecked = true;
                            }
                        }
                        if (checkBoxClick) {
                            notifyDataSetChanged();
                            checkBoxClick = false;
                            showAllCategories();
                        }

                    } else {
                        if (checkBoxClick) {
                            boolean checker = true;
                            for (int i = 1; i < data.length; i++) {
                                if (!data[i].isChecked) {
                                    checker = false;
                                    break;
                                }
                            }
                            if (checker) {
                                data[0].isChecked = true;
                            }
                            notifyDataSetChanged();
                            checkBoxClick = false;
                            showChosenCatigories();
                        }

                    }
                } else {
                    if (position == 0 && !data[0].isChecked) {
                        boolean check = false;
                        for (int i = 1; i < data.length; i++) {
                            if (!data[i].isChecked) {
                                check = true;
                                break;
                            }
                        }
                        if (!check && checkBoxClick) {
                            for (int i = 1; i < data.length; i++) {
                                if (data[i].isChecked) {
                                    data[i].isChecked = false;
                                }
                            }
                            if (checkBoxClick) {
                                notifyDataSetChanged();
                                checkBoxClick = false;
                                showChosenCatigories();
                            }

                        }
                    } else {
                        if (!data[0].isChecked) {
                            if (checkBoxClick) {
                                notifyDataSetChanged();
                                checkBoxClick = false;
                                showChosenCatigories();
                            }
                        } else {
                            if (data[0].isChecked) {
                                data[0].isChecked = false;
                            }
                            if (checkBoxClick) {
                                notifyDataSetChanged();
                                checkBoxClick = false;
                                showChosenCatigories();
                            }

                        }
                    }

                }
            }
        });
        textViewName.setText(drawerItem.name);
        checkBox.setChecked(drawerItem.isChecked);

        checkBox.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                checkBoxClick = true;
                return false;
            }
        });

        return view;
    }

    public void showChosenCatigories() {
        final List<PoiCategory> visibleategories = new ArrayList<PoiCategory>();
        for (int i = 1; i < data.length; i++) {
            if (data[i].isChecked) {
                if (categoriesTabs.containsKey(data[i].name)) {
                    PoiCategory category = categoriesTabs.get(data[i].name);
                    if (category != null) {
                        visibleategories.add(category);
                    }
                }

            }
        }
        if (visibleategories != null) {
            PoisUtils.setPoiCategoriesVisible(visibleategories);
            SpreoSearchDataHolder.getInstance().setVisibleCategories(visibleategories);
        }
    }

    public void showAllCategories() {
        PoisUtils.setAllPoisCategoriesVisible();
        SpreoSearchDataHolder.getInstance().setVisibleCategories(SpreoSearchDataHolder.getInstance().getFilterCategories());
    }
}
