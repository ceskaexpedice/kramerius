package cz.incad.kramerius.client.resources.merge.css;

public class SimpleCssCommentsLexer {

    public static enum Type { COMMENT, NOT_COMMENT, EOI } 

    public static class Token {
        private String content;
        private Type type;
        
        public Token(String content, Type type) {
            super();
            this.content = content;
            this.type = type;
        }

        public String getContent() {
            return content;
        }
        
        public Type getType() {
            return type;
        }
        
        @Override
        public String toString() {
            return "Token [content=" + content + ", type=" + type + "]";
        }
    }
    
    
    public static class SimpleLexerException extends Exception {

        public SimpleLexerException() {
            super();
        }

        public SimpleLexerException(String message, Throwable cause) {
            super(message, cause);
        }

        public SimpleLexerException(String message) {
            super(message);
        }

        public SimpleLexerException(Throwable cause) {
            super(cause);
        }
    }
    
    private String input;
    private int point = 0;
    private int length = 0;
    
    public SimpleCssCommentsLexer(String input) {
        super();
        this.input = input;
        this.length = input.length();
    }

    protected Token readComment() throws SimpleLexerException {
        StringBuilder tokenData = new StringBuilder();
        this.match("/*");
        while (canRead() && (!look(2).equals("*/"))) {
            tokenData.append(this.consume());
        }
        this.match("*/");
        return new Token(tokenData.toString(), Type.COMMENT);
    }
    
    protected char consume() {
        char ch = this.input.charAt(this.point);
        this.point += 1;
        return ch;
    }
    
    protected Token readNotComment() {
        StringBuilder tokenData = new StringBuilder();
        while (canRead() && (!look(2).equals("/*"))) {
            tokenData.append(this.consume());
        }
        return new Token(tokenData.toString(), Type.NOT_COMMENT);
    }
    
    protected void match(String expected) throws SimpleLexerException {
        if (this.point + expected.length() >= this.length) {
            throw new SimpleLexerException("position:"+this.point+"  expecting "+expected);
        }
        char[] chrs = expected.toCharArray();
        for (int i = 0; i < chrs.length; i++) {
            if (chrs[i] != this.input.charAt(this.point + i))
                throw new SimpleLexerException("position:"+this.point+"  expecting "+expected);
        }
        this.point  = this.point + expected.length();
    }
    
    protected String look(int number) {
        if (this.point + number >= this.length) {
            return "";
        }
        StringBuilder bld = new StringBuilder();
        for (int i = 0; i < number; i++) {
            bld.append(this.input.charAt(this.point+i));
        }
        return bld.toString();
    }
    
    protected boolean canRead() {
        return this.point < this.length -1;
    }
    
    public Token nextToken() throws SimpleLexerException {
        if (canRead()) {
            if (this.point < this.length-2) {
                if (look(2).equals("/*")) {
                    return readComment();
                } else {
                    return readNotComment();
                }
            } else {
                return readNotComment();
            }
        } else return new Token("", Type.EOI);
    }
}
