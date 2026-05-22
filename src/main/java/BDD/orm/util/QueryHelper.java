package BDD.orm.util;

import BDD.orm.util.QueryHelper;
import BDD.orm.util.ObjectHelper; // Haz lo mismo para el ObjectHelper si lo metiste en la misma carpeta
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class QueryHelper {

    public static String createQueryINSERT(Object entity) {

        StringBuffer sb = new StringBuffer("INSERT INTO ");
        sb.append(entity.getClass().getSimpleName()).append(" ");
        sb.append("(");

        String [] fields = ObjectHelper.getFields(entity);

        sb.append(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            sb.append(", ").append(fields[i]);
        }
        sb.append(") VALUES (?");

        // Construimos los interrogantes: (?, ?, ?)
        for (int i = 1; i < fields.length; i++) {
            sb.append(", ?");
        }
        sb.append(")");

        System.out.println(sb.toString());

        return sb.toString();
    }

    public static String createQuerySELECT(Object entity) {
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT * FROM ").append(entity.getClass().getSimpleName());
        sb.append(" WHERE ID = ?");

        return sb.toString();
    }


    public static String createSelectFindAll(Class theClass, HashMap<String, String> params) {

        Set<Map.Entry<String, String>> set = params.entrySet();

        StringBuffer sb = new StringBuffer("SELECT * FROM "+theClass.getSimpleName()+" WHERE 1=1");
        for (String key: params.keySet()) {
            sb.append(" AND "+key+"=?");
        }


        return sb.toString();
    }
}
