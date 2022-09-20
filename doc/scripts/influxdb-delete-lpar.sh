#!/bin/bash

# For InfluxDB v. 1.x

if [ "$#" -ne 1 ]; then
	echo "Usage: $0 <lpar>"
	exit 1
fi


DB="hmci"
LPAR=$1

for s in $(influx -database ${DB} -execute 'SHOW SERIES' -format column | grep $LPAR); do
	n=$(echo $s | cut -f 1 -d,)

	influx -database ${DB} -execute "DELETE FROM ${n} WHERE \"lparname\"=\"${LPAR}\" AND time > '1980-01-01';"
	influx -database ${DB} -execute "DROP SERIES FROM ${n} WHERE \"lparname\"=\"${LPAR}\";"
done
