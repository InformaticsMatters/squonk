#!/bin/bash

echo "Entering containers-setup-nginx.sh"

if [ ! $PUBLIC_HOST ]; then
	echo "environment variables not set? Run 'source setenv.sh' to set them"
	exit 1
fi

set -e

base=$PWD

echo "checking we have some content for the websites"
if [ ! -d images/nginx/sites/informaticsmatters.com/html ]; then
	echo "creating dummy content for informaticsmatters.com"
	mkdir -p images/nginx/sites/informaticsmatters.com/html
fi
if [ ! -d images/nginx/sites/squonk.it/html ]; then
	echo "creating dummy content for squonk.it"
	mkdir -p images/nginx/sites/squonk.it/html || exit 1
	cp images/nginx/sites/index.html images/nginx/sites/squonk.it/html/
fi

# setup nginx
sed "s/__public_host__/${PUBLIC_HOST}/g" images/nginx/default.ssl.conf.template > images/nginx/default.ssl.conf
sed "s/#XWIKI_PLACEHOLDER#/include snippets\/xwiki.conf;/g" images/nginx/default.ssl.conf > images/nginx/default.site.conf

