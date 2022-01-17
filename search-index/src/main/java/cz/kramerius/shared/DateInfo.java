package cz.kramerius.shared;

import java.util.Date;

public class DateInfo {
    public String value;
    public String valueStart;
    public String valueEnd;

    //rozsah datumu, kam urcite spada, i kdyz to nemusi byt presne
    //napr. pro hodnotu 1947 tu bude 1.1.1947,00:00:00:001-31.12.1947,23:59:59:999
    public Date dateMin;
    public Date dateMax;

    //range-start
    public Integer rangeStartYear;
    public Integer rangeStartMonth;
    public Integer rangeStartDay;

    //range-end
    public Integer rangeEndYear;
    public Integer rangeEndMonth;
    public Integer rangeEndDay;

    //point
    public Integer instantYear;
    public Integer instantMonth;
    public Integer instantDay;

    public DateInfo() {
    }

    public DateInfo(DateInfo from) {
        this.value = from.value;
        this.valueStart = from.valueStart;
        this.valueEnd = from.valueEnd;
        this.dateMin = from.dateMin;
        this.dateMax = from.dateMax;
        this.rangeStartYear = from.rangeStartYear;
        this.rangeStartMonth = from.rangeStartMonth;
        this.rangeStartDay = from.rangeStartDay;
        this.rangeEndYear = from.rangeEndYear;
        this.rangeEndMonth = from.rangeEndMonth;
        this.rangeEndDay = from.rangeEndDay;
        this.instantYear = from.instantYear;
        this.instantMonth = from.instantMonth;
        this.instantDay = from.instantDay;
    }

    @Override
    public String toString() {
        return "DateInfo{" +
                "value='" + value + '\'' +
                ", valueStart='" + valueStart + '\'' +
                ", valueEnd='" + valueEnd + '\'' +
                ", dateMin=" + dateMin +
                ", dateMax=" + dateMax +
                ", rangeStartYear=" + rangeStartYear +
                ", rangeStartMonth=" + rangeStartMonth +
                ", rangeStartDay=" + rangeStartDay +
                ", rangeEndYear=" + rangeEndYear +
                ", rangeEndMonth=" + rangeEndMonth +
                ", rangeEndDay=" + rangeEndDay +
                ", instantYear=" + instantYear +
                ", instantMonth=" + instantMonth +
                ", instantDay=" + instantDay +
                '}';
    }

    public boolean isInstant() {
        if (rangeStartYear != null && rangeEndYear != null && !rangeStartYear.equals(rangeEndYear)) {
            return false; //start- and end- years differ
        }
        if ((rangeStartYear == null && rangeEndYear != null) || (rangeStartYear != null && rangeEndYear == null)) {
            return false; //one of start-/end- years present, other not
        }
        if (rangeStartMonth != null && rangeEndMonth != null && !rangeStartMonth.equals(rangeEndMonth)) {
            return false; //start- and end- months differ
        }
        if ((rangeStartMonth == null && rangeEndMonth != null) || (rangeStartMonth != null && rangeEndMonth == null)) {
            return false; //one of start-/end- months present, other not
        }
        if (rangeStartDay != null && rangeEndDay != null && !rangeStartDay.equals(rangeEndDay)) {
            return false; //start- and end- days differ
        }
        if ((rangeStartDay == null && rangeEndDay != null) || (rangeStartDay != null && rangeEndDay == null)) {
            return false; //one of start-/end- days present, other not
        }
        return true;
    }

    public void setStart(int day, int month, int year) {
        this.rangeStartDay = day;
        this.rangeStartMonth = month;
        this.rangeStartYear = year;
    }

    public void setStart(int month, int year) {
        this.rangeStartMonth = month;
        this.rangeStartYear = year;
    }

    public void setEnd(int day, int month, int year) {
        this.rangeEndDay = day;
        this.rangeEndMonth = month;
        this.rangeEndYear = year;
    }

    public void setEnd(int month, int year) {
        this.rangeEndMonth = month;
        this.rangeEndYear = year;
    }

    public void updateInstantData() {
        if (rangeStartYear != null & rangeEndYear != null && rangeStartYear.equals(rangeEndYear)) {
            instantYear = rangeStartYear;
        }
        if (rangeStartMonth != null & rangeEndMonth != null && rangeStartMonth.equals(rangeEndMonth)) {
            instantMonth = rangeStartMonth;
        }
        if (rangeStartDay != null & rangeEndDay != null && rangeStartDay.equals(rangeEndDay)) {
            instantDay = rangeStartDay;
        }
    }

    public boolean isEmpty() {
        return value == null && valueStart == null && valueEnd == null;
    }
}
