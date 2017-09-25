package se.ess.ics.csstudio.display.builder;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

    // The plug-in ID
    public static final String ID = "se.ess.ics.csstudio.display.builder";

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	@Override
    public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
	}

	@Override
    public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
