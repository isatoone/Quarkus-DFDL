package org.acme;

import io.quarkus.runtime.annotations.RegisterForReflection;

@RegisterForReflection(targets = {org.apache.xerces.impl.dv.dtd.DTDDVFactoryImpl.class,
        org.apache.xerces.impl.dv.xs.SchemaDVFactoryImpl.class, org.apache.xerces.jaxp.SAXParserFactoryImpl.class,
        org.apache.xerces.parsers.XIncludeAwareParserConfiguration.class})
public class ReflectionConfiguration {
}