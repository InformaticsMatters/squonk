#!/usr/bin/env python

# Python code that uses selenium to get the Keycloak admin user password
# using the Chrome web-driver and then checks the Keycloak 'Realm'
# for the 'standard-user' Role and makes sure the role exists and is part
# of the 'Default Roles'.
#
# This runs as part of a playbook that installs the Python selenium
# module and verifies a suitable web driver (chrome) is present.
#
# This module expects you to have run `source setenv.sh` and placed a suitable
# Chrome web-driver on the execution path.
#
# Alan Christie
# Sep 2018

import logging
import os
import subprocess
import sys

from selenium.webdriver.chrome.webdriver import WebDriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.support.wait import WebDriverWait

# Setup logging
FORMAT = '%(asctime)-15s %(message)s'
logging.basicConfig(format=FORMAT, level=logging.INFO)

# Log the chrome driver version
cd = subprocess.check_output('chromedriver --version'.split())
logging.info(cd.strip())

# Variables (environment)
admin = os.environ['OC_ADMIN']
admin_password = os.environ['OC_ADMIN_PASSWORD']
main_url = os.environ['OC_MASTER_URL']
sso_project = os.environ['OC_INFRA_PROJECT']
routes_basename = os.environ['OC_ROUTES_BASENAME']
keycloak_server_url = os.environ['KEYCLOAK_SERVER_URL']
keycloak_realm = os.environ['KEYCLOAK_REALM']

# Variables (well-known)
sso_secrets_name = 'keycloak-secrets'
sso_secret_name = 'sso-admin-password'
sso_secrets_url = os.path.join(main_url,
                               'console/project',
                               sso_project,
                               'browse/secrets',
                               sso_secrets_name)

# The standard-user
sso_standard_user_role_name = 'standard-user'
sso_standard_user_role_description = 'A standard end user'

# Launch Chrome (headless)
chrome_options = Options()
chrome_options.add_argument('--disable-extensions')
chrome_options.add_argument('--headless')
chrome_options.add_argument('--disable-gpu')
chrome_options.add_argument('--no-sandbox')
selenium = WebDriver(chrome_options=chrome_options)

selenium.delete_all_cookies()
selenium.implicitly_wait(8)

logging.info('Looking up Keycloak %s password...', admin)

# Navigate directly to the required secrets (in the OpenShift console).
# We'll be required to login to het there...
selenium.get(sso_secrets_url)
WebDriverWait(selenium, 20).\
    until(lambda driver: driver.find_element_by_tag_name('body'))

# Navigate the login
u_i = selenium.find_element_by_name('username')
u_i.send_keys(admin)
p_i = selenium.find_element_by_name('password')
p_i.send_keys(admin_password)
selenium.find_element_by_css_selector('button[type="submit"]').click()
WebDriverWait(selenium, 20).\
    until(lambda driver: driver.find_element_by_tag_name('body'))

# Now click the 'Reveal Secret' link...
selenium.find_element_by_link_text('Reveal Secret').click()

# Get the secret's value.
# It's essentially a list so we have to search the individual WebElements
# for a secret whose 'title' matches the name of secret we want.
# Once we've found that we can then get the secret value from
# the paired 'input' element's 'value'...
selenium.find_element_by_class_name('secret-data')
secrets = selenium.find_elements_by_css_selector(
    'div[ng-repeat="(secretDataName, secretData) in decodedSecretData"]')
sso_admin_password = None
for secret in secrets:
    try:
        if secret.find_element_by_css_selector('dt[title="{}"]'.format(sso_secret_name)):
            sso_admin_password = secret.\
                find_element_by_css_selector('input').get_attribute('value')
            break
    except selenium.common.exceptions.NoSuchElementException:
        pass

# The secret value (sso_admin_password) should be set here.
if not sso_admin_password:
    logging.error('Failed to get %s', sso_secret_name)
    sys.exit(1)

logging.info('Logging into Keycloak...')

# Now we have the SSO admin password we can try to login there
# in order to adjust its setup.
selenium.get(keycloak_server_url + '/admin/')
WebDriverWait(selenium, 20). \
    until(lambda driver: driver.find_element_by_tag_name('body'))

# Click the "Administration Console" link...
#selenium.find_element_by_link_text('Administration Console').click()
#WebDriverWait(selenium, 20). \
#    until(lambda driver: driver.find_element_by_tag_name('body'))

# Login to Keycloak
u_i = selenium.find_element_by_name('username')
u_i.send_keys('admin')
u_i = selenium.find_element_by_name('password')
u_i.send_keys(sso_admin_password)
selenium.find_element_by_css_selector('input[type="submit"]').click()
WebDriverWait(selenium, 20). \
    until(lambda driver: driver.find_element_by_tag_name('body'))

logging.info('Checking Roles...')

# Look for the Standard User in Roles.
# Navigate to 'Roles'
selenium.find_element_by_css_selector(
    'a[href="#/realms/{}/roles"]'.format(keycloak_realm)).click()
WebDriverWait(selenium, 20). \
    until(lambda driver: driver.find_element_by_tag_name('body'))

# Is the 'standard-user' in the table?
standard_user_known = False
roles_links = selenium.find_elements_by_css_selector('table tbody tr a')
for roles_link in roles_links:
    if roles_link.text == sso_standard_user_role_name:
        standard_user_known = True
        break

if not standard_user_known:
    logging.info('Creating Standard User Role...')
    # Click the Add Role button...
    selenium.find_element_by_link_text('Add Role').click()
    name_i = selenium.find_element_by_css_selector('input[id="name"]')
    name_i.send_keys(sso_standard_user_role_name)
    description_i = selenium.find_element_by_css_selector('#description')
    description_i.send_keys(sso_standard_user_role_description)
    selenium.find_element_by_css_selector('button[type="submit"]').click()
    WebDriverWait(selenium, 20). \
        until(lambda driver: driver.find_element_by_tag_name('body'))
else:
    logging.info('Standard User is Already Known')

logging.info('Checking Default Roles...')

# Is standard-user in 'Default Roles'?
# Navigate back to roles (re-collect page content)
selenium.find_element_by_css_selector(
    'a[href="#/realms/{}/roles"]'.format(keycloak_realm)).click()
WebDriverWait(selenium, 20). \
    until(lambda driver: driver.find_element_by_tag_name('body'))

# Navigate to 'Default Roles'
selenium.find_element_by_css_selector(
    'a[href="#/realms/{}/default-roles"]'.format(keycloak_realm)).click()
# Is standard-user a default role?
standard_user_in_defaults = False
default_roles = selenium.find_elements_by_css_selector('#assigned option')
for default_role in default_roles:
    if default_role.get_attribute('label') == sso_standard_user_role_name:
        standard_user_in_defaults = True
        break

if not standard_user_in_defaults:
    logging.info('Adding Standard User to Default Roles...')
    available_roles = selenium.find_elements_by_css_selector('#available option')
    for available_role in available_roles:
        if available_role.get_attribute('label') == sso_standard_user_role_name:
            # Found the standard-user role in the 'Available Roles' table
            available_role.click()
            # Now 'click' the 'Add Selected >>' button.
            selenium.find_element_by_css_selector(
                'button[ng-click="addRealmDefaultRole()"]').click()
            WebDriverWait(selenium, 20). \
                until(lambda driver: driver.find_element_by_tag_name('body'))
            break
else:
    logging.info('Standard User is already a Default Role')

# Close Chrome (after a short delay.
# If we close too quickly prio actions may be lost.
selenium.quit()
