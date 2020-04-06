### Testing with JUBULA

Due to the GUI testing problems described in the documentation, the following preconditions must be met before the test suite is executed. 
The following section also describes some conditions common to all Jubula tests, such as the Application Under Test 
AUT (an agent running on user-defined port behaving as a mediator between ITE and tested application) setup.

#### System Environment
GnuPG is installed on the computer, its binary executable accessible via `$PATH` environment variable or specified in the Settings. 
JCAppStore's public key is imported into PGP keyring. The Internet connection must be available.

#### Jubula Setup
The test project require AUT to be set up with tested jar executable provided. 
The AUT must be running and the tested application started. 
The application should be directly behind Jubula in system window hierarchy - once the test is started, the IDE is minimized
 and the application should get the focus.

#### JCAppstore Setup
The store should be untouched as if just installed, except
   - a smart card is inserted and no other application is intervening with it,
   - the inserted card must be in `OP_READY` state, fully functional, empty, support at least SDK version of 2.2.2, 
   the JCAppStore must be able to authenticate to the card automatically,
   - the JCAppStore language is set to English.

Empty card means all packages and applet instances are deleted except an ISD applet. Toggle "packages" checkbox to verify
no packages without instantiated applet are present.
Also some other test-case-specific conditions must be met. The `my_card/` folder must contain`JCAlgTest_v1.7.4_sdk2.2.2.cap`
for custom installation test execution, along with `\[the cap filename\].sig`
 file for custom signature verification. Both files can be copied from the `store/` folder.
 
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