package com.reactlibrary;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.spreo.nav.interfaces.INavInstruction;

import java.util.List;



public class SpreoInstrcutionsListAdapter extends ArrayAdapter<INavInstruction> {
    Context context;
    List<INavInstruction> instructions;
    private String lastInstruction = null;


    public SpreoInstrcutionsListAdapter(Context context, int resource, List<INavInstruction> objects) {
        super(context, resource, objects);
        this.context = context;
        instructions = objects;
    }


    public View getView(final int position, View convertView, ViewGroup parent) {


        ViewHolder holder = null;
        INavInstruction current = getItem(position);

        LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.spreo_instruction_item, null);
            holder = new ViewHolder();
            holder.insIcon = (ImageView) convertView.findViewById(R.id.instructionIcon);
            holder.insText = (TextView) convertView.findViewById(R.id.instructionText);


            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();

        String txt = current.getText();
        if (txt != null && !txt.isEmpty()) {
            holder.insText.setText(txt);
        }

        Bitmap bm = current.getSignBitmap();
        if (bm != null) {
            holder.insIcon.setImageBitmap(bm);
        }
        return convertView;
    }

    @Override
    public INavInstruction getItem(int position) {
        return instructions.get(position);
    }


    private class ViewHolder {
        ImageView insIcon;
        TextView insText;
    }



}
