#!/bin/sh
# 
# Environment Variables
#
#   INGRID_JAVA_HOME Overrides JAVA_HOME.
#
#   INGRID_HEAPSIZE  heap to use in mb, if not setted we use 1000.
#
#   INGRID_OPTS      addtional java runtime options
#

CLASS=de.ingrid.iplug.PlugServer
sh starter.sh $CLASS --descriptor conf/jxta.conf.xml --busurl 
# sh starter.sh $CLASS 11111 11112 ibus.de 11113