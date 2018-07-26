#!/bin/bash
#

cd ../repository/target/products

export CSSVER=$(cat cs-studio-ess/linux/gtk/x86_64/cs-studio/ess-version.txt)

echo "Fixing BATIK dependencies for CS-Studio ${CSSVER}..."

echo "Patching Window product:"
zip -d cs-studio-ess-${CSSVER}-win32.win32.x86_64.zip "plugins/org.apache.batik*1.8*.jar"; \

echo "Patching MacOS X product:"
zip -d cs-studio-ess-${CSSVER}-macosx.cocoa.x86_64.zip "ESS CS-Studio.app/Contents/Eclipse/plugins/org.apache.batik*1.8*.jar"; \

echo "Patching Linux product:"
mkdir cs-studio-ess-${CSSVER}-linux.gtk.x86_64
cd cs-studio-ess-${CSSVER}-linux.gtk.x86_64
tar -zxf ../cs-studio-ess-${CSSVER}-linux.gtk.x86_64.tar.gz
find ./cs-studio/plugins -type f -iname 'org.apache.batik*1.8*.jar' -delete -print
rm -f ../cs-studio-ess-${CSSVER}-linux.gtk.x86_64.tar.gz
tar -zcf ../cs-studio-ess-${CSSVER}-linux.gtk.x86_64.tar.gz .
cd ..
rm -fr cs-studio-ess-${CSSVER}-linux.gtk.x86_64
