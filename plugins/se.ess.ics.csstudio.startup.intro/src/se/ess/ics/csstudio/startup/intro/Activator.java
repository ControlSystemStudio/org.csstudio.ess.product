/**
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (C) 2016 European Spallation Source ERIC.
 * All rights reserved
 *
 * The contents of this file are subject to the terms of
 * the GNU General Public License Version 3 only ("GPL" 
 * Version 3, or the "License"). You can obtain a copy of
 * the License at https://www.gnu.org/licenses/gpl-3.0.html
 *
 * You may use, distribute and modify this code under the
 * terms of the GPL Version 3 license. See the License for 
 * the specific language governing permissions and 
 * limitations under the License.
 *
 * When distributing the software, include this License 
 * Header Notice in each file. If applicable, add the 
 * following below the License Header, with the fields 
 * enclosed by brackets [] replaced by your own identifying 
 * information:
 *   "Portions Copyrighted [year] [name of copyright owner]"
 */
package se.ess.ics.csstudio.startup.intro;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * @author claudiorosati, European Spallation Source ERIC
 * @version 1.0.0 23 Aug 2016
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "se.ess.ics.csstudio.startup.intro";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {

		super.start(context);
		
		plugin = this;
		
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {

		plugin = null;
		
		super.stop(context);
	
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
}
