import java.util.Map;
import lrapi.lr;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;

public class TestVolumeStatus implements TestCaseValidater{
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
						if("notshare".equals(name)){
							JsonArray mountArr = volume.get("mountAppNameArray").getAsJsonArray();
							for(JsonElement mountEle : mountArr){
								if(mountEle != null){
									String mountAppName = mountEle.getAsString();
									System.out.println("volume is use, the mount app name is : "+mountAppName);
									isNotUse = false;
								}else{
									System.out.println("volume is not use");
								}
							}
						}else if("notshare-1".equals(name)){
							JsonArray mountArr = volume.get("mountAppNameArray").getAsJsonArray();
							for(JsonElement mountEle : mountArr){
								if(mountEle != null){
									String mountAppName = mountEle.getAsString();
									System.out.println("volume is use, the mount app name is : "+mountAppName);
									isNotUse = false;
								}else{
									System.out.println("volume is not use");
								}
							}

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