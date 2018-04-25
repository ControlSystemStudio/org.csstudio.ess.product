/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder;


import static se.ess.ics.csstudio.display.builder.Activator.LOGGER;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import se.europeanspallationsource.javafx.control.knobs.controlled.ControlledKnob;
import se.europeanspallationsource.javafx.control.knobs.controller.Controllers;;


/**
 * This singleton class is used to grant a single point of discovery and
 * initialization of the available controllers.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 25 Apr 2018
 */
public class ControllersUtility {

    public static final List<String> CONTROLLERS = new ArrayList<>(1);

    public static ControllersUtility get ( ) {
        return ControllersUtilitySingleton.INSTANCE;
    }

    private final AtomicBoolean inited = new AtomicBoolean(false);

    private ControllersUtility ( ) {
    }

    /**
     * Discover and initialize all available controllers.
     */
    public void discoverAndInitialize ( ) {
        if ( inited.compareAndSet(false, true) ) {
            CONTROLLERS.addAll(Controllers.get().getIdentifiers());
            Collections.sort(CONTROLLERS);
            CONTROLLERS.add(0, ControlledKnob.CONTROLLER_NONE);
            CONTROLLERS.forEach(c -> {
                LOGGER.config(MessageFormat.format("Successfully loaded controller ''{0}''.", c));
            });
        }
    }

    private static interface ControllersUtilitySingleton {

        ControllersUtility INSTANCE = new ControllersUtility();

    }

}
