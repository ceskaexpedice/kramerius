package cz.incad.kramerius.client.resources.merge.css;

import cz.incad.kramerius.client.resources.merge.css.SimpleCssCommentsLexer.SimpleLexerException;

public class SimpleCSSPreprocessor {

    private SimpleCssCommentsLexer lexer;

    private StringBuilder preprocessData = new StringBuilder();
    private StringBuilder cssData = new StringBuilder();

    public static class Result {
        private String preprocessData ;
        private String cssData ;
        public Result(String preprocessData, String cssData) {
            super();
            this.preprocessData = preprocessData;
            this.cssData = cssData;
        }

        public String getPreprocessData() {
            return preprocessData;
        }

        public String getCssData() {
            return cssData;
        }
    }


    public SimpleCSSPreprocessor(SimpleCssCommentsLexer lexer) {
        super();
        this.lexer = lexer;
    }

    public Result preprocess() throws SimpleLexerException {
        SimpleCssCommentsLexer.Token tok = lexer.nextToken();
        while(tok.getType() != SimpleCssCommentsLexer.Type.EOI) {
            if (tok.getType() == SimpleCssCommentsLexer.Type.COMMENT) {
                String content = tok.getContent();
                if (content.contains("#set")) {
                    this.preprocessData.append(content);
                }
                if (content.contains("$params.put")) {
                    this.preprocessData.append(content);
                }
            } else {
                this.cssData.append(tok.getContent());
            }
            tok = lexer.nextToken();
        }
        return new Result(this.preprocessData.toString(), this.cssData.toString());
    }
    
}
