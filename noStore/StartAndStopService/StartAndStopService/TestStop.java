import java.util.Map;
import lrapi.lr;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class TestStop implements TestCaseValidater{
	public boolean validate(Map<String, Object> bundle){
		System.out.println(bundle.get("something"));
		return true;
	}
	public boolean validate(String ret){
//		System.out.println("get pod validate");
//		System.out.println(ret);
		if("".equals(ret)){
			lr.error_message("result is empty");
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
					JsonObject dataJson = jsonObj.getAsJsonObject("data");
					JsonArray itemArr = dataJson.getAsJsonArray("items");
					System.out.println("this items is"+itemArr);
					int i = 0;
					if(itemArr != null ){
//						System.out.println("-----itemArr-----");
						for(JsonElement jsonElement : itemArr){
							JsonObject item = jsonElement.getAsJsonObject();
							i++;
//							System.out.println("item  is " + item);
						}
						if(i == 0){
								System.out.println("item validate stop success");
								
						}else{
							isSuccess = false;
							lr.error_message("item is exist");
							}
					}else{
						isSuccess = false;
						lr.error_message("items is null ");
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