# JCAppStore 

Have you ever wondered how to manage your passwords? Or would you like to have your own virtual wallet, but do not know what to do? Then it is time for you to discover JCAppStore, first open source smart card store and GUI tool. Our goal is to push open-source smart card development by introducing intuitive smart card distribution platform that can be used by anyone!

#### I am in! ####

There are several things you need to have and know first. Don't worry, we tried to make it as 
intuitive as possible.

1. :credit_card: Own a card - naturally, you need to have a card. If don't check this 
[awesome buyer's guide](https://github.com/martinpaljak/GlobalPlatformPro/tree/master/docs/JavaCardBuyersGuide). 
2. :key: Make sure you know the master password. This should've been given to you 
by card vendor. Though for most common card types, the store recognizes the default passwords 
automatically :thumbsup:.
3. :abcd: Get yourself familiar with basic terms. You've learned words like software, touch 
screen, charger and so on. Smart cards have their own terms too.

How to get the store
-----

**The store runs on java, make sure you have Java Runtime Environment (JRE) first.** Any version of java 11+ should be convenient. [The store was developed using java 11](https://www.oracle.com/java/technologies/javase-jdk11-downloads.html).
More information on how to install the store [see wiki](https://github.com/crocs-muni/JCAppStore/wiki/Installation). 

Unix users also need BASH as we run command line tools from time to time. We will work on this to make the JCAppStore more portable in the future.

How the store works?
------

**This application is only a tool for card management. It will not allow you to use the card 
applications themselves** (mostly). Finding an application for a smart card that is useful can be very tough: 
that's why we introduce the JCAppStore that contains safe, and intuitive (as much as the applets themselves allow) software to install. 
You will always find the usage guide in the store item details. 
Also, in the right upper corner, browse help for more detailed description. 

More details, description and
 howto's can be found on [our wiki](https://github.com/crocs-muni/JCAppStore/wiki). Full documentation is accessible in `readme_res` resources folder.

Useful set of tutorials and how-tos can be found on [OpenSC wiki](https://github.com/OpenSC/OpenSC/wiki/Using-smart-cards-with-applications).

JCAppStore furthermore hosts [JCAlgTest](https://github.com/crocs-muni/JCAlgTest): you can run the test suite directly from the GUI.

Authentication
-----
The card authentication is automatic unless you change the default test key. For more information see [our wiki](https://github.com/crocs-muni/JCAppStore/wiki/Inserting-a-new-card).


Installing Applets
-----
To install an applet, simply select any product from the store or click on the "install from this PC" icon. The files are _verified_
using PGP signatures - that means, the store makes sure no one maliciously modified the software you are going to install on your card.

After submitting the installation dialog window, the free persistent memory is evaluated first. To do so, we install **JCMemory** applet onto
your card and obtain the free space size. 


Deleting Applets
-----
The deletion is very simple; there are only two things you need to know:
1) We track whether the applet stores sensitive data. If so, you are asked **twice** before the delete action proceeds.
2) When not in verbose mode (default state, see settings), the store **force** uninstalls package, its applets implicitly (and vice versa). 

 (advanced) _force_ option will uninstall any applet instances when deleting a package


Store content
-----
You can browse the store using search bar in the left menu. You can search either by application name, category or a developer. Follow these steps:

1) Read the applet use info field before installing. It states any necessary steps that has to be taken in order to use the applet.
2) Install the newest applet version. In the case of failure check whether:
    3) the card have enough install memory space,
    4) you know whether the sdk installed is supported by your card (see SDK version field in the install dialog window),
    5) the applet requires a technology not suported by your card (NFC for example) - find your card [here](https://www.fi.muni.cz/~xsvenda/jcalgtest/),
    6) you modified advanced install section with incorrect values.
6) Enjoy!    

___

## Contribute
If you like our goal, you can support us or help us in following ways:
- we are looking for open-source, intuitive smart card applets to include in our database
- we are looking for translation volunteers who want to bring the store to broader variety of users
- we are looking for users - start using our platform and introduce it to others! let's push the open-source smart card community and support its developers


Build (Advanced)
-----
You are encouraged to build your own executable. For IntelliJ IDEA make sure that:
 - your project language is at least java 11 and your gradle JVM is using also at least java 11
 - 'Use Gradle from' option is set to: **wrapper** task in Gradle build script
 - add module JCAppStore for base project (pointing to the repository root)
 - add module JCAppStore.main pointing to the src/main folder
 - inside JCAppStore.main, mark appropriate directories as sources/resources (main/java and main/resources)
 - you can repeat similar process for test folder (to main), but it is not required
 - for unix systems, gradle runs some `.sh` scripts - don't forget to run `chmod u+x <script name>.sh`  otherwise, the _Permission denied_ message will pop up
 - when running the jar file, make sure src/main/resources folder is copied to the same directory as executed `.jar` - otherwise, 
 assets will not be recognized (they are intentionally excluded from the `.jar` file to be modifiable easily)
