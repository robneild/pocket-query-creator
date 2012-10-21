package net.sourceforge.htmlunit.corejs.javascript.annotations;

import java.lang.annotation.*;

/**
 * An annotation that marks a Java method as JavaScript constructor. This can
 * be used as an alternative to the <code>jsConstructor</code> naming convention desribed in
 * {@link net.sourceforge.htmlunit.corejs.javascript.ScriptableObject#defineClass(net.sourceforge.htmlunit.corejs.javascript.Scriptable, java.lang.Class)}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
public @interface JSConstructor {
}
