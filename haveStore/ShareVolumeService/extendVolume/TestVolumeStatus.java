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
					boolean isUse = true;
					JsonArray dataArr = jsonObj.get("data").getAsJsonArray();
					for(JsonElement volumeEle : dataArr){
						JsonObject volume = volumeEle.getAsJsonObject();
						String name = volume.get("name").getAsString();
//						String status = volume.get("status").getAsString();
						if("extendvolume".equals(name)){
							JsonArray mountArr = volume.get("mountAppNameArray").getAsJsonArray();
							System.out.println(mountArr);
							int i = 0;
							if(mountArr != null && !"".equals(mountArr.toString())){
								for(JsonElement mountEle : mountArr){
									i++;
									String mountAppName = mountEle.getAsString();
									System.out.println(name + "volume is use, the mount app name is : "+mountAppName);
								}
							}else{
								lr.error_message(name + "volume is not use");
								isUse = true;
							}
//							
							if(i != 1){
								lr.error_message("mountApp number is " + i);
								isUse = false;
							}else{
								System.out.println("voulme is mount success");
							}
						}else if("extendvolume-1".equals(name)){
							JsonArray mountArr = volume.get("mountAppNameArray").getAsJsonArray();
							System.out.println(mountArr);
							int i = 0;
//							String mountAppName = new String();
							if(mountArr != null && !"".equals(mountArr.toString())){
								for(JsonElement mountEle : mountArr){
									i++;
									String mountAppName = mountEle.getAsString();
									System.out.println(name + "volume is use, the mount app name is : "+mountAppName);
								}
							}else{
								lr.error_message(name + "volume is not use");
								isUse = true;
							}
							if(i != 1){
								lr.error_message("mountApp number is " + i);
								isUse = false;
							}else{
								System.out.println("voulme is mount success");
							}
						}
					}
					return isUse;
				}
			}catch(Exception err){
				lr.error_message("["+ret+"]exception:"+err.getMessage());
				return false;
			}
		}	
		
	}
}