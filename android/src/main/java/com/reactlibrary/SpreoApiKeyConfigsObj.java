package spreo.spreomobile;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SpreoApiKeyConfigsObj {

    private String key = null;
    private String projectName = null;

    public boolean parse(JSONObject jobj) {
        boolean res = false;
        try {
            List<SpreoApiKeyConfigsObj> keyConfs = new ArrayList<SpreoApiKeyConfigsObj>();
            key = jobj.getString("key");
            projectName = jobj.getString("name");
            res = true;
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return res;
    }

    public String getKey() {
        return key;
    }

    public String getProjectName() {
        return projectName;
    }


}