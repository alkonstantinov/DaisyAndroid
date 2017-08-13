package com.ipltd_bg.daisyandroid.BoundService.Data;

/**
 * Created by alkon on 13-Apr-17.
 */

//Данни за начало на продажба
public class StartFiskData {
    //Оператор номер
    private int ClerkNum;

    //Оператор - парола
    private String Password;

    //Флаг за фактура
    private boolean Invoice;

    public StartFiskData() {
        this.ClerkNum = 0;
        this.Password = "";
        this.Invoice = false;
    }


    public int getClerkNum() {
        return ClerkNum;
    }

    public void setClerkNum(int clerkNum) {
        if (clerkNum > 0)
            ClerkNum = clerkNum;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }

    public boolean getInvoice() {
        return Invoice;
    }

    public void setInvoice(boolean invoice) {
        Invoice = invoice;
    }
}
