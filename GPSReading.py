__author__  = "Maxime CHRIST"
__email__   = "maxime.christ@insa-lyon.fr"

import serial
import io, os, time, threading, json, dateutil.parser
from gps import *
from time import *

gpsd = None #seting the global variable
accelerometer = serial.Serial("/dev/ttyACM0", 9600)
os.system('clear') #clear the terminal (optional)

class GpsPoller(threading.Thread):
 	def __init__(self):
		threading.Thread.__init__(self)
		global gpsd #bring it in scope
		gpsd = gps(mode=WATCH_ENABLE) #starting the stream of info
		self.current_value = None
		self.running = True #setting the thread running to true

  	def run(self):
		global gpsd
		while gpsp.running:
	  		gpsd.next() #this will continue to loop and grab EACH set of gpsd info to clear the buffer

def isNan(num):
  	return num != num

def cpFile(file_name):
	file.write(']')
	file.close()
	src_path = "/home/pi/Documents/PLDSmartCity/"
	dest_path = src_path
	src_path += file_name
	dest_path += "completedFiles/"
	dest_path += file_name
	print "Copying",src_path, ' ',  dest_path
	os.rename(src_path, dest_path)

if __name__ == '__main__':
	gpsp = GpsPoller() # create the thread
	
	try:
		gpsp.start() # start it up
		
		while True:
	  		data_number = 0
			file_name = 'data'
			file_name += str(int(time()))
			file_name += '.json'
			
			file = open(file_name, 'w')
			file.write(unicode("["))
			while True:
				#It may take a second or two to get good data
				#print gpsd.fix.latitude,', ',gpsd.fix.longitude,'  Time: ',gpsd.utc

				os.system('clear')
				acceleration = float(accelerometer.readline())

				print ' Acceleration reading'
				print '----------------------------------------'
				print 'Bump        ' , acceleration
				print ' GPS reading'
				print '----------------------------------------'
				print 'latitude    ' , gpsd.fix.latitude
				print 'longitude   ' , gpsd.fix.longitude
				print 'time utc    ' , gpsd.utc
				fulldate = dateutil.parser.parse(gpsd.utc)
				#date = '"'
				date = fulldate.date()
				#date += '"'
			
				if acceleration < 3:
					tier = 1
				elif acceleration < 6:
					tier = 2
				elif acceleration < 9:
					tier = 3
				elif acceleration < 12:
					tier = 4
				else:
					tier = 5
				print date
				file.write(unicode(json.dumps({"lat":gpsd.fix.latitude,"lng":gpsd.fix.longitude,"date":str(date),"val":tier}, separators=(',',':'))))
				
				data_number += 1
				if (data_number > 100):
					cpFile(file_name)
					break
				else:
					file.write(',')
	except (KeyboardInterrupt, SystemExit): #when you press ctrl+c
		file.seek(-1, 1)
		file.truncate()
		pass

	print "Copying last file"
  	cpFile(file_name)
	print "\nKilling Thread..."
	gpsp.running = False
	gpsp.join() # wait for the thread to finish what it's doing
	print "Done.\nExiting."
