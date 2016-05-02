systemctl stop serial-getty@ttyAMA0
systemctl disable serial-getty@ttyAMA0
dpkg-reconfigure gpsd
gpsd /dev/ttyAMA0 -F /var/run/gpsd.sock
#python GPSReading.py
