## CS-Studio ESS Product  

ESS-specific configuration for CS-Studio

```
|----[+] repository:
|     |---- cs-studio-ess.product: The product definition file (.product),
|             this is the file which defines the list of features that are to be included into your project.
|             To inlcude or exclude additional feature simply add or remove them
|   
|----[+]  features: The features module conatins all the site specific features.
|     |----[+] o.c.ess.product.configuration.feature: The feature containing customization and config files.
|     |     |----[+]rootfiles/configuration
|     |           |---- plugin_customization.ini (optional): A config file defining site specific preferences
|                                                       for the various applications included in the product.              
|     |           |---- *.conf (optional): AA configuration files                  
|     |---- (optional) Extra features to be included in your product(To add your own plugins and applications)
|                      from the list of dependencies of this product.
|
|----[+]  (optional) plugins: The plugins module will contain all your site specific plugins.
```

#### Building the Product

The cs-studio product can be build using maven tycho.
Run maven verify for the top level pom (in ```org.csstudio.ess.product```)
```
mvn clean verify
```

#### Eclipse IDE Integration
Setting up Eclipse IDE to compile, run and debug CS-Studio is explained in [Control System Studio Guide](http://cs-studio.sourceforge.net/docbook/index.html), chapter 4: [Compiling, Running, Debugging CSS](http://cs-studio.sourceforge.net/docbook/ch04.html), section: [Using the Eclipse IDE](http://cs-studio.sourceforge.net/docbook/ch04.html#sec_runnning_in_ide). Refer to it for the instructions.

##### Target Platform on Neon
Using Eclipse Neon and following the [Control System Studio Guide](http://cs-studio.sourceforge.net/docbook/index.html) will almost certainly produce errors at start-up of CS-Studio, often of such gravity to impede the program to start. Solving the problem requires uncheck duplicates in the current Target Platform:

- Open the **_Preferences_** dialog,
- Select the **_Plug-in Development → Target Platform_** category,
- Select the currently checked **_Target definition_** and press the **_Edit…_** button,
- Select the **_Content_** tab in the **_Edit Target Definition_** dialog, then uncheck the second copy of each duplicated plug-in or the one with a lower version number,
- Press **_Finish_**, then **_Apply_** and **_OK_**.

Note that when a new library/plug-in is added to the application, it will result unchecked in the **_Target Platform_**, thus requiring it to me manually checked in the **_Edit Target Definition_** dialog.
