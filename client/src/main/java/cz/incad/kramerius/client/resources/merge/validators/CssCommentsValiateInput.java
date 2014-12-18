package cz.incad.kramerius.client.resources.merge.validators;

import java.util.logging.Level;
import java.util.logging.Logger;

import cz.incad.kramerius.client.resources.merge.css.SimpleCSSPreprocessor;
import cz.incad.kramerius.client.resources.merge.css.SimpleCssCommentsLexer;
import cz.incad.kramerius.client.resources.merge.css.SimpleCssCommentsLexer.SimpleLexerException;

public class CssCommentsValiateInput implements ValidateInput<String>{

    public static final Logger LOGGER = Logger.getLogger(CssCommentsValiateInput.class.getName());
    
    @Override
    public boolean validate(String raw) {
        try {
            SimpleCssCommentsLexer lexer = new SimpleCssCommentsLexer(raw);
            SimpleCSSPreprocessor preprocessor = new SimpleCSSPreprocessor(lexer);
            preprocessor.preprocess();
            return true;
        } catch (SimpleLexerException e) {
            LOGGER.log(Level.SEVERE, e.getMessage(),e);
            return false;
        }
    }

    
}
