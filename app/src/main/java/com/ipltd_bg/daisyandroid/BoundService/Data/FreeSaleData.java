package com.ipltd_bg.daisyandroid.BoundService.Data;

import java.util.Arrays;

/**
 * Created by alkon on 13-Apr-17.
 */

//Данни за свободна продажба
public class FreeSaleData {

    //Текст 1 за реда
    private String Text1;

    //Текст 2 за реда
    private String Text2;

    //Данъчна група
    private char TaxGr;

    //Знак + или -
    private char Sign;

    //Цена
    private double Price;

    //Количество
    private double QTY;

    //Процент отстъпка
    private double Percent;

    //Нетна отстъпка
    private double Netto;

    public FreeSaleData() {
        this.Text1 = "";
        this.Text2 = "";
        this.TaxGr = 'Б';
        this.Sign = '+';
        this.Price = 0;
        this.QTY = 0;
        this.Percent = 0;
        this.Netto = 0;
    }

    public String getText1() {
        return Text1;
    }

    public void setText1(String text1) {
        Text1 = text1;
    }

    public String getText2() {
        return Text2;
    }

    public void setText2(String text2) {
        Text2 = text2;
    }

    public char getTaxGr() {


        return TaxGr;
    }

    public void setTaxGr(char taxGr) {

        if (Arrays.asList('А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З').contains(taxGr))
            TaxGr = taxGr;
    }

    public char getSign() {
        return Sign;
    }

    public void setSign(char sign) {
        if (Arrays.asList('+', '-').contains(sign))
            Sign = sign;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {
        Price = price;
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


}
