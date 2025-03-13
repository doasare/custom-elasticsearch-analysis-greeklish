package org.elasticsearch.plugin.analysis.greeklish;

import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.index.analysis.GreeklishTokenFilterFactory;

import java.util.Map;
import java.util.Collections;

public class GreeklishPlugin extends Plugin implements AnalysisPlugin {

    // Use explicit type parameters for the Map to resolve the type inference issue
    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        // Explicitly define the Map with the correct types
        return Collections.singletonMap("skroutz_greeklish", GreeklishTokenFilterFactory::new);
    }
}
