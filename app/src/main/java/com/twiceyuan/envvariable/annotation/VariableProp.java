package com.twiceyuan.envvariable.annotation;

import com.twiceyuan.envvariable.model.Variable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface VariableProp {

    String name();

    String desc();

    Class<? extends Variable.DefaultItemProvider> defaultValue();

    Class<? extends Variable.Item>[] selections();
}
