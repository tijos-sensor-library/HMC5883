
package tijos.framework.sensor.hmc5883;

import java.io.IOException;

import tijos.framework.devicecenter.TiI2CMaster;
import tijos.util.BigBitConverter;
import tijos.util.Delay;
import tijos.util.LittleBitConverter;

/*
 *  Honeywell HMC5883L digital compass.
 *  Datasheet: http://www.ssec.honeywell.com/magnetic/datasheets/HMC5883L.pdf
 *  
 *  https://os.mbed.com/users/ritarosakai/code/MPU6050/docs/tip/
 */

class TiHMC5883Register {

	public static final int HMC5883L_ADDRESS = 0x1E;
	public static final int HMC5883L_REG_CONFIG_A = 0x00;
	public static final int HMC5883L_REG_CONFIG_B = 0x01;
	public static final int HMC5883L_REG_MODE = 0x02;
	public static final int HMC5883L_REG_OUT_X_M = 0x03;
	public static final int HMC5883L_REG_OUT_X_L = 0x04;
	public static final int HMC5883L_REG_OUT_Z_M = 0x05;
	public static final int HMC5883L_REG_OUT_Z_L = 0x06;
	public static final int HMC5883L_REG_OUT_Y_M = 0x07;
	public static final int HMC5883L_REG_OUT_Y_L = 0x08;
	public static final int HMC5883L_REG_STATUS = 0x09;
	public static final int HMC5883L_REG_IDENT_A = 0x0A;
	public static final int HMC5883L_REG_IDENT_B = 0x0B;
	public static final int HMC5883L_REG_IDENT_C = 0x0C;

}

class Vector {
	float XAxis;
	float YAxis;
	float ZAxis;
};

/**
 * Honeywell HMC5883 3-Axis Compass sensor driver
 * 
 * @author TiJOS
 *
 */
public class TiHMC5883 {

	public static final int HMC5883_ADDRESS = 0x1E << 1;

	public enum SAMPLES {
		SAMPLES_1, SAMPLES_2, SAMPLES_4, SAMPLES_8
	};

	public enum DATARATE {
		HZ_0_75, HZ_1_5, HZ_3, HZ_7_5, HZ_15, HZ_30, HZ_75
	};

	public enum RANGE {
		GA_0_88, GA_1_3, GA_1_9, GA_2_5, GA_4, GA_4_7, GA_5_6, GA_8_1
	};

	public enum MODE {
		CONTINOUS, SINGLE, IDLE
	};

	/**
	 * TiI2CMaster object
	 */
	private TiI2CMaster i2cmObj;

	private int i2cSlaveAddr = HMC5883_ADDRESS;

	private byte[] data = new byte[6];

	float mgPerDigit;
	Vector v;
	int xOffset, yOffset;

	/**
	 * Initialize with I2C master and default I2C slave address 0x1E
	 * 
	 * @param i2c
	 */
	public TiHMC5883(TiI2CMaster i2c) {
		this(i2c, HMC5883_ADDRESS);
	}

	/**
	 * Initialize with I2C master and slave address
	 * 
	 * @param i2c
	 * @param address
	 */
	public TiHMC5883(TiI2CMaster i2c, int address) {
		this.i2cmObj = i2c;
		this.i2cSlaveAddr = address;
	}

	public void initialize() throws IOException {
		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_IDENT_A, data, 0, 1);
		if (data[0] != 0x48)
			throw new IOException("Invalid identity");

		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_IDENT_B, data, 0, 1);
		if (data[0] != 0x34)
			throw new IOException("Invalid identity");

		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_IDENT_C, data, 0, 1);
		if (data[0] != 0x33)
			throw new IOException("Invalid identity");

		setRange(RANGE.GA_1_3);
		setMeasurementMode(MODE.CONTINOUS);
		setDataRate(DATARATE.HZ_15);
		setSamples(SAMPLES.SAMPLES_1);

		mgPerDigit = 0.92f;

	}

	public Vector readRaw() throws IOException {
		v.XAxis = readRegister16(TiHMC5883Register.HMC5883L_REG_OUT_X_M) - xOffset;
		v.YAxis = readRegister16(TiHMC5883Register.HMC5883L_REG_OUT_Y_M) - yOffset;
		v.ZAxis = readRegister16(TiHMC5883Register.HMC5883L_REG_OUT_Z_M);

		return v;
	}

	public Vector readNormalize() throws IOException {
		v.XAxis = ((float) readRegister16(TiHMC5883Register.HMC5883L_REG_OUT_X_M) - xOffset) * mgPerDigit;
		v.YAxis = ((float) readRegister16(TiHMC5883Register.HMC5883L_REG_OUT_Y_M) - yOffset) * mgPerDigit;
		v.ZAxis = (float) readRegister16(TiHMC5883Register.HMC5883L_REG_OUT_Z_M) * mgPerDigit;

		return v;
	}

	public void setOffset(int xo, int yo) {
		xOffset = xo;
		yOffset = yo;
	}

	public void setRange(RANGE range) throws IOException {
		switch (range) {
		case GA_0_88:
			mgPerDigit = 0.073f;
			break;

		case GA_1_3:
			mgPerDigit = 0.92f;
			break;

		case GA_1_9:
			mgPerDigit = 1.22f;
			break;

		case GA_2_5:
			mgPerDigit = 1.52f;
			break;

		case GA_4:
			mgPerDigit = 2.27f;
			break;

		case GA_4_7:
			mgPerDigit = 2.56f;
			break;

		case GA_5_6:
			mgPerDigit = 3.03f;
			break;

		case GA_8_1:
			mgPerDigit = 4.35f;
			break;

		default:
			break;
		}

		int value = (int) range.ordinal() << 5;

		data[0] = (byte) value;

		this.i2cmObj.write(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_CONFIG_B, data, 0, 1);
	}

	public RANGE getRange() throws IOException {
		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_CONFIG_B, data, 0, 1);

		return RANGE.values()[data[0]];
	}

	public void setMeasurementMode(MODE mode) throws IOException {

		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_MODE, data, 0, 1);
		int value = data[0];
		value &= 0b11111100;
		value |= mode.ordinal();

		data[0] = (byte) value;
		this.i2cmObj.write(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_MODE, data, 0, 1);

	}

	public MODE getMeasurementMode() throws IOException {
		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_MODE, data, 0, 1);
		int value = data[0];
		value &= 0b00000011;

		return MODE.values()[value];

	}

	public void setDataRate(DATARATE dataRate) throws IOException {
		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_CONFIG_A, data, 0, 1);
		int value = data[0];
		value &= 0b11100011;
		value |= (dataRate.ordinal() << 2);

		data[0] = (byte) value;
		this.i2cmObj.write(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_CONFIG_A, data, 0, 1);

	}

	public DATARATE getDataRate() throws IOException {
		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_CONFIG_A, data, 0, 1);
		int value = data[0];
		value &= 0b00011100;
		value >>= 2;

		return DATARATE.values()[value];
	}

	public void setSamples(SAMPLES samples) throws IOException {
		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_CONFIG_A, data, 0, 1);
		int value = data[0];
		value &= 0b10011111;
		value |= (samples.ordinal() << 5);

		data[0] = (byte) value;
		this.i2cmObj.write(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_CONFIG_A, data, 0, 1);
	}

	public SAMPLES getSamples() throws IOException {
		this.i2cmObj.read(this.i2cSlaveAddr, TiHMC5883Register.HMC5883L_REG_CONFIG_A, data, 0, 1);
		int value = data[0];
		value &= 0b01100000;
		value >>= 5;

		return SAMPLES.values()[value];

	}

	// Read word from register
	int readRegister16(int reg) throws IOException {
		this.i2cmObj.read(this.i2cSlaveAddr, reg, data, 0, 2);
		return BigBitConverter.ToInt16(data, 0);

	}

}
