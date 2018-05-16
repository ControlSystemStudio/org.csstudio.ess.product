package se.ess.ics.csstudio.fonts;


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
import java.util.logging.Logger;

import org.apache.commons.lang3.StringUtils;
import org.csstudio.diirt.util.core.preferences.ExceptionUtilities;
import org.csstudio.utility.product.IWorkbenchWindowAdvisorExtPoint;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.WorkbenchException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import javafx.scene.text.Font;


public class Activator implements BundleActivator, IWorkbenchWindowAdvisorExtPoint {

    public static final String   FONTS_FOLDER    = "fonts/";
    public static final String   FONTS_LIST_FILE = FONTS_FOLDER + "fonts-list.txt";
    public static final String   ID              = "se.ess.ics.csstudio.fonts";
    public static final Logger   LOGGER          = Logger.getLogger(Activator.class.getName());
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

        List<File> fontFiles = findFontFiles();

        loadFXFonts(fontFiles);
        loadSWTFonts(fontFiles);

    }

    @Override
    public boolean preWindowShellClose ( ) {
        return true;
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
        Activator.context = bundleContext;
    }

    @Override
    public void stop ( BundleContext bundleContext ) throws Exception {
        Activator.context = null;
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

    private void loadSWTFonts ( List<File> fontFiles ) {
        Display.getCurrent().syncExec(() -> {
            fontFiles.stream().forEach(f -> {
                if ( !Display.getCurrent().loadFont(f.toString()) ) {
                    LOGGER.warning(MessageFormat.format("Font file ''{0}'' not loaded.", f.toString()));
                } else {
                    LOGGER.config(MessageFormat.format("SWT engine successfully loaded font ''{0}''.", f.toString()));
                }
            });
        });
    }

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

}
