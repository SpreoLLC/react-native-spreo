package com.reactlibrary;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Outline;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mlins.dualmap.DualMapNavUtil;
import com.mlins.dualmap.RouteCalculationHelper;
import com.mlins.instructions.Instruction;
import com.mlins.project.Campus;
import com.mlins.project.ProjectConf;
import com.mlins.utils.FacilityConf;
import com.mlins.utils.PoiData;
import com.mlins.utils.ResourceTranslator;
import com.spreo.nav.interfaces.INavInstruction;

import java.util.ArrayList;
import java.util.List;



public class SpreoStaticNavView extends LinearLayout {

    private final Context ctx;
    private SpreoFromToListener fromToListener = null;
    private ImageView navCloseIcon;
    private TextView navText,distanceText1, distanceText2;
    private SpreoInstrcutionsListAdapter instrcutionsListAdapter = null;
    private ListView instructionsList;
    private RelativeLayout showLess, showMore;
    private LinearLayout insLayout;
    private SpreoNavViewListener navViewListener = null;

    public SpreoStaticNavView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        ctx = context;

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.spreo_static_nav_view, this, true);

        navCloseIcon = (ImageView) findViewById(R.id.navCloseIcon);
        navCloseIcon.setOnClickListener(navCloseClick);

        navText = (TextView) findViewById(R.id.navText);
        instructionsList = (ListView) findViewById(R.id.instructionsList);

        showMore = (RelativeLayout) findViewById(R.id.showMore);
        showMore.setOnClickListener(showMoreClick);

        showLess = (RelativeLayout) findViewById(R.id.showLess);
        showLess.setOnClickListener(showLessClick);

        insLayout = (LinearLayout) findViewById(R.id.insLayout);

        distanceText1 = (TextView) findViewById(R.id.distanceText1);
        distanceText2 = (TextView) findViewById(R.id.distanceText2);
    }

    private OnClickListener showMoreClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showInstructionsList();
        }
    };

    private OnClickListener showLessClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            hideInstructionsList();
        }
    };

    private void showInstructionsList() {
//        updateInstructions();
        showMore.setVisibility(View.GONE);
        insLayout.setVisibility(View.VISIBLE);
    }

    public void hideInstructionsList() {
        insLayout.setVisibility(View.GONE);
        showMore.setVisibility(View.VISIBLE);
    }

    private OnClickListener navCloseClick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            if (fromToListener != null) {
                try {
                    hideInstructionsList();
                    fromToListener.navigationCanceled();
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    };

    public void setFromToListener(SpreoFromToListener fromToListener) {
        this.fromToListener = fromToListener;
    }

    public void setText(String text) {
        navText.setText(text);
    }

    public void updateInstructions() {
        List<Instruction> instructions = RouteCalculationHelper.getInstance().getCombinedInstructions();
        List<INavInstruction> simplified = getInstructions(instructions);
        if (instructions != null && !instructions.isEmpty()) {
            instrcutionsListAdapter = new SpreoInstrcutionsListAdapter(ctx, R.layout.spreo_instruction_item, simplified);
            instructionsList.setAdapter(instrcutionsListAdapter);
            limitSize();
        }
        updateDistance();
    }


    private List<INavInstruction> getInstructions(List<Instruction> ins) {
        List<INavInstruction> result = new ArrayList<>();
        List<Instruction> instructions = new ArrayList<>();
        instructions.addAll(ins);
        for (Instruction o: instructions) {
            Bitmap bmp = null;
            if (o.getType() == Instruction.TYPE_DESTINATION) {
                bmp = BitmapFactory.decodeResource(getResources(), R.drawable.map_destination);
            } else {
                List<Integer> bmres = o.getImage();
                if (bmres != null && !bmres.isEmpty()) {
                    Integer cres = bmres.get(0);
                    if (cres != null) {
                        bmp = BitmapFactory.decodeResource(getResources(), cres);
                    }
                }
            }

            String txt = o.toString(); //getSimplifiedText(o);
            if (o.getType() == Instruction.TYPE_SWITCH_FLOOR) {
                try {
                    double fromz = o.getLocation().getZ();
                    Instruction nextins = instructions.get(instructions.indexOf(o) + 1);
                    double toz = nextins.getLocation().getZ();
                    if (toz > fromz) {
                        txt = txt.replace("Proceed to floor", "Go up to level");
                    } else if (fromz > toz) {
                        txt = txt.replace("Proceed to floor", "Go down to level");
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }

            if (txt != null && !txt.isEmpty()) {
                txt = txt.replace("Follow the line", "Follow the path");
                int id = o.getID();
                SpreoInstructionObj obj = new SpreoInstructionObj(id, txt,bmp, "",0.0);
                result.add(obj);
            }
        }
        return result;
    }

    private void updateDistance() {
        if (navViewListener != null) {
            try {
                String txt = navViewListener.calculateDistance();
                if (txt != null) {
                    distanceText1.setText(txt);
                    distanceText2.setText(txt);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

    }

    private void limitSize() {
        if(instrcutionsListAdapter.getCount() > 5){
            View item = instrcutionsListAdapter.getView(0, null, instructionsList);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, (int) (5.5 * item.getMeasuredHeight()));
            instructionsList.setLayoutParams(params);
        } else if (instrcutionsListAdapter.getCount() > 0){
            View item = instrcutionsListAdapter.getView(0, null, instructionsList);
            item.measure(0, 0);
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            instructionsList.setLayoutParams(params);
        } else if (instrcutionsListAdapter.getCount() == 0){
            ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
            instructionsList.setLayoutParams(params);
        }
    }

    private String getSimplifiedText(Instruction instruction) {
        String result = "";
        try {
            int type = instruction.getType();
            if (type == Instruction.TYPE_SWITCH_FLOOR) {
//                int tmptxt = ResourceTranslator.getInstance().getTranslatedResourceId("string", "simplified_switch");
                result = "Follow the line to floor " + instruction.getTofloor();
            } else if (type == Instruction.TYPE_EXIT) {
                int tmptxt = ResourceTranslator.getInstance().getTranslatedResourceId("string", "simplified");
                result = "Follow the line to the exit";
                result = getResources().getString(tmptxt);
                String cfacid = instruction.getLocation().getFacilityId();
                Campus campus = ProjectConf.getInstance().getSelectedCampus();
                FacilityConf fac = campus.getFacilityConf(cfacid);
                if (fac != null) {
                    String facid = fac.getId();
                    if (facid != null) {
                        PoiData poi = DualMapNavUtil.getFacilityDestination(facid);
                        if (poi != null) {
                            String tmp = poi.getpoiDescription();
                            if (tmp != null && !tmp.isEmpty()) {
                                String exit = tmp;
                                result += " " + exit;
                            }
                        }
                    }
                }
            } else if (type == Instruction.TYPE_DESTINATION) {
                result = instruction.toString();
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    public void setNavViewListener(SpreoNavViewListener navViewListener) {
        this.navViewListener = navViewListener;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /// ..
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setOutlineProvider(new SpreoStaticNavView.CustomOutline(w, h));
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
