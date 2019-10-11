package com.esri.arcgis.performance.test;

public class Tuple {
  long requestTime;
  long returnedFeatures;

  public Tuple(long requestTime, long returnedFeatures) {
    this.requestTime = requestTime;
    this.returnedFeatures = returnedFeatures;
  }
}
