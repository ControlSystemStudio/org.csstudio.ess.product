/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder;


import org.eclipse.osgi.util.NLS;


/**
 * Externalized texts
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 24 Sep 2017
 */
@SuppressWarnings( "nls" )
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "se.ess.ics.csstudio.display.builder";

    // Keep in alphabetical order!
    public static String WidgetProperties_Channel;
    public static String WidgetProperties_CoarseIncrement;
    public static String WidgetProperties_FineIncrement;

    static {
        // Initialize resource bundle.
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages ( ) {
    }

}
