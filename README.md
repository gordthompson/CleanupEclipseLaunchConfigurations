# CleanupEclipseLaunchConfigurations

Code to remove obsolete "Launch configuration" entries in Eclipse that clog up the
list when doing an Export to a Runnable JAR file. (Eclipse does not clean up these 
entries when a project is deleted.)

**Inspired by the Stack Overflow answer here:**

[http://stackoverflow.com/a/21687507/2144390](http://stackoverflow.com/a/21687507/2144390)

**How it works:**

Each "Launch configuration" is a `.launch` file in the folder

{WORKSPACE_ROOT}/.metadata/.plugins/org.eclipse.debug.core/.launches/

for example, "CleanupEclipseLaunchConfigurationsMain.launch". It contains XML that defines a number of attributes, one of which is `org.eclipse.debug.core.MAPPED_RESOURCE_PATHS` which points to the `.java` file containing the `main` method, e.g., 

    <listAttribute key="org.eclipse.debug.core.MAPPED_RESOURCE_PATHS">
    <listEntry value="/CleanupEclipseLaunchConfigurations/src/com/gordthompson/util/cleanupeclipselaunchconfigurations/CleanupEclipseLaunchConfigurationsMain.java"/>
    </listAttribute>

This code simply iterates through each `.launch` file in the aforementioned folder and checks to see if the `.java` file exists. If not, it deletes the `.launch` file.
