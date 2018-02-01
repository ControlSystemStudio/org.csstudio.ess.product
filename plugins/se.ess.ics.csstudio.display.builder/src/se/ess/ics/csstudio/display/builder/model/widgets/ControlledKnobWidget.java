/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder.model.widgets;


import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newDoublePropertyDescriptor;
import static org.csstudio.display.builder.model.properties.CommonWidgetProperties.newIntegerPropertyDescriptor;

import java.util.List;

import org.csstudio.display.builder.model.Widget;
import org.csstudio.display.builder.model.WidgetCategory;
import org.csstudio.display.builder.model.WidgetDescriptor;
import org.csstudio.display.builder.model.WidgetProperty;
import org.csstudio.display.builder.model.WidgetPropertyCategory;
import org.csstudio.display.builder.model.WidgetPropertyDescriptor;
import org.csstudio.display.builder.model.properties.EnumWidgetProperty;
import org.csstudio.display.builder.model.widgets.KnobWidget;

import se.ess.ics.csstudio.display.builder.Messages;
import se.europeanspallationsource.javafx.control.knobs.controlled.ControlledKnob;
import se.europeanspallationsource.javafx.control.knobs.controller.Controllable.OperatingMode;
import se.europeanspallationsource.javafx.control.knobs.controller.midi.djtechtools.MidiFighterTwisterController;


/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 24 Sep 2017
 */
public class ControlledKnobWidget extends KnobWidget {

    public static final WidgetDescriptor WIDGET_DESCRIPTOR = new WidgetDescriptor(
        "controlled-knob",
        WidgetCategory.CONTROL,
        "Controlled Knob",
        "platform:/plugin/se.ess.ics.csstudio.display.builder/icons/controlled-knob.png",
        "Controlled knob controller that can read/write a numeric PV"
    ) {
        @Override
        public Widget createWidget ( ) {
            return new ControlledKnobWidget();
        }
    };

    public enum Controller {

        NONE(ControlledKnob.CONTROLLER_NONE),
        MFT(MidiFighterTwisterController.IDENTIFIER);

        private String id;

        Controller ( String id ) {
            this.id = id;
        }

        public String getID() {
            return id;
        }

    }

    public static final WidgetPropertyDescriptor<Integer>       propChannel         = newIntegerPropertyDescriptor               (WidgetPropertyCategory.BEHAVIOR, "channel",          Messages.WidgetProperties_Channel, 0, 63);
    public static final WidgetPropertyDescriptor<Controller>    propController      = new WidgetPropertyDescriptor<Controller>   (WidgetPropertyCategory.BEHAVIOR, "controller",       Messages.WidgetProperties_Controller) {
        @Override
        public EnumWidgetProperty<Controller> createProperty ( Widget widget, Controller defaultMode ) {
            return new EnumWidgetProperty<>(this, widget, defaultMode);
        }
    };
    public static final WidgetPropertyDescriptor<Double>        propCoarseIncrement = newDoublePropertyDescriptor                (WidgetPropertyCategory.BEHAVIOR, "coarse_increment", Messages.WidgetProperties_CoarseIncrement);
    public static final WidgetPropertyDescriptor<Double>        propFineIncrement   = newDoublePropertyDescriptor                (WidgetPropertyCategory.BEHAVIOR, "fine_increment",   Messages.WidgetProperties_FineIncrement);
    public static final WidgetPropertyDescriptor<OperatingMode> propOperatingMode   = new WidgetPropertyDescriptor<OperatingMode>(WidgetPropertyCategory.BEHAVIOR, "operating_mode",   Messages.WidgetProperties_OperatingMode) {
        @Override
        public EnumWidgetProperty<OperatingMode> createProperty ( Widget widget, OperatingMode defaultMode ) {
            return new EnumWidgetProperty<>(this, widget, defaultMode);
        }
    };

    private volatile WidgetProperty<Integer>       channel;
    private volatile WidgetProperty<Controller>    controller;
    private volatile WidgetProperty<Double>        coarse_increment;
    private volatile WidgetProperty<Double>        fine_increment;
    private volatile WidgetProperty<OperatingMode> operating_mode;

    public ControlledKnobWidget ( ) {
        super(ControlledKnobWidget.WIDGET_DESCRIPTOR.getType(), 220, 220);
    }

    public WidgetProperty<Controller> propController ( ) {
        return controller;
    }

    public WidgetProperty<Integer> propChannel ( ) {
        return channel;
    }

    public WidgetProperty<Double> propCoarseIncrement ( ) {
        return coarse_increment;
    }

    public WidgetProperty<Double> propFineIncrement ( ) {
        return fine_increment;
    }

    public WidgetProperty<OperatingMode> propOperatingMode ( ) {
        return operating_mode;
    }

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        propTagVisible().setValue(true);

        properties.add(channel          = propChannel.createProperty(this, 0));
        properties.add(controller       = propController.createProperty(this, Controller.NONE));
        properties.add(coarse_increment = propCoarseIncrement.createProperty(this, 1.0));
        properties.add(fine_increment   = propFineIncrement.createProperty(this, 0.025));
        properties.add(operating_mode   = propOperatingMode.createProperty(this, OperatingMode.SET_AND_CLICK));

    }

}
