TODO: rpm

Name:           JCAppStore
Version:        1.3
Release:        1%{?dist}
Summary:        The open source Applet Store. Install anything onto your smartcard and secure your privacy.

License:        MIT
URL:            https://github.com/JavaCardSpot-dev/JCAppStore
Source0:        https://github.com/JavaCardSpot-dev/JCAppStore/releases/download/%{version}/JCAppStore-%{version}-unix.tar.gz

Requires:       bash

%description    
JCAppStore, the first open source smart card store and GUI tool. This application is only a tool for card management. It will not allow you to use the card applications themselves. That's why we try to include a pool (store), that contains safe, and intuitive software to install.

%prep
%setup -q

%build

%configure
make %{?_smp_mflags}

%install
mkdir -p %{buildroot}/%{_bindir}
mkdir -p %{buildroot}/usr/lib/%{name}
cat > %{buildroot}/%{_bindir}/%{name} <<-EOF
#!/bin/bash
java -jar /usr/lib/%{name}/%{name}-%{version}.jar
EOF
chmod 0755 %{buildroot}/%{_bindir}/%{name}
install -m 0644 %{name}-%{version}.jar %{buildroot}/usr/lib/%{name}/
install -m 0644 -d src %{buildroot}/usr/lib/%{name}/
rm -rf $RPM_BUILD_ROOT

if ! gpg --help > /dev/null 2&>1 ; then
   echo You don't have GnuPG installed, the store will be unable to verify software integrity.\
      this can be fixed anytime by GnuPG installation and JCAppStore key import.
else
   echo JCAppStore automatically verifies the software integrity for you. In order to do that \
      we need to import our public key to your keyring and set the ultimate trust.
   echo You don't have to import the key, then the verification will not work. This can be \
      changed anytime.
   echo Do you wish to import y/n?\n
   read ANSWER
   while [ $ANSWER != "y" || $ANSWER != "n" ] ; do
      if ! gpg --help > /dev/null 2&>1 ; then
         echo "You don't have GnuPG installed, the store will be unable to verify software integrity."\
                "this can be fixed anytime by GnuPG installation and JCAppStore key import."
else
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
      gpg --import store.asc
      (echo 5 && echo y)|gpg --command-fd 0 --expert --edit-key 7B9FE0F5 trust
      echo "The key has been imported."
   fi
fi
%make_install

%files
%license LICENSE
%dir /usr/lib/%{name}/
%{_bindir}/%{name}
/usr/lib/%{name}/%{name}-%{version}.jar
%dir /usr/lib/%{name}/src/

%changelog
* Thu Dec 12 2019 Jiří Horák <horakj7@gmail.com> - 1.0
	- First app release
