package com.ipltd_bg.daisyandroid.BoundService.Data;

/**
 * Created by alkon on 13-Apr-17.
 */

//Данни за начало на продажба
public class StartFiskData {
    private String UniqueSellingNumber;

    //Оператор номер
    private int ClerkNum;

    //Оператор - парола
    private String Password;

    //Флаг за фактура
    private int Mode;//0-нормално, 1-invoice, 2 - сторно

    private int Reason;//0 = ВРЪЩАНЕ / РЕКЛАМАЦИЯ 1 = ОПЕРАТОРСКА ГРЕШКА 2 = НАМАЛЯВАНЕ НА ДАН. ОСНОВА


    private String DocLink;//номер на ФБ, по който се прави сторно
    private String DocDT;//{DD–MM–YY}{space}{HH:mm[:SS]}
    private String FiskMem;//Номер на устройство

    public StartFiskData() {
        this.ClerkNum = 0;
        this.Password = "";
        this.setMode(0);
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


    public String getUniqueSellingNumber() {
        return UniqueSellingNumber;
    }

    public void setUniqueSellingNumber(String uniqueSellingNumber) {
        UniqueSellingNumber = uniqueSellingNumber;
    }

    public int getMode() {
        return Mode;
    }

    public void setMode(int mode) {
        Mode = mode;
    }

    public int getReason() {
        return Reason;
    }

    public void setReason(int reason) {
        Reason = reason;
    }

    public String getDocLink() {
        return DocLink;
    }

    public void setDocLink(String docLink) {
        DocLink = docLink;
    }

    public String getDocDT() {
        return DocDT;
    }

    public void setDocDT(String docDT) {
        DocDT = docDT;
    }

    public String getFiskMem() {
        return FiskMem;
    }

    public void setFiskMem(String fiskMem) {
        FiskMem = fiskMem;
    }
}
