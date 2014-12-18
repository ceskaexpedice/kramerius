package cz.incad.kramerius.client.resources.merge;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;
import org.apache.velocity.runtime.parser.node.SimpleNode;

import com.sun.jersey.spi.StringReader;

import cz.incad.kramerius.client.resources.merge.css.SimpleCSSPreprocessor;
import cz.incad.kramerius.client.resources.merge.css.SimpleCSSPreprocessor.Result;
import cz.incad.kramerius.client.resources.merge.css.SimpleCssCommentsLexer;
import cz.incad.kramerius.client.resources.merge.css.SimpleCssCommentsLexer.SimpleLexerException;
import cz.incad.kramerius.client.resources.merge.validators.ValidateInput;

public class CSSVelocityMerge implements Merge<String>{

    public static final Logger LOGGER = Logger.getLogger(CSSVelocityMerge.class.getName());
    
    private SimpleCSSPreprocessor.Result preprocessData(String input) throws SimpleLexerException {
        SimpleCssCommentsLexer lexer = new SimpleCssCommentsLexer(input);
        SimpleCSSPreprocessor preprocessor = new SimpleCSSPreprocessor(lexer);
        return preprocessor.preprocess();
    }

    private ValidateInput<String> inputVal;
    
    @Override
    public String merge(String fromWar, String fromConf) {
        Velocity.init();
        VelocityContext ctx = new VelocityContext();
        ctx.put("params", new HashMap<String,String>());

        if (this.inputVal != null && (!this.inputVal.validate(fromConf))) {
            StringWriter writer = new StringWriter();
            Velocity.evaluate(ctx, writer, "stylesheet template ", new java.io.StringReader(fromWar.toString()));
            return writer.toString();
        } else {
            StringBuilder fullTemplate = new StringBuilder();
            try {
                // preprocess data first
                Result fromWarResult = preprocessData(fromWar);
                fullTemplate.append(fromWarResult.getPreprocessData());
                Result fromConfresult = preprocessData(fromConf);
                fullTemplate.append(fromConfresult.getPreprocessData());
                // css data after that
                fullTemplate.append(fromWarResult.getCssData());
                fullTemplate.append(fromConfresult.getCssData());
                
            } catch (SimpleLexerException e) {
                LOGGER.log(Level.SEVERE,e.getMessage(),e);
            }
            StringWriter writer = new StringWriter();
            Velocity.evaluate(ctx, writer, "stylesheet template ", new java.io.StringReader(fullTemplate.toString()));
            return writer.toString();
        }
        
    }

    
    //TODO: CSS validation !! 
    @Override
    public void setValidateInput(ValidateInput<String> v) {
        this.inputVal = v;
    }

    @Override
    public ValidateInput<String> getValidateInput() {
        return this.inputVal;
    }

}
