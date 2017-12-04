// $ANTLR 2.7.7 (2006-11-01): "mwdates.g" -> "DateLexer.java"$

    package cz.incad.kramerius.security.impl.criteria.mw;
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
//BEGIN GENERATED CODE
public class DateLexer extends antlr.CharScanner implements DatesParserTokenTypes, TokenStream
 {
public DateLexer(InputStream in) {
    this(new ByteBuffer(in));
}
public DateLexer(Reader in) {
    this(new CharBuffer(in));
}
public DateLexer(InputBuffer ib) {
    this(new LexerSharedInputState(ib));
}
public DateLexer(LexerSharedInputState state) {
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
                case '-':
                {
                    mMINUS(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '0':  case '1':  case '2':  case '3':
                case '4':  case '5':  case '6':  case '7':
                case '8':  case '9':
                {
                    mDIGIT(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '.':
                {
                    mDOT(true);
                    theRetToken=_returnToken;
                    break;
                }
                case '\t':  case '\n':  case '\r':  case ' ':
                {
                    mWS(true);
                    theRetToken=_returnToken;
                    break;
                }
                default:
                {
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

    public final void mMINUS(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = MINUS;
        int _saveIndex;
        
        match('-');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mDIGIT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = DIGIT;
        int _saveIndex;
        
        matchRange('0','9');
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    public final void mDOT(boolean _createToken) throws RecognitionException, CharStreamException, TokenStreamException {
        int _ttype; Token _token=null; int _begin=text.length();
        _ttype = DOT;
        int _saveIndex;
        
        match('.');
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
        case '\n':
        {
            match('\n');
            newline();
            break;
        }
        case '\r':
        {
            match('\r');
            break;
        }
        default:
        {
            throw new NoViableAltForCharException((char)LA(1), getFilename(), getLine(), getColumn());
        }
        }
        }
        _ttype = Token.SKIP;
        if ( _createToken && _token==null && _ttype!=Token.SKIP ) {
            _token = makeToken(_ttype);
            _token.setText(new String(text.getBuffer(), _begin, text.length()-_begin));
        }
        _returnToken = _token;
    }
    
    
    
    }
//END GENERATED CODE