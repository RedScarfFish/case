import java.util.Map;
import lrapi.lr;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class ValidatePodInfo implements TestCaseValidater{
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
//					boolean isSuccess = true;
					JsonObject js1 = jsonObj.getAsJsonObject("data");
					JsonArray itemArr = js1.getAsJsonArray("items");
					int i = 0;
					String containerInfo;
					for(JsonElement jsonElement : itemArr){
						JsonObject item = jsonElement.getAsJsonObject();
						JsonObject statusObj = item.getAsJsonObject("status");
						JsonObject metaObj = item.getAsJsonObject("metadata");
						JsonObject specObj = item.getAsJsonObject("spec");
						JsonArray containerArr = specObj.getAsJsonArray("containers");
						JsonObject labelObj = metaObj.getAsJsonObject("labels");
						String version = labelObj.get("version").getAsString();
						
						//version 版本
						if("1.9.14".equals(version)){
							System.out.println("version = " + version);
						}else{
							lr.error_message("update version error");
						}
						
						JsonArray conditionArr = statusObj.getAsJsonArray("conditions");
//						System.out.println("status is " + statusObj.get("phase").getAsString());
						
						for(JsonElement containerEle : containerArr){
							JsonObject container = containerEle.getAsJsonObject();
//							System.out.println(container);
							
							//ports 端口
							JsonArray portsArr = container.getAsJsonArray("ports");
							if(portsArr != null){
								for(JsonElement portsELe : portsArr){
								JsonObject portObj = portsELe.getAsJsonObject();
								String containerPort = portObj.get("containerPort").getAsString();
								String protocol = portObj.get("protocol").getAsString();
								System.out.println("containerPort:"+containerPort + ";" + "protocol" +protocol);
								}
							}
							
							//command 命令
							if(container.get("command") != null){
								String command = container.get("command").getAsString();
								System.out.println("start command is : " + command);
							}
							
							//resources 内存/CPU
							JsonObject resources = container.getAsJsonObject("resources");
							JsonObject limits = resources.getAsJsonObject("limits");
							JsonObject requests = resources.getAsJsonObject("requests");
							System.out.println("limits: cpu: " + limits.get("cpu") + ";memory : " + limits.get("memory"));
							System.out.println("requests: cpu: " + requests.get("cpu") + ";memory : " + requests.get("memory"));
							
							//volumeMounts 挂载卷
							JsonArray volumeMountArr = container.getAsJsonArray("volumeMounts");
							for(JsonElement mountEle : volumeMountArr){
								JsonObject mountObj = mountEle.getAsJsonObject();
								System.out.println("mountPath: "+mountObj.get("mountPath")+";name: "+mountObj.get("name")+";readOnly: " + 
								                   mountObj.get("readOnly"));
							}
							//环境变量 
							JsonArray envArr = container.getAsJsonArray("env");
							if(envArr != null){
								for(JsonElement envELe : envArr){
									JsonObject envObj = envELe.getAsJsonObject();
									System.out.println("env: name: " + envObj.get("name") + ";value: " + envObj.get("value"));
								}
							}
						
						}
						//是否 running 
						if("Running".equals(statusObj.get("phase").getAsString())){
							System.out.println("status is " + statusObj.get("phase").getAsString());
						}else{
							lr.error_message("status is " + statusObj.get("phase").getAsString());
							for(JsonElement jsonElement1 : conditionArr){
								JsonObject conObj = jsonElement1.getAsJsonObject();
								if("False".equals(conObj.get("status").getAsString())){
									lr.error_message("error:"+conObj.get("message")+";reson:"+conObj.get("reson"));
								}
							}
						}
						i++;
					}
					//几个实例
					if(i != 2){
						lr.error_message("items extend error");
					}else{
						System.out.println("items is " + i);						
					}
					return true;
				}
			}catch(Exception err){
				lr.error_message("["+ret+"]exception:"+err.getMessage());
				return false;
			} 
		}
	}
	
}