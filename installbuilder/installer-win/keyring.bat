@ECHO OFF
echo "Include public key into GnuPG key ring"

gpg --import ./scripts/store.asc

(echo 5 && echo y)|gpg --command-fd 0 --expert --edit-key 7436D09AC9304C3F trust

echo "Done!"