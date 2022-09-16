package com.reactlibrary;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.mlins.project.Campus;
import com.mlins.project.ProjectConf;
import com.mlins.res.setup.GalleryListener;
import com.mlins.res.setup.GalleryUpdateStatus;
import com.mlins.utils.FacilityConf;
import com.mlins.utils.GalleryObject;
import com.spreo.nav.interfaces.IPoi;
import com.spreo.sdk.poi.PoisUtils;

import java.util.ArrayList;
import java.util.Map;



public class SpreoDetailsView extends LinearLayout implements GalleryListener {

    private final Context ctx;
    private static final String POI_ID_KEY = "POI_ID_KEY";

    private IPoi poi = null;
    private SpreoCustomTextView phoneTv1;
    private SpreoCustomTextView phoneTv2;
    private SpreoCustomTextView emailTv;
    private SpreoCustomTextView hoursTv;
    private SpreoCustomTextView poiMapTv, closestParkingName, webUrl, closestParkingDuration, poiCategory;
    private SpreoCustomTextView goBtn, FavoritesBtn, ShowOnMapBtn;
    private SpreoCustomTextView addToFavoritesBtn;
    private SpreoCustomTextView showOnMapBtn, createEventBtn, showScheduleBtn, keywordsText, poiNameTv;
    private TextView showMoreLoc;
    private LinearLayout descriptionLL, createEventLL, contactsPhoneLL1, contactsPhoneLL2, contactsMailLL, poiMapLL, parkingLotLL, directionsLL, reserveLL, keywordsLL;
    private RelativeLayout hoursLL;
    private RelativeLayout webInfo, navigate, reservebtn;
    private SpreoCustomTextView directions_btn;

    // private PagerContainer mContainer;
    private ViewPager pager;
    private RelativeLayout showMoreLL;
    private ImageView mainLogo;
    IPoi closestParking;
    private boolean menuBtnpressed = false;
    TabLayout tabLayout;
    SpreoCustomTextView expandableTextView;
    ImageView showMore;
    RelativeLayout viewPagerContainer;
    SpreoViewPagerAdapter viewPagerAdapter;
    private LinearLayout quickBookLL, quickBookActionsLL;
    private RadioGroup quickRadioGroup;
    private RadioButton firstRadBtn, secondRadBtn, thirdRadBtn;
    private long eventDuration = 0;
    private SpreoSearchClickListener searchClickListener = null;
    private Bitmap facilityLogo = null;
    private String removeFromFavorites = "Remove Favorite";
    private String addToFavorites = "Add Favorite";
    private int dailogButtonsColor = getResources().getColor(R.color.actionbar_background);
    private ScrollView scrollView;
    private boolean sizeCalculated = false;

    public SpreoDetailsView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        ctx = context;
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spreo_details_view, this, true);


        viewPagerContainer = (RelativeLayout) findViewById(R.id.view_pager_container);
        hoursLL = (RelativeLayout) findViewById(R.id.hoursLL);
        descriptionLL = (LinearLayout) findViewById(R.id.descriptionLL);
        contactsMailLL = (LinearLayout) findViewById(R.id.contact_emailLL);
        contactsPhoneLL1 = (LinearLayout) findViewById(R.id.contact_phoneLL1);
        contactsPhoneLL2 = (LinearLayout) findViewById(R.id.contact_phoneLL2);
        //  poiMapLL = (LinearLayout) rootView.findViewById(R.id.poi_mapLL);
        poiMapTv = (SpreoCustomTextView) findViewById(R.id.poi_map_tv);
        poiCategory = (SpreoCustomTextView) findViewById(R.id.poi_category);
        phoneTv1 = (SpreoCustomTextView) findViewById(R.id.phone_text_view1);
        phoneTv2 = (SpreoCustomTextView) findViewById(R.id.phone_text_view2);
        emailTv = (SpreoCustomTextView) findViewById(R.id.cont_mail_text_view);
        hoursTv = (SpreoCustomTextView) findViewById(R.id.hoursTv);

        directions_btn = (SpreoCustomTextView) findViewById(R.id.go_poi_btn);
        directions_btn.setOnClickListener(dirBtnListener);

        addToFavoritesBtn = (SpreoCustomTextView) findViewById(R.id.addToFavoritesText);
        addToFavoritesBtn.setOnClickListener(addfavoriteListener);

        showOnMapBtn = (SpreoCustomTextView) findViewById(R.id.showOnMapText);
        showOnMapBtn.setOnClickListener(showOnMapListener);

        expandableTextView = (SpreoCustomTextView) findViewById(R.id.expandableTextView);
        showMore = (ImageView) findViewById(R.id.show_more_tv);
        showMoreLL = (RelativeLayout) findViewById(R.id.show_more_ll);
        mainLogo = (ImageView) findViewById(R.id.main_logo);
        parkingLotLL = (LinearLayout) findViewById(R.id.patking_lotLL);


        // reserveLL = (LinearLayout) rootView.findViewById(R.id.reserveLL);
        //   directionsLL = (LinearLayout) rootView.findViewById(R.id.DirectionsLL);
        //  navigate = (RelativeLayout) rootView.findViewById(R.id.navigate_btn);
        //   navigate.setOnClickListener(dirBtnListener);
        //   reservebtn = (RelativeLayout) rootView.findViewById(R.id.reserve_btn);
        //  reservebtn.setOnClickListener(reservebtnListener);
        closestParkingName = (SpreoCustomTextView) findViewById(R.id.poi_parking);
        closestParkingDuration = (SpreoCustomTextView) findViewById(R.id.duration);


        pager = (ViewPager) findViewById(R.id.view_pager);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_dots);
        tabLayout.bringToFront();


        webUrl = (SpreoCustomTextView) findViewById(R.id.web_info_tv);
        webInfo = (RelativeLayout) findViewById(R.id.web_info_detailsLL);

        keywordsLL = (LinearLayout) findViewById(R.id.keywordsLL);
        keywordsText = (SpreoCustomTextView) findViewById(R.id.keywordsText);

        poiNameTv = (SpreoCustomTextView) findViewById(R.id.poiNameTv);

        scrollView = (ScrollView) findViewById(R.id.scrollView);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 80) {
            int realw = w - 200;
            if (poiNameTv != null) {
                poiNameTv.setMaxWidth(realw/2 - 20);
            }
            if (poiMapTv != null) {
                poiMapTv.setMaxWidth(realw/2 - 20);
            }
            if (closestParkingName != null) {
                closestParkingName.setMaxWidth(realw/2 - 20);
            }
            invalidate();
            sizeCalculated = true;
        }
    }

    public void setSearchClickListener(SpreoSearchClickListener listener) {
        searchClickListener = listener;
    }

    public void setPoi(final IPoi poi) {
        if (!sizeCalculated) {
            post(new Runnable() {
                @Override
                public void run() {
                    setPoiContent(poi);
                }
            });
        } else {
            setPoiContent(poi);
        }
    }

    public void setPoiContent(final IPoi poi) {

        clear();

        this.poi = poi;

        boolean isfavorite = SpreoSearchDataHolder.getInstance().isFavorite(poi);
        if (isfavorite) {
            addToFavoritesBtn.setText(removeFromFavorites);
        } else {
            addToFavoritesBtn.setText(addToFavorites);
        }

        PoisUtils.updateIPoiHeadImage(poi, this);
        PoisUtils.updateIPoiGallery(poi, this);

        if (poi.getpoiDescription() != null) {
            poiNameTv.setText(poi.getpoiDescription());
        }

        closestParking = PoisUtils.getClosestParking(poi);
        if (closestParking != null) {
            closestParkingName.setText(closestParking.getpoiDescription());
            closestParkingName.setPaintFlags(closestParkingName.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);

            closestParkingName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setPoi(closestParking);
                }
            });

        }


            //        expandableTextView.setAnimationDuration(1000L);
//        expandableTextView.setInterpolator(new OvershootInterpolator());
//        showMore.setOnClickListener(showMoreListener);
        String poiInfo = "";
        String category = "";
        String floor = getFloorText(poi);
        Campus campus = ProjectConf.getInstance().getSelectedCampus();
        if (campus != null) {
            if (floor != null && !floor.isEmpty()) {
                floor = floor.replace("L", "");
                poiInfo = campus.getFacilityConf(poi.getFacilityID()).getName() + ", " + " Floor" + " " + floor;
                category = poi.getPoitype().get(0);
            } else {
                poiInfo = campus.getFacilityConf(poi.getFacilityID()).getName();
                category = poi.getPoitype().get(0);
            }
        }
//        poiInfo = poiInfo.replace("ach hospital", getString(R.string.american_medical_center));
//        poiInfo = poiInfo.replace("[", "");
//        poiInfo = poiInfo.replace("]", "");
        poiMapTv.setText(poiInfo);
        poiCategory.setText(category);

        if (poi.getActivehours().size() > 0) {
            hoursLL.setVisibility(View.VISIBLE);
            hoursTv.setText(poi.getActivehours().get(0).trim());
        }

        if (!poi.getDetails().isEmpty()) {
            descriptionLL.setVisibility(View.VISIBLE);
            expandableTextView.setText(poi.getDetails().trim());
//            showMoreLL.setVisibility(expandableTextView.getLineCount() > 3 ? View.VISIBLE : View.GONE);
//            showMore.post(new Runnable() {
//                @Override
//                public void run() {
//                    Layout layout = expandableTextView.getLayout();
//                    if (layout != null) {
//                        int lines = layout.getLineCount();
//                        if (lines > 0) {
//                            final int ellipsisCount = layout.getEllipsisCount(lines - 1);
//                            getActivity().runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    showMoreLL.setVisibility(ellipsisCount > 0 ? View.VISIBLE : View.GONE);
//                                    final View view = (View) findViewById(R.id.description_sep);
//                                    view.setVisibility(ellipsisCount > 0 ? View.GONE : View.VISIBLE);
//                                    if (ellipsisCount > 0) {
//                                        expandableTextView.setOnClickListener(showMoreListener);
//                                    }
//                                }
//                            });
//                        }
//                    }
//                }
//            });
        }

        if (poi.getUrl() != null && !poi.getUrl().isEmpty() && !poi.getUrl().equals("null")) {
            webUrl.setPaintFlags(webUrl.getPaintFlags() |   Paint.UNDERLINE_TEXT_FLAG);
            webUrl.setText("More information");
            webInfo.setVisibility(View.VISIBLE);
            webInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(poi.getUrl()));
                    ctx.startActivity(i);
                }
            });
        }

        if (!poi.getEmailaddress().isEmpty()) {
            emailTv.setText(poi.getEmailaddress());
            contactsMailLL.setVisibility(View.VISIBLE);
            contactsMailLL.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_EMAIL, new String[]{poi.getEmailaddress()});
                    ctx.startActivity(Intent.createChooser(intent, ""));
                }
            });
        }

        if (poi.getPhone1() != null && poi.getPhone1().size() > 0) {
            phoneTv1.setText(poi.getPhone1().get(0));
            contactsPhoneLL1.setVisibility(View.VISIBLE);
            contactsPhoneLL1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    String uri = "tel:" + poi.getPhone1().get(0).replaceAll("[^0-9|\\+]", "");
                    intent.setData(Uri.parse(uri));
                    ctx.startActivity(intent);
                }
            });
        }

        if (poi.getPhone2() != null && poi.getPhone2().size() > 0) {
            phoneTv2.setText(poi.getPhone2().get(0));
            contactsPhoneLL2.setVisibility(View.VISIBLE);
            contactsPhoneLL2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_DIAL);
                    String uri = "tel:" + poi.getPhone2().get(0).replaceAll("[^0-9|\\+]", "");
                    intent.setData(Uri.parse(uri));
                    ctx.startActivity(intent);
                }
            });
        }

        if (poi.getPoiKeywords() != null && !poi.getPoiKeywords().isEmpty()) {
            String keywordtext = "";
            for (String o : poi.getPoiKeywords()) {
                if (poi.getPoiKeywords().indexOf(o) == 0) {
                    keywordtext += o;
                } else {
                    keywordtext += ", " + o;
                }
            }
            keywordsText.setText(keywordtext);
            keywordsLL.setVisibility(View.VISIBLE);
        }

        if (poi.getGallery() != null) {
            viewPagerContainer.setVisibility(poi.getGallery().size() > 0 ? View.VISIBLE : View.GONE);
            if (poi.getGallery().size() > 0) {
                mainLogo.setVisibility(View.GONE);
            }
            if (poi.getGallery().size() == 0) {
                if (poi.getMediaurl() != null && !poi.getMediaurl().isEmpty() && poi.isPoiPlayMultyMedia()) {
                    viewPagerContainer.setVisibility(View.VISIBLE);
                    ArrayList<SpreoCoverFlowData> mData = new ArrayList<>(0);
                    String mediaurl = poi.getMediaurl();
                    mData.add(new SpreoCoverFlowData(null, mediaurl));
                    viewPagerAdapter = new SpreoViewPagerAdapter(ctx, mData);
                    pager.setAdapter(viewPagerAdapter);
                    pager.setOffscreenPageLimit(viewPagerAdapter.getCount());
                    pager.setClipChildren(false);
                } else {
                    loadMainLogo();
                }
            }
        } else {
            loadMainLogo();
        }
    }

    public void clear() {
        stopVideo();

        scrollView.scrollTo(0, 0);

        viewPagerAdapter = null;

        closestParkingName.setText("");
//        closestParkingDuration.setText("");
        poiMapTv.setText("");
        poiCategory.setText("");


        mainLogo.setImageDrawable(null);
        pager.setAdapter(null);

        descriptionLL.setVisibility(View.GONE);
        expandableTextView.setText("");

        hoursLL.setVisibility(View.GONE);
        hoursTv.setText("");

        webInfo.setVisibility(View.GONE);
        webUrl.setText("");

        contactsMailLL.setVisibility(View.GONE);
        emailTv.setText("");

        contactsPhoneLL1.setVisibility(View.GONE);
        phoneTv1.setText("");

        contactsPhoneLL2.setVisibility(View.GONE);
        phoneTv2.setText("");

        keywordsLL.setVisibility(View.GONE);
        keywordsText.setText("");

        poiNameTv.setText("");

    }


    private void loadMainLogo() {
        viewPagerContainer.setVisibility(View.GONE);
        mainLogo.setVisibility(View.VISIBLE);
        try {
            if (facilityLogo != null) {
                mainLogo.setImageBitmap(facilityLogo);

            } else {
                mainLogo.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.spreo_default_poi));
            }
        } catch (Exception e) {
            mainLogo.setImageDrawable(ContextCompat.getDrawable(ctx, R.drawable.spreo_default_poi));
        }
    }

    public static String getFloorText(IPoi citem) {
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

    private View.OnClickListener dirBtnListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (poi != null) {
                if (searchClickListener != null) {
                    try {
                        searchClickListener.onGoClickListener(poi);
                    } catch (Throwable t) {
                        t.printStackTrace();
                    }
                }
            }
        }
    };

        private View.OnClickListener showOnMapListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (poi != null) {
                    if (searchClickListener != null) {
                        try {
                            searchClickListener.onShowClickListener(poi);
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    }
                }
            }
        };

    private View.OnClickListener addfavoriteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (poi != null) {
                boolean isfavorite = SpreoSearchDataHolder.getInstance().isFavorite(poi);
                if (isfavorite) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setMessage("Are you sure about deleting this point of interest from favorites?")
                            .setCancelable(false)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    SpreoSearchDataHolder.getInstance().deleteFavorite(poi);
                                    addToFavoritesBtn.setText(addToFavorites);
                                    dialog.dismiss();
                                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                                    builder.setMessage("This point of interest was deleted from favorites.")
                                            .setCancelable(false)
                                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    AlertDialog alert = builder.create();
                                    alert.show();
                                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dailogButtonsColor);
                                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dailogButtonsColor);
                                }
                            })
                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dailogButtonsColor);
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dailogButtonsColor);
                } else {
                    SpreoSearchDataHolder.getInstance().addFavorite(poi);
                    addToFavoritesBtn.setText(removeFromFavorites);
                    AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                    builder.setMessage("This point of interest has been added to your favorites.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                    alert.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(dailogButtonsColor);
                    alert.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(dailogButtonsColor);
                }
            }
        }
    };

    @Override
    public void onPostDownload(GalleryUpdateStatus status) {
        if (status == GalleryUpdateStatus.OK) {
            GalleryObject headImage = poi.getHeadImage();
            ArrayList<SpreoCoverFlowData> mData = new ArrayList<>(0);

            if (headImage != null) {
                mData.add(new SpreoCoverFlowData(headImage, ""));
            }

            if (poi.getGallery().size() > 0) {
                for (GalleryObject galleryObject : poi.getGallery()) {
                    mData.add(new SpreoCoverFlowData(galleryObject, ""));
                }
                String mediaurl = poi.getMediaurl();
                if (mediaurl != null && (!mediaurl.isEmpty() && mediaurl.length() > 4) && poi.isPoiPlayMultyMedia()) {
                    String subStrMedia = mediaurl.substring(mediaurl.length() - 4);
                    if (subStrMedia.equals(".mp4")) {
                        mData.add(new SpreoCoverFlowData(null, mediaurl));
                    }
                }
                viewPagerAdapter = new SpreoViewPagerAdapter(ctx, mData);
                pager.setAdapter(viewPagerAdapter);
                pager.setOffscreenPageLimit(viewPagerAdapter.getCount());
                pager.setClipChildren(false);
                if (viewPagerAdapter.getCount() >= 2) {
                    tabLayout.setupWithViewPager(pager, false);
                }
                pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                    }

                    @Override
                    public void onPageSelected(int position) {
                        viewPagerAdapter.setItem(position);
                        if (position != viewPagerAdapter.getCount() - 1) {
                            // viewPagerAdapter.hideMediaController();
                            viewPagerAdapter.pauseVideo();
                        } else {
                            viewPagerAdapter.startVideo();
                        }
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {

                    }
                });

            } else if (headImage != null) {
                mainLogo.setImageBitmap(headImage.getBitmap());
                mData.add(new SpreoCoverFlowData(headImage, ""));
                if (poi.getMediaurl() != null && !poi.getMediaurl().isEmpty()) {
                    String mediaurl = poi.getMediaurl();
                    mData.add(new SpreoCoverFlowData(null, mediaurl));
                }
                final SpreoViewPagerAdapter viewPagerAdapter = new SpreoViewPagerAdapter(ctx, mData);
                pager.setAdapter(viewPagerAdapter);
                pager.setOffscreenPageLimit(viewPagerAdapter.getCount());
                pager.setClipChildren(false);
                if (viewPagerAdapter.getCount() >= 2) {
                    tabLayout.setupWithViewPager(pager, false);
                }
            }
        }
    }

    public void stopVideo() {
        if (viewPagerAdapter != null) {
            viewPagerAdapter.stopVideo();
        }
    }

    public void setFacilityLogo(Bitmap facilityLogo) {
        this.facilityLogo = facilityLogo;
    }

}
