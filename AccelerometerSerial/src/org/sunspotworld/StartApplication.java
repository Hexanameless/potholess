

package org.sunspotworld;

import com.sun.spot.resources.Resources;
import com.sun.spot.resources.transducers.IAccelerometer3D;
import com.sun.spot.resources.transducers.ITriColorLED;
import com.sun.spot.resources.transducers.ITriColorLEDArray;
import com.sun.spot.service.BootloaderListenerService;
import com.sun.spot.util.Utils;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

/**
 * This application is the 'on SPOT' portion of the SendDataDemo. It
 * periodically samples a sensor value on the SPOT and transmits it to
 * a desktop application (the 'on Desktop' portion of the SendDataDemo)
 * where the values are displayed.
 *
 * @author: Vipul Gupta
 * modified: Ron Goldman
 */
public class StartApplication extends MIDlet {

    private static final int SAMPLE_PERIOD = 100;  // in milliseconds
    private static final long DEBOUNCE_TIME = 1000; // in seconds

    protected void startApp() throws MIDletStateChangeException {

        IAccelerometer3D accel = (IAccelerometer3D) Resources.lookup(IAccelerometer3D.class);
        ITriColorLEDArray leds = (ITriColorLEDArray)Resources.lookup(ITriColorLEDArray.class);

        for (int i = 0; i<8 ; i++)
        {
            leds.getLED(i).setRGB(i*36, 255-(i*36), 0);
        }
        System.out.println("Starting sensor sampler application on ");


	// Listen for downloads/commands over USB connection
	    BootloaderListenerService.getInstance().start();

        double prec = 0;
        long precSend = 0;
        double max = 0;

        while (true) {
            try {
                // Get the current time and sensor reading
                long now = System.currentTimeMillis();
                //int reading = lightSensor.getValue();
               double reading = accel.getAccelZ();       // get current acceleration along Z axis
               double squareDiff = (reading - prec)*(reading - prec);
               if(squareDiff > 0.25){
                    // Flash an LED to indicate a sampling event
                    for(int i = 0; i < squareDiff ; i += 2)
                    {
                        leds.getLED(i/2).setOn();
                    }
                    Utils.sleep(50);
                    leds.setOff();
                    if(now-precSend > DEBOUNCE_TIME && max != 0){
                        System.out.println(max);
                        max = 0;
                        precSend = now;
                    }else if (max<squareDiff) {
                        max = squareDiff;
                    }

               }
               prec = reading;

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
