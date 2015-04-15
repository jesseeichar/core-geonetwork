package org.fao.geonet.services.metadata.format;

import com.google.common.annotations.VisibleForTesting;
import org.fao.geonet.transformer.GroovyTransformer;
import org.fao.geonet.transformer.groovy.Transformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

/**
 * Formatter for groovy dialect of formatters.
 *
 * @author Jesse on 10/15/2014.
 */
@Component
public class GroovyFormatter implements FormatterImpl {

    @Autowired
    GroovyTransformer groovyTransformer;
    @VisibleForTesting
    Transformer findTransformer(final FormatterParams fparams) throws ExecutionException {
        return groovyTransformer.findTransformer(fparams);
    }
    public String format(FormatterParams fparams) throws Exception {
        return groovyTransformer.format(fparams);
    }
}
