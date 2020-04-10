### Unix installer files

Files used to create unix tar ball and packages.

##### launcher.sh
File that can load JCAppStore's public key to your GPG keyring (for the first time the JCAppStore runs) and run the application.
##### targenerator.sh
Script that creates a tar ball.
##### store.asc
Our public key.

#### deb folder
Contains files used to geterate `.deb` package.
 - jcapp: shell script placed in `/usr/bin` to start the JCAppStore with. Same as `launcher.sh`, but also modifies the access rights
 for jcappstore files (these are given to root from `dpkg`)
#### rpm folder
To-be rpm packaging.
