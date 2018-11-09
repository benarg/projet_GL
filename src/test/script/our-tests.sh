#! /bin/sh

# This file will be executed by mvn test from the main project directory
# It will not be executed from this directory

cd "$(dirname "$0")"

syntTest="python basic-synt.py -i -v"
lexTest="python basic-lex.py -i -v"
contTest="python basic-context.py -i -v"
imaTest="python basic-exe.py -i -v"
optTest="python basic-options.py -b -p"

echo " "
echo "###############################"
echo "###  Executing Option Tests ###"
echo "###############################"
echo " "

eval $optTest

echo " "
echo "###############################"
echo "####  Executing Lex Tests  ####"
echo "###############################"
echo " "

eval $lexTest

echo " "
echo "###############################"
echo "####  Executing Synt Tests  ###"
echo "###############################"
echo " "

eval $syntTest

echo " "
echo "###############################"
echo "####  Executing Cont Tests  ###"
echo "###############################"
echo " "

eval $contTest

echo " "
echo "###############################"
echo "###  Executing Ima/BC Tests ###"
echo "###############################"
echo " "

eval $imaTest

#echo " "
