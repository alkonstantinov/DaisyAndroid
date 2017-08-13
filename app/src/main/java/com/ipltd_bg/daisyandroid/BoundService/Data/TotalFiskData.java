package com.ipltd_bg.daisyandroid.BoundService.Data;

import java.util.Arrays;

/**
 * Created by alkon on 13-Apr-17.
 */

//Данни за отпечатване на тотал на фиш
public class TotalFiskData {
    //Текст 1
    private String Text1;

    //Текст 2
    private String Text2;

    //Вид плащане
    private char Payment;

    //Сума на плащане
    private double Ammount;

    public TotalFiskData() {
        this.Text1 = "";
        this.Text2 = "";
        this.Payment = 'P';
        this.Ammount = 0;
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

    public char getPayment() {
        return Payment;
    }

    public void setPayment(char payment) {

        if (Arrays.asList('P', 'N', 'C', 'D', 'B', 'U', 'E').contains(payment))

            Payment = payment;
    }

    public double getAmmount() {
        return Ammount;
    }

    public void setAmmount(double ammount) {
        if (ammount > 0)
            Ammount = ammount;
    }
}
