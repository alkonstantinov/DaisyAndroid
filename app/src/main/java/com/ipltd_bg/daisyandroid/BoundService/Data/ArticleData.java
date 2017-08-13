package com.ipltd_bg.daisyandroid.BoundService.Data;

import java.util.Arrays;

/**
 * Created by alkon on 13-Apr-17.
 */

//Данни за артикул
public class ArticleData {
    //Данъчна група
    private char TaxGroup;
    //Пореден номер
    private int PLUNum;

    //Цена
    private double Price;

    //Име
    private String Name;

    //Баркод
    private String Barcode;

    //Департамент
    private int Dept;

    //Стокова наличност
    private int StockQty;

    public ArticleData() {
        this.TaxGroup = 'А';
        this.PLUNum = 1;
        this.Price = 0;
        this.Name = "Артикул";
        this.Barcode = "0000000000000";
        this.Dept = 1;
        this.StockQty = 0;
    }

    public char getTaxGroup() {
        return TaxGroup;
    }

    public void setTaxGroup(char taxGroup) {
        if (Arrays.asList('А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ж', 'З').contains(taxGroup))
            TaxGroup = taxGroup;
    }

    public int getPLUNum() {
        return PLUNum;
    }

    public void setPLUNum(int PLUNum) {
        if (PLUNum > 0)
            this.PLUNum = PLUNum;
    }

    public double getPrice() {
        return Price;
    }

    public void setPrice(double price) {

        if (price > 0)
            Price = price;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getBarcode() {
        return Barcode;
    }

    public void setBarcode(String barcode) {
        Barcode = barcode;
    }

    public int getDept() {
        return Dept;
    }

    public void setDept(int dept) {
        if (dept > 0)
            Dept = dept;
    }

    public int getStockQty() {
        return StockQty;
    }

    public void setStockQty(int stockQty) {

        if (stockQty > 0)
            StockQty = stockQty;
    }
}
