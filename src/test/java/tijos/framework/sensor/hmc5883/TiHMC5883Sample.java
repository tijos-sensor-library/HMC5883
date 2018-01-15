package tijos.framework.sensor.hmc5883;

import java.io.IOException;

import tijos.framework.devicecenter.TiI2CMaster;
import tijos.framework.sensor.hmc5883.TiHMC5883;
import tijos.util.Delay;

public class TiHMC5883Sample {

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
			hmc5883.setDataRate(TiHMC5883.DATARATE.HZ_15);

			// Set number of samples averaged
			hmc5883.setSamples(TiHMC5883.SAMPLES.SAMPLES_1);

			checkSettings(hmc5883);

			int num = 100;
			while (num-- > 0) {
				try {

					Vector raw = hmc5883.readRaw();
					Vector norm = hmc5883.readNormalize();

					System.out.print(" Xraw = ");
					System.out.print(raw.XAxis);
					System.out.print(" Yraw = ");
					System.out.print(raw.YAxis);
					System.out.print(" Zraw = ");
					System.out.print(raw.ZAxis);
					System.out.print(" Xnorm = ");
					System.out.print(norm.XAxis);
					System.out.print(" Ynorm = ");
					System.out.print(norm.YAxis);
					System.out.print(" ZNorm = ");
					System.out.print(norm.ZAxis);
					System.out.println();

					Delay.msDelay(2000);
				} catch (Exception ex) {

					ex.printStackTrace();
				}

			}
		} catch (IOException ie) {
			ie.printStackTrace();
		}

	}

	public static void checkSettings(TiHMC5883 compass) throws IOException {
		System.out.print("Selected range: ");

		switch (compass.getRange()) {

		case GA_0_88:
			System.out.println("0.88 Ga");
			break;
		case GA_1_3:
			System.out.println("1.3 Ga");
			break;
		case GA_1_9:
			System.out.println("1.9 Ga");
			break;
		case GA_2_5:
			System.out.println("2.5 Ga");
			break;
		case GA_4:
			System.out.println("4 Ga");
			break;
		case GA_4_7:
			System.out.println("4.7 Ga");
			break;
		case GA_5_6:
			System.out.println("5.6 Ga");
			break;
		case GA_8_1:
			System.out.println("8.1 Ga");
			break;
		default:
			System.out.println("Bad range!");
		}

		System.out.print("Selected Measurement Mode: ");
		switch (compass.getMeasurementMode()) {
		case IDLE:
			System.out.println("Idle mode");
			break;
		case SINGLE:
			System.out.println("Single-Measurement");
			break;
		case CONTINOUS:
			System.out.println("Continuous-Measurement");
			break;
		default:
			System.out.println("Bad mode!");
		}

		System.out.print("Selected Data Rate: ");
		switch (compass.getDataRate()) {
		case HZ_0_75:
			System.out.println("0.75 Hz");
			break;
		case HZ_1_5:
			System.out.println("1.5 Hz");
			break;
		case HZ_3:
			System.out.println("3 Hz");
			break;
		case HZ_7_5:
			System.out.println("7.5 Hz");
			break;
		case HZ_15:
			System.out.println("15 Hz");
			break;
		case HZ_30:
			System.out.println("30 Hz");
			break;
		case HZ_75:
			System.out.println("75 Hz");
			break;
		default:
			System.out.println("Bad data rate!");
		}

		System.out.print("Selected number of samples: ");
		switch (compass.getSamples()) {
		case SAMPLES_1:
			System.out.println("1");
			break;
		case SAMPLES_2:
			System.out.println("2");
			break;
		case SAMPLES_4:
			System.out.println("4");
			break;
		case SAMPLES_8:
			System.out.println("8");
			break;
		default:
			System.out.println("Bad number of samples!");
		}

	}
}
