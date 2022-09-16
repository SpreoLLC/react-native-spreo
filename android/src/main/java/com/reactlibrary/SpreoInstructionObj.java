package com.reactlibrary;

import android.graphics.Bitmap;

import com.spreo.nav.interfaces.INavInstruction;

public class SpreoInstructionObj implements INavInstruction {
    private String text = null;
    private String base64 = null;
    private Bitmap image = null;
    private int id = 0;
    private double distance = 0;


    public SpreoInstructionObj(int id, String text, Bitmap image, String base64,double distance) {
        this.id = id;
        this.text = text;
        this.image = image;
        this.base64 = base64;
        this.distance = distance;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setText(String s) {
        text = s;
    }

    @Override
    public Bitmap getSignBitmap() {
        return image;
    }

    public String getBase64() {
        return base64;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
    public void setBase64(String base64) {
        this.base64 = base64;
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public double getDistance() {
        return this.distance;
    }



    @Override
    public void setType(int i) {

    }

    @Override
    public int getType() {
        return 0;
    }
}
