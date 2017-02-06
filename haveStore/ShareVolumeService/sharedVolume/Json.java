import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class Json{
	public static JsonObject toJsonObj(String json){
		Gson gson = new Gson();
		JsonElement element = gson.fromJson(json, JsonElement.class);
		return element.getAsJsonObject();
	}
	
	public static JsonArray toJsonArray(String json){
		Gson gson = new Gson();
		JsonElement element = gson.fromJson(json, JsonElement.class);
		return element.getAsJsonArray();
	}
}