package org.bailey.taskfront.client;

import java.util.List;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.core.client.JsArrayBoolean;
import com.google.gwt.core.client.JsArrayInteger;
import com.google.gwt.core.client.JsArrayNumber;
import com.google.gwt.core.client.JsArrayString;

public final class JsArrays {
  public static JsArrayString fromArray(String... values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      JsArrayString ret = JavaScriptObject.createArray().cast();
      for (int i = 0, l = values.length; i < l; i++) {
        ret.set(i, values[i]);
      }
      return ret;
    }
  }
  
  public static String[] toArray(JsArrayString values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      int length = values.length();
      String[] ret = new String[length];
      for (int i = 0, l = length; i < l; i++) {
        ret[i] = values.get(i);
      }
      return ret;
    }
  }
  
  public static JsArrayBoolean fromArray(boolean... values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      JsArrayBoolean ret = JavaScriptObject.createArray().cast();
      for (int i = 0, l = values.length; i < l; i++) {
        ret.set(i, values[i]);
      }
      return ret;
    }
  }
  
  public static boolean[] toArray(JsArrayBoolean values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      int length = values.length();
      boolean[] ret = new boolean[length];
      for (int i = 0, l = length; i < l; i++) {
        ret[i] = values.get(i);
      }
      return ret;
    }
  }
  
  public static JsArrayInteger fromArray(int... values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      JsArrayInteger ret = JavaScriptObject.createArray().cast();
      for (int i = 0, l = values.length; i < l; i++) {
        ret.set(i, values[i]);
      }
      return ret;
    }
  }
  
  public static int[] toArray(JsArrayInteger values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      int length = values.length();
      int[] ret = new int[length];
      for (int i = 0, l = length; i < l; i++) {
        ret[i] = values.get(i);
      }
      return ret;
    }
  }
  
  public static JsArrayNumber fromArray(double... values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      JsArrayNumber ret = JavaScriptObject.createArray().cast();
      for (int i = 0, l = values.length; i < l; i++) {
        ret.set(i, values[i]);
      }
      return ret;
    }
  }
  
  public static double[] toArray(JsArrayNumber values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      int length = values.length();
      double[] ret = new double[length];
      for (int i = 0, l = length; i < l; i++) {
        ret[i] = values.get(i);
      }
      return ret;
    }
  }
  
public static <T extends JavaScriptObject> JsArray<T> fromArray(T... values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      JsArray<T> ret = JavaScriptObject.createArray().cast();
      for (int i = 0, l = values.length; i < l; i++) {
        ret.set(i, values[i]);
      }
      return ret;
    }
  }
  
  public static <T extends JavaScriptObject> T[] toArray(JsArray<? extends T> values) {
    if (GWT.isScript()) {
      return reinterpretCast(values);
    } else {
      int length = values.length();
      @SuppressWarnings("unchecked")
      T[] ret = (T[]) new JavaScriptObject[length];
      for (int i = 0, l = length; i < l; i++) {
        ret[i] = values.get(i);
      }
      return ret;
    }
  }
  
  public static JsArrayString fromList(List<String> list){
	  if (GWT.isScript()){
		  Object [] array = list.toArray();
		  return reinterpretCastToString(array);
	  } else {
	      JsArrayString ret = JavaScriptObject.createArray().cast();
	      for (int i = 0, l = list.size(); i < l; i++) {
	        ret.set(i, list.get(i));
	      }
	      return ret;
	  }
	  
  }
  private static native JsArrayString reinterpretCastToString(Object[] value) /*-{return value; }-*/;
  private static native JsArrayString reinterpretCast(String[] value) /*-{ return value; }-*/;
  private static native String[] reinterpretCast(JsArrayString value) /*-{ return value; }-*/;
  private static native JsArrayBoolean reinterpretCast(boolean[] value) /*-{ return value; }-*/;
  private static native boolean[] reinterpretCast(JsArrayBoolean value) /*-{ return value; }-*/;
  private static native JsArrayInteger reinterpretCast(int[] value) /*-{ return value; }-*/;
  private static native int[] reinterpretCast(JsArrayInteger value) /*-{ return value; }-*/;
  private static native JsArrayNumber reinterpretCast(double[] value) /*-{ return value; }-*/;
  private static native double[] reinterpretCast(JsArrayNumber value) /*-{ return value; }-*/;
  private static native <T extends JavaScriptObject> JsArray<T> reinterpretCast(T[] value) /*-{ return value; }-*/;
  private static native <T extends JavaScriptObject> T[] reinterpretCast(JsArray<T> value) /*-{ return value; }-*/;

  private JsArrays() {
  }
}
