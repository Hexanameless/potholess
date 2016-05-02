package org.sunspotworld;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.Utils;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * This application sends data over serial port (9600bauds).
 * When an acceleration difference is over a threshold, it is sent over the
 * serial port
 * To reduce the quantity of data generated, only the maximum value is sent if
 * there are more than one value in a second.
 *
 * @author: Emilien Bai
 */
public class StartApplication extends MIDlet {

    private static final int LED_NUMBER = 8; //number of leds
    private static final int SAMPLE_PERIOD = 100;  // in milliseconds
    private static final long DEBOUNCE_TIME = 1000; // in milliseconds
    private static final double VIBRATION_THRESHOLD  = 0.5; //in G(s)
    private static final double SQUARE_VIBE = VIBRATION_THRESHOLD*VIBRATION_THRESHOLD; //Threshold used in the program

    protected void startApp() throws MIDletStateChangeException {

        IAccelerometer3D accel = (IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);
        ITriColorLEDArray leds = (ITriColorLEDArray)Resources.lookup(ITriColorLEDArray.class);

        //Setting LED colors for a better visualization of the acceleration on sunspot
        for (int i = 0; i<8 ; i++)
        {
            leds.getLED(i).setRGB(i*36, 255-(i*36), 0);
        }

      	// Listen for downloads/commands over USB connection
	    BootloaderListenerService.getInstance().start();

        double prec = 0; //precedent value used to find difference between two accelerations
        long precSend = 0; //Time when the last value has been sent
        double max = 0; //maximum value from the last second

        while (true) {
            try {
               // Get the current time and sensor reading
               long now = System.currentTimeMillis();
               //int reading = lightSensor.getValue();
               double reading = accel.getAccelZ();       // get current acceleration along Z axis
               //determining a positive value for the acceleration difference
               double squareDiff = (reading - prec)*(reading - prec);
               if(squareDiff > SQUARE_VIBE){
                    // Flash an LED to indicate a sampling event
                    for(int i = 0; (i < squareDiff&& i < LED_NUMBER) ; i += 1)
                    {
                        leds.getLED(i).setOn();
                    }
                    Utils.sleep(50);
                    leds.setOff();
                    //Send the maximum value of the last second
                    if(now-precSend > DEBOUNCE_TIME && max != 0){
                        System.out.println(max);
                        max = 0;
                        precSend = now;
                    }else if (max<squareDiff) {//update the maximum value of the last second
                        max = squareDiff;
                    }
               }
               prec = reading; // update precedent value
                // Go to sleep to conserve battery
                Utils.sleep(SAMPLE_PERIOD - (System.currentTimeMillis() - now));
            } catch (Exception e) {
                System.err.println("Caught " + e + " while collecting/sending sensor sample.");
            }
        }
    }

    protected void pauseApp() {
        // This will never be called by the Squawk VM
    }

    protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
        // Only called if startApp throws any exception other than MIDletStateChangeException
    }
}