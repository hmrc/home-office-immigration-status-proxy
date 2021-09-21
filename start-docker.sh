#!/bin/sh

CLIENT_JKS_FILE=$(find . -type f -name client.jks)

# Overwrite the keystore with a client certificate.
if [ -n "${CLIENT_JKS}" ] ; then
  if [ -f $CLIENT_JKS_FILE ]; then
    echo "$CLIENT_JKS" | base64 --decode > "$CLIENT_JKS_FILE"
  else
    echo "Cannot find client.jks file location."
  fi
fi

SCRIPT=$(find . -type f -name home-office-settled-status-proxy)
exec $SCRIPT $HMRC_CONFIG -Dconfig.file=conf/home-office-settled-status-proxy.conf