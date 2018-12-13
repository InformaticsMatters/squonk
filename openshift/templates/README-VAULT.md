# .vault files (Ansible Vault)
`.vault` files are encrypted using Ansible Vault.
To decrypt a file you will need the Ansible vault Password.
For this project see **KeePass > Squonk > Ansible Vault Password**.

You will be prompted for the password when decrypting...

    $ ansible-vault decrypt <file>.vault --output=<file>
    New Vault password:
