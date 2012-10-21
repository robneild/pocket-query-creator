package net.sourceforge.htmlunit.corejs.javascript.commonjs.module.provider;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sourceforge.htmlunit.corejs.javascript.commonjs.module.ModuleScript;

/**
 * A module script provider that uses a module source provider to load modules
 * and caches the loaded modules. It strongly references the loaded modules, 
 * thus a module once loaded will not be eligible for garbage collection before
 * the module provider itself becomes eligible. 
 * @author Attila Szegedi
 * @version $Id: StrongCachingModuleScriptProvider.java 6395 2011-05-05 17:00:20Z mguillem $
 */
public class StrongCachingModuleScriptProvider extends CachingModuleScriptProviderBase
{
    private static final long serialVersionUID = 1L;

    private final Map<String, CachedModuleScript> modules = 
        new ConcurrentHashMap<String, CachedModuleScript>(16, .75f, getConcurrencyLevel()); 

    /**
     * Creates a new module provider with the specified module source provider.
     * @param moduleSourceProvider provider for modules' source code
     */
    public StrongCachingModuleScriptProvider(
            ModuleSourceProvider moduleSourceProvider)
    {
        super(moduleSourceProvider);
    }
    
    @Override
    protected CachedModuleScript getLoadedModule(String moduleId) {
        return modules.get(moduleId);
    }
    
    @Override
    protected void putLoadedModule(String moduleId, ModuleScript moduleScript,
            Object validator) {
        modules.put(moduleId, new CachedModuleScript(moduleScript, validator));
    }
}