package com.reactlibrary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mlins.project.Campus;
import com.mlins.project.ProjectConf;
import com.mlins.utils.FacilityConf;
import com.spreo.nav.interfaces.INavInstruction;
import com.spreo.nav.interfaces.IPoi;

import java.util.Map;



public class SpreoLiveNavView extends LinearLayout {
    private final Context ctx;
    private Button stopBtn;
    private SpreoFromToListener fromToListener = null;
    private SpreoNavViewListener navViewListener = null;
    TextView destName, destInfo, distanceInfo, insTextView;
    private ImageView insImageView;

    public SpreoLiveNavView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        ctx = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spreo_live_nav_view, this, true);

        stopBtn = (Button) findViewById(R.id.stopBtn);
        stopBtn.setOnClickListener(navCloseClick);
        destName = (TextView) findViewById(R.id.destName);
        destInfo = (TextView) findViewById(R.id.destInfo);
        distanceInfo = (TextView) findViewById(R.id.distanceInfo);
        insTextView = (TextView) findViewById(R.id.insText);
        insImageView = (ImageView) findViewById(R.id.insSign);
    }

    public void setFromToListener(SpreoFromToListener fromToListener) {
        this.fromToListener = fromToListener;
    }

    public void setNavViewListener(SpreoNavViewListener navViewListener) {
        this.navViewListener = navViewListener;
    }

    private OnClickListener navCloseClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (fromToListener != null) {
                try {
                    clear();
                    fromToListener.navigationCanceled();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    };

    public void setPoi(IPoi poi) {
        if (poi != null) {
            String dname = poi.getpoiDescription();
            if (dname != null) {
                destName.setText(dname);
            }
            String dinfo = getDestinationInfo(poi);
            if (dinfo != null) {
                destInfo.setText(dinfo);
            }
        }

        String distance = navViewListener.calculateDistance();
        if (distance != null && !distance.isEmpty()) {
            distanceInfo.setText(distance);
        }

    }

    public void setParkingNav() {
        destName.setText("Parking");
    }

    private String getDestinationInfo(IPoi poi) {
        String result = "";
        if (poi != null) {
            String floor = getFloorText(poi);
            Campus campus = ProjectConf.getInstance().getSelectedCampus();
            if (campus != null) {
                if (floor != null && !floor.isEmpty()) {
                    floor = floor.replace("L", "");
                    String Floor = ctx.getString(R.string.floor);
                    result = campus.getFacilityConf(poi.getFacilityID()).getName() + ", " + Floor + " " + floor;
                } else {
                    result = campus.getFacilityConf(poi.getFacilityID()).getName();
                }
            }
        }
        return result;
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
        } else {
            result = "L1";
        }
        return result;
    }

    public void setInstruction (INavInstruction instruction) {
        if (instruction != null) {
            clearInstructions();
            String txt = instruction.getText();
            if (txt != null) {
                insTextView.setText(txt);
            }

            Bitmap bm = instruction.getSignBitmap();
            if (bm != null) {
                if (instruction.getId() == INavInstruction.DESTINATION_INSTRUCTION_TAG) {
                    insImageView.setColorFilter(null);
                } else {
                    insImageView.setColorFilter(Color.argb(255, 255, 255, 255));
                }
                insImageView.setImageBitmap(bm);
            }

        }
    }

    public void clear() {
        clearDest();
        clearInstructions();
    }

    private void clearInstructions() {
        insTextView.setText("");
        insImageView.setImageDrawable(null);
    }

    private void clearDest() {
        destName.setText("");
        destInfo.setText("");
        distanceInfo.setText("");
    }


}
