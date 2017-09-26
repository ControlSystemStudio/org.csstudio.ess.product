/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import se.ess.knobs.controlled.ControlledKnob;
import se.ess.knobs.controller.Controllers;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Sep 2017
 */
public class Activator implements BundleActivator {

    public static final List<String> CONTROLLERS;
    // The plug-in ID
    public static final String ID     = "se.ess.ics.csstudio.display.builder";
    public static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

    private static BundleContext context;

    static {

        CONTROLLERS = new ArrayList<>(Controllers.get().getIdentifiers());

        Collections.sort(CONTROLLERS);
        CONTROLLERS.add(0, ControlledKnob.CONTROLLER_NONE);

    }

    static BundleContext getContext ( ) {
        return context;
    }

    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        Activator.context = bundleContext;

    }

    @Override
    public void stop ( BundleContext bundleContext ) throws Exception {
        Activator.context = null;
    }

}
