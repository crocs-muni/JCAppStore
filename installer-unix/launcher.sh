#!/bin/bash
VERSION=1.3
DIR='./'

if ! cat ${DIR}/jcappstore-do-not-ask-${VERSION}.info ; then

	#'sudo chown -R' stuff must have root
        if [[ $EUID -ne 0 ]]; then
             echo "You must run JCAppStore as a superuser for the first time."
             exit 2;
        fi

        #check GPG presence
	if ! gpg --help > /dev/null 2&>1 ; then
	   echo "You don't have GnuPG installed, the store will be unable to verify software integrity.\
	      this can be fixed anytime by GnuPG installation and JCAppStore key import."
        echo "Next time, the application will start WITHOUT THIS NOTICE!"
        #import GPG key if possible
	else
              if ! gpg --list-keys 3D6FE2832EDFE9C9 > /dev/null 2&>1 ; then
		    echo "JCAppStore automatically verifies the software integrity for you. In order to do that" \
		      "we need to import our public key to your keyring and set the ultimate trust."
		    echo "You don't have to import the key, then the verification will not work. This can be" \
		      "changed anytime."
		    echo "Do you wish to import y/n?"
		    read ANSWER
		    while [ "$ANSWER" != "y" ] && [ "$ANSWER" != "n" ] ; do
		       echo "Wrong answer. Try again:"
		       read ANSWER
		    done
		    if [[ "$ANSWER" == "y" ]] ; then
		       gpg --import ${DIR}/store.asc
		       (echo 5 && echo y)|gpg --command-fd 0 --expert --edit-key 3D6FE2832EDFE9C9 trust
		       echo "The key has been imported."
		    else
		       echo "The key can be imported anytime. Next time, the application will start WITHOUT THIS NOTICE!"
		    fi
              fi
	fi


	#change owner to current user for jcappstore
	if sudo chown ${SUDO_USER}: /usr/bin/jcapp ; then
		echo "Setting shell executable rights to ${SUDO_USER}."
	else
		echo "Failed to set current user's ownership for /usr/bin/jcapp" \
		": Either do this manually or use \"sudo jcapp\" to run the store each time."
		echo "Use \"sudo chown -R [your user name]: /usr/bin/jcapp\""
	fi
	if sudo chown -R ${SUDO_USER}: /usr/share/java/jcappstore ; then
		echo "Setting access rights to ${SUDO_USER}."
		echo "The store has been succesfully set. Run jcapp again, now without super user."
	        touch ${DIR}jcappstore-do-not-ask${VERSION}.info
		exit 0;
	else
		echo "Failed to set current user's ownership for /usr/share/java/jcappstore" \
		": Either do this manually or use \"sudo jcapp\" to run the store each time."
		echo "Use \"sudo chown -R [your user name]: /usr/share/java/jcappstore\""
	fi
	touch ${DIR}jcappstore-do-not-ask${VERSION}.info
fi
cd $DIR
java -jar ./JCAppStore-${VERSION}.jar