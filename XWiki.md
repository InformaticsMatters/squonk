# Wiki Setup

When setting up a new Squonk site you often want to copy the Wiki docs to the new site.
This is a skeleton for doing this.
TODO - run through the process and document more completely.

## Setup

* In Keycloak in the new site make sure that the following roles exist in the realm:
** XWiki.XWikiAllGroup
** XWiki.XWikiAdminGroup
** XWiki.XWikiUserGroup
* Add XWiki.XWikiUserGroup as a default role for new users.
* Update existing users to have this role.
* Create a user tdudgeon that will own most of the wiki pages (not sure if this is needed).

## Exporting

* On the public Squonk site log into XWiki as a user with XWiki.XWikiAdminGroup role.
* Click on 'Administer Wiki' in the menu in the top right corner.
* Go to Export in the Content section.
* Give the export package a name, and check the 'With history' and 'Backup package' options. 
* Run the export

An .xar file with the Wiki contents will be downloaded to your computer.

## Importing

* On the new site connect to the Wiki. If this is for the first time XWiki will be initialised.
* Once done login as a user with XWiki.XWikiAdminGroup role.
* Click on Administer Wiki in the menu in the top right corner.
* Go to Import in the Content section.
* Select the .xar file to import and upload.
* Select what is to be imported (everything).
* Run the import. This will take a few mins.

