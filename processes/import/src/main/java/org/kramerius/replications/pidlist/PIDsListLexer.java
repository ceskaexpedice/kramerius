// $ANTLR 2.7.7 (2006-11-01): "pidslist.g" -> "PIDsListLexer.java"$

    package org.kramerius.replications.pidlist;
    import java.util.*;

import java.io.InputStream;
import antlr.TokenStreamException;
import antlr.TokenStreamIOException;
import antlr.TokenStreamRecognitionException;
import antlr.CharStreamException;
import antlr.CharStreamIOException;
import antlr.ANTLRException;
import java.io.Reader;
import java.util.Hashtable;
import antlr.CharScanner;
import antlr.InputBuffer;
import antlr.ByteBuffer;
import antlr.CharBuffer;
import antlr.Token;
import antlr.CommonToken;
import antlr.RecognitionException;
import antlr.NoViableAltForCharException;
import antlr.MismatchedCharException;
import antlr.TokenStream;
import antlr.ANTLRHashString;
import antlr.LexerSharedInputState;
import antlr.collections.impl.BitSet;
import antlr.SemanticException;

public class PIDsListLexer extends antlr.CharScanner implements PIDsListParserTokenTypes, TokenStream
 {
public PIDsListLexer(InputStream in) {
    this(new ByteBuffer(in));
}
public PIDsListLexer(Reader in) {
    this(new CharBuffer(in));
}
public PIDsListLexer(InputBuffer ib) {
    this(new LexerSharedInputState(ib));
}
public PIDsListLexer(LexerSharedInputState state) {
    super(state);
    caseSensitiveLiterals = true;
    setCaseSensitive(true);
    literals = new Hashtable();
}

public Token nextToken() throws TokenStreamException {
    Token theRetToken=null;
tryAgain:
    for (;;) {
        Token _token = null;
        int _ttype = Token.INVALID_TYPE;
        resetText();
        try {   // for char stream error handling
            try {   // for lexical error handling
                switch ( LA(1)) {
                case '(':
                {
                    mL_BRACKET(true);
                    theRetToken=_returnToken;
                    break;
                }
                case ')':
                {
                    mR_BRACKET(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '{':
                {
                    mCURLYL_BRACKET(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '}':
                {
                    mCURLYR_BRACKET(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '[':
                {
                    mARRAYL_BRACKET(true);
                    theRetToken=_returnToken;
                    break;
                }
                case ']':
                {
                    mARRAYR_BRACKET(true);
                    theRetToken=_returnToken;
                    break;
                }
                case ',':
                {
                    mCOMMA(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '=':
                {
                    mEQUAL(true);
                    theRetToken=_returnToken;
                    break;
                }
                case ':':
                {
                    mDOUBLEDOT(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '"':  case '\'':
                {
                    mSTRING_LITERAL(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '\n':  case '\r':
                {
                    mNEWLINE(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '\t':  case ' ':
                {
                    mWS(true);
                    theRetToken=_returnToken;
                    break;
                }
                default:
                    if ((_tokenSet_0.member(LA(1)))) {
                        mIDENT(true);
                        theRetToken=_returnToken;
                    }
                else {
                    if (LA(1)==EOF_CHAR) {uponEOF(); _returnToken = makeToken(Token.EOF_TYPE);}
                else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
                }
                }
                if ( _returnToken==null ) continue tryAgain; // found SKIP token
                _ttype = _returnToken.getType();
                _ttype = testLiteralsTable(_ttype);
                _returnToken.setType(_ttype);
                return _returnToken;
            }
            catch (RecognitionException e) {
                throw new TokenStreamRecognitionException(e);
            }
        }
        catch (CharStreamException cse) {
            if ( cse instanceof CharStreamIOException ) {
                throw new TokenStreamIOException(((CharStreamIOException)cse).io);
            }
            else {
                throw new TokenStreamException(cse.getMessage());
            }
        }
    }
}

    public final void mL_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = L_BRACKET;
        int _saveIndex;
        
        match('(');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mR_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = R_BRACKET;
        int _saveIndex;
        
        match(')');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mCURLYL_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CURLYL_BRACKET;
        int _saveIndex;
        
        match('{');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mCURLYR_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = CURLYR_BRACKET;
        int _saveIndex;
        
        match('}');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mARRAYL_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ARRAYL_BRACKET;
        int _saveIndex;
        
        match('[');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mARRAYR_BRACKET(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ARRAYR_BRACKET;
        int _saveIndex;
        
        match(']');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mCOMMA(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = COMMA;
        int _saveIndex;
        
        match(',');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mEQUAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = EQUAL;
        int _saveIndex;
        
        match('=');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mDOUBLEDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = DOUBLEDOT;
        int _saveIndex;
        
        match(':');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mIDENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = IDENT;
        int _saveIndex;
        
        {
        int _cnt28=0;
        _loop28:
        do {
            if ((_tokenSet_0.member(LA(1)))) {
                {
                match(_tokenSet_0);
                }
            }
            else {
                if ( _cnt28>=1 ) { break _loop28; } else {throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());}
            }
            
            _cnt28++;
        } while (true);
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mSTRING_LITERAL(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = STRING_LITERAL;
        int _saveIndex;
        
        switch ( LA(1)) {
        case '"':
        {
            match('"');
            {
            _loop32:
            do {
                if ((_tokenSet_1.member(LA(1)))) {
                    {
                    match(_tokenSet_1);
                    }
                }
                else {
                    break _loop32;
                }
                
            } while (true);
            }
            match('"');
            break;
        }
        case '\'':
        {
            match('\'');
            {
            _loop35:
            do {
                if ((_tokenSet_1.member(LA(1)))) {
                    {
                    match(_tokenSet_1);
                    }
                }
                else {
                    break _loop35;
                }
                
            } while (true);
            }
            match('\'');
            break;
        }
        default:
        {
            throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
        }
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    protected final void mSL_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = SL_COMMENT;
        int _saveIndex;
        
        match("//");
        {
        _loop39:
        do {
            if ((_tokenSet_2.member(LA(1)))) {
                {
                match(_tokenSet_2);
                }
            }
            else {
                break _loop39;
            }
            
        } while (true);
        }
        {
        switch ( LA(1)) {
        case '\n':
        {
            match('\n');
            break;
        }
        case '\r':
        {
            match('\r');
            {
            if ((LA(1)=='\n')) {
                match('\n');
            }
            else {
            }
            
            }
            break;
        }
        default:
            {
            }
        }
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    protected final void mML_COMMENT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = ML_COMMENT;
        int _saveIndex;
        
        match("/*");
        {
        _loop45:
        do {
            if (((LA(1)=='*'))&&( LA(2)!='/' )) {
                match('*');
            }
            else if ((LA(1)=='\r')) {
                match('\r');
                match('\n');
                if ( inputState.guessing==0 ) {
                    newline();
                }
            }
            else if ((LA(1)=='\r')) {
                match('\r');
                if ( inputState.guessing==0 ) {
                    newline();
                }
            }
            else if ((LA(1)=='\n')) {
                match('\n');
                if ( inputState.guessing==0 ) {
                    newline();
                }
            }
            else if ((_tokenSet_3.member(LA(1)))) {
                {
                match(_tokenSet_3);
                }
            }
            else {
                break _loop45;
            }
            
        } while (true);
        }
        match("*/");
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mNEWLINE(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = NEWLINE;
        int _saveIndex;
        
        boolean synPredMatched48 = false;
        if (((LA(1)=='\r'))) {
            int _m48 = mark();
            synPredMatched48 = true;
            inputState.guessing++;
            try {
                {
                match('\r');
                match('\n');
                }
            }
            catch (RecognitionException pe) {
                synPredMatched48 = false;
            }
            rewind(_m48);
inputState.guessing--;
        }
        if ( synPredMatched48 ) {
            match('\r');
            match('\n');
        }
        else if ((LA(1)=='\r')) {
            match('\r');
        }
        else if ((LA(1)=='\n')) {
            match('\n');
            if ( inputState.guessing==0 ) {
                newline();  _ttype = Token.SKIP;
            }
        }
        else {
            throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
        }
        
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mWS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = WS;
        int _saveIndex;
        
        {
        switch ( LA(1)) {
        case ' ':
        {
            match(' ');
            break;
        }
        case '\t':
        {
            match('\t');
            break;
        }
        default:
        {
            throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
        }
        }
        }
        if ( inputState.guessing==0 ) {
            _ttype = Token.SKIP;
        }
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    
    private static final long[] mk_tokenSet_0() {
        long[] data = new long[1024];
        data[0]=-2594235584805348872L;
        data[1]=-2882303762188206081L;
        for (int i = 2; i<=510; i++) { data[i]=-1L; }
        data[511]=9223372036854775807L;
        return data;
    }
    public static final BitSet _tokenSet_0 = new BitSet(mk_tokenSet_0());
    private static final long[] mk_tokenSet_1() {
        long[] data = new long[1024];
        data[0]=-566935692296L;
        data[1]=-268435457L;
        for (int i = 2; i<=510; i++) { data[i]=-1L; }
        data[511]=9223372036854775807L;
        return data;
    }
    public static final BitSet _tokenSet_1 = new BitSet(mk_tokenSet_1());
    private static final long[] mk_tokenSet_2() {
        long[] data = new long[1024];
        data[0]=-9224L;
        for (int i = 1; i<=510; i++) { data[i]=-1L; }
        data[511]=9223372036854775807L;
        return data;
    }
    public static final BitSet _tokenSet_2 = new BitSet(mk_tokenSet_2());
    private static final long[] mk_tokenSet_3() {
        long[] data = new long[1024];
        data[0]=-4398046520328L;
        for (int i = 1; i<=510; i++) { data[i]=-1L; }
        data[511]=9223372036854775807L;
        return data;
    }
    public static final BitSet _tokenSet_3 = new BitSet(mk_tokenSet_3());
    
    }
