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
					boolean isSuccess = true;
					JsonObject js1 = jsonObj.getAsJsonObject("data");
					JsonArray itemArr = js1.getAsJsonArray("items");
					for(JsonElement jsonElement : itemArr){
						JsonObject item = jsonElement.getAsJsonObject();
						JsonObject statusObj = item.getAsJsonObject("status");
						JsonObject specObj = item.getAsJsonObject("spec");
						JsonArray conditionArr = statusObj.getAsJsonArray("conditions");
						JsonArray volumeArr = specObj.getAsJsonArray("volumes");
//						String name = new String();
						for(JsonElement volumeEle : volumeArr){
							JsonObject volumeObj = volumeEle.getAsJsonObject();
//							System.out.println(volumeObj);
							String volName = volumeObj.get("name").getAsString();
							
							if(!("default-token-e0d8c".equals(volName))){
								JsonObject glusterfs = volumeObj.getAsJsonObject("glusterfs");
								String endpoints = glusterfs.get("endpoints").getAsString();
//								name = endpoints;
								if("glusterfs-cluster".equals(endpoints)){
									System.out.println("name:" + volName +",claimName: " + endpoints);
//									name = endpoints;
								}else{
									lr.error_message("volumes is different");
									isSuccess = false;
								}
							}
							
						}
						
						if("Running".equals(statusObj.get("phase").getAsString())){
							System.out.println("status is " + statusObj.get("phase").getAsString());
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
					return isSuccess;
				}
			}catch(Exception err){
				lr.error_message("["+ret+"]exception:"+err.getMessage());
				return false;
			} 
		}
	}
	
}