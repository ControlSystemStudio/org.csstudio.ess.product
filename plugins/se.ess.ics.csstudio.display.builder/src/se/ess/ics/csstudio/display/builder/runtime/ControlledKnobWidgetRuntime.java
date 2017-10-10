/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder.runtime;


import static org.csstudio.display.builder.runtime.RuntimePlugin.logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;

import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyListener;
import org.csstudio.display.builder.runtime.WidgetRuntime;
import org.csstudio.display.builder.runtime.pv.PVFactory;
import org.csstudio.display.builder.runtime.pv.RuntimePV;
import org.csstudio.display.builder.runtime.pv.RuntimePVListener;
import org.diirt.vtype.VType;

import se.ess.ics.csstudio.display.builder.model.widgets.ControlledKnobWidget;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 10 Oct 2017
 */
public class ControlledKnobWidgetRuntime extends WidgetRuntime<ControlledKnobWidget> {

    private final List<PVBinding> bindings = new ArrayList<>();

    @Override
    public void start ( ) throws Exception {

        super.start();

        bindings.add(new PVBinding(widget.propReadbackPVName(), widget.propReadbackPVValue()));

    }

    @Override
    public void stop ( ) {
        bindings.stream().forEach(PVBinding::dispose);
        super.stop();
    }

    private class PVBinding implements WidgetPropertyListener<String> {

        private final WidgetProperty<String>     name;
        private final RuntimePVListener          listener;
        private final AtomicReference<RuntimePV> pv_ref = new AtomicReference<>();

        public PVBinding ( final WidgetProperty<String> name, final WidgetProperty<VType> value ) {

            this.name = name;
            this.listener = new PropertyUpdater(value);

            connect();

            name.addPropertyListener(this);

        }

        public void dispose ( ) {
            disconnect();
            name.removePropertyListener(this);
        }

        @Override
        public void propertyChanged ( final WidgetProperty<String> property, String oldValue, String newValue ) {
            //  PV name changed: Disconnect existing PV...
            disconnect();
            //  ...and connect to new PV.
            connect();
        }

        private void connect ( ) {

            final String pv_name = name.getValue();

            if ( pv_name.isEmpty() ) {
                return;
            }

            logger.log(Level.FINE, "Connecting {0} {1}", new Object[] { widget, name });

            final RuntimePV pv;

            try {
                pv = PVFactory.getPV(pv_name);
            } catch ( Exception ex ) {
                logger.log(Level.WARNING, "Cannot connect to PV " + pv_name, ex);
                return;
            }

            pv.addListener(listener);
            addPV(pv, true);
            pv_ref.set(pv);

        }

        private void disconnect ( ) {

            final RuntimePV pv = pv_ref.getAndSet(null);

            if ( pv == null ) {
                return;
            }

            pv.removeListener(listener);
            PVFactory.releasePV(pv);
            removePV(pv);

        }

    }

}
