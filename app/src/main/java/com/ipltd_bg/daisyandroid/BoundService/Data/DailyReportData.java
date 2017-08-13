package com.ipltd_bg.daisyandroid.BoundService.Data;

/**
 * Created by alkon on 13-Apr-17.
 */

//Данни за формат на дневния отчет
public class DailyReportData {
    //Незадължителен параметър, указващ вида на отчета. Ако не е въведено се приема = “0”.
    private int Item;

    //Незадължителен параметър. Ако е подадена стойност true, при изпълнение на дневен финансов Z отчет не се нулира информацията по оператори.
    private boolean Option;

    public DailyReportData() {
        this.Item = 0;
        this.Option = true;
    }

    public int getItem() {
        return Item;
    }

    public void setItem(int item) {

        if (item == 0 || item == 1)
            Item = item;
    }

    public boolean getOption() {
        return Option;
    }

    public void setOption(boolean option) {
        Option = option;
    }
}
