package module.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public final class BaseTypesRestrictions {
    public final static Map<Class<?>, Double[]> restrictions = new HashMap<>();

    static {
        restrictions.put(Integer.class, new Double[]{(double) Integer.MIN_VALUE, (double) Integer.MAX_VALUE});
        restrictions.put(int.class, restrictions.get(Integer.class));
        restrictions.put(Double.class, new Double[]{Double.MIN_VALUE, Double.MAX_VALUE});
        restrictions.put(double.class, restrictions.get(Double.class));
        restrictions.put(Float.class, new Double[]{(double) Float.MIN_VALUE, (double) Float.MAX_VALUE});
        restrictions.put(float.class, restrictions.get(Float.class));
        restrictions.put(Byte.class, new Double[]{(double) Byte.MIN_VALUE, (double) Byte.MAX_VALUE});
        restrictions.put(byte.class, restrictions.get(Byte.class));
        restrictions.put(String.class, new Double[]{1D, 300D});
        restrictions.put(Date.class, new Double[]{0D, 32503593600000D});
        restrictions.put(Long.class, new Double[]{(double) Long.MIN_VALUE, (double) Long.MAX_VALUE});
        restrictions.put(long.class, restrictions.get(Long.class));
    }
}
