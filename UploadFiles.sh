#!/bin/bash

wifi_connected="0"
eth_connected="0"
test_address="https://potholess.herokuapp.com/vibrations"
main_directory="/home/pi/Documents/PLDSmartCity/completedFiles"
RESP_CODE="0"

#getting wifi connection status
read wifi_connected</sys/class/net/wlan0/carrier
#getting ethernet connection status
read eth_connected</sys/class/net/eth0/carrier

#if a connection exist
if [ $wifi_connected = "1" -o $eth_connected = "1" ]; then
	#silently checking server reachability
	wget -q --spider $test_address
	if [ $? -eq 0 ];
	then
		echo "Online - Starting file upload"
		#Sending every file in the finished file directory
		for file in $main_directory/*.json

	do
			curl -H "Content-Type: application/json" -d @$file -w 'RESP_CODE:%{response_code}' $test_address #| echo $RESP_CODE
			#Code 201 : CREATED : if created file is removed : if it failed, the file is kept for another try next time
			if [ $response_code = "201" ]; then
				echo "Je supprime!"
				rm $file
			else
				echo "Je supprime pas!"
			fi
		done
	fi
fi
