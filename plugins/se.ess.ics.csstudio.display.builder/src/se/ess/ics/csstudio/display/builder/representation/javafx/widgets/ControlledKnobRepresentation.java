/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder.representation.javafx.widgets;


import java.util.Objects;

import org.csstudio.display.builder.model.DirtyFlag;
import org.csstudio.display.builder.model.UntypedWidgetPropertyListener;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.representation.javafx.widgets.BaseKnobRepresentation;

import se.ess.ics.csstudio.display.builder.model.widgets.ControlledKnobWidget;
import se.ess.ics.csstudio.display.builder.model.widgets.ControlledKnobWidget.Controller;
import se.europeanspallationsource.javafx.control.knobs.controlled.ControlledKnob;
import se.europeanspallationsource.javafx.control.knobs.controller.Controllable.OperatingMode;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 24 Sep 2017
 */
public class ControlledKnobRepresentation extends BaseKnobRepresentation<ControlledKnob, ControlledKnobWidget> {

    private final DirtyFlag                     dirtyControl           = new DirtyFlag();
    private final UntypedWidgetPropertyListener controlChangedListener = this::controlChanged;

    @Override
    protected ControlledKnob createJFXNode ( ) throws Exception {

        try {
            return super.createJFXNode();
        } finally {
            dirtyControl.mark();
            toolkit.scheduleUpdate(this);
        }

    }

    @Override
    protected ControlledKnob createKnob ( ) {
        return new ControlledKnob();
    }

    @Override
    public void dispose ( ) {
        jfx_node.dispose();
        super.dispose();
    }

    @Override
    public void updateChanges ( ) {

        super.updateChanges();

        Object value;

        if ( dirtyControl.checkAndClear() ) {

            value = model_widget.propChannel().getValue();

            if ( !Objects.equals(value, jfx_node.getChannel()) ) {
                jfx_node.setChannel((int) value);
            }

            value = model_widget.propCoarseIncrement().getValue();

            if ( !Objects.equals(value, jfx_node.getCoarseIncrement()) ) {
                jfx_node.setCoarseIncrement((double) value);
            }

            value = model_widget.propFineIncrement().getValue();

            if ( !Objects.equals(value, jfx_node.getFineIncrement()) ) {
                jfx_node.setFineIncrement((double) value);
            }

            value = model_widget.propOperatingMode().getValue();

            if ( !Objects.equals(value, jfx_node.getOperatingMode()) ) {
                jfx_node.setOperatingMode((OperatingMode) value);
            }

            value = toolkit.isEditMode() ? Controller.NONE.getID() : model_widget.propController().getValue().getID();

            if ( !Objects.equals(value, jfx_node.getController()) ) {
                jfx_node.setController((String) value);
            }

        }

    }

    @Override
    protected void registerListeners ( ) {

        super.registerListeners();

        model_widget.propChannel().addUntypedPropertyListener(controlChangedListener);
        model_widget.propCoarseIncrement().addUntypedPropertyListener(controlChangedListener);
        model_widget.propFineIncrement().addUntypedPropertyListener(controlChangedListener);
        model_widget.propOperatingMode().addUntypedPropertyListener(controlChangedListener);

    }

    @Override
    protected void unregisterListeners ( ) {

        model_widget.propChannel().removePropertyListener(controlChangedListener);
        model_widget.propCoarseIncrement().removePropertyListener(controlChangedListener);
        model_widget.propFineIncrement().removePropertyListener(controlChangedListener);
        model_widget.propOperatingMode().removePropertyListener(controlChangedListener);

        super.unregisterListeners();

    }

    private void controlChanged ( final WidgetProperty<?> property, final Object old_value, final Object new_value ) {
        dirtyControl.mark();
        toolkit.scheduleUpdate(this);
    }

}
