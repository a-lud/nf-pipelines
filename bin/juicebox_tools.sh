#!/usr/bin/env bash
set -e
java -Xms150000m -Xmx150000m -jar `dirname $0`/juicebox_tools.jar $*
