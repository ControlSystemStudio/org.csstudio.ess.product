/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.representation.javafx.widgets.BaseKnobRepresentation;

import se.ess.ics.csstudio.display.builder.model.widgets.ControlledKnobWidget;
import se.ess.ics.csstudio.display.builder.model.widgets.ControlledKnobWidget.Controller;
import se.ess.knobs.controlled.ControlledKnob;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 24 Sep 2017
 */
public class ControlledKnobRepresentation extends BaseKnobRepresentation<ControlledKnob, ControlledKnobWidget> {

    private final DirtyFlag dirtyControl = new DirtyFlag();

    @Override
    protected ControlledKnob createKnob() {

        ControlledKnob knob = new ControlledKnob();

        dirtyControl.mark();

        return knob;

    }

    @Override
    public void dispose ( ) {
        jfx_node.dispose();
        super.dispose();
    }

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        if ( dirtyControl.checkAndClear() ) {

            jfx_node.setChannel(model_widget.propChannel().getValue());
            jfx_node.setCoarseIncrement(model_widget.propCoarseIncrement().getValue());
            jfx_node.setFineIncrement(model_widget.propFineIncrement().getValue());
            jfx_node.setOperatingMode(model_widget.propOperatingMode().getValue());

            if ( toolkit.isEditMode() ) {
                jfx_node.setController(Controller.NONE.getID());
            } else {
                jfx_node.setController(model_widget.propController().getValue().getID());
            }

        }

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propChannel().addUntypedPropertyListener(this::controlChanged);
        model_widget.propCoarseIncrement().addUntypedPropertyListener(this::controlChanged);
        model_widget.propFineIncrement().addUntypedPropertyListener(this::controlChanged);
        model_widget.propOperatingMode().addUntypedPropertyListener(this::controlChanged);

    }

    private void controlChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyControl.mark();
        toolkit.scheduleUpdate(this);
    }

}
