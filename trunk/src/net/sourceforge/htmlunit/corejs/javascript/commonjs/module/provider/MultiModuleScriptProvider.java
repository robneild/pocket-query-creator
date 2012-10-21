package net.sourceforge.htmlunit.corejs.javascript.commonjs.module.provider;

import java.net.URI;
import java.util.LinkedList;
import java.util.List;

import net.sourceforge.htmlunit.corejs.javascript.Context;
import net.sourceforge.htmlunit.corejs.javascript.Scriptable;
import net.sourceforge.htmlunit.corejs.javascript.commonjs.module.ModuleScript;
import net.sourceforge.htmlunit.corejs.javascript.commonjs.module.ModuleScriptProvider;

/**
 * A multiplexer for module script providers.
 * @author Attila Szegedi
 * @version $Id: MultiModuleScriptProvider.java 6395 2011-05-05 17:00:20Z mguillem $
 */
public class MultiModuleScriptProvider implements ModuleScriptProvider
{
    private final ModuleScriptProvider[] providers;
    
    /**
     * Creates a new multiplexing module script provider tht gathers the 
     * specified providers
     * @param providers the providers to multiplex.
     */
    public MultiModuleScriptProvider(Iterable<? extends ModuleScriptProvider> providers) {
        final List<ModuleScriptProvider> l = new LinkedList<ModuleScriptProvider>();
        for (ModuleScriptProvider provider : providers) {
            l.add(provider);
        }
        this.providers = l.toArray(new ModuleScriptProvider[l.size()]);
    }
    
    public ModuleScript getModuleScript(Context cx, String moduleId, URI uri,
                                        Scriptable paths) throws Exception {
        for (ModuleScriptProvider provider : providers) {
            final ModuleScript script = provider.getModuleScript(cx, moduleId,
                    uri, paths);
            if(script != null) {
                return script;
            }
        }
        return null;
    }
}
