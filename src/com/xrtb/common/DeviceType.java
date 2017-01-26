package com.xrtb.common;


public class DeviceType {
	public static final int MobileTablet = 1;
	public static final int PersonalComputer = 2;
	public static final int ConnectedTV = 3;
	public static final int Phone = 4;
	public static final int Tablet = 5;
	public static final int ConnectedDevice = 6;
	public static final int SetTopBox = 7;

	public static int adxToRtb(String type) {
		switch (type) {
		case "PHONE":
			return Phone;
		case "HIGHEND_PHONE":
			return Phone;
		case "TABLET":
			return Tablet;
		}
		return PersonalComputer;
	}
}
