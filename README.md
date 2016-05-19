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

#### Status
Master [![Build Status](https://travis-ci.org/ControlSystemStudio/org.csstudio.ess.product.svg?branch=master)](https://travis-ci.org/ControlSystemStudio/org.csstudio.ess.product)
