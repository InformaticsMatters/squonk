# .vault files (Ansible Vault)
`.vault` files in this project are encrypted using [Ansible Vault].
To decrypt a file you will need the Ansible vault Password.
For this project see **KeePass > Squonk > Ansible Vault Password**.

You will be prompted for the password when decrypting...

    $ ansible-vault decrypt <file>.vault --output=site-setenv.sh
    Vault password:
    Decryption successful

If you need to re-encrypt a file you can run the following,
but make sure you use the correct password for the project,
which you will have to enter twice...

    $ ansible-vault encrypt site-setenv.sh --output=<file>.vault
    New Vault password:
    Confirm New Vault password:
    Encryption successful

---

[Ansible Vault]: https://docs.ansible.com/ansible/latest/user_guide/vault.html
