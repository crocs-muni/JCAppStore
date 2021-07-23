#!/bin/bash

gpg --import store.asc
(echo 5 && echo y)|gpg --command-fd 0 --expert --edit-key 3D6FE2832EDFE9C9 trust
