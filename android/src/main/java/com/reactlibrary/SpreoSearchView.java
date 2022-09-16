package com.reactlibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.os.Build;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.mlins.utils.SortingPoiUtil;
import com.spreo.nav.interfaces.IPoi;
import com.spreo.sdk.location.SpreoLocationProvider;
import com.spreo.sdk.poi.PoiCategory;
import com.spreo.sdk.poi.PoisUtils;

import java.util.ArrayList;
import java.util.List;



public class SpreoSearchView extends LinearLayout implements SpreoSearchClickListener, SpreoSearchFilterListener {

    private final ListView slist, clist;
    private final EditText filterText;
    private Context ctx = null;
    private List<IPoi> poilist = new ArrayList<IPoi>();
    private SpreoSearchListAdapter sadapter;
    private boolean sundexChecker = false;
    private LinearLayout searchLayout, menuItems, categoriesItem, favoritesItem, parkingItem, selectCampusItem, catgoryLayout;
    private ImageView menuIcon;
    private SpreoSearchItem seacrhType = null;
    private SpreoSearchClickListener searchClickListener = null;
    private SpreoSearchFilterListener searchFilterListener = this;
    private SpreoCategoriesListAdapter cadapter;
    private TextView favoritesMsg;
    private LinearLayout menu_icon_ll;


    public SpreoSearchView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        ctx = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spreo_search_view, this, true);

        searchLayout = (LinearLayout) findViewById(R.id.searchLayout);
        catgoryLayout = (LinearLayout) findViewById(R.id.catgoryLayout);
        slist = (ListView) findViewById(R.id.searchpoiListView01);
        slist.setOnItemClickListener(searchItemClickListener);
        clist = (ListView) findViewById(R.id.ctegoriesListView);
        clist.setOnItemClickListener(categoryItemClickListener);
        filterText = (EditText) findViewById(R.id.search_edit);
        menuIcon = (ImageView) findViewById(R.id.menu_icon);
//        menuIcon.setOnClickListener(menuIconListener);
        filterText.addTextChangedListener(filterTextWatcher);
        filterText.setOnClickListener(filreTextClickListener);
        menuItems = (LinearLayout) findViewById(R.id.menuItems);
        categoriesItem = (LinearLayout) findViewById(R.id.categoriesItem);
        categoriesItem.setOnClickListener(categoriesItemListener);
        favoritesItem = (LinearLayout) findViewById(R.id.favoritesItem);
        favoritesItem.setOnClickListener(favoritesItemListener);
        parkingItem = (LinearLayout) findViewById(R.id.parkingItem);
        parkingItem.setOnClickListener(parkingItemListener);
        selectCampusItem = (LinearLayout) findViewById(R.id.selectCampusItem);
        selectCampusItem.setOnClickListener(selectCampusItemListener);
        favoritesMsg = findViewById(R.id.favorites_msg);
        menu_icon_ll = findViewById(R.id.menu_icon_ll);
        menu_icon_ll.setOnClickListener(menuIconListener);

    }

    private OnClickListener filreTextClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (seacrhType == null) {
                openMenu();
            }
        }
    };

    private OnClickListener categoriesItemListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            openCategoriesSerachResult();
            setMenuBackButton(true);
        }
    };

    private OnClickListener favoritesItemListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            seacrhType = SpreoSearchItem.FAVORITE;
            openFavoritesSerachResult();
            menuItems.setVisibility(View.GONE);
            setMenuBackButton(true);
        }
    };

    private OnClickListener parkingItemListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            hideMenu();
            if (searchClickListener != null) {
                try {
                    searchClickListener.onParkingClickListener();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    };

    private OnClickListener selectCampusItemListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            hideMenu();
            if (searchClickListener != null) {
                try {
                    searchClickListener.onCampusSelectionClick();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    };

    public void setSearchClickListener(SpreoSearchClickListener listener) {
        searchClickListener = listener;
    }

    private AdapterView.OnItemClickListener categoryItemClickListener = new AdapterView.OnItemClickListener() {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            PoiCategory category = (PoiCategory) parent.getItemAtPosition(position);
            if (category != null) {
                List<IPoi> pois = SpreoSearchDataHolder.getInstance().getCategoryPoiList(category.getPoitype());
                if (pois != null) {
                    openPoiSerachResult(pois);
                }
            }
        }
    };

    private AdapterView.OnItemClickListener searchItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            IPoi poi = (IPoi) parent.getItemAtPosition(position);
            if (poi != null) {
                if (searchClickListener != null) {
                    try {
//                        setMenuBackButton(false);
//                        clearSearch();
                        searchClickListener.onItemClickListener(poi);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    };

    private OnClickListener menuIconListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            setMenuBackButton(false);
            hideFavoritesMsg();
            if (seacrhType == SpreoSearchItem.HISTORY) {
                menuItems.setVisibility(View.GONE);
                hideMenu();
            } else {
                openMenu();
            }

        }
    };

    private void openMenu() {
        clearSearch();
        seacrhType = SpreoSearchItem.HISTORY;
        openHistorySerachResult();
        menuItems.setVisibility(View.VISIBLE);
        if (searchClickListener != null) {
            try {
                searchClickListener.menuOpened();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private TextWatcher filterTextWatcher = new TextWatcher() {


        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {

            hideFavoritesMsg();

            if (count !=  0 && seacrhType != SpreoSearchItem.POI) {
                openPoiSerachResult();
            }
            if (sundexChecker) {
                sundexChecker = false;
                List<IPoi> sortedlist = PoisUtils.getPoiListSortedAlphabetical(poilist);
                sadapter = new SpreoSearchListAdapter(ctx, R.layout.spreo_search_list_item, sortedlist, SpreoSearchItem.POI);
                slist.setAdapter(sadapter);
            }
            listFilter(sundexChecker, s);

        }

    };

    public void listFilter(final boolean isSoundex, final CharSequence s) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (isSoundex) {
//                    if (sadapter != null && !filterText.getText().toString().isEmpty()) {
//                        String text = filterText.getText().toString();
//                        Soundex soundex = new Soundex();
//                        float currentPercent = (float) 0.85;
//                        String[] words;
//                        String poiName, substringText, subStringPoiName;
//                        List<IPoi> spois = PoisUtils.getPoiListSortedAlphabetical(poilist);
//                        final List<IPoi> fspois = new ArrayList<>();
//                        List<String> keywords = new ArrayList<String>();
//                        for (IPoi poi : spois) {
//                            poiName = poi.getpoiDescription();
//                            words = poiName.split(" ");
//                            if (words.length > 1) {
//                                boolean isAdded = false;
//                                for (String word : words) {
//                                    if (soundex.getSimilarity(text, word) > currentPercent) {
//                                        fspois.add(poi);
//                                        isAdded = true;
//                                        break;
//                                    }
//                                }
//                                if (!isAdded) {
//                                    keywords = poi.getPoiKeywords();
//                                    if (keywords != null && keywords.size() > 0) {
//                                        for (String keyword : keywords) {
//                                            if (soundex.getSimilarity(text, keyword) > currentPercent) {
//                                                fspois.add(poi);
//                                                break;
//                                            }
//                                        }
//                                    }
//                                }
//                            } else {
//                                if (soundex.getSimilarity(text, poiName) > currentPercent) {
//                                    fspois.add(poi);
//                                }
//                            }
//                        }
//                        if (fspois.size() > 0) {
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    ((MainActivity) getActivity()).hideSoftKeyboard();
//                                    sadapter = new SpreoSearchListAdapter(ctx, R.layout.spreo_search_list_item, fspois);
//                                    slist.setAdapter(sadapter);
//                                }
//                            });
//                        }
//                    }
////            else if (filterText.getText().toString().isEmpty()) {
////                List<IPoi> sortedlist = PoisUtils.getPoiListSortedAlphabetical(poilist);
////                sadapter = new SpreoSearchListAdapter(rootView.getContext(), R.layout.spreo_search_list_item, sortedlist);
////                slist.setAdapter(sadapter);
////            }
                } else {
                    Handler mainHandler = new Handler(ctx.getMainLooper());

                    Runnable myRunnable = new Runnable() {
                        @Override
                        public void run() {
                            if (sadapter != null) {
                                sadapter.setSearchFilterListener(searchFilterListener);
                                sadapter.getFilter().filter(s);
                            }
                        } // This is your code6
                    };
                    mainHandler.post(myRunnable);

                }
            }
        });
        thread.start();
    }

    private void openCategoriesSerachResult() {
        searchLayout.setVisibility(View.GONE);
        catgoryLayout.setVisibility(View.VISIBLE);
        seacrhType = null;
        List<PoiCategory> ctaegories = new ArrayList<>();
        ctaegories.addAll(SpreoSearchDataHolder.getInstance().getMenuCategories());
        cadapter = new SpreoCategoriesListAdapter(ctx, R.layout.spreo_category_list_item, ctaegories);
        clist.setAdapter(cadapter);
        limitCategorySize();
    }

    private void openFavoritesSerachResult() {
        catgoryLayout.setVisibility(View.GONE);
        searchLayout.setVisibility(View.VISIBLE);
        List<IPoi> tmpsarchlist;
        tmpsarchlist = SpreoSearchDataHolder.getInstance().getFavoritesPOIs();
        poilist.clear();
        poilist.addAll(tmpsarchlist);
        sadapter = new SpreoSearchListAdapter(ctx, R.layout.spreo_search_list_item, poilist, SpreoSearchItem.FAVORITE);
        slist.setAdapter(sadapter);
        limitSearchSize();
        sadapter.setSearchClickListener(this);
        if (tmpsarchlist.isEmpty()) {
            favoritesMsg.setVisibility(VISIBLE);
        } else {
            favoritesMsg.setVisibility(GONE);
        }
    }

    private void openHistorySerachResult() {
        catgoryLayout.setVisibility(View.GONE);
        searchLayout.setVisibility(View.VISIBLE);
        List<IPoi> tmpsarchlist;
        tmpsarchlist = SpreoSearchDataHolder.getInstance().getHistoryPOIs();
        poilist.clear();
        poilist.addAll(tmpsarchlist);
        sadapter = new SpreoSearchListAdapter(ctx, R.layout.spreo_search_list_item, poilist, SpreoSearchItem.HISTORY);
        slist.setAdapter(sadapter);
        limitSearchSize();
        sadapter.setSearchClickListener(this);
    }

    private void openPoiSerachResult(List<IPoi> list) {
        seacrhType = SpreoSearchItem.POI;
        menuItems.setVisibility(View.GONE);
        catgoryLayout.setVisibility(View.GONE);
        searchLayout.setVisibility(View.VISIBLE);
        SpreoSearchDataHolder.getInstance().resetPoiList();
        poilist.clear();
        poilist.addAll(list);
        if (SpreoSearchDataHolder.getInstance().isSortBYLocation()) {
            sortByLocation();
        } else {
            sortAlphabetical();
        }
    }

    private void openPoiSerachResult() {
        seacrhType = SpreoSearchItem.POI;
        menuItems.setVisibility(View.GONE);
        catgoryLayout.setVisibility(View.GONE);
        searchLayout.setVisibility(View.VISIBLE);
        SpreoSearchDataHolder.getInstance().resetPoiList();
        List<IPoi> tmpsarchlist;
        tmpsarchlist = SpreoSearchDataHolder.getInstance().getPoiList();
        poilist.clear();
        poilist.addAll(tmpsarchlist);

        if (SpreoSearchDataHolder.getInstance().isSortBYLocation()) {
            sortByLocation();
        } else {
            sortAlphabetical();
        }

        if (searchClickListener != null) {
            try {
                searchClickListener.typeStarted();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void sortAlphabetical() {
        List<IPoi> sortedlist = PoisUtils.getPoiListSortedAlphabetical(poilist);
        sadapter = new SpreoSearchListAdapter(ctx, R.layout.spreo_search_list_item, sortedlist, SpreoSearchItem.POI);
        slist.setAdapter(sadapter);
        limitSearchSize();
        sadapter.setSearchClickListener(this);
    }

    private void limitCategorySize() {
        if(cadapter.getCount() > 5){
            View item = cadapter.getView(0, null, clist);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) (5.5 * item.getMeasuredHeight()));
            clist.setLayoutParams(params);
        } else if (cadapter.getCount() > 0){
            View item = cadapter.getView(0, null, clist);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) (cadapter.getCount() * item.getMeasuredHeight()));
            clist.setLayoutParams(params);
        } else if (cadapter.getCount() == 0){
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
            clist.setLayoutParams(params);
        }
    }

    private void limitSearchSize() {
        if(sadapter.getCount() > 5){
            View item = sadapter.getView(0, null, slist);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) (5.5 * item.getMeasuredHeight()));
            slist.setLayoutParams(params);
        } else if (sadapter.getCount() > 0){
            View item = sadapter.getView(0, null, slist);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            slist.setLayoutParams(params);
        } else if (sadapter.getCount() == 0){
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
            slist.setLayoutParams(params);
        }
    }

    public void sortByLocation() {
//        PoiDataHelper.getInstance().getAllPoiOfFloorAsList(1);
        List<IPoi> sortedlistbyLoc = SortingPoiUtil.getPoisSortedByLocation(poilist, SpreoLocationProvider.getInstance().getUserLocation());
        sadapter = new SpreoSearchListAdapter(ctx, R.layout.spreo_search_list_item, sortedlistbyLoc, SpreoSearchItem.POI);
        slist.setAdapter(sadapter);
        limitSearchSize();
        sadapter.setSearchClickListener(this);
    }

    @Override
    public void onGoClickListener(IPoi poi) {
       if (poi != null) {
           if (searchClickListener != null) {
               try {
                   hideMenu();
                   searchClickListener.onGoClickListener(poi);
               } catch (Throwable t) {
                   t.printStackTrace();
               }
           }
       }
    }

    @Override
    public void onItemClickListener(IPoi poi) {
        if (poi != null) {

        }
    }

    @Override
    public void onShowClickListener(IPoi poi) {

    }

    @Override
    public void onParkingClickListener() {

    }

    @Override
    public void menuOpened() {

    }

    @Override
    public void typeStarted() {

    }

    @Override
    public void onCampusSelectionClick() {

    }

    public void hideMenu() {
        hideKeyboard();
        clearSearch();
        setMenuBackButton(false);
        seacrhType = null;
        if (searchLayout != null) {
            searchLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onFilterResultPublished() {
        sadapter.setSearchFilterListener(null);
        limitSearchSize();
    }

    public void setseacrhType(SpreoSearchItem type) {
        seacrhType = type;
    }

    public void clearSearch() {
        filterText.setText("");
    }

    public void setMenuBackButton(boolean back) {
        Bitmap bm = null;
        if (back) {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.back);
        } else {
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.serach_bar_menu);
        }
        if (bm != null) {
            menuIcon.setImageBitmap(bm);
        }
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /// ..
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new CustomOutline(w, h));
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private class CustomOutline extends ViewOutlineProvider {

        int width;
        int height;

        CustomOutline(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void getOutline(View view, Outline outline) {
            outline.setRect(0, 0, width, height);
        }
    }

    public void refreshFavorites() {
        if (seacrhType == SpreoSearchItem.FAVORITE) {
            openFavoritesSerachResult();
        }
    }

    public void hideFavoritesMsg() {
        if (favoritesMsg != null) {
            favoritesMsg.setVisibility(GONE);
        }
    }

    public void refreshSearchListByLocation() {
        if(searchLayout.getVisibility() == VISIBLE && seacrhType == SpreoSearchItem.POI) {
            sortByLocation();
        }
    }
}
