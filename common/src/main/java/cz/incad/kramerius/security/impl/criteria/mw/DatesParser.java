// $ANTLR 2.7.7 (2006-11-01): "mwdates.g" -> "DatesParser.java"$

    package cz.incad.kramerius.security.impl.criteria.mw;
    import java.util.*;

import antlr.TokenBuffer;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.ANTLRException;
import antlr.LLkParser;
import antlr.Token;
import antlr.TokenStream;
import antlr.RecognitionException;
import antlr.NoViableAltException;
import antlr.MismatchedTokenException;
import antlr.SemanticException;
import antlr.ParserSharedInputState;
import antlr.collections.impl.BitSet;
//BEGIN GENERATED CODE
/**
 * Moving wall dates grammar
 */
public class DatesParser extends antlr.LLkParser       implements DatesParserTokenTypes
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


protected DatesParser(TokenBuffer tokenBuf, int k) {
  super(tokenBuf,k);
  tokenNames = _tokenNames;
}

public DatesParser(TokenBuffer tokenBuf) {
  this(tokenBuf,1);
}

protected DatesParser(TokenStream lexer, int k) {
  super(lexer,k);
  tokenNames = _tokenNames;
}

public DatesParser(TokenStream lexer) {
  this(lexer,1);
}

public DatesParser(ParserSharedInputState state) {
  super(state,1);
  tokenNames = _tokenNames;
}

/** definuje mozne patterny datumu */
    public final Date  dates() throws RecognitionException, TokenStreamException {
        Date d;
        
        d=null; Date rd=null,dd=null,rm=null,m=null; int ry=0,y=0;
        
        boolean synPredMatched3 = false;
        if (((LA(1)==DIGIT))) {
            int _m3 = mark();
            synPredMatched3 = true;
            inputState.guessing++;
            try {
                {
                rangeday();
                }
            }
            catch (RecognitionException pe) {
                synPredMatched3 = false;
            }
            rewind(_m3);
inputState.guessing--;
        }
        if ( synPredMatched3 ) {
            rd=rangeday();
            if ( inputState.guessing==0 ) {
                d=rd;
            }
        }
        else {
            boolean synPredMatched5 = false;
            if (((LA(1)==DIGIT))) {
                int _m5 = mark();
                synPredMatched5 = true;
                inputState.guessing++;
                try {
                    {
                    day();
                    }
                }
                catch (RecognitionException pe) {
                    synPredMatched5 = false;
                }
                rewind(_m5);
inputState.guessing--;
            }
            if ( synPredMatched5 ) {
                dd=day();
                if ( inputState.guessing==0 ) {
                    d=dd;
                }
            }
            else {
                boolean synPredMatched7 = false;
                if (((LA(1)==DIGIT))) {
                    int _m7 = mark();
                    synPredMatched7 = true;
                    inputState.guessing++;
                    try {
                        {
                        rangemonth();
                        }
                    }
                    catch (RecognitionException pe) {
                        synPredMatched7 = false;
                    }
                    rewind(_m7);
inputState.guessing--;
                }
                if ( synPredMatched7 ) {
                    rm=rangemonth();
                    if ( inputState.guessing==0 ) {
                        d=rm;
                    }
                }
                else {
                    boolean synPredMatched9 = false;
                    if (((LA(1)==DIGIT))) {
                        int _m9 = mark();
                        synPredMatched9 = true;
                        inputState.guessing++;
                        try {
                            {
                            month();
                            }
                        }
                        catch (RecognitionException pe) {
                            synPredMatched9 = false;
                        }
                        rewind(_m9);
inputState.guessing--;
                    }
                    if ( synPredMatched9 ) {
                        m=month();
                        if ( inputState.guessing==0 ) {
                            d = m;
                        }
                    }
                    else {
                        boolean synPredMatched11 = false;
                        if (((LA(1)==DIGIT))) {
                            int _m11 = mark();
                            synPredMatched11 = true;
                            inputState.guessing++;
                            try {
                                {
                                rangeyear();
                                }
                            }
                            catch (RecognitionException pe) {
                                synPredMatched11 = false;
                            }
                            rewind(_m11);
inputState.guessing--;
                        }
                        if ( synPredMatched11 ) {
                            ry=rangeyear();
                            if ( inputState.guessing==0 ) {
                                d=yearToDate(ry);
                            }
                        }
                        else if ((LA(1)==DIGIT)) {
                            y=year();
                            if ( inputState.guessing==0 ) {
                                d=yearToDate(y);
                            }
                            match(Token.EOF_TYPE);
                        }
                        else {
                            throw new NoViableAltException(LT(1), getFilename());
                        }
                        }}}}
                        return d;
                    }
                    
/** Zpracuje pattern dd.-dd.mm.yyyy */
    public final Date  rangeday() throws RecognitionException, TokenStreamException {
        Date d;
        
        d=null;int dd=0,m=0,y=0;
        
        dd=rangedaymonth();
        m=daymonthpat();
        y=year();
        if ( inputState.guessing==0 ) {
            d=dayToDate(dd,m,y);
        }
        return d;
    }
    
/** Zpracuje pattern dd.mm.yyyy */
    public final Date  day() throws RecognitionException, TokenStreamException {
        Date d;
        
        d=null;int dd=0,m=0,y=0;
        
        dd=daymonthpat();
        m=daymonthpat();
        y=year();
        if ( inputState.guessing==0 ) {
            d=dayToDate(dd,m,y);
        }
        return d;
    }
    
/** Zpracuje pattern mm.-mm.yyyy */
    public final Date  rangemonth() throws RecognitionException, TokenStreamException {
        Date d;
        
        d=null;int sm=0,y=0;
        
        sm=rangedaymonth();
        y=year();
        if ( inputState.guessing==0 ) {
            d=monthToDate(sm,y);
        }
        return d;
    }
    
/** Zpracuje pattern mm.yyyy */
    public final Date  month() throws RecognitionException, TokenStreamException {
        Date d;
        
        d=null;int m=0,y=0;
        
        m=daymonthpat();
        y=year();
        if ( inputState.guessing==0 ) {
            d=monthToDate(m,y);
        }
        return d;
    }
    
/** Zpracuje yyyy - yyyy */
    public final int  rangeyear() throws RecognitionException, TokenStreamException {
        int y;
        
        y=0;int f=0,s=0;
        
        f=year();
        match(MINUS);
        s=year();
        if ( inputState.guessing==0 ) {
            y=s;
        }
        return y;
    }
    
/** Zpracuje yyyy */
    public final int  year() throws RecognitionException, TokenStreamException {
        int y;
        
        Token  d1 = null;
        Token  d2 = null;
        Token  d3 = null;
        Token  d4 = null;
        y=0;
        
        d1 = LT(1);
        match(DIGIT);
        d2 = LT(1);
        match(DIGIT);
        d3 = LT(1);
        match(DIGIT);
        d4 = LT(1);
        match(DIGIT);
        if ( inputState.guessing==0 ) {
            y=Integer.parseInt(d1.getText())*1000 + Integer.parseInt(d2.getText())*100+Integer.parseInt(d3.getText())*10+Integer.parseInt(d4.getText());
        }
        return y;
    }
    
    public final int  rangedaymonth() throws RecognitionException, TokenStreamException {
        int m;
        
        m=0;int f=0,s=0;
        
        f=daymonthpat();
        match(MINUS);
        s=daymonthpat();
        if ( inputState.guessing==0 ) {
            m=s;
        }
        return m;
    }
    
    public final int  daymonthpat() throws RecognitionException, TokenStreamException {
        int val;
        
        Token  d1 = null;
        Token  d2 = null;
        Token  d3 = null;
        val=0;
        
        boolean synPredMatched18 = false;
        if (((LA(1)==DIGIT))) {
            int _m18 = mark();
            synPredMatched18 = true;
            inputState.guessing++;
            try {
                {
                match(DIGIT);
                match(DIGIT);
                match(DOT);
                }
            }
            catch (RecognitionException pe) {
                synPredMatched18 = false;
            }
            rewind(_m18);
inputState.guessing--;
        }
        if ( synPredMatched18 ) {
            d1 = LT(1);
            match(DIGIT);
            d2 = LT(1);
            match(DIGIT);
            match(DOT);
            if ( inputState.guessing==0 ) {
                val=Integer.parseInt(d1.getText()) * 10; val+= Integer.parseInt(d2.getText());
            }
        }
        else if ((LA(1)==DIGIT)) {
            d3 = LT(1);
            match(DIGIT);
            match(DOT);
            if ( inputState.guessing==0 ) {
                val= Integer.parseInt(d3.getText());
            }
        }
        else {
            throw new NoViableAltException(LT(1), getFilename());
        }
        
        return val;
    }
    
    
    public static final String[] _tokenNames = {
        "<0>",
        "EOF",
        "<2>",
        "NULL_TREE_LOOKAHEAD",
        "DIGIT",
        "DOT",
        "MINUS",
        "white space"
    };
    
    
    }
//END GENERATED CODE