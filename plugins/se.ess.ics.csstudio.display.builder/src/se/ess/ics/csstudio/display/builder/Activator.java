/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder;


import java.util.logging.Logger;

import org.csstudio.utility.product.IWorkbenchWindowAdvisorExtPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javafx.embed.swt.FXCanvas;
import javafx.scene.Scene;
import se.europeanspallationsource.javafx.control.knobs.controlled.ControlledKnob;


/**
 * @author Claudio Rosati, European Spallation Source ERIC
 * @version 1.0.0 25 Sep 2017
 */
public class Activator implements BundleActivator, IWorkbenchWindowAdvisorExtPoint {

    // The plug-in ID
    public static final String ID     = "se.ess.ics.csstudio.display.builder";
    public static final Logger LOGGER = Logger.getLogger(Activator.class.getName());

    private static BundleContext context;

    static BundleContext getContext ( ) {
        return context;
    }

    @Override
    public void postWindowClose ( ) {
    }

    @Override
    public void postWindowCreate ( ) {
    }

    @Override
    public void postWindowOpen ( ) {
    }

    @Override
    public void postWindowRestore ( ) throws WorkbenchException {
    }

    @Override
    public void preWindowOpen ( ) {

        final Display display = Display.getCurrent();
        final Shell shell = new Shell(display);
        final FXCanvas canvas = new FXCanvas(shell, SWT.NONE);

        ControllersUtility.get().discoverAndInitialize();
        FontsUtility.get().loadFonts();

        canvas.setScene(new Scene(new ControlledKnob()));

    }

    @Override
    public boolean preWindowShellClose ( ) {
        return false;
    }

    @Override
    public IStatus restoreState ( IMemento memento ) {
        return null;
    }

    @Override
    public IStatus saveState ( IMemento memento ) {
        return null;
    }

    @Override
    public void start ( BundleContext bundleContext ) throws Exception {
        context = bundleContext;
    }

    @Override
    public void stop ( BundleContext bundleContext ) throws Exception {
        context = null;
    }

}
