### Windows installer files

Xml files used by IzPack to create an installation wizard. Unlike with UNIX, the key import is done when installing the store.
`dependency/` folder contains our public key, store icon, script for the key import and wizard xml files.

##### Launch4j
The installer creates an easy-to-run application - but it is itself a .jar file! For that case, we use launch4j to create an .exe installer. 
This is done via gradle automatically.