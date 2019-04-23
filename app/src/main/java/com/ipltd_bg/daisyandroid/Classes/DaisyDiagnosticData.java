package com.ipltd_bg.daisyandroid.Classes;

public class DaisyDiagnosticData {
    private String FirmwareRev;// Версия на програмното осигуряване (4 символа).

    private String FirmwareDate;// Датата на програмното осигуряване DDMМYY (6 байта).

    private String FirmwareTime;// Час на програмното осигуряване HHMM (4 байта).
    private String ChekSum;// Контролна сума на EPROM (4 байта низ в шестнайсетичен вид).
    private String Sw;// Състояние на ключетата на ФУ(4 цифри).
    private String Country;// Номер на страната (1 байт), за България = 6
    private String SerNum;// Инд. номер на ФУ (#MACHNO_LEN# символа).
    private String FМ;// Номер на фискалния модул (#FMNO_LEN# символа).

    public String getFirmwareRev() {
        return FirmwareRev;
    }

    public void setFirmwareRev(String firmwareRev) {
        FirmwareRev = firmwareRev;
    }

    public String getFirmwareDate() {
        return FirmwareDate;
    }

    public void setFirmwareDate(String firmwareDate) {
        FirmwareDate = firmwareDate;
    }

    public String getFirmwareTime() {
        return FirmwareTime;
    }

    public void setFirmwareTime(String firmwareTime) {
        FirmwareTime = firmwareTime;
    }

    public String getChekSum() {
        return ChekSum;
    }

    public void setChekSum(String chekSum) {
        ChekSum = chekSum;
    }

    public String getSw() {
        return Sw;
    }

    public void setSw(String sw) {
        Sw = sw;
    }

    public String getCountry() {
        return Country;
    }

    public void setCountry(String country) {
        Country = country;
    }

    public String getSerNum() {
        return SerNum;
    }

    public void setSerNum(String serNum) {
        SerNum = serNum;
    }

    public String getFМ() {
        return FМ;
    }

    public void setFМ(String FМ) {
        this.FМ = FМ;
    }
}
