# JCAppStore 

Have you ever wondered how to manage your passwords? Or would you like to have your own virtual wallet, but do not know what to do? Then it is time for you to discover JCAppStore, first open source smart card store and GUI tool.

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

How the store works?
------

This application is only a tool for card management. It will not allow you to use the card 
applications themselves. That's why we try to include a pool (store), that contains safe, and
intuitive software to install. You will always find the usage guide in the store item details. 
Also, in the right upper corner, browse help for more detailed description. 
Full documentation is accessible in [TODO]

####Basic terms (not exact, but a lot easier to understand to)####
<details>
   <summary>Applet</summary>
   <p>Applet is the software running on your card. You can think of it as a synonym for application.</p>
</details>

<details>
   <summary>Main applet</summary>
   <p>Main applet is the default application running. Some applets require to be main in order to work.</p>
</details>

<details>
   <summary>Package</summary>
   <p>Package is a context for applet. The applets or applications are installed from a package. Package can have more applets active.</p>
</details>

<details>
   <summary>Security domain</summary>
   <p>It is a card manager. It is also an applet.</p>
</details>

<details>
   <summary>Master key</summary>
   <p>Master key is the key that is required from you by a card manager (security domain). Without the key, you 
   can't **modify** (e.g. install, delete..) the card contents. **The master key is not a PIN or a card password you are used to.** The key may be one single long sequence, or it can consist
   of three parts. [Detailed key information here](https://github.com/martinpaljak/GlobalPlatformPro/wiki/Keys). You need not to change the master key.</p>
</details>

<details>
   <summary>Host application</summary>
   <p>The applet on a card needs to communicate with your computer. Most applets do not have
   these hosts and are very hard to use. The store does not display such software.
   
   The host application can be accessible through command line only. The command line guide can
   be found in the app help section. At best, the host application has a GUI host twin and 
   should be intuitive.</p>
</details>

<details>
   <summary>SDK</summary>
   <p>Setup development kit; a library for card software. If install fails, the cause
   may be that your card does not support the newest SDK: you can try to install with older
   SDK instead.</p>
</details>

#### Installing ####

#### Deleting ####

#### Authentication ####

The card authentication is executed as follows:
1) If the card is present in **cards.ini** file, the authentication is performed, if **auth = true** is set and the key value is not in an invalid format
2) If the card is not present in the file, or the key is invalid, a default test key is searched.
    1) If the test key is found the card is authenticated **immediately** with it as **we suppose user did not change the test key**.
    2) If no test key found the user is asked whether to use the common test key 4041..4E4F.

![Authentication activity diagram](readme-res/auth-activity-diagram.png)


#### Custom master key

We do not recommend to change default master key if you don't have a reason to do so 
(e.g. you know you have to). However, if you've already changed it (or the application doesn't know the default test key for your card), 
then you need to modify the **cards.ini** file.

1) Locate the file in your default documents folder/JCAppStore/data/
2) Search for your card block, beginning with \[card id value here\]. The card id can be obtained from the store (just plug in the card, the error message will show you the card id).
3) Modify the key field - set the correct master key value (in hex string, we don't support the 3-piece keys yet).
4) Modify the auth field - set value to true if not set, for the store to actually try to authenticate to the card.

This store is a GUI layer for GlobalPlatformPro basic functionality. It's purpose is not 
to mimic GPPro, but to ease the basic card management. Also, the store holds chosen applets 
that are fully fu .