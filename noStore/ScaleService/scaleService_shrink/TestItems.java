import java.util.Map;
import lrapi.lr;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class TestItems implements TestCaseValidater{
	public boolean validate(Map<String, Object> bundle){
		System.out.println(bundle.get("something"));
		return true;
	}
	public boolean validate(String ret){
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
					int i = 0;
					if(itemArr != null ){
						for(JsonElement jsonElement : itemArr){
							JsonObject item = jsonElement.getAsJsonObject();
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
//						System.out.println("items number = "+i);
//						ReadData read = new ReadData();
//						String fileContent = read.readFileContent("C:\\Users\\hjqi\\Documents\\data.txt");
//						int rep = Integer.parseInt(fileContent);
//						System.out.println(fileContent);
						
						if(1 == i){
							System.out.println("change living exmaple success,itme = " + i);
						}else{
							isSuccess = false;
							lr.error_message("service number is not expected, itme = " + i);
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