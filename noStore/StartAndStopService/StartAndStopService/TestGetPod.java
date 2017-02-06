import java.util.Map;
import lrapi.lr;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class TestGetPod implements TestCaseValidater{
	public boolean validate(Map<String, Object> bundle){
		System.out.println(bundle.get("something"));
		return true;
	}
	public boolean validate(String ret){
//		System.out.println("get pod validate");
//		System.out.println(ret);
		if("".equals(ret)){
			lr.error_message("return is empty");
			return false;
		}else{
			try{
				JsonObject jsonObj = Json.toJsonObj(ret);
//				System.out.println(jsonObj);
				if(jsonObj.get("ret").getAsBoolean() == false){
					lr.error_message(jsonObj.get("error").getAsString());
					return false;
				}else{
					boolean isSuccess = true;
					JsonObject js1 = jsonObj.getAsJsonObject("data");
					JsonArray itemArr = js1.getAsJsonArray("items");
					int i = 0;
					for(JsonElement jsonElement : itemArr){
						JsonObject item = jsonElement.getAsJsonObject();
//						System.out.println(item);
						i++;
						JsonObject statusObj = item.getAsJsonObject("status");
						JsonArray conditionArr = statusObj.getAsJsonArray("conditions");
						
						if("Running".equals(statusObj.get("phase").getAsString())){
							System.out.println(statusObj.get("phase").getAsString());
						}else{
							for(JsonElement jsonElement1 : conditionArr){
								JsonObject conObj = jsonElement1.getAsJsonObject();
								if("False".equals(conObj.get("status").getAsString())){
									lr.error_message("error:"+conObj.get("message")+";reson:"+conObj.get("reson"));
								}
							}
							isSuccess = false;
						}
						
					}
					if(i != 2){
						lr.error_message("error.itmes number is " + i);
					}else{
						System.out.println("itmes number is " + i);
					}
					return isSuccess;
				}
			}catch(Exception err){
				lr.error_message("["+ret+"]exception:"+err.getMessage());
				return false;
			} 
		}
	}
	
}