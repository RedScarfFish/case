import java.util.Map;
import lrapi.lr;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class TestReplicas implements TestCaseValidater{
	public boolean validate(Map<String, Object> bundle){
		System.out.println(bundle.get("something"));
		return true;
	}
	public boolean validate(String ret){
		if("".equals(ret)){
			lr.error_message("return is empty");
			return false;
		}else{
			try{
				JsonObject jsonObj = Json.toJsonObj(ret);
				System.out.println("modifyReplicas" + jsonObj);
				if(jsonObj.get("ret").getAsBoolean() == false){
					lr.error_message(jsonObj.get("error").getAsString());
					return false;
				}else{
//					boolean isSuccess = true;
					JsonObject jsData = jsonObj.getAsJsonObject("data");
					System.out.println("totalStatue is" + jsData.get("totalStatue").getAsString());
					JsonObject jsRc = jsData.getAsJsonObject("rc");
					JsonObject jsSpec = jsRc.getAsJsonObject("spec");
					String con = jsSpec.get("replicas").getAsString();
					System.out.println("replicas is " + con);
//					SaveData saveData = new SaveData();
//					saveData.save(con);
					return true;
				}
			}catch(Exception err){
				lr.error_message("["+ret+"]exception:"+err.getMessage());
				return false;
			} 
		}
		
	}
}