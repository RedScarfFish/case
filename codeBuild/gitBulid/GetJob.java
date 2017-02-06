import java.util.Map;
import lrapi.lr;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import java.io.StringWriter;
import java.io.PrintWriter;

public class GetJob implements TestCaseValidater{
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
					JsonObject dataObj = jsonObj.getAsJsonObject("data");
					JsonArray buildArr = dataObj.getAsJsonArray("builds");
					boolean isSuccess = true;
					for(JsonElement buildEle : buildArr){
						JsonObject build = buildEle.getAsJsonObject();
						try{
							String result = build.get("result").getAsString();
							if("SUCCESS".equals(result)){
								System.out.println("code is build success");
							}else if("FAILURE".equals(result)){
								lr.error_message("code is build failed");
								isSuccess = false;
							}	
						}catch(Exception e){
							StringWriter sw = new StringWriter();    
							PrintWriter pw = new PrintWriter(sw);    
							e.printStackTrace(pw);    
							String msg=sw.toString();
							if(!("".equals(msg))){
								System.out.println("code is building");
							}
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