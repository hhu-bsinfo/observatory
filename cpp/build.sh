#!/bin/bash

mkdir -p build
cd build

for binding in "$@"; do
    cmake -D OBSERVATORY_BINDING:STRING=${binding} ..
    make
done
