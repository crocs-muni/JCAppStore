### Testing with JUBULA

Due to the GUI testing problems described in the documentation, the following preconditions must be met before the test suite is executed. 
The following section also describes some conditions common to all Jubula tests, such as the Application Under Test 
AUT (an agent running on user-defined port behaving as a mediator between ITE and tested application) setup.

#### System Environment
GnuPG is installed on the computer, its binary executable accessible via `$PATH` environment variable or specified in the Settings. 
JCAppStore's public key is imported into PGP keyring. The Internet connection must be available.

#### Jubula Setup
The file `jcappstore_test_suite.jub` should be imported to Jubula using `Test` menu item.
The test project require AUT to be set up with tested jar executable provided. Select `Test > Properties` and Add or Edit AUT depending on
whether an AUT is already created. Add or Edit AUT configuration, use `localhost`, provide path to JCAppStore executable `.jar` file
in **Executable JAR File Name** field and set your **AUT Working Directory** to the folder the .jre is in (leave the other fields empty).

Example values when installed with windows installer:
```
Executable JAR File Name: C:\Program Files\JCAppStore\JCAppStore-1.0.jar
AUT Working Directory:    C:\Program Files\JCAppStore
```


The AUT must be running and the tested application started (see the red ball and green arrow icons). 
The "Running AUTs" window should display running JCAppstore application.
The application should be directly behind Jubula in system window hierarchy - once the test is started, the ITE is minimized
 and the application should get the focus.

#### JCAppstore Setup
The store should be untouched as if just installed, except
   - a smart card is inserted and no other application is intervening with it,
   - the inserted card must be in `OP_READY` state, fully functional, empty, support at least SDK version of 2.2.2, 
   the JCAppStore must be able to authenticate to the card automatically,
   - the JCAppStore language is set to English.

Empty card means all packages and applet instances are deleted except an ISD applet. Toggle "packages" checkbox to verify
no packages without instantiated applet are present. The store must be "as installed" means that search bar is cleared, enabled modes are 
"keep JCMemory" and "Simple use". Also, no "don't show this again" messages are blocked. To set this up easily, just delete `data/jcappstore.options`
file. This will reset all your setup (or move it somewhere else for the testing purpose and replace afterwards).
Also some other test-case-specific conditions must be met. The `my_card/` folder must contain`JCAlgTest_v1.7.4_sdk2.2.2.cap`
for custom installation test execution, along with `\[the cap filename\].sig` file for custom signature verification. 
Both files can be copied from the `store/JCAlgTest/` folder.

#### Testing
The suite contains folders with testing scenarios. All the tests are grouped in `FULLTEST` taget - in Test Suite Browser,
select FULLTEST and wait for the testing to start. There is around 15 seconds delay before the AUT starts the execution.
 
#### Possible failures
 
The tests may fail nevertheless. It is hard to ensure the very same conditions for the environment. The testing was performed
on a Windows platform using JavaCos A40 smart card. Any card that can install all versions of JCAlgTest shoud be OK, but
we do not know for sure.
 
###### Reasons the tests could fail for you
 
  - different GUI components: other OS can use slightly different components or the Swing was bit adjusted to work differently. For example,
  Apple code is compiled using different libraries for Swing (aqua stuff). Also, the code includes `if OS_UNIX then ... else if OS_WINDOWS`
  conditions.
  - different performance: the card activities may take different time on your platform. There are timeouts for actions set, JUBULA can not
  handle our "WORKING..." window well and the timeouts for installations are between 15 and 30 seconds, deletion between 5 and 15 seconds. 
  In case an action is not performed within these limits, the tests will fail.
  - updates: use release dated to 5.4. 2020. Any newer releases might change behaviour and the tests could fail.
  - the tests are deterministic, yet the environment is not (from the application view): GUI features used by the store make the testing difficult. For example,
  a test can fail because AUT is unable to click a button that has been just moved, because a notification message (e.g. Successful installation)
  has vanished in the very moment the agent tried to perform the action. Although there are waiting phases that should cover these message timeouts,
  your machine or card reader might be slower and the timeout not long enough.

Jubula saves a screenshot on failure: in case there is an assertion fail and the test did not exit, check the screenshot: sometimes, it is obvious the test should've passed and the agent was just
unable to locate the component (problematic is mainly a variable number of items when listing card contents or any dynamic GUI features).