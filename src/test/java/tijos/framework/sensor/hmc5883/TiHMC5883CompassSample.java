package tijos.framework.sensor.hmc5883;

import java.io.IOException;

import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.util.Delay;

public class TiHMC5883CompassSample {

	public static void main(String[] args) {
		try {
			/*
			 * 定义使用的TiI2CMaster port
			 */
			int i2cPort0 = 0;

			/*
			 * 资源分配， 将i2cPort0分配给TiI2CMaster对象i2c0
			 */
			TiI2CMaster i2c0 = TiI2CMaster.open(i2cPort0);

			TiHMC5883 hmc5883 = new TiHMC5883(i2c0);

			hmc5883.initialize();

			// Set measurement range
			hmc5883.setRange(TiHMC5883.RANGE.GA_1_3);

			// Set measurement mode
			hmc5883.setMeasurementMode(TiHMC5883.MODE.CONTINOUS);

			// Set data rate
			hmc5883.setDataRate(TiHMC5883.DATARATE.HZ_30);

			// Set number of samples averaged
			hmc5883.setSamples(TiHMC5883.SAMPLES.SAMPLES_1);

			// Set calibration offset. See HMC5883L_calibration.ino
			hmc5883.setOffset(0, 0);

			while (true) {
				Vector norm = hmc5883.readNormalize();

				// Calculate heading
				double heading = Math.atan2(norm.YAxis, norm.XAxis);

				// Set declination angle on your location and fix heading
				// You can find your declination on:
				// http://magnetic-declination.com/
				// (+) Positive or (-) for negative
				// For Bytom / Poland declination angle is 4'26E (positive)
				// Formula: (deg + (min / 60.0)) / (180 / M_PI);
				double declinationAngle = (4.0 + (26.0 / 60.0)) / (180 / Math.PI);
				heading += declinationAngle;

				// Correct for heading < 0deg and heading > 360deg
				if (heading < 0) {
					heading += 2 * Math.PI;
				}

				if (heading > 2 * Math.PI) {
					heading -= 2 * Math.PI;
				}

				// Convert to degrees
				double headingDegrees = heading * 180 / Math.PI;

				// Output
				System.out.print(" Heading = " + heading );
				System.out.println(" Degress = " + headingDegrees);

				Delay.msDelay(100);
			}

		} catch (IOException ie) {
			ie.printStackTrace();
		}
	}
}
