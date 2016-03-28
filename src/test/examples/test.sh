#!/bin/bash

function usage() {
  echo "${0} [options] directory|file" >&2
  echo "     directory - directory with the XML file to test" >&2
  echo "     file      - XML file to test" >&2
  echo "     options:" >&2
  echo "       -k: keep tmp files" >&2
  exit -1
}


function check_file_1() {
  file=$1
  echo -n "checking J->C ${file}... "
  # convert from xml to wbxml and then use C to compare MXL
  ${XML2WBXML} -o - ${file} 2>/dev/null | ${WBXML2XML} -o /tmp/tmp11.$$.xml - 2>/dev/null
  ${JXML2WBXML} ${file} - 2>/dev/null | ${WBXML2XML} -o /tmp/tmp12.$$.xml - 2>/dev/null
  if [ ! -e "/tmp/tmp11.$$.xml" ]
  then
    echo "tmp11 not generated!"
  elif [ ! -e "/tmp/tmp12.$$.xml" ]
  then
    echo "tmp12 not generated!"
  else
    diff -q /tmp/tmp11.$$.xml /tmp/tmp12.$$.xml >/dev/null 2>&1
    if [ $? -eq 0 ]
    then
      echo "OK"
    else
      echo "DIFFER"
    fi
  fi
  if $DELETE_FILES
  then
    rm -f /tmp/tmp11.$$.xml /tmp/tmp12.$$.xml
  fi
}

function check_file_2() {
  file=$1
  echo -n "checking C->J ${file}... "
  # convert from xml to wbxml and then use C to compare MXL
  (${XML2WBXML} -o - ${file} | ${JWBXML2XML} - /tmp/tmp21.$$.xml) >/dev/null 2>&1
  (${JXML2WBXML} ${file} - | ${JWBXML2XML} - /tmp/tmp22.$$.xml) >/dev/null 2>&1
  if [ ! -e "/tmp/tmp21.$$.xml" ]
  then
    echo "tmp21 not generated!"
  elif [ ! -e "/tmp/tmp22.$$.xml" ]
  then
    echo "tmp22 not generated!"
  else
    diff -q /tmp/tmp21.$$.xml /tmp/tmp22.$$.xml >/dev/null 2>&1
    if [ $? -eq 0 ]
    then
      echo "OK"
    else
      echo "DIFFER"
    fi
  fi
  if $DELETE_FILES
  then
    rm -f /tmp/tmp21.$$.xml /tmp/tmp22.$$.xml
  fi
}

WBXML2XML=wbxml2xml
XML2WBXML=xml2wbxml
JWBXML2XML="java -cp ../dist/wbxml-stream.jar es.rickyepoderi.wbxml.tools.Converter wbxml2xml"
JXML2WBXML="java -cp ../dist/wbxml-stream.jar es.rickyepoderi.wbxml.tools.Converter xml2wbxml"

DELETE_FILES=true

while getopts k opt
do
  case "${opt}" in
    k)   DELETE_FILES=false ;;
    [?]) usage ;;
  esac
done 
shift $(( $OPTIND - 1 ))

#
# check the first argument is a directory
if [ ! -d "$1" ] && [ ! -r "$1" ]
then
  usage
fi

#
# for every xml in the directory perform xml2wbxml

if [ -d "$1" ]
then
  for file in `ls -1 ${1}/*.xml`
  do
    check_file_1 "${file}"
    check_file_2 "${file}"
  done
else
  check_file_1 "${1}"
  check_file_2 "${1}"
fi
