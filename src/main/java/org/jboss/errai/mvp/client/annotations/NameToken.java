/*
 * Errai-MVP, inspired by GWT-Platform for the Errai Framework
 * Copyright : Cedric Hauber (cedbossneo) 2012.
 */

package org.jboss.errai.mvp.client.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target( {ElementType.TYPE})
@Retention(RUNTIME)
public @interface NameToken {
    String value() default "";
}
