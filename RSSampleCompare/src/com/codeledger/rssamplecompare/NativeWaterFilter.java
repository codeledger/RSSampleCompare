package com.codeledger.rssamplecompare;

public class NativeWaterFilter {

	public static native void nativeWaterFilter(int startWidth, int startHeight, int width, int height, int range, int[] inPixels, int[] outPixels);

	static {
		System.loadLibrary("nativeWaterFilter");
	}
}
