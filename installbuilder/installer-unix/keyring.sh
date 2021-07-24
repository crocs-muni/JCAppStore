#!/bin/bash
gpg --import store.asc
(echo 5 && echo y)|gpg --command-fd 0 --expert --edit-key 7436D09AC9304C3F trust

