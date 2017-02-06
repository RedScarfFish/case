import java.util.Map;
import lrapi.lr;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class GetListJob implements TestCaseValidater{
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
					JsonObject dataObj = jsonObj.getAsJsonObject("data");
					JsonArray jobsArr = dataObj.getAsJsonArray("retlst");
					boolean isTrue = true;
					int i = 0;
					for(JsonElement jobEle : jobsArr){
						JsonObject job = jobEle.getAsJsonObject();
						String name = job.get("repoName").getAsString();
//						System.out.println(name);
						if("samplegit".equals(name)){
							i++;
							System.out.println("creat "+name+" bulid");
						}
					}
					if(i < 1){
						isTrue = false;
						lr.error_message("creat samplegit buliding failed");
					}
					return isTrue;
				}
			}catch(Exception err){
				lr.error_message("["+ret+"]exception:"+err.getMessage());
				return false;
			}
		}	
		
	}
}