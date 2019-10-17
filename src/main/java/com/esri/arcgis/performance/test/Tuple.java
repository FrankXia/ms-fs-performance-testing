package com.esri.arcgis.performance.test;

public class Tuple {
  long requestTime;
  long returnedFeatures;
  int errorCode = 0;  // 0 => success, 1 => return null, 2 => not a valid return, 3 => error return

  public Tuple(long requestTime, long returnedFeatures, int errorCode) {
    this.requestTime = requestTime;
    this.returnedFeatures = returnedFeatures;
    this.errorCode = errorCode;
  }
}
