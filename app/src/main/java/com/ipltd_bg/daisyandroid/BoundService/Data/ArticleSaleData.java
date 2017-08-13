package com.ipltd_bg.daisyandroid.BoundService.Data;

import java.util.Arrays;

/**
 * Created by alkon on 13-Apr-17.
 */

//Данни за продажба на артикул
public class ArticleSaleData {
    //Знак + или -
    private char Sign;

    //Пореден номер на артикул
    private int PLU;

    //Количество
    private double QTY;

    //Процент отстъпка
    private double Percent;

    //Нетна отстъпка
    private double Netto;

    //Цена
    private double Price;

    public ArticleSaleData() {
        this.Sign = '+';
        this.PLU = 1;
        this.QTY = 1;
        this.Percent = 0;
        this.Netto = 0;
        this.Price = 0;
    }


    public char getSign() {
        return Sign;
    }

    public void setSign(char sign) {

        if (Arrays.asList('+', '-').contains(sign))

            Sign = sign;
    }

    public int getPLU() {
        return PLU;
    }

    public void setPLU(int PLU) {
        if (PLU > 0)
            this.PLU = PLU;
    }

    public double getQTY() {
        return QTY;
    }

    public void setQTY(double QTY) {
        if (QTY > 0)
            this.QTY = QTY;
    }

    public double getPercent() {
        return Percent;
    }

    public void setPercent(double percent) {
        if (percent >= 0) {
            if (percent > 0)
                setNetto(0);
            Percent = percent;
        }
    }

    public double getNetto() {
        return Netto;
    }

    public void setNetto(double netto) {
        if (netto >= 0) {
            if (netto > 0)
                setPercent(0);
            Netto = netto;
        }

    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        if (price >= 0)
            Price = price;
    }
}
