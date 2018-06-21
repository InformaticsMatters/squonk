#!/usr/bin/env python

"""A python 3 module to simplify the loading of jenkins secrets,
that depends in the im-jenkins-utils module.

A setenv-template.sh can be used to create your own setenv.sh
Once you've define project-specific variables set them with `source setenv.sh`.

Alan Christie
June 2018
"""

import os

from im_jenkins_server import ImJenkinsServer

# Extract some key material from the environment...
JENKINS_URL = os.environ['SQUONK_JENKINS_URL']
JENKINS_USER = os.environ['SQUONK_JENKINS_USER']
JENKINS_USER_TOKEN = os.environ['SQUONK_JENKINS_USER_TOKEN']

CXN_MVN_USER = os.environ['SQUONK_CXN_MAVEN_USER']
CXN_MVN_PASS = os.environ['SQUONK_CXN_MAVEN_PASSWORD']


J_URL = 'https://%s:%s@%s' % (JENKINS_USER, JENKINS_USER_TOKEN, JENKINS_URL)
J_SERVER = ImJenkinsServer(J_URL)

print('Setting credentials on "%s"...' % JENKINS_URL)

# Set general text credentials

J_SERVER.set_secret_text('cxnMavenUser', CXN_MVN_USER, 'The ChemAxon Maven User')
J_SERVER.set_secret_text('cxnMavenPassword', CXN_MVN_PASS, 'The ChemAxon Maven User Password')

# Set some secret files

J_SERVER.set_secret_file('cpSignLicense', '../../data/licenses/cpsign0.3pro.license', 'The CpSign License')
J_SERVER.set_secret_file('chemAxonLicense', '../../data/licenses/license.cxl', 'The ChemAxon License')
J_SERVER.set_secret_file('chemAxonReactionLibrary', '../../docker/deploy/images/chemservices/chemaxon_reaction_library.zip', 'The ChemAxon Reaction Library')

print('Done')
