#!/bin/bash

# Iterate over each file in the current directory
for file in *; do
    # Check if the item is a file
    if [ -f "$file" ]; then
        # Calculate MD5 checksum and save it to a file
        md5sum "$file" | cut -d' ' -f1 > "$file.md5"
        
        # Calculate SHA1 checksum and save it to a file
        sha1sum "$file" | cut -d' ' -f1 > "$file.sha1"
    fi
done
