# Encrypted files
A directory of encrypted/sensitive files for CI/CD builds. The files here
are encrypted with **gpg** where all files here should have the default
`.gpg` suffix.

You must have the `SQUONK_DECRYPTION_KEY` environment variable
defined in order to build and test the application (see KeePassXC
**Squonk -> SQUONK_DECRYPTION_KEY**).

## Encrypting files
Put the file containing sensitive data in a sensibly-named sub-directory of
`encrypted` and then use the preferred environment variable and the **gpg**
command-line utility to encrypt files.

To encrypt `sensitive.data` (into `sensitive.data.gpg`) -

    $ echo $SQUONK_DECRYPTION_KEY | \
        gpg --batch --yes --passphrase-fd 0 -c sensitive.data
        
>   Remember to remove the original file (`sensitive.data` in this example)

## Example decryption tasks
For example to decrypt the ChemAxon data and the files into the expected
data locations you can run: -

    $ ./gradlew installChemaxonLicense
    $ ./gradlew installChemaxonLicenseToHome

    $ ./gradlew installChemaxonLibrary
