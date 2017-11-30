#!/usr/bin/env bash

SSO_USERNAME=admin
SSO_PASSWORD=WDT277rO
SSO_REALM=squonk
sso_service=https://sso.192.168.42.201.nip.io/auth
CURL="curl -s"

function get_token() {

  token=""
  if [ -n "$SSO_USERNAME" ] && [ -n "$SSO_PASSWORD" ]; then
    data="username=${SSO_USERNAME}&password=${SSO_PASSWORD}&grant_type=password&client_id=admin-cli"
    url=${sso_service}/realms/${SSO_REALM}/protocol/openid-connect/token
    token=`$CURL --data $data $url`
    echo $data
    echo $url
    if [ $? -ne 0 ] || [[ $token != *"access_token"* ]]; then
      echo "ERROR: Unable to connect to SSO/Keycloak at $sso_service for user $SSO_USERNAME and realm $SSO_REALM. SSO Clients *not* created"
      if [ -z "$token" ]; then
        echo "Reason: Check the URL, no response from the URL above, check if it is valid or if the DNS is resolvable."
      else
        echo "Reason: `echo $token | grep -Po '((?<=\<p\>|\<body\>).*?(?=\</p\>|\</body\>)|(?<="error_description":")[^"]*)' | sed -e 's/<[^>]*>//g'`"
      fi
      token=
    else
      token=`echo $token | grep -Po '(?<="access_token":")[^"]*'`
      echo "Obtained auth token from $sso_service for realm $SSO_REALM"
    fi
  else
    echo "Missing SSO_USERNAME and/or SSO_PASSWORD. Unable to generate SSO Clients"
  fi

}


get_token