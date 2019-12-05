@ECHO OFF
echo "Include public key into GnuPG key ring"

gpg --import ./scripts/store.asc

(echo 5 && echo y)|gpg --command-fd 0 --expert --edit-key 7B9FE0F5 trust

echo "Done!"