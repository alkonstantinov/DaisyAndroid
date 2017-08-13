package com.ipltd_bg.daisyandroid.BoundService.Data;

/**
 * Created by alkon on 12-Apr-17.
 */

//Данни за оператопр
public class OperatorData {

    //Име на оператор
    private String OperatorName;

    public OperatorData()
    {
        this.OperatorName="";
        this.OperatorNumber = "1";
        this.Password="1";
    }

    public String getOperatorName() {
        return OperatorName;
    }

    public void setOperatorName(String operatorName) {
        OperatorName = operatorName;
    }

    //Пореден номер на оператор
    private String OperatorNumber;

    public String getOperatorNumber() {
        return OperatorNumber;
    }

    public void setOperatorNumber(String operatorNumber) {
        OperatorNumber = operatorNumber;
    }

    //Парола на оператор
    private String Password;

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }


}
