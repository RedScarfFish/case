import java.util.Map;
import lrapi.lr;
import com.google.gson.JsonObject;

public class TestCreat implements TestCaseValidater{
	public boolean validate(Map<String, Object> bundle){
		System.out.println(bundle.get("something"));
		return true;
	}
	public boolean validate(String ret){
//	System.out.println("create validate");
		if("".equals(ret)){
			lr.error_message("return is empty");
			return false;
		}else{
			try{
				JsonObject jsonObj = Json.toJsonObj(ret);
				if(jsonObj.get("ret").getAsBoolean() == false){
					lr.error_message(jsonObj.get("error").getAsString());
					return false;
				}else{
//					System.out.println(jsonObj);
					System.out.println(jsonObj.get("ret").getAsBoolean()+",create job success");
					return true;
				}
//				return true;
			}catch(Exception err){
				err.printStackTrace();
				lr.error_message("["+ret+"]exception:"+err.getMessage());
				return false;
			}
		}	
		
	}
}