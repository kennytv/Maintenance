#!/bin/bash
VERSION="3.4.8" #Version of the ServerListPlus Build

URL="https://github.com/Minecrell/ServerListPlus/releases/download/${VERSION}/ServerListPlus-${VERSION}-Universal.jar"
FILENAME="ServerListPlus-${VERSION}.jar"

confirm=true

if [ "$1" == "skip-input" ]; then
	confirm=false
fi


hash mvn 2>/dev/null || {
echo >&2 "Maven is required."
exit 1
}

echo -e "\e[32mServerListPlus version ${VERSION}\e[0m"

if [ "$confirm" = true ] ; then
    read -p $'\e[32mpress \e[5m[Enter] \e[25mto start Downloading...\e[0m'
fi

echo -e $'\e[32mDownloading...\e[0m'
wget -O $FILENAME $URL

if [ -e $FILENAME ]; then
  echo -e "\e[32mFile downloaded.\e[0m"
else
  echo "\e[32mError while downloading file.\e[0m"
  exit 1
fi

echo -e "\e[32mInstalling $FILENAME in local maven repository.\e[0m"

if [ "$confirm" = true ] ; then
    read -p $'\e[32mpress \e[5m[Enter] \e[25mto start...\e[0m'
fi

mvn install:install-file -Dfile=$FILENAME -DgroupId=net.minecrell -DartifactId=ServerListPlus -Dversion=$VERSION -Dpackaging=jar
echo -e "\e[32mRemoving temp jar file...\e[0m"
rm -f $FILENAME
echo -e "\e[32mFinish!"
