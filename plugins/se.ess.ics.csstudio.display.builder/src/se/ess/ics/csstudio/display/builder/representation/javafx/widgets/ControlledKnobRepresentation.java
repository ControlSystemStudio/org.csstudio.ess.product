/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder.representation.javafx.widgets;

import org.csstudio.display.builder.representation.javafx.widgets.BaseKnobRepresentation;

import se.ess.ics.csstudio.display.builder.model.widgets.ControlledKnobWidget;
import se.ess.knobs.controlled.ControlledKnob;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 24 Sep 2017
 */
public class ControlledKnobRepresentation extends BaseKnobRepresentation<ControlledKnob, ControlledKnobWidget> {

    @Override
    protected ControlledKnob createKnob() {
        return new ControlledKnob();
    }

}
