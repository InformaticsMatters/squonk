#!/usr/bin/env bash
#
# Decrypt all ".vault" files
# using "vault-pass.txt".
# The output files are prefixed with the
# "site-" to satisfy gitignore.

PASSWORD_FILE="vault-pass.txt"
if [ ! -e ${PASSWORD_FILE} ]
then
  echo "You need to create a ./${PASSWORD_FILE} file"
  exit 1
fi

for f in *.vault
do
  out_f=site-${f%.vault}
  echo ${out_f}
  ansible-vault decrypt ${f} --output=site-${f%.vault} \
    --vault-password-file ${PASSWORD_FILE}
done
