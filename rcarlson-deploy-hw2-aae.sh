#!/usr/bin/env bash

set -eux

cd deploy
deployAsyncService.sh ../hw3-rcarlson/src/main/resources/hw2-rcarlson-aae-deploy.xml
