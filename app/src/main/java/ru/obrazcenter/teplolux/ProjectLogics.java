package ru.obrazcenter.teplolux;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import ru.obrazcenter.teplolux.Fragment1.FloorValues;
import ru.obrazcenter.teplolux.Fragment1.Values;


import static ru.obrazcenter.teplolux.Main.A_PREF_PROJECT_NAMES;
import static ru.obrazcenter.teplolux.Main.frag2;
import static ru.obrazcenter.teplolux.Main.frag3;
import static ru.obrazcenter.teplolux.Main.frag4;
import static ru.obrazcenter.teplolux.Main.frag5;
import static ru.obrazcenter.teplolux.StartActivity.gson;
import static ru.obrazcenter.teplolux.StartActivity.prefs;
import static ru.obrazcenter.teplolux.StartActivity.theProject;
import static ru.obrazcenter.teplolux.Utils.getPreferences;

@SuppressLint("ApplySharedPref")
enum ProjectLogics {
    ;

    static void saveNewProject(String name) {
        Set<String> pNames = prefs.getStringSet(A_PREF_PROJECT_NAMES, null);
        if (pNames == null || pNames.isEmpty())
            pNames = new HashSet<>(1);
        pNames.add(name);
        Editor editor = prefs.edit();
        long time = System.currentTimeMillis();
        editor.putString(name, gson.toJson(new Place(name, time)))
                .putStringSet(A_PREF_PROJECT_NAMES, pNames)
                .commit();
    }

    static void saveRoom(Room room, String name, String projectName) {
        Place oldRoom = gson.fromJson(
                getPreferences(projectName).getString(name, null), Place.class);

        Place project = gson.fromJson(prefs.getString(projectName, "err"), Place.class);
        project.area += room.area - oldRoom.area;
        project.power += room.power - oldRoom.power;
        prefs.edit()
                .putString(projectName, gson.toJson(project)).apply();
        getPreferences(projectName).edit()
                .putString(name, gson.toJson(room)).commit();
    }

    static void saveNewRoom(String name, long time, String projectName) {
        getPreferences(projectName).edit()
                .putString(name, gson.toJson(new Room(name, time))).apply();
    }

    @Nullable
    static Place obtainProject(String name) {
        String projectStr = prefs.getString(name, null);
        return projectStr == null ? null
                : gson.fromJson(projectStr, Place.class);
    }

    @Nullable
    static Room obtainRoom(String name, String projectName) {
        String roomStr = getPreferences(projectName).getString(name, null);
        return roomStr == null ? null
                : gson.fromJson(roomStr, Room.class);
    }

    @Nullable
    static String obtainProjectStr(String name) {
        return prefs.getString(name, null);
    }

    @Nullable
    static String obtainRoomStr(String name, String projectName) {
        return getPreferences(projectName).getString(name, null);
    }

    /**
     * Obtain a list all projects.
     *
     * @return a list all projects. Null if it is empty.
     */
    @Nullable
    static PlaceList<Place> obtainProjectList(Comparator<Place> projectComparator) {
        Set<String> pNames = prefs.getStringSet(A_PREF_PROJECT_NAMES, new HashSet<String>(0));
        if (!pNames.isEmpty()) {
            PlaceList<Place> projects = new PlaceList<>(pNames.size());
            for (String s : pNames) {
                String pStr = prefs.getString(s, "err");
                if ("err".equals(pStr)) throw new RuntimeException("Не найден проект " + s);
                projects.add(gson.fromJson(pStr, Place.class));
            }
            Collections.sort(projects, projectComparator);
            return projects;
        } else return null;
    }

    /**
     * @param projectName name of project to get rooms from
     * @return a list of all rooms in project. Null if it is empty.
     */
    @Nullable
    static PlaceList<Place> obtainRoomList(String projectName, Comparator<Place> roomComparator) {
        SharedPreferences pPrefs = getPreferences(projectName);
        List<Object> pValues = new ArrayList<>(pPrefs.getAll().values());
        if (!pValues.isEmpty()) {
            PlaceList<Place> rooms = new PlaceList<>(pValues.size());
            for (Object o : pValues)
                if (o instanceof String)
                    rooms.add(gson.fromJson((String) o, Room.class));
                else {
                    String key = null;
                    Object[] objects = pPrefs.getAll().entrySet().toArray();
                    for (Object object : objects)
                        if (!(((Entry) object).getValue() instanceof String))
                            key = ((Entry) object).getKey().toString();
                    throw new RuntimeException("В SharedPreferences проекта " + theProject
                            + " обнаружена переменная \"" + key + "\" типа " + o.getClass());
                }
            Collections.sort(rooms, roomComparator);
            return rooms;
        } else return null;
    }

    static void deleteProject(String name) {
        Set<String> set = prefs.getStringSet(A_PREF_PROJECT_NAMES, null);
        if (set == null)
            throw new RuntimeException("Не найден список проектов");
        set.remove(name);
        prefs.edit().putStringSet(A_PREF_PROJECT_NAMES, set)
                .remove(name).apply();

        getPreferences(name).edit().clear().apply();
    }

    static void deleteRoom(String name, String projectName) {
        Editor editor = getPreferences(projectName).edit();
        editor.remove(name);
        editor.apply();
    }

    static void renameProject(String newName, String oldName) {
        Editor editor = prefs.edit();
        Place placeToRename = gson.fromJson(
                prefs.getString(oldName, null), Place.class);
        placeToRename.name = newName;
        editor.remove(oldName)
                .putString(newName, gson.toJson(placeToRename));
        Set<String> pNames = prefs.getStringSet(A_PREF_PROJECT_NAMES, null);
        if (pNames == null) throw new RuntimeException(
                "При попытке переименовать проект \"" + oldName
                        + "\" список проектов равен null");
        pNames.add(newName);
        pNames.remove(oldName);
        editor.putStringSet(A_PREF_PROJECT_NAMES, pNames);
        editor.apply();
        Utils.renameMyPrefsFile(oldName, newName);
    }

    static void renameRoom(String newName, String oldName, String projectName) {
        getPreferences(projectName).edit().putString(
                newName, prefs.getString(oldName, null)).commit();
    }

    static boolean isThereARoomInProject(String name, String project) {
        return getPreferences(project).contains(name);
    }

    static boolean isThereAProject(String name) {
        return prefs.contains(name);
    }


    static class Place implements Comparable<Place>, Cloneable {
        String name;
        float area = 0;
        int power;
        long date;

        //only for child constructor
        private Place() {
        }

        Place(@NonNull String name, long millis) {
            this.name = name;
            date = millis;
        }

        Place(@NonNull String name, long millis, int area, int power) {
            this(name, millis);
            this.name = name;
            this.area = area;
            this.power = power;
        }

        public int compareTo(@NonNull Place p) {
            return name.compareTo(p.name);
        }

        public String toString() {
            return name;
        }
    }

    static class Room extends Place {
        Values cv;
        FloorValues fv;
        WallValues[] wv;
        float height;
        int insideTemper;
        boolean saved;


        Room(@NonNull String name, long millis) {
            this.name = name;
            this.date = millis;
            saved = false;
        }

        Room(@NonNull String name, long millis, int area, int power,
             float height, int insideTemper, @NonNull WallValues[] wv,
             @NonNull Values cv, @NonNull FloorValues fv) {
            this.name = name;
            this.date = millis;
            this.area = area;
            this.power = power;
            saved = true;
            this.wv = wv;
            this.cv = cv;
            this.fv = fv;
            this.height = height;
            this.insideTemper = insideTemper;
            //it works only in case when Main activity is ready
            this.wv[0].lNumb = frag2.performed ? frag2.layerNum : 0;
            this.wv[1].lNumb = frag3.performed ? frag3.layerNum : 0;
            this.wv[2].lNumb = frag4.performed ? frag4.layerNum : 0;
            this.wv[3].lNumb = frag5.performed ? frag5.layerNum : 0;
        }
    }

    static class PlaceList<T> extends ArrayList<T> {
        PlaceList(int initialCapacity) {
            super(initialCapacity);
        }

        int indexOfPlaceWithName(@NonNull String name) {
            try {
                Field field = getClass().getDeclaredField("elementData");
                field.setAccessible(true);
                Place[] elementData = (Place[]) field.get(this);
                for (int i = 0; i < size(); i++)
                    if (name.equals(elementData[i].name))
                        return i;
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return -1;
        }
    }
}
