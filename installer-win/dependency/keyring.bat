@ECHO OFF
echo "Include public key into GnuPG key ring"

gpg --import ./scripts/store.asc

(echo 5 && echo y)|gpg --command-fd 0 --expert --edit-key 3D6FE2832EDFE9C9 trust

echo "Done!"