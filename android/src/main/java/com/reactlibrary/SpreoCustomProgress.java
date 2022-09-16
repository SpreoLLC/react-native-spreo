package com.reactlibrary;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SpreoCustomProgress {
//    public static SpreoCustomProgress customProgress = null;
    private Dialog mDialog;
    private ProgressBar mProgressBar;
    private TextView cancelText;
    private SpreoCancelLocationListener cancelLocationListener = null;

    public SpreoCustomProgress(SpreoCancelLocationListener listener) {
        cancelLocationListener = listener;
    }

    public void showProgress(Context context, String message, boolean cancelable) {
        mDialog = new Dialog(context);
        // no tile for the dialog
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.spreo_progress_dialog);
        mProgressBar = (ProgressBar) mDialog.findViewById(R.id.progress_bar);
        //  mProgressBar.getIndeterminateDrawable().setColorFilter(context.getResources()
        // .getColor(R.color.material_blue_gray_500), PorterDuff.Mode.SRC_IN);
        TextView progressText = (TextView) mDialog.findViewById(R.id.progress_text);
        progressText.setText("" + message);

        cancelText = (TextView) mDialog.findViewById(R.id.cancel_text);
        cancelText.setOnClickListener(cancelClick);

        progressText.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        // you can change or add this line according to your need
        mProgressBar.setIndeterminate(true);
        mDialog.setCancelable(cancelable);
        mDialog.setCanceledOnTouchOutside(cancelable);
        mDialog.show();
    }

    private View.OnClickListener cancelClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            try {
                if (cancelLocationListener != null) {
                    cancelLocationListener.locationCalculationCanceled();
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    };

    public void hideProgress() {
        if (mDialog != null) {
            mDialog.dismiss();
            mDialog = null;
        }
    }
}

