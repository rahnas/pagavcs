#!/bin/sh

rm -f -r ../temp-build

for dist in precise quantal raring
do
	./upload-ppa.sh $dist
done
