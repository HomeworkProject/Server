package de.mlessmann.allocation;

import de.mlessmann.homework.HomeWork;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Life4YourGames on 08.06.16.
 */
public class HWUser {

    public static HWUser getDefaultUser(HWGroup g) {

        JSONObject obj = new JSONObject();

        JSONArray perms = new JSONArray();

        obj.put("permissions", perms);
        obj.put("name", DEFNAME);
        obj.put("password", DEFPASS);

        return new HWUser(obj, g);

    }

    public static final String DEFNAME = "default";
    public static final String DEFPASS = "default";

    private String userName = DEFNAME;
    private String userPass = DEFPASS;

    private Map<String, HWPermission> permissions;

    private JSONObject json;

    private HWGroup myGroup;

    public HWUser(JSONObject obj, HWGroup group) {

        permissions = new HashMap<String, HWPermission>();

        json = obj;

        if (json.has("permissions")) {

            JSONArray arr = json.getJSONArray("permissions");

            for (Object o : arr) {

                HWPermission perm = new HWPermission((JSONArray) o);

            }

        }

        userName = json.getString("name");
        userPass = json.getString("password");
        myGroup = group;

    }

    public String getUserName() {

        return userName;

    }

    public String getUserPass() {

        return userPass;

    }

    public boolean authenticate(String auth) {

        //TODO: More auth options ?

        return userPass.equals(auth);

    }

    public int getPermissionValue(String permissionName) {

        if (permissions.containsKey(permissionName)) {

            return permissions.get(permissionName).hasValue();

        }

        return HWPermission.DEFAULT.hasValue();

    }

    public void addPermission(HWPermission perm) {

        permissions.put(perm.getName(), perm);

    }

    public int addHW(JSONObject obj) {

        return myGroup.addHW(obj, this);

    }

    public ArrayList<HomeWork> getHWOn(LocalDate date, ArrayList<String> subjectFilter) {

        return myGroup.getHWOn(date, subjectFilter);

    }

    public ArrayList<HomeWork> getHWBetween(LocalDate from, LocalDate to, ArrayList<String> subjectFilter, boolean overrideLimit) {

        return myGroup.getHWBetween(from, to, subjectFilter, overrideLimit);

    }

    public int delHW(LocalDate date, String id) {

        return myGroup.delHW(date, id, this);

    }

}