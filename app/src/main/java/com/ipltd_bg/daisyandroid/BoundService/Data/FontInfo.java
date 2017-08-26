package com.ipltd_bg.daisyandroid.BoundService.Data;

import com.ipltd_bg.daisyandroid.Enums.ParagraphAlignment;

/**
 * Created by alkon on 26-Aug-17.
 */

public class FontInfo {
    private boolean big;//Двоен шрифт
    private boolean bold;//болд
    private boolean underline;//подчертан

    public boolean isBig() {
        return big;
    }

    public void setBig(boolean big) {
        this.big = big;
    }

    public boolean isBold() {
        return bold;
    }

    public void setBold(boolean bold) {
        this.bold = bold;
    }

    public boolean isUnderline() {
        return underline;
    }

    public void setUnderline(boolean underline) {
        this.underline = underline;
    }

}
