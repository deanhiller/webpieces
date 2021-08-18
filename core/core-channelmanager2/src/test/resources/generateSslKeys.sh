#!/bin/bash

set +e

keytool -genkey -alias deansKey -keyalg RSA -keysize 2048 -dname "CN=Dean Hiller,OU=Dean, O=Dean,L=Broomfield,ST=CO,C=US" -keypass 123456 -keystore server2.keystore -storepass 123456

keytool -list -v -keystore server2.keystore

keytool -export -alias deanskey -file server_public2.cer -keystore server2.keystore -storepass 123456

keytool -import -alias deanskey -trustcacerts -file server_public2.cer -keystore client2.keystore -storepass 123456

keytool -list -v -keystore client2.keystore
