FROM rabbitmq:3-management
MAINTAINER tdudgeon@informaticsmatters.com

ENV RABBITMQ_ERLANG_COOKIE=secret

COPY ./init.sh /usr/local/etc/
COPY ./update-credentials.sh /usr/local/etc/
COPY ./clean.sh /usr/local/etc/


