package com.reactlibrary;

import android.util.Log;

import com.mlins.utils.PropertyHolder;
import com.spreo.nav.interfaces.IPoi;
import com.spreo.sdk.data.SpreoDataProvider;
import com.spreo.sdk.poi.PoiCategory;
import com.spreo.sdk.poi.PoisUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class SpreoSearchDataHolder {
    private static SpreoSearchDataHolder instance = null;
    private static String favoritesFileName = "favorites.json";
    private static String historyFileName = "history.json";
    private static String mytripFileName = "mytrip.json";
    private IPoi selectedPoi = null;
    private List<IPoi> allPoiList = new ArrayList<IPoi>();
    private List<IPoi> poiList = new ArrayList<IPoi>();
    private List<SpreoFavorite> favorites = new ArrayList<SpreoFavorite>();
    private List<SpreoHistory> history = new ArrayList<SpreoHistory>();
    private List<IPoi> editedfavs = new ArrayList<IPoi>();
    private List<PoiCategory> menuCategories = new ArrayList<PoiCategory>();
    private List<PoiCategory> filterCategories = new ArrayList<PoiCategory>();
    private List<SpreoMyTrip> mytrip = new ArrayList<SpreoMyTrip>();
    private int historysize = 3;
    private boolean sortBYLocation = true;
    private List<PoiCategory> visibleCategories = new ArrayList<>();

    public SpreoSearchDataHolder() {
        loadfavorites();
        loadHistory();
        loadPoiList();
        loadMenuCategories();
        loadFilterCategories();
        loadMyTrip();
    }

    public static SpreoSearchDataHolder getInstance() {
        if (instance == null) {
            instance = new SpreoSearchDataHolder();
        }
        return instance;
    }

    public static void releaseInstance() {
        if (instance != null) {
            instance.clean();
            instance = null;
        }
    }

    private void clean() {
        selectedPoi = null;
        getFavorites().clear();
        getHistory().clear();
        getMenuCategories().clear();
        getFilterCategories().clear();
        getEditedfavs().clear();
    }

    public IPoi getSelectedPoi() {
        return selectedPoi;
    }

    public void setSelectedPoi(IPoi selectedPoi) {
        this.selectedPoi = selectedPoi;
    }

    private void loadPoiList() {
        String campusidtxt = SpreoDataProvider.getCampusId();
        if (campusidtxt != null) {
            /**
             String campusId = new String(campusidtxt);
             String facilityId = new String(SpreoDataProvider.getFacilityId());
             List<IPoi> alllist = PoisUtils.getAllFacilityPoisList(campusId,facilityId);
             */
            List<IPoi> alllist = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId());
            allPoiList.clear();
            for (IPoi temppoi : alllist) {
                if (temppoi.isShowPoiOnSearches()) {
                    allPoiList.add(temppoi);
                }
            }
            resetPoiList();
        }
    }


//    public void forceLoadPoisList() {
//
//        List<IPoi> alllist = PoisUtils.getAllCampusPoisList(SpreoDataProvider.getCampusId()); /**PoiDataHelper.getInstance().getAllPoiAsList(); */
//        allPoiList.clear();
//        allPoiList.addAll(alllist);
//        resetPoiList();
//    }

    public void resetPoiList() {
        poiList.clear();
        poiList.addAll(allPoiList);
    }

    public void resetMenuCategoriesList() {
        loadMenuCategories();
    }

    public void resetFilterCategoriesList() {
        loadFilterCategories();
    }

    private void loadMenuCategories() {
        List<PoiCategory> tmplist = PoisUtils.getPoiCategoies();
        ArrayList<PoiCategory> tmpcategories = new ArrayList<>();
        for (PoiCategory o : tmplist) {
            String name = o.getPoitype();
            if (!name.trim().isEmpty() && o.showInCatgories) {
                tmpcategories.add(o);
            }
        }
        menuCategories = getCategoriesSortedAlphabetical(tmpcategories);
    }

    private void loadFilterCategories() {
        List<PoiCategory> tmplist = PoisUtils.getPoiCategoies();
        ArrayList<PoiCategory> tmpcategories = new ArrayList<>();
        for (PoiCategory o : tmplist) {
            String name = o.getPoitype();
            if (!name.trim().isEmpty() && o.showInMapFilter) {
                tmpcategories.add(o);
            }
        }
        filterCategories = getCategoriesSortedAlphabetical(tmpcategories);
        visibleCategories = filterCategories;
    }

    private static List<PoiCategory> getCategoriesSortedAlphabetical(List<PoiCategory> categorylist) {
        List<PoiCategory> result = new ArrayList();
        result.addAll(categorylist);
        Collections.sort(result, new Comparator<PoiCategory>() {
            public int compare(PoiCategory p1, PoiCategory p2) {
                String s1 = p1.getPoitype();
                String s2 = p2.getPoitype();
                return s1.compareToIgnoreCase(s2);
            }
        });
        return result;
    }

    public void saveHistory() {
        JSONArray historyjsonarray = new JSONArray();
        for (SpreoHistory o : getHistory()) {
            JSONObject hjson = o.getAsJson();
            historyjsonarray.put(hjson);
        }

        JSONObject historyjson = new JSONObject();
        try {
            historyjson.put("history", historyjsonarray);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        File dir = PropertyHolder.getInstance().getProjectDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = historyFileName;
        File pfile = new File(dir, fileName);
        if (pfile.exists()) {
            pfile.delete();
        }
        try {
            pfile.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(pfile, true));
            out.write(historyjson.toString());
            out.flush();
        } catch (IOException e) {
            e.toString();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (Exception e2) {
                    Log.e("", e2.getMessage());
                    e2.printStackTrace();
                }
        }
    }

    public void saveMyTrip() {
        JSONArray mytripjsonarray = new JSONArray();
        for (SpreoMyTrip o : getMytrip()) {
            JSONObject mtjson = o.getAsJson();
            mytripjsonarray.put(mtjson);
        }

        JSONObject mytripjson = new JSONObject();
        try {
            mytripjson.put("mytrip", mytripjsonarray);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        File dir = PropertyHolder.getInstance().getProjectDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = mytripFileName;
        File pfile = new File(dir, fileName);
        if (pfile.exists()) {
            pfile.delete();
        }
        try {
            pfile.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new FileWriter(pfile, true));
            out.write(mytripjson.toString());
            out.flush();
        } catch (IOException e) {
            e.toString();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (Exception e2) {
                    Log.e("", e2.getMessage());
                    e2.printStackTrace();
                }
        }
    }

    public void saveFavorites() {
        JSONArray favoritesjsonarray = new JSONArray();
        for (SpreoFavorite o : getFavorites()) {
            JSONObject favoritejson = o.getAsJson();
            favoritesjsonarray.put(favoritejson);
        }

        JSONObject favoritesjson = new JSONObject();
        try {
            favoritesjson.put("favorites", favoritesjsonarray);
        } catch (JSONException e1) {
            e1.printStackTrace();
        }

        File dir = PropertyHolder.getInstance().getProjectDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String fileName = favoritesFileName;
        File pfile = new File(dir, fileName);
        if (pfile.exists()) {
            pfile.delete();
        }
        try {
            pfile.createNewFile();
        } catch (IOException e1) {
            e1.printStackTrace();
        }

        BufferedWriter out = null;

        try {
            out = new BufferedWriter(new FileWriter(pfile, true));
            out.write(favoritesjson.toString());
            out.flush();
        } catch (IOException e) {
            e.toString();
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (Exception e2) {
                    Log.e("", e2.getMessage());
                    e2.printStackTrace();
                }
        }
    }

    public void loadHistory() {
        File dir = PropertyHolder.getInstance().getProjectDir();
        String filename = historyFileName;
        File file = new File(dir, filename);
        if (!file.exists()) {
            return;
        }

        String jsonString = "";

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = in.readLine()) != null) {
                jsonString += line;
            }
        } catch (IOException e) {
            e.toString();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e2) {
                    Log.e("", e2.getMessage());
                    e2.printStackTrace();
                }
        }

        try {
            JSONObject historyjson = new JSONObject(jsonString);
            JSONArray jsonArray = historyjson.getJSONArray("history");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject hjson = jsonArray.getJSONObject(i);
                SpreoHistory historyobj = new SpreoHistory();
                historyobj.parse(hjson);
                getHistory().add(historyobj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadMyTrip() {
        File dir = PropertyHolder.getInstance().getProjectDir();
        String filename = mytripFileName;
        File file = new File(dir, filename);
        if (!file.exists()) {
            return;
        }

        String jsonString = "";

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = in.readLine()) != null) {
                jsonString += line;
            }
        } catch (IOException e) {
            e.toString();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e2) {
                    Log.e("", e2.getMessage());
                    e2.printStackTrace();
                }
        }

        try {
            JSONObject mytripjson = new JSONObject(jsonString);
            JSONArray jsonArray = mytripjson.getJSONArray("mytrip");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject mtjson = jsonArray.getJSONObject(i);
                SpreoMyTrip mytripobj = new SpreoMyTrip();
                mytripobj.parse(mtjson);
                getMytrip().add(mytripobj);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void loadfavorites() {
        File dir = PropertyHolder.getInstance().getProjectDir();
        String filename = favoritesFileName;
        File file = new File(dir, filename);
        if (!file.exists()) {
            return;
        }

        String jsonString = "";

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = in.readLine()) != null) {
                jsonString += line;
            }
        } catch (IOException e) {
            e.toString();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e2) {
                    Log.e("", e2.getMessage());
                    e2.printStackTrace();
                }
        }

        try {
            JSONObject favoritesjson = new JSONObject(jsonString);
            JSONArray jsonArray = favoritesjson.getJSONArray("favorites");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject favoritejson = jsonArray.getJSONObject(i);
                SpreoFavorite favorite = new SpreoFavorite();
                favorite.parse(favoritejson);
                getFavorites().add(favorite);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addFavorite(IPoi poi) {
        if (poi != null && !isFavorite(poi)) {
            SpreoFavorite fav = new SpreoFavorite(poi);
            getFavorites().add(fav);
            saveFavorites();
        }

    }

    public boolean isFavorite(IPoi poi) {
        Boolean result = false;
        if (poi != null && favorites != null) {
            for (SpreoFavorite o : favorites) {
                String fid = o.getPoiID();
                String pid = poi.getPoiID();
                if (fid != null & pid != null && pid.equals(fid)) {
                    result = true;
                    break;
                }
            }
        }
        return result;
    }

    public void addEditedFavs(IPoi poi) {
        if (poi != null) {
            SpreoEditedFavs edited = new SpreoEditedFavs(poi);
            getEditedfavs().add(edited);
        }

    }

    public void addHistory(IPoi poi) {
        if (poi != null) {
            deleteHistory(poi);
            SpreoHistory historyobj = new SpreoHistory(poi);
            history.add(0, historyobj);
            if (history.size() > historysize) {
                history.remove(history.size() - 1);
            }
            saveHistory();
        }
    }

    public void addMyTrip(IPoi poi) {
        if (poi != null) {
            SpreoMyTrip mytripobj = new SpreoMyTrip(poi);
            mytrip.add(mytripobj);
            saveMyTrip();
        }
    }

    public List<SpreoFavorite> getFavorites() {
        return favorites;
    }

    public void setFavorites(List<SpreoFavorite> favorites) {
        this.favorites = favorites;
    }

    public List<SpreoHistory> getHistory() {
        return history;
    }

    public void setHistory(List<SpreoHistory> history) {
        this.history = history;
    }

    public List<SpreoMyTrip> getMytrip() {
        return mytrip;
    }

    public void setMytrip(List<SpreoMyTrip> mytrip) {
        this.mytrip = mytrip;
        saveMyTrip();
    }

    public void setPoiListAsMytrip(List<IPoi> poilist) {
        mytrip.clear();
        for (IPoi o : poilist) {
            addMyTrip(o);
        }
    }

    public void clearMyTrip() {
        mytrip.clear();
        saveMyTrip();
    }

    public List<PoiCategory> getMenuCategories() {
        return menuCategories;
    }

    public List<PoiCategory> getFilterCategories() {
        return filterCategories;
    }

    public void setMenuCategories(List<PoiCategory> categories) {
        this.menuCategories = categories;
    }

    public void setFilterCategories(List<PoiCategory> categories) {
        this.filterCategories = categories;
    }

    public List<IPoi> getPoiList() {
        return poiList;
    }

    public void setPoiList(List<IPoi> poiList) {
        this.poiList = poiList;
    }

    public List<IPoi> getCategoryPoiList(String categoryname) {
        List<IPoi> result = new ArrayList<IPoi>();
        for (IPoi o : poiList) {
            for (String type : o.getPoitype()) {
                if (type.equals(categoryname)) {
                    result.add(o);
                    break;
                }
            }

        }
        return result;
    }

    public IPoi getPoiById(String id) {
        return PoisUtils.getPoiById(id);
//        IPoi result = null;
//        for (IPoi o : poiList) {
//            if (o.getPoiID().equals(id)) {
//                result = o;
//                break;
//            }
//        }
//        return result;
    }

    public List<IPoi> getEditedfavs() {
        return editedfavs;
    }

    public void setEditedfavs(List<IPoi> editedfavs) {
        this.editedfavs = editedfavs;
    }

    public void deleteFavorite(IPoi fav) {
        String id = fav.getPoiID();
        SpreoFavorite favtodelete = null;
        for (SpreoFavorite o : favorites) {
            if (o.getPoiID().equals(id)) {
                favtodelete = o;
                break;
            }
        }
        if (favtodelete != null && favorites.contains(favtodelete)) {
            favorites.remove(favtodelete);
        }

        saveFavorites();
    }

    public void removePoiFromTrip(IPoi poitoremove) {
        if (poitoremove != null) {
            SpreoMyTrip triptoremove = null;
            for (SpreoMyTrip o : mytrip) {
                if (o.getPoiID().equals(poitoremove.getPoiID())) {
                    triptoremove = o;
                    break;
                }
            }

            if (triptoremove != null) {
                mytrip.remove(triptoremove);
            }
        }

    }

    private void deleteHistory(IPoi poi) {
        SpreoHistory toremove = null;
        if (history != null) {
            for (SpreoHistory o : history) {
                String id = o.getPoiID();
                if (poi != null) {
                    String poid = poi.getPoiID();
                    if (id != null && poid != null && id.equals(poid)) {
                        toremove = o;
                        break;
                    }
                }

            }
        }
        if (toremove != null) {
            history.remove(toremove);
        }
    }

    public List<IPoi> getHistoryPOIs() {
        List<IPoi> result = new ArrayList<>();
        if (history != null) {
            for (SpreoHistory o : history) {
                String id = o.getPoiID();
                if (id != null) {
                    IPoi poi = getPoiById(id);
                    if (poi != null) {
                        result.add(poi);
                    }
                }
            }
        }
        return result;
    }

    public List<IPoi> getFavoritesPOIs() {
        List<IPoi> result = new ArrayList<>();
        if (favorites != null) {
            for (SpreoFavorite o : favorites) {
                String id = o.getPoiID();
                if (id != null) {
                    IPoi poi = getPoiById(id);
                    if (poi != null) {
                        result.add(poi);
                    }
                }
            }
        }
        return result;
    }

    public boolean isSortBYLocation() {
        return sortBYLocation;
    }

    public void setSortBYLocation(boolean sortBYLocation) {
        this.sortBYLocation = sortBYLocation;
    }

    public List<PoiCategory> getVisibleCategories() {
        return visibleCategories;
    }

    public void setVisibleCategories(List<PoiCategory> visibleCategories) {
        this.visibleCategories = visibleCategories;
    }

}
