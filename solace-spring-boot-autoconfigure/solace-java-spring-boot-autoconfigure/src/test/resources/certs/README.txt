Note: where a password is required, the password is "changeMe123"

Create a Certificate Authority (CA) and use it to sign certificates for a Solace PubSub+ broker, a Solace PubSub+ client, and a Keycloak server.

The directory contains the following files:
- solbroker_san.conf: Solace PubSub+ broker certificate signing request (CSR) configuration file, mainly for Subject Alternative Name (SAN) extension.
- keycloak_san.conf: Keycloak server certificate signing request (CSR) configuration file, mainly for Subject Alternative Name (SAN) extension.


Create sub-directories:
=======================
mkdir rootCA
mkdir broker
mkdir client
mkdir keycloak

Root CA Key/Certificate:
========================
1. Generate a 4096-bit RSA private key for the Root CA:

openssl genrsa -out ./rootCA/rootCA.key 4096

2. Create a self-signed Root CA certificate valid for 20 years:
openssl req -x509 -new -nodes -key ./rootCA/rootCA.key -sha256 -days 7300 -out ./rootCA/rootCA.crt \
  -subj "/C=CA/ST=Ontario/L=Kanata/O=Solace Systems/CN=Root CA"

3. Create a pem file with the key and certificate:
cat ./rootCA/rootCA.key ./rootCA/rootCA.crt > ./rootCA/rootCA.pem

4. verify the certificate:
openssl x509 -in ./rootCA/rootCA.crt -text -noout

Solace Broker Key/Certificate: 
==============================
1. Generate a 2048-bit RSA private key for the server:
openssl genrsa -out ./broker/solbroker.key 2048

2. Create a CSR (Certificate Signing Request) for the server:
openssl req -new -key ./broker/solbroker.key -out ./broker/solbroker.csr -config ./solbroker_san.conf

3. Sign the server CSR with your Root CA to generate the server certificate valid for 20 years:
openssl x509 -req -in ./broker/solbroker.csr -CA ./rootCA/rootCA.crt -CAkey ./rootCA/rootCA.key -CAcreateserial \
  -out ./broker/solbroker.crt -days 7300 -sha256 -extensions req_ext -extfile ./solbroker_san.conf

4. Create a pem file with the key and certificate:
cat ./broker/solbroker.key ./broker/solbroker.crt > ./broker/solbroker.pem

5. verify the certificate:
openssl x509 -in ./broker/solbroker.crt -text -noout

Solace Client Key/Certificate: 
==============================
1. Generate a 2048-bit RSA private key for the client:
openssl genrsa -out ./client/client.key 2048

2. Create a CSR (Certificate Signing Request) for the client:
openssl req -new -key ./client/client.key -out ./client/client.csr \
  -subj "/C=CA/ST=Ontario/L=Kanata/O=Solace Systems/CN=solclient"

3. Sign the client CSR with your Root CA to generate the client certificate valid for 20 years:
openssl x509 -req -in ./client/client.csr -CA ./rootCA/rootCA.crt -CAkey ./rootCA/rootCA.key -CAcreateserial \
  -out ./client/client.crt -days 7300 -sha256

4. Create a pem file with the key and certificate:
cat ./client/client.key ./client/client.crt > ./client/client.pem

5. verify the certificate:
openssl x509 -in ./client/client.crt -text -noout

6. Create and verify a client truststore containing the Root CA certificate:
openssl x509 -outform der -in ./rootCA/rootCA.pem -out ./rootCA/rootCA.der
keytool -import -trustcacerts -alias root_ca -file ./rootCA/rootCA.der -keystore ./client/client-truststore.p12 -storepass changeMe123 -noprompt
keytool -v -list -keystore ./client/client-truststore.p12 -storepass changeMe123

7. Create a client keystore containing the client key and certificate:
openssl pkcs12 -export -in ./client/client.pem -inkey ./client/client.key -name client -out ./client/client.p12 -passout pass:changeMe123
keytool -importkeystore -srckeystore ./client/client.p12 -srcstoretype PKCS12 -destkeystore ./client/client-keystore.jks -deststoretype JKS -srcstorepass changeMe123 -deststorepass changeMe123


Keycloak Server Key/Certificate: 
================================
1. Generate a 2048-bit RSA private key for the keycloak:
openssl genrsa -out ./keycloak/keycloak.key 2048

2. Create a CSR (Certificate Signing Request) for the keycloak:
openssl req -new -key ./keycloak/keycloak.key -out ./keycloak/keycloak.csr -config ./keycloak_san.conf

3. Sign the keycloak CSR with your Root CA to generate the keycloak certificate valid for 20 years:
openssl x509 -req -in ./keycloak/keycloak.csr -CA ./rootCA/rootCA.crt -CAkey ./rootCA/rootCA.key -CAcreateserial \
  -out ./keycloak/keycloak.crt -days 7300 -sha256 -extensions req_ext -extfile ./keycloak_san.conf

4. Create a pem file with the key and certificate:
cat ./keycloak/keycloak.key ./keycloak/keycloak.crt > ./keycloak/keycloak.pem

5. verify the certificate:
openssl x509 -in ./keycloak/keycloak.crt -text -noout
