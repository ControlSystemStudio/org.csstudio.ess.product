/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package se.ess.ics.csstudio.display.builder;


import static se.ess.ics.csstudio.display.builder.Activator.ID;
import static se.ess.ics.csstudio.display.builder.Activator.LOGGER;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.csstudio.diirt.util.core.preferences.ExceptionUtilities;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

import javafx.scene.text.Font;


/**
 * This singleton class loads the ESS required fonts.
 *
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 25 Apr 2018
 */
public class FontsUtility {

    public static final String FONTS_FOLDER    = "fonts/";
    public static final String FONTS_LIST_FILE = FONTS_FOLDER + "fonts-list.txt";

    public static FontsUtility get ( ) {
        return FontsUtilitySingleton.INSTANCE;
    }

    private final AtomicBoolean inited = new AtomicBoolean(false);

    private FontsUtility ( ) {
    }

    public void loadFonts ( ) {

        if ( inited.compareAndSet(false, true) ) {

            List<File> fontFiles = findFontFiles();

            loadFXFonts(fontFiles);
//            loadSWTFonts(fontFiles);

        }

    }

    /**
     * Read the "fonts/font-list.txt" file and for each non empty line
     * try to create a valid entry in the returned {@link List}.
     *
     * @return A {@link List} of {@link File}s pointing to TTF/OTF fonts
     *         to be loaded. {@code null} is never returned.
     */
    private List<File> findFontFiles ( ) {

        List<File> fontFiles = new ArrayList<>(32);
        Bundle bundle = Platform.getBundle(ID);

        if ( bundle != null ) {

            URL url = null;

            try {
                url = FileLocator.toFileURL(FileLocator.find(bundle, new Path(FONTS_LIST_FILE), Collections.emptyMap()));
            } catch ( Exception ex ) {
                LOGGER.warning(MessageFormat.format("Problems retrieving URL for ''{0}'' bundle-relative path [{1}].\n{2}", FONTS_LIST_FILE, ex.getMessage(), ExceptionUtilities.reducedStackTrace(ex, "se.ess")));
            }

            if ( url != null ) {
                try ( BufferedReader reader = new BufferedReader(new FileReader(new File(url.getPath()))) ) {

                    String fontFileName;

                    while ( ( fontFileName = reader.readLine() ) != null ) {
                        if ( StringUtils.isNotBlank(fontFileName) ) {

                            url = null;
                            fontFileName = FONTS_FOLDER + fontFileName.trim();

                            try {
                                url = FileLocator.toFileURL(FileLocator.find(bundle, new Path(fontFileName), Collections.emptyMap()));
                            } catch ( Exception ex ) {
                                LOGGER.warning(MessageFormat.format("Problems retrieving URL for ''{0}'' bundle-relative path [{1}].\n{2}", fontFileName, ex.getMessage(), ExceptionUtilities.reducedStackTrace(ex, "se.ess")));
                            }

                            if ( url != null ) {
                                fontFiles.add(new File(url.getPath()));
                            }

                        }
                    }

                } catch ( IOException ex ) {
                    LOGGER.warning(MessageFormat.format("Unable to read ''{0}'' [{1}].\n{2}", FONTS_LIST_FILE, ex.getMessage(), ExceptionUtilities.reducedStackTrace(ex, "se.ess")));
                }
            } else {
                LOGGER.warning(MessageFormat.format("Null URL for ''{0}'' bundle-relative path.", FONTS_LIST_FILE));
            }

        } else {
            LOGGER.warning(MessageFormat.format("Null bundle for ''{0}'' symbolic name.", ID));
        }

        LOGGER.config(MessageFormat.format("Found {0,number,####0} font files to be loaded.", fontFiles.size()));

        return fontFiles;

    }

//    private void loadSWTFonts ( List<File> fontFiles ) {
//        Display.getCurrent().syncExec( ( ) -> {
//            fontFiles.stream().forEach(f -> {
//                if ( !Display.getCurrent().loadFont(f.toString()) ) {
//                    LOGGER.warning(MessageFormat.format("Font file ''{0}'' not loaded.", f.toString()));
//                } else {
//                    LOGGER.config(MessageFormat.format("SWT engine successfully loaded font ''{0}''.", f.toString()));
//                }
//            });
//        });
//    }

    private void loadFXFonts ( List<File> fontFiles ) {
        fontFiles.stream().forEach(f -> {
            try {
                Font.loadFont(f.toURI().toURL().toExternalForm(), 10);
                LOGGER.config(MessageFormat.format("JavaFX engine successfully loaded font ''{0}''.", f.toString()));
            } catch ( MalformedURLException ex ) {
                LOGGER.warning(MessageFormat.format("Font file ''{0}'' not loaded [{1}].\n{2}", f.toString(), ex.getMessage(), ExceptionUtilities.reducedStackTrace(ex, "se.ess")));
            }
        });
    }

    private static interface FontsUtilitySingleton {

        FontsUtility INSTANCE = new FontsUtility();

    }

}
