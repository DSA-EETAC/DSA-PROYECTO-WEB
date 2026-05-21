package BDD.orm.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ObjectHelper {
    public static String[] getFields(Object entity) {

        Class theClass = entity.getClass();

        Field[] fields = theClass.getDeclaredFields();

        String[] sFields = new String[fields.length];
        int i = 0;

        for (Field f : fields) sFields[i++] = f.getName();

        return sFields;

    }


    public static void setter(Object object, String property, Object value) {
        try { // Convierte "nombre" en "Nombre" para poder llamar a "setNombre"
            String propCapitalizada = property.substring(0, 1).toUpperCase() + property.substring(1);
            String nombreMetodo = "set" + propCapitalizada;

            Method[] metodos = object.getClass().getMethods();
            for (Method m : metodos) {
                if (m.getName().equals(nombreMetodo)) {
                    m.invoke(object, value);
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Object getter(Object object, String property) {
        try {
            // Convierte "nombre" en "Nombre" para poder llamar a "getNombre"
            String propCapitalizada = property.substring(0, 1).toUpperCase() + property.substring(1);
            String nombreMetodo = "get" + propCapitalizada;

            Method getter = object.getClass().getMethod(nombreMetodo);
            return getter.invoke(object);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}