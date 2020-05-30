FROM ruby:2.7-alpine3.11

LABEL maintainer="ffimnsr <ffimnsr@gmail.com>" 
RUN set -xe \
    && apk add --no-cache libstdc++ sqlite-libs \
    && apk add --no-cache --virtual .build-deps build-base sqlite-dev \
    && gem install mailcatcher -v 0.7.1 --no-document \
    && apk del .build-deps

EXPOSE 1025
EXPOSE 1080

CMD ["mailcatcher", "--no-quit", "--foreground", "--ip=0.0.0.0"]