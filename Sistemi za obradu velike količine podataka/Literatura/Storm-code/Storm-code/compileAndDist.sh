#!/bin/bash

mvn clean install package
cp target/*jar-with-dependencies.jar dist/data/exclamation/.  
cp target/*jar-with-dependencies.jar dist/data/rollingcount/. 
cp target/*jar-with-dependencies.jar dist/data/wordcount/.
cp target/*jar-with-dependencies.jar dist/data/wordcountwindowbased/.
echo "Done!"
