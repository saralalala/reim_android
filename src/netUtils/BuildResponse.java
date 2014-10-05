package netUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import netUtils.Response.*;;

public class BuildResponse {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Object get_repsonse(Object resp, Class _g) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		Constructor con = null;
		try {
			con = _g.getConstructor(new Class[]{Object.class});
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Object obj=null;
		obj = con.newInstance(resp);
		BaseResponse _obj = (BaseResponse)obj;
		_obj.forceConstructData();
		return (Object)_obj;
	}
}
