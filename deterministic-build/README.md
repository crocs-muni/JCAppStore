### Deterministic builds

Because all sources from store contents repository (https://github.com/petrs/JCAppStoreContent) are
compiled, we provide an advices on how to compile the exact binaries independently.

The process is pretty straightforward, since it is Java. For applet building we recommend
AppletPlayground (https://github.com/martinpaljak/AppletPlayground) by Martin Paljak.

sdk-urls.txt file contains a link and sdk sources used ofr the builds. We also used
some other tools:

 - vJCRE (https://github.com/martinpaljak/vJCRE/releases)
 - VisOp2.0 (provided by AppletPlayground)
 - jpp (java preprocessor) - 1.3.0 (https://github.com/abego/jpp)
 - gplobalplatform 2.1.1 (provided by AppletPlayground)
 
 Note: some applets were missing some stuff and had to be adjusted in order to compile
 (missing short casts mostly). Check the applet pull requests from Aiosa user (if not accepted)!
 
In case you run to same difficulties, we used java 8 to build the applets. Some applets also have
a difficult build systems - in case of doubt, ask me for help at _horakj7@gmail.com_.

##### _build.xml_
A messy build file from Applet Playground given to you for better insight on compilation of various applets. There are dates of compilation
included and other useful information (AIDs etc.) included. It is just provided AS-IS to help you with compilation.


#### The actual verification
is pretty simple - we are in Java. Open the `.cap` file and compare sources. The usual tree is:
```
 -- META-INF 
        | -- MANIFEST.mf
    the
        | -- package
                  | -- path
                           | -- file1.cap
                           | -- file2.cap
              
```
Compare only the files in package name tree structure - avoid MANIFEST.mf as it contains various meta data such as the time of the compilation.

To compare two `.cap` files (and avoid the manifest), you can use [python script jcapsum.py](https://gist.github.com/walterschell/3c4350c1aeb660d175a330ec151b1f5f) made by [walterschell](https://gist.github.com/walterschell):
```
$ python jcapsum.py store/JCApplets/AnssiSmartPGP/AnssiSmartPGP_v1.13_sdk3.0.4.cap ../../temp/ansismartpgp.cap
Total Hash of store/JCApplets/AnssiSmartPGP/AnssiSmartPGP_v1.13_sdk3.0.4.cap: eed8f40879e06a32cde06c268c023ce6a94c2cc8abeb493f5023fd8c54bdb871
Total Hash of ../../temp/ansismartpgp.cap: eed8f40879e06a32cde06c268c023ce6a94c2cc8abeb493f5023fd8c54bdb871
```
