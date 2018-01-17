package com.pycredit.h5sdk.utils;

import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

public class JsonUtils {
	
	private static final String EMPTY_JSON_STRING = "{}";
	
	public static String toString(JSONObject obj) {
		JSONStringer stringer = new JSONStringer();
		try {
			JSONObjectToStringInternal(stringer, obj);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return stringer.toString();
	}
	
	private static void JSONObjectToStringInternal(JSONStringer stringer, JSONObject object) throws JSONException {
		Map<String, Object> map = new TreeMap<String, Object>();
		if(object != null && object.length() != 0) { 
			Iterator<String> iter = object.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				Object obj = object.get(key);
				map.put(key, obj);
			}
		}
		stringer.object();
        for(Map.Entry<String, Object> entry : map.entrySet()) {
            stringer.key(entry.getKey());
            Object value = entry.getValue();
            if(value instanceof JSONObject) {
            	JSONObjectToStringInternal(stringer, (JSONObject)value);
            } else {
            	stringer.value(value);
            }
        }
        stringer.endObject();
	}
	
	public static Object parseObject(String json) {
		if(TextUtils.isEmpty(json) || TextUtils.isEmpty(json.trim())) {
			return null;
		}
		String jsonString = json.trim();
		try {
			if(jsonString.startsWith("[")) {
				JSONArray array = new JSONArray(jsonString);
				return array;
			} else {
				JSONObject object = new JSONObject(jsonString);
				return object;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static String toJsonString(Object obj) {
		try {
			Object object = toJsonObject(obj);
			return object.toString();
//			if(object instanceof JSONObject) {
//				return JsonUtils.toString((JSONObject)object);
//			} else {
//				return object.toString();
//			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return EMPTY_JSON_STRING;
	}
	
	public static Object toJsonObject(Object obj) throws JSONException {
		return toJsonObject(obj, EMPTY_JSON_STRING);
	}
	
	@SuppressWarnings("rawtypes")
	private static Object toJsonObject(Object obj, String defaultValue) throws JSONException {
		if(obj instanceof Map) {
			return toJSONObject((Map)obj);
		} else if(obj instanceof Collection) {
			return toJSONObject((Collection)obj);
		} else if(obj instanceof Boolean
				|| obj instanceof Byte
				|| obj instanceof Character
				|| obj instanceof Float
				|| obj instanceof Short
				|| obj instanceof Integer
				|| obj instanceof Long
				|| obj instanceof Double
				|| obj instanceof String) {
			return obj;
		} else if(obj instanceof JsonSerializable) {
			return ((JsonSerializable)obj).toJsonString();
		} else {
			return defaultValue;
		}
	}
	
	@SuppressWarnings("rawtypes")
	private static JSONObject toJSONObject(Map map) throws JSONException {
		JSONObject ret = new JSONObject();
		if(map != null && map.size() != 0) { 
			Iterator iter = map.entrySet().iterator();
			while(iter.hasNext()) {
				Map.Entry ety = (Map.Entry)iter.next();
				if(!(ety.getKey() instanceof String)) {
					continue;
				}
				ret.put((String)ety.getKey(), toJsonObject(ety.getValue(), null));
			}
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	private static JSONArray toJSONObject(Collection list) throws JSONException {
		JSONArray ret = new JSONArray();
		if(list != null && list.size() != 0) { 
			Iterator iter = list.iterator();
			while (iter.hasNext()) {
				ret.put(toJsonObject(iter.next()));
			}
		}
		return ret;
	}

	@SuppressWarnings({ "rawtypes" })
	public static Map praseJSONObject(JSONObject object) {
		Map<String, Object> map = new TreeMap<String, Object>(new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				return lhs.compareTo(rhs);
			}
		});
		if(object != null && object.length() != 0) { 
			Iterator<String> iter = object.keys();
			while (iter.hasNext()) {
				String key = iter.next();
				try {
					Object obj = object.get(key);
					if(JSONObject.NULL.equals(obj)) {
						map.put(key, null);
					} else if(obj instanceof JSONObject) {
						map.put(key, praseJSONObject((JSONObject)obj));
					} else if(obj instanceof JSONArray) {
						map.put(key, praseJSONArray((JSONArray)obj));
					} else {
						map.put(key, obj);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return map;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Collection praseJSONArray(JSONArray array) {
		Collection collection = new ArrayList();
		if(array != null && array.length() != 0) { 
			for(int  i = 0; i < array.length(); i++) {
				try {
					Object obj = array.get(i);
					if(obj instanceof JSONObject) {
						collection.add(praseJSONObject((JSONObject)obj));
					} else if(obj instanceof JSONArray) {
						collection.add(praseJSONArray((JSONArray)obj));
					} else {
						collection.add(obj);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}
		return collection;
	}

	@SuppressWarnings("rawtypes")
	public static JSONObject getJSONObject(Map map) {
		JSONObject object = null;
		try {
			object = (JSONObject)toJsonObject(map);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}

	@SuppressWarnings("rawtypes")
	public static JSONArray getJSONArray(Collection collection) {
		JSONArray array = null;
		try {
			array = (JSONArray)toJsonObject(collection);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return array;
	}

	public static JSONObject getJSONObject(String jsonStr) {
		JSONObject object = new JSONObject();
		try {
			object = new JSONObject(jsonStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return object;
	}
	
	public static JSONArray getJSONArray(String jsonStr) {
		JSONArray array = new JSONArray();
		try {
			array = new JSONArray(jsonStr);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return array;
	}

	public static JSONObject getJSONObject(JSONObject jsonObject, String name) {
		JSONObject object;
		try {
			object = new JSONObject(jsonObject.get(name).toString());
		} catch (JSONException e) {
			object = new JSONObject();
		}
		return object;
	}

}
