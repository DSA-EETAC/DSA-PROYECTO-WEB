package BDDtests;
import Model.User;
import BDD.orm.util.ObjectHelper;
import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;

public class ObjectHelperTest {


    @Test
    public void setterTest() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        User e = new User("nombreViejo","contraVieja", "correo@viejo.com");

        ObjectHelper.setter(e, "nombre", "Pepito");
        ObjectHelper.setter(e, "password", "321");

        Assert.assertEquals("Pepito", e.getNombre());
        Assert.assertEquals("321", e.getPassword());

    }


    @Test
    public void getterTest() throws NoSuchFieldException, InvocationTargetException, IllegalAccessException {
        User e = new User("aaron","1234", "aaron@grillo.com" );


        String userName = (String) ObjectHelper.getter(e, "nombre");
        String userPswrd = (String) ObjectHelper.getter(e, "password");


        Assert.assertEquals("aaron", userName);
        Assert.assertEquals("1234", userPswrd);

    }

}
