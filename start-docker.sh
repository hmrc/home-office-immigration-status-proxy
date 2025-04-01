#!/bin/sh

#CLIENT_JKS_FILE=$(find . -type f -name client.jks)
#
## Overwrite the keystore with a client certificate.
#if [ -n "${CLIENT_JKS}" ] ; then
#  if [ -f $CLIENT_JKS_FILE ]; then
#    echo "$CLIENT_JKS" | base64 --decode > "$CLIENT_JKS_FILE"
#  else
#    echo "Cannot find client.jks file location."
#  fi
#fi

TRUSTSTORE="/tmp/hmrc_truststore.jks"
TRUSTSTORE_PASSWORD="changeit"
# For details of keyStore and trustStore defaults and locations, see:
# https://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#CustomizingStores
ARGS=""
# ARGS="$ARGS -Djavax.net.debug=all"
# Add the server certificate used to authenticate the other party.
if [ $HMRC_CONFIG ] ; then
  echo $HMRC_CONFIG | base64 --decode - > hmrc_ca.crt
  keytool -noprompt -import -alias hmrc_ca -file hmrc_ca.crt -keystore $TRUSTSTORE -storepass $TRUSTSTORE_PASSWORD -trustcacerts
  ARGS="${ARGS} -Djavax.net.ssl.trustStore=${TRUSTSTORE}"
  ARGS="${ARGS} -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD}"
  ARGS="${ARGS} -Djavax.net.debug=ssl"
fi

SCRIPT=$(find . -type f -name home-office-immigration-status-proxy)
exec $SCRIPT $HMRC_CONFIG -Dconfig.file=conf/home-office-immigration-status-proxy.conf