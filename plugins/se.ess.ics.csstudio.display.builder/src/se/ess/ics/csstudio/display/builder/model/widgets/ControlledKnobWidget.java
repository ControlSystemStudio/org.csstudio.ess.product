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
import org.csstudio.display.builder.model.widgets.KnobWidget;

import se.ess.ics.csstudio.display.builder.Messages;


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

    public static final WidgetPropertyDescriptor<Integer> propChannel         = newIntegerPropertyDescriptor(WidgetPropertyCategory.BEHAVIOR, "channel",          Messages.WidgetProperties_Channel);
    public static final WidgetPropertyDescriptor<Double>  propCoarseIncrement = newDoublePropertyDescriptor (WidgetPropertyCategory.BEHAVIOR, "coarse_increment", Messages.WidgetProperties_CoarseIncrement);
    public static final WidgetPropertyDescriptor<Double>  propFineIncrement   = newDoublePropertyDescriptor (WidgetPropertyCategory.BEHAVIOR, "fine_increment",   Messages.WidgetProperties_FineIncrement);

    public static int freeChannel = 0;

    private volatile WidgetProperty<Integer>     channel;
    private volatile WidgetProperty<Double>      coarse_increment;
    private volatile WidgetProperty<Double>      fine_increment;

    public ControlledKnobWidget ( ) {
        super(ControlledKnobWidget.WIDGET_DESCRIPTOR.getType(), 220, 220);
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

    @Override
    protected void defineProperties ( final List<WidgetProperty<?>> properties ) {

        super.defineProperties(properties);

        propTagVisible().setValue(true);

        properties.add(channel          = propChannel.createProperty(this, freeChannel++));
        properties.add(coarse_increment = propCoarseIncrement.createProperty(this, 1.0));
        properties.add(fine_increment   = propFineIncrement.createProperty(this, 0.025));

    }

}
