package com.reactlibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mlins.utils.SortingPoiUtil;
import com.spreo.nav.interfaces.IPoi;
import com.spreo.sdk.location.SpreoLocationProvider;
import com.spreo.sdk.poi.PoisUtils;

import java.util.ArrayList;
import java.util.List;



public class SpreoFromToView extends LinearLayout implements SpreoSearchFilterListener {

    private Context ctx = null;
    private LinearLayout fromTextLL, toTextLL, fromEditLL, toEditLL, myloc;
    private TextView fromText, toText;
    private EditText fromEdit, toEdit;
    private IPoi from = null;
    private IPoi to = null;
    private ListView slist;
    private SpreoFromToListAdapter sadapter;
    private List<IPoi> poilist = new ArrayList<IPoi>();
    private Button startButton;
    private boolean sundexChecker = false;
    private SpreoSearchFilterListener searchFilterListener = this;
    private SpreoSearchItem seacrhType = null;
    private FromToState state = null;
    private RelativeLayout closeFromToBtn;
    private SpreoFromToListener fromToListener = null;

    public SpreoFromToView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        ctx = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spreo_fromto_view, this, true);

        fromTextLL = (LinearLayout) findViewById(R.id.fromTextLL);
        fromTextLL.setOnClickListener(fromTextLLClick);
        toTextLL = (LinearLayout) findViewById(R.id.toTextLL);
        toTextLL.setOnClickListener(toTextLLClick);

        fromEditLL = (LinearLayout) findViewById(R.id.fromEditLL);
//        fromEditLL.setOnClickListener(fromEditLLClick);
        toEditLL = (LinearLayout) findViewById(R.id.toEditLL);
//        toEditLL.setOnClickListener(toEditLLClick);

        fromText = (TextView) findViewById(R.id.fromText);
        toText = (TextView) findViewById(R.id.toText);

        fromEdit = (EditText) findViewById(R.id.fromEdit);
//        fromEdit.setOnTouchListener(fromEditLLClick);
        fromEdit.addTextChangedListener(toTextWatcher);
        toEdit = (EditText) findViewById(R.id.toEdit);
//        toEdit.setOnTouchListener(toEditLLClick);
        toEdit.addTextChangedListener(toTextWatcher);

        slist = (ListView) findViewById(R.id.fromToListView);
        slist.setOnItemClickListener(searchItemClickListener);

        startButton = (Button) findViewById(R.id.startButton);
        startButton.setOnClickListener(startButtonClick);

        closeFromToBtn = (RelativeLayout) findViewById(R.id.closeFromToBtn);
        closeFromToBtn.setOnClickListener(closeFromToBtnClick);

        myloc = (LinearLayout) findViewById(R.id.myloc);
        myloc.setOnClickListener(mylocClick);

    }

    private OnClickListener mylocClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            hideKeyboard();
            from = null;
            fromText.setText("My current location");
            showFromText();
            myloc.setVisibility(GONE);
            closeSearch();
        }
    };

    private OnClickListener closeFromToBtnClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            notifyCanceled();
            close();
        }
    };

//    private OnTouchListener fromEditLLClick = new OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            if(MotionEvent.ACTION_UP == event.getAction()) {
//                if (state != null && state == FromToState.TO) {
//                    showFromEdit();
//                }
//                state = FromToState.FROM;
//                cleacrToSearch();
//                return false;
//            }
//            return false;
//
//        }
//    };
//
//    private OnTouchListener toEditLLClick = new OnTouchListener() {
//        @Override
//        public boolean onTouch(View v, MotionEvent event) {
//            if(MotionEvent.ACTION_UP == event.getAction()) {
//                if (state != null && state == FromToState.FROM) {
//                    showToEdit();
//                }
//                state = FromToState.TO;
//                clearFromSearch();
//                return false;
//            }
//            return false;
//
//        }
//    };

//    private OnClickListener fromEditLLClick = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (state != null && state == FromToState.TO) {
//                showFromEdit();
//            }
//            state = FromToState.FROM;
//        }
//    };

//    private OnClickListener toEditLLClick = new OnClickListener() {
//        @Override
//        public void onClick(View v) {
//            if (state != null && state == FromToState.FROM) {
//                showToEdit();
//            }
//            state = FromToState.TO;
//        }
//    };

    private AdapterView.OnItemClickListener searchItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            myloc.setVisibility(GONE);
            IPoi poi = (IPoi) parent.getItemAtPosition(position);
            if (poi != null) {
                hideKeyboard();
                if (state == FromToState.FROM) {
                    clearFromSearch();
                    from = poi;
                    showFromText();
                } else if (state == FromToState.TO) {
                    cleacrToSearch();
                    to = poi;
                    showToText();
                }
                closeSearch();
            }
        }
    };

    private OnClickListener startButtonClick = new OnClickListener() {
        @Override
        public void onClick(View v) {

            notifyNavigation();
            close();
        }
    };

    private OnClickListener fromTextLLClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            cleacrToSearch();
            showFromEdit();
            showToText();
            state = FromToState.FROM;
        }
    };

    private OnClickListener toTextLLClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            clearFromSearch();
            showToEdit();
            showFromText();
            state = FromToState.TO;
        }
    };

    public void showFromText() {
        if (from != null) {
            fromText.setText(from.getpoiDescription());
        }
        fromEditLL.setVisibility(View.GONE);
        fromTextLL.setVisibility(View.VISIBLE);
//        fromEdit.clearFocus();
    }

    public void showFromEdit() {
        fromTextLL.setVisibility(View.GONE);
        fromEditLL.setVisibility(View.VISIBLE);
        fromEdit.requestFocus();
        openHistorySerachResult(true);
        state = FromToState.FROM;
        showKeyboard();
    }

    public void showToText() {
        if (to != null) {
            toText.setText(to.getpoiDescription());
        }
        toEditLL.setVisibility(View.GONE);
        toTextLL.setVisibility(View.VISIBLE);
//        toEdit.clearFocus();
    }

    public void showToEdit() {
        toTextLL.setVisibility(View.GONE);
        toEditLL.setVisibility(View.VISIBLE);
        toEdit.requestFocus();
        openHistorySerachResult(false);
        state = FromToState.TO;
        showKeyboard();
    }

    public IPoi getFrom() {
        return from;
    }

    public void setFrom(IPoi from) {
        this.from = from;
    }

    public IPoi getTo() {
        return to;
    }

    public void setTo(IPoi to) {
        if (to != null) {
            this.to = to;
            toText.setText(to.getpoiDescription());
            startButton.setVisibility(View.VISIBLE);
        }
    }

    private void openPoiSerachResult() {
        myloc.setVisibility(GONE);
        seacrhType = SpreoSearchItem.POI;
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

    }

    public void refreshSearchListByLocation() {
        if(seacrhType == SpreoSearchItem.POI) {
            sortByLocation();
        }
    }

    public void sortAlphabetical() {
        List<IPoi> sortedlist = PoisUtils.getPoiListSortedAlphabetical(poilist);
        sadapter = new SpreoFromToListAdapter(ctx, R.layout.spreo_search_list_item, sortedlist, SpreoSearchItem.POI);
        slist.setAdapter(sadapter);
        limitSearchSize();
    }

    public void sortByLocation() {
//        PoiDataHelper.getInstance().getAllPoiOfFloorAsList(1);
        List<IPoi> sortedlistbyLoc = SortingPoiUtil.getPoisSortedByLocation(poilist, SpreoLocationProvider.getInstance().getUserLocation());
        sadapter = new SpreoFromToListAdapter(ctx, R.layout.spreo_search_list_item, sortedlistbyLoc, SpreoSearchItem.POI);
        slist.setAdapter(sadapter);
        limitSearchSize();
    }

    private void closeSearch() {
        myloc.setVisibility(GONE);
        slist.setVisibility(View.GONE);
        if (to != null) {
            startButton.setVisibility(View.VISIBLE);
            notifyPresentDestination();
        }
    }

    private void notifyPresentDestination() {
        if (to != null && fromToListener != null) {
            try {
                fromToListener.presentDestination(to);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void openHistorySerachResult(boolean showMyLoc) {
        if (showMyLoc) {
            myloc.setVisibility(VISIBLE);
        } else {
            myloc.setVisibility(GONE);
        }
        seacrhType = SpreoSearchItem.HISTORY;
        startButton.setVisibility(View.GONE);
        slist.setVisibility(View.VISIBLE);
        List<IPoi> tmpsarchlist;
        tmpsarchlist = SpreoSearchDataHolder.getInstance().getHistoryPOIs();
        poilist.clear();
        poilist.addAll(tmpsarchlist);
        sadapter = new SpreoFromToListAdapter(ctx, R.layout.spreo_search_list_item, poilist, SpreoSearchItem.HISTORY);
        slist.setAdapter(sadapter);
        limitSearchSize();
//        sadapter.setSearchClickListener(this);
    }

    private void limitSearchSize() {
        if(sadapter.getCount() > 5){
            View item = sadapter.getView(0, null, slist);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) (5.5 * item.getMeasuredHeight()));
            slist.setLayoutParams(params);
        } else if (sadapter.getCount() > 0){
            View item = sadapter.getView(0, null, slist);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            slist.setLayoutParams(params);
        } else if (sadapter.getCount() == 0){
            ViewGroup.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0);
            slist.setLayoutParams(params);
        }
    }

    private TextWatcher toTextWatcher = new TextWatcher() {


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
            if (count !=  0 && seacrhType != SpreoSearchItem.POI) {
                openPoiSerachResult();
            }
            if (sundexChecker) {
                sundexChecker = false;
                List<IPoi> sortedlist = PoisUtils.getPoiListSortedAlphabetical(poilist);
                sadapter = new SpreoFromToListAdapter(ctx, R.layout.spreo_search_list_item, sortedlist, SpreoSearchItem.POI);
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

    @Override
    public void onFilterResultPublished() {
        limitSearchSize();
    }

    private void clearFromSearch() {
        fromEdit.setText("");
    }

    private void cleacrToSearch() {
        toEdit.setText("");
    }

    public void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindowToken(), 0);
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    public void close() {
        cleacrToSearch();
        clearFromSearch();
        showFromText();
        showToText();
        from = null;
        to = null;
        state = null;
        seacrhType = null;
        hideKeyboard();
        fromText.setText("My current location");
        toText.setText("");
        slist.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        myloc.setVisibility(GONE);
        this.setVisibility(View.GONE);
    }

    private void notifyCanceled() {
        if (fromToListener != null) {
            try {
                fromToListener.navigationCanceled();
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private void notifyNavigation() {
        if (fromToListener != null && to != null) {
            try {
                fromToListener.navigate(from, to);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    public void setFromToListener(SpreoFromToListener fromToListener) {
        this.fromToListener = fromToListener;
    }

    private enum FromToState {
        FROM,
        TO;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /// ..
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new SpreoFromToView.CustomOutline(w, h));
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
}
