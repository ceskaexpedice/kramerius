/*
 * Copyright (C) 2012 Pavel Stastny
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

header {
    package cz.incad.kramerius.security.impl.criteria.mw;
    import java.util.*;
}  


/**
 * Moving wall dates grammar
 */
class DatesParser extends Parser;
options{
    defaultErrorHandler=false;
}
{
    public static Date yearToDate(int y) throws SemanticException {
        Calendar c = Calendar.getInstance(); c.set(Calendar.YEAR,y);
        return c.getTime();
    }

    public static Date monthToDate(int m, int y) throws SemanticException{
        checkMonth(m);
        Calendar c = Calendar.getInstance(); 
        c.set(Calendar.YEAR,y);
        c.set(Calendar.MONTH,(m-1));
        return c.getTime();
    }

    public static Date dayToDate(int d, int m, int y) throws SemanticException{
        checkMonth(m);
        Calendar c = Calendar.getInstance(); 
        c.set(Calendar.DAY_OF_MONTH,d);
        c.set(Calendar.YEAR,y);
        c.set(Calendar.MONTH,(m-1));
        return c.getTime();
    }

    public static void checkMonth(int m) throws SemanticException{
        if (m<0 || m>12) throw new SemanticException("month must be between 1 - 12");
    }    

}
/** definuje mozne patterny datumu */
dates returns[Date d] {d=null; Date rd=null,dd=null,rm=null,m=null; int ry=0,y=0;}
     : (rangeday) => rd=rangeday {d=rd;} | // rozsah dni 
       (day) => dd=day {d=dd;} | // kokretni den
       (rangemonth) => rm=rangemonth {d=rm;} | // rozsah mesicu 
       (month) => m = month {d = m; }  | // konkretni mesic 
       (rangeyear) => ry = rangeyear {d=yearToDate(ry);} | // rozsah let
       y=year {  d=yearToDate(y); } // konkretni rok
        
       EOF
       ; 



/** Zpracuje pattern dd.-dd.mm.yyyy */
rangeday returns[Date d] {d=null;int dd=0,m=0,y=0;}: dd=rangedaymonth m=daymonthpat y=year {d=dayToDate(dd,m,y);};

/** Zpracuje pattern mm.-mm.yyyy */
rangemonth returns[Date d] {d=null;int sm=0,y=0;}: sm=rangedaymonth  y=year {d=monthToDate(sm,y);};

/** Zpracuje pattern dd.mm.yyyy */
day returns[Date d] {d=null;int dd=0,m=0,y=0;}: dd=daymonthpat m=daymonthpat y=year {d=dayToDate(dd,m,y);};

/** Zpracuje pattern mm.yyyy */
month returns[Date d] {d=null;int m=0,y=0;}: m=daymonthpat y=year { d=monthToDate(m,y); };

// pravidlo vv. | v. -> dny mesice 
daymonthpat returns[int val]{val=0;}: (DIGIT DIGIT DOT) => d1:DIGIT d2:DIGIT DOT { val=Integer.parseInt(d1.getText()) * 10; val+= Integer.parseInt(d2.getText());  } |  d3:DIGIT DOT! { val= Integer.parseInt(d3.getText());  };

// pravidlo vv. - vv.
rangedaymonth returns[int m] {m=0;int f=0,s=0;}: f=daymonthpat MINUS s=daymonthpat { m=s; };

/** Zpracuje yyyy - yyyy */
rangeyear returns [int y]{y=0;int f=0,s=0;}: f=year MINUS s=year { y=s; };

/** Zpracuje yyyy */
year returns [int y]{y=0;}: d1:DIGIT d2:DIGIT d3:DIGIT d4:DIGIT { y=Integer.parseInt(d1.getText())*1000 + Integer.parseInt(d2.getText())*100+Integer.parseInt(d3.getText())*10+Integer.parseInt(d4.getText());};


class DateLexer extends Lexer;


MINUS:
'-'
;

DIGIT:
'0'..'9'
;

DOT:'.'
;


WS
options {
  paraphrase = "white space";
}
        :       (' '
        |       '\t'
        |       '\n'  { newline(); }
        |       '\r')
                { $setType(Token.SKIP); }
    ;

    
