package net.sourceforge.htmlunit.corejs.javascript.annotations;

import java.lang.annotation.*;

/**
 * An annotation that marks a Java method as JavaScript getter. This can
 * be used as an alternative to the <code>jsGet_</code> prefix desribed in
 * {@link net.sourceforge.htmlunit.corejs.javascript.ScriptableObject#defineClass(net.sourceforge.htmlunit.corejs.javascript.Scriptable, java.lang.Class)}.
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface JSGetter {
    String value() default "";
}
