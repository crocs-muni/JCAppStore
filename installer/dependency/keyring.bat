@ECHO OFF
echo "Include public key into GnuPG key ring"

start gpg --import store.asc
start gpg --

echo "Done!"