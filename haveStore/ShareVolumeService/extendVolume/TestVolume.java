import java.util.Map;
import lrapi.lr;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

public class TestVolume implements TestCaseValidater{
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
					boolean isNotUse = true;
					JsonArray dataArr = jsonObj.get("data").getAsJsonArray();
					for(JsonElement volumeArr : dataArr){
						JsonObject volume = volumeArr.getAsJsonObject();
						String name = volume.get("name").getAsString();
//						String status = volume.get("status").getAsString();
//						String mountAppName = new String();
						if("extendvolume".equals(name) || "extendvolume-1".equals(name)){
							isNotUse = false;
							lr.error_message("volume test is not delete");
						}else{
							isNotUse = true;
						}
					}
					return isNotUse;
				}
			}catch(Exception err){
				lr.error_message("["+ret+"]exception:"+err.getMessage());
				return false;
			}
		}	
		
	}
}