/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 4.0.2
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package resources;

public final class ztypes {
  public final static ztypes zdoc = new ztypes("zdoc");
  public final static ztypes zinc = new ztypes("zinc");
  public final static ztypes zadd = new ztypes("zadd");
  public final static ztypes zmax = new ztypes("zmax");
  public final static ztypes zmin = new ztypes("zmin");
  public final static ztypes ZTYPEreal = new ztypes("ZTYPEreal");
  public final static ztypes wadd = new ztypes("wadd");
  public final static ztypes wmax = new ztypes("wmax");
  public final static ztypes wmin = new ztypes("wmin");
  public final static ztypes ZTYPEend = new ztypes("ZTYPEend");

  public final int swigValue() {
    return swigValue;
  }

  public String toString() {
    return swigName;
  }

  public static ztypes swigToEnum(int swigValue) {
    if (swigValue < swigValues.length && swigValue >= 0 && swigValues[swigValue].swigValue == swigValue)
      return swigValues[swigValue];
    for (int i = 0; i < swigValues.length; i++)
      if (swigValues[i].swigValue == swigValue)
        return swigValues[i];
    throw new IllegalArgumentException("No enum " + ztypes.class + " with value " + swigValue);
  }

  private ztypes(String swigName) {
    this.swigName = swigName;
    this.swigValue = swigNext++;
  }

  private ztypes(String swigName, int swigValue) {
    this.swigName = swigName;
    this.swigValue = swigValue;
    swigNext = swigValue+1;
  }

  private ztypes(String swigName, ztypes swigEnum) {
    this.swigName = swigName;
    this.swigValue = swigEnum.swigValue;
    swigNext = this.swigValue+1;
  }

  private static ztypes[] swigValues = { zdoc, zinc, zadd, zmax, zmin, ZTYPEreal, wadd, wmax, wmin, ZTYPEend };
  private static int swigNext = 0;
  private final int swigValue;
  private final String swigName;
}
