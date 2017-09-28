//Если этот код работает, его написал Соколов Филипп, а если нет, то не знаю, кто его писал.

//Todo: В будущем лучше использовать для проектов с комнатами Recycler View with Expandable Items
// площадь и мощность проекта
//Todo: использование ActionMode
//Todo: Override pending transition
//Todo: Use files instead of SharedPreferences
//http://stackoverflow.com/questions/7944601/saving-a-hash-map-into-shared-preferences
package ru.obrazcenter.teplolux;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.jetbrains.annotations.Contract;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import ru.obrazcenter.teplolux.ProjectLogics.Place;
import ru.obrazcenter.teplolux.ProjectLogics.PlaceList;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;
import static android.graphics.Color.TRANSPARENT;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static android.text.Html.fromHtml;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_BACK;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static ru.obrazcenter.teplolux.Main.APP_PREFERENCES;
import static ru.obrazcenter.teplolux.Main.A_PREF_CITY;
import static ru.obrazcenter.teplolux.Main.A_PREF_COLDEST_T;
import static ru.obrazcenter.teplolux.Main.A_PREF_EXISTS;
import static ru.obrazcenter.teplolux.Main.A_PREF_SELECTED_COMPARATOR;
import static ru.obrazcenter.teplolux.Main.SAVED_ROOM;
import static ru.obrazcenter.teplolux.Main.SUBTITLE;
import static ru.obrazcenter.teplolux.Main.TITLE;
import static ru.obrazcenter.teplolux.ProjectLogics.deleteProject;
import static ru.obrazcenter.teplolux.ProjectLogics.deleteRoom;
import static ru.obrazcenter.teplolux.ProjectLogics.doesProjectContain;
import static ru.obrazcenter.teplolux.ProjectLogics.isThereAProject;
import static ru.obrazcenter.teplolux.ProjectLogics.obtainProjectList;
import static ru.obrazcenter.teplolux.ProjectLogics.obtainRoomList;
import static ru.obrazcenter.teplolux.ProjectLogics.obtainRoomStr;
import static ru.obrazcenter.teplolux.ProjectLogics.renameProject;
import static ru.obrazcenter.teplolux.ProjectLogics.renameRoom;
import static ru.obrazcenter.teplolux.ProjectLogics.saveNewProject;
import static ru.obrazcenter.teplolux.ProjectLogics.saveNewRoom;
import static ru.obrazcenter.teplolux.Utils.toPx;

@SuppressWarnings("RestrictedApi")
public class StartActivity extends AppCompatActivity
        implements OnClickListener, DialogInterface.OnClickListener {

    static SharedPreferences prefs;
    private TextInputLayout pInput;
    private TextInputLayout rInput;
    private EditText pNameET;
    private EditText rNameET;
    private Builder builder;
    private RecyclerView rv1;
    private RecyclerView rv2;
    private TextView noElementsTV;
    static String theProject = null;
    private Menu menu;
    private MenuItem sortItem;
    private MenuItem byName;
    private MenuItem byDate;
    private MenuItem byArea;
    private MenuItem cityItem;

    @SuppressLint("StaticFieldLeak")
    static StartActivity activity1;
    private final Comparators comparators = new Comparators();
    static final Gson gson = new Gson();
    static boolean isInSelectedItemMode;
    private String selectedName;
    View selectedRL;
    private boolean qq = false;
    private AlertDialog alertNew;
    private AlertDialog alertRename;
    private String openedPrj;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity1 = this;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        prefs = getPreferences(APP_PREFERENCES);
        setContentView(R.layout.app_bar_start);
        if (!prefs.getBoolean(A_PREF_EXISTS, false)) {
            Editor editor = prefs.edit();
            editor.putInt(A_PREF_COLDEST_T, 25);
            editor.putString(A_PREF_CITY, "Москва");
            editor.putBoolean(A_PREF_EXISTS, true);
            editor.apply();
            showCityDialog(true);
        }
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        View fab = findViewById(R.id.fab);
        fab.setOnClickListener(this);

        pInput = (TextInputLayout) getLayoutInflater()
                .inflate(R.layout.dialog_new_project, null, false);
        pNameET = pInput.getEditText();
        if (pNameET == null) throw new RuntimeException("pNameET is null");

        rInput = (TextInputLayout) getLayoutInflater()
                .inflate(R.layout.dialog_new_room, null, false);
        rNameET = rInput.getEditText();
        if (rNameET == null) throw new RuntimeException("rNameET is null");

        OnKeyListener keyListener = new OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KEYCODE_ENTER) {
                    if (event.getAction() == ACTION_DOWN) onClick(null, -1);
                    return true;
                } else
                    return false;
            }
        };
        pNameET.setOnKeyListener(keyListener);
        rNameET.setOnKeyListener(keyListener);

        pNameET.addTextChangedListener(watcher);
        rNameET.addTextChangedListener(watcher);

        noElementsTV = (TextView) findViewById(R.id.noElements_tv);
        rv1 = (RecyclerView) findViewById(R.id.projects_recycler_view);
        rv1.setLayoutManager(new LinearLayoutManager(this));
        rv1.setItemAnimator(new DefaultItemAnimator());
        rv1.addItemDecoration(new DividerItemDecoration(this, VERTICAL));

        rv2 = (RecyclerView) findViewById(R.id.rooms_recycler_view);
        rv2.setLayoutManager(new LinearLayoutManager(this));
        rv2.setItemAnimator(new DefaultItemAnimator());
        rv2.addItemDecoration(new DividerItemDecoration(this, VERTICAL));
        builder = new Builder(this).setNegativeButton("Отмена", this);
        loadProjectList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds names to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        this.menu = menu;
        sortItem = menu.findItem(R.id.sort_by);
        cityItem = menu.findItem(R.id.city_item);

        SubMenu subMenu = sortItem.getSubMenu();
        byName = subMenu.findItem(R.id.by_name);
        byDate = subMenu.findItem(R.id.by_date);
        byArea = subMenu.findItem(R.id.by_area);
        byName.setChecked(true);
        sortItem.setVisible((p() ? rv1 : rv2).getAdapter().getItemCount() != 0);

        final String cityStr = prefs.getString(A_PREF_CITY, null);
        if (cityStr != null)
            cityItem.setTitle(getString(R.string.selected_city, cityStr, prefs.getInt(A_PREF_COLDEST_T, 2147483647)));
        else if (prefs.getBoolean(A_PREF_EXISTS, false)) {
            cityItem.setTitle(getString(R.string.selected_temper, prefs.getInt(A_PREF_COLDEST_T, 2147483647)));
        }

        return true;
    }

    @SuppressWarnings("ConstantConditions")
    void loadProjectList() {
        theProject = null;
        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.project_list);
        bar.setDisplayHomeAsUpEnabled(false);
        rv2.setVisibility(GONE);
        PlaceList<Place> projects = obtainProjectList(obtainSelectedComparator());
        if (projects != null) {
            noElementsTV.setVisibility(GONE);
            rv1.setAdapter(new ProjectAdapter(projects));
            rv1.setVisibility(VISIBLE);
            if (sortItem != null) // else in onCreateOptionsMenu()
                sortItem.setVisible(true);
        } else {
            rv1.setAdapter(new ProjectAdapter(new PlaceList<Place>(0)));
            rv1.setVisibility(GONE);
            noElementsTV.setVisibility(VISIBLE);
            noElementsTV.setText(R.string.no_saved_projects);
            if (sortItem != null) // else in onCreateOptionsMenu()
                sortItem.setVisible(false);
        }
    }

    @SuppressWarnings("ConstantConditions")
    void loadProject(String pName, boolean isItNotEmpty, boolean animate) {
        theProject = pName;
        ActionBar bar = getSupportActionBar();
        bar.setTitle(theProject);
        bar.setDisplayHomeAsUpEnabled(true);
        if (isInSelectedItemMode) stopSelectedItemMode();
        rv2.setVisibility(VISIBLE);
        PlaceList<Place> rooms = !isItNotEmpty ? null
                : obtainRoomList(theProject, obtainSelectedComparator());
        if (rooms != null) {
            rv2.setAdapter(new ProjectAdapter(rooms));
            noElementsTV.setVisibility(GONE);

            if (animate) pOpeningAnimation();
            else {
                rv1.setVisibility(GONE);
                rv2.setVisibility(VISIBLE);
            }

            if (sortItem != null)
                sortItem.setVisible(true);
        } else {
            rv2.setAdapter(new ProjectAdapter(new PlaceList<Place>(0)));
            rv2.setVisibility(GONE);
            noElementsTV.setText(R.string.no_saved_rooms);
            noElementsTV.setVisibility(VISIBLE);
            if (sortItem != null)
                sortItem.setVisible(false);
        }
    }

    private void pOpeningAnimation() {
        AnimationSet anim = new AnimationSet(true);
        int p = Animation.RELATIVE_TO_SELF;
        anim.addAnimation(new TranslateAnimation(p, 0f, p, -1f, p, 0f, p, 0f));
//        anim.addAnimation(new ScaleAnimation(1, 0.5f, 0, 0, 0, 0));
//        anim.addAnimation(new AlphaAnimation(1, 0));
        anim.setDuration(3000);
        anim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                rv1.setVisibility(GONE);
                rv1.setTranslationX(0);
            }
        });
        rv2.startAnimation(anim);

        AnimationSet anim2 = new AnimationSet(true);
        anim2.addAnimation(new TranslateAnimation(p, 1f, p, 0f, p, 0f, p, 0f));
//        anim2.addAnimation(new AlphaAnimation(0, 1));
        anim2.setDuration(3000);
        rv1.setTranslationX(rv1.getRight() - rv1.getLeft());
        rv1.startAnimation(anim2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (isInSelectedItemMode)
                    stopSelectedItemMode();
                initDialog();
                alertNew.show();
                break;
            default:
                onClick(null, -1);
        }
    }

    /**
     * Initializes <i>new project</i> dialog.
     */
    @SuppressWarnings("ConstantConditions")
    private void initDialog() {
        if (p()) {
            ViewParent parent = pInput.getParent();
            if (parent != null)
                if (parent instanceof ViewGroup)
                    ((ViewGroup) parent).removeView(pInput);
                else
                    Toast.makeText(this, "Parent is not ViewGroup", LENGTH_SHORT).show();
            //noinspection deprecation
            builder.setView(pInput, toPx(36), toPx(12), toPx(36), toPx(12))
                    .setPositiveButton("Создать проект", this);
            builder.setOnDismissListener(new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    if (pNameET.length() > 0)
                        pNameET.setText(null);
                }
            });
            alertNew = builder.create();
            alertNew.getWindow().getAttributes().horizontalWeight = 0.8f;
            alertNew.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    pInput.setError(null);
                    alertNew.getButton(-1).setVisibility(VISIBLE);
                }
            });
            alertNew.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    alertNew.getButton(-1).setOnClickListener(StartActivity.this);
                }
            });
            pNameET.post(new Runnable() {
                public void run() {
                    qq = true;
                    pNameET.setText(null);
                }
            });
        } else {
            ViewParent parent = rInput.getParent();
            if (parent != null && parent instanceof ViewGroup)
                ((ViewGroup) parent).removeView(rInput);
            //noinspection deprecation
            builder.setView(rInput, toPx(36), toPx(12), toPx(12), toPx(12))
                    .setPositiveButton("Создать помещение", this);
            alertNew = builder.create();
            alertNew.getWindow().getAttributes().horizontalWeight = 0.8f;
            alertNew.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    pInput.setError(null);
                    alertNew.getButton(-1).setVisibility(VISIBLE);
                }
            });
            alertNew.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    alertNew.getButton(-1).setOnClickListener(StartActivity.this);
                }
            });
            if (rNameET.length() > 0) {
                qq = true;
                rNameET.setText(null);
            }
        }
    }

    private TextWatcher watcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence t, int st, int c, int a) {
        }

        public void afterTextChanged(Editable ed) {
        }

        @Override
        public void onTextChanged(CharSequence cs, int st, int b, int c) {
            if (qq) {
                qq = false;
                return;
            }
            String s = cs.toString();
            if (p()) if (s.length() == 0) {
                pInput.setError("Введите название проекта");
                alertNew.getButton(-1).setVisibility(INVISIBLE);
            } else if (isThereAProject(s)) {
                pInput.setError("Проект с таким названием уже сущесвует");
                alertNew.getButton(-1).setVisibility(INVISIBLE);
            } else {
                pInput.setError(null);
                alertNew.getButton(-1).setVisibility(VISIBLE);
            }
            else if (s.length() == 0) {
                rInput.setError("Введите название помещения");
                alertNew.getButton(-1).setVisibility(INVISIBLE);
            } else if (Utils.getPreferences(theProject).contains(s)) {
                rInput.setError("Помещение с таким названием уже сущесвует");
                alertNew.getButton(-1).setVisibility(INVISIBLE);
            } else {
                rInput.setError(null);
                alertNew.getButton(-1).setVisibility(VISIBLE);
            }
        }
    };

    //for dialog button new project
    @SuppressLint("ApplySharedPref")
    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            if (p()) {
                if (pNameET.length() == 0) {
                    pInput.setError("Введите название проекта");
                    alertNew.getButton(-1).setVisibility(INVISIBLE);
                } else {
                    String pName = pNameET.getText().toString();
                    if (isThereAProject(pName)) {
                        pInput.setError("Проект с таким названием уже сущесвует");
                        alertNew.getButton(-1).setVisibility(INVISIBLE);
                    } else {
                        alertNew.dismiss();
                        saveNewProject(pName);
                        loadProject(pName, false, false);
                    }
                }
            } else if (rNameET.length() == 0) {
                rInput.setError("Введите название помещения");
                alertNew.getButton(-1).setVisibility(INVISIBLE);
            } else {
                String roomName = rNameET.getText().toString();
                if (doesProjectContain(roomName, theProject)) {
                    rInput.setError("Помещение с таким названием уже сущесвует");
                    alertNew.getButton(-1).setVisibility(INVISIBLE);
                } else {
                    alertNew.dismiss();
                    saveNewRoom(roomName, System.currentTimeMillis(), theProject);
                    loadRoom(roomName);
                }
            }
        }
    }

    @SuppressLint("InflateParams")
    void loadRoom(String roomName) {
        String roomStr = obtainRoomStr(roomName, theProject);
        openedPrj = theProject;
        startActivity(new Intent(this, Main.class)
                .putExtra(SAVED_ROOM, roomStr)
                .putExtra(TITLE, roomName)
                .putExtra(SUBTITLE, theProject));
    }

//    void saveRoom(@NonNull String projectName, @NonNull String name, @NonNull Room room) {
//        Place oldRoom = gson.fromJson(
//                getPreferences(projectName).getString(name, null), Place.class);
//
//        Place project = gson.fromJson(prefs.getString(projectName, "err"), Place.class);
//        project.area += room.area - oldRoom.area;
//        project.power += room.power - oldRoom.power;
//        prefs.edit()
//                .putString(projectName, gson.toJson(project)).apply();
//        getPreferences(projectName).edit()
//                .putString(name, gson.toJson(room)).apply();
//    }
//
//    void saveNewRoom(@NonNull String projectName, @NonNull String name,
//                     @NonNull Room room) {
////        String tempProjectStr = prefs.getString(projectName, null);
////        if (tempProjectStr == null)
////            throw new RuntimeException("Не найден проект \"" + projectName + "\"");
////        prefs.edit().putString(projectName, projectName).apply();
//        getPreferences(projectName).edit().putString(name, gson.toJson(room)).apply();
//    }

    void startSelectedItemMode(RelativeLayout v) {
        isInSelectedItemMode = true;
        selectedRL = v;
        selectedName = ((TextView) v.getChildAt(2)).getText().toString();
        menu.setGroupVisible(R.id.selection_group, true);
        v.setBackgroundColor(ContextCompat.getColor(this, android.R.color.holo_blue_bright));
        if (sortItem != null)
            sortItem.setVisible(false);
    }

    void stopSelectedItemMode() {
        isInSelectedItemMode = false;
        selectedRL.setBackgroundColor(TRANSPARENT);
        menu.setGroupVisible(R.id.selection_group, false);
        if (sortItem != null)
            sortItem.setVisible(true);
    }

    @SuppressLint("InflateParams")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isInSelectedItemMode)
                    stopSelectedItemMode();
                loadProjectList();
                return true;
            case R.id.delete_project:
                if (isInSelectedItemMode) {
                    ProjectAdapter a = (ProjectAdapter) (p() ? rv1 : rv2).getAdapter();
                    int pos = a.places.indexOfPlaceWithName(selectedName);
                    if (p()) {
                        deleteProject(selectedName);
                        rv1.removeViewAt(pos);
                    } else {
                        deleteRoom(selectedName, theProject);
                        rv2.removeViewAt(pos);
                    }
                    a.places.remove(pos);
                    a.notifyItemRemoved(pos);
                    a.notifyItemRangeChanged(pos, a.places.size());
                    if (a.places.isEmpty()) {
                        (p() ? rv1 : rv2).setVisibility(GONE);
                        noElementsTV.setVisibility(VISIBLE);
                        if (sortItem != null)
                            sortItem.setVisible(false);
                    }

                    isInSelectedItemMode = false;
                    selectedRL = null;
                    menu.setGroupVisible(R.id.selection_group, false);
                    if (sortItem != null)
                        sortItem.setVisible(true);
                }
                return true;
            case R.id.rename_project: // запустить диалог выбора нового имени
                if (isInSelectedItemMode) {
                    final TextInputLayout content = (TextInputLayout) getLayoutInflater().inflate(p()
                            ? R.layout.dialog_new_project
                            : R.layout.dialog_new_room, null, false);
                    //noinspection deprecation
                    Builder builder = new Builder(this)
                            .setView(content, toPx(36), toPx(12), toPx(36), toPx(12))
                            .setTitle(p() ? "Переименовать проект" : "Переименовать комнату")
                            .setNegativeButton("Отмена", this)
                            .setPositiveButton("Переименовать",
                                    dialogClickListenerRename(content));
                    alertRename = builder.create();
                    //noinspection ConstantConditions
                    content.getEditText().addTextChangedListener(new TextWatcher() {
                        public void beforeTextChanged(CharSequence s, int st, int c, int a) {
                        }

                        public void afterTextChanged(Editable s) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s, int st, int b, int c) {
                            if (s.length() == 0)
                                if (!qq) {
                                    content.setError("Введите название "
                                            + (p() ? "проекта" : "помещения"));
                                    alertRename.getButton(-1).setVisibility(INVISIBLE);
                                } else qq = false;
                            else if (p() ? isThereAProject(s.toString())
                                    : doesProjectContain(s.toString(), theProject)
                                    && !s.equals(selectedName)) {

                                content.setError((p() ? "Проект" : "Помещение")
                                        + " с таким названием уже существует");
                                alertRename.getButton(-1).setVisibility(INVISIBLE);
                            } else {
                                content.setError(null);
                                alertRename.getButton(-1).setVisibility(VISIBLE);
                            }
                        }
                    });
                    alertRename.show();
                    return true;
                }
                break;
            case R.id.city_item:
                showCityDialog(false);
                break;
            //region case: sortItems
            case R.id.by_name:
            case R.id.by_date:
            case R.id.by_area:
                Comparator<Place> selectedComparator = null;
                int selectedComparatorId = 0;
                switch (item.getItemId()) {
                    case R.id.by_name:
                        selectedComparator = comparators.COMPARATOR_BY_NAME;
                        selectedComparatorId = 0;
                        byName.setChecked(true);
                        break;
                    case R.id.by_date:
                        selectedComparator = comparators.COMPARATOR_BY_DATE;
                        selectedComparatorId = 1;
                        byDate.setChecked(true);
                        break;
                    case R.id.by_area:
                        selectedComparator = comparators.COMPARATOR_BY_AREA;
                        selectedComparatorId = 2;
                        byArea.setChecked(true);
                        break;
                }
                ProjectAdapter adapter = (ProjectAdapter) (p() ? rv1 : rv2).getAdapter();
                Collections.sort(adapter.places, selectedComparator);
                adapter.notifyDataSetChanged();
                prefs.edit().putInt(A_PREF_SELECTED_COMPARATOR, selectedComparatorId).apply();
                return true;
            //endregion
        }

        return super.onOptionsItemSelected(item);
    }

    private DialogInterface.OnClickListener dialogClickListenerRename(final TextInputLayout input) {
        return new DialogInterface.OnClickListener() {
            EditText et = input.getEditText();

            @Override
            public void onClick(DialogInterface d, int which) {
                if (p()) {
                    if (et.length() == 0) {
                        input.setError("Введите новое название проекта");
                        alertRename.getButton(-1).setVisibility(INVISIBLE);
                    } else {
                        String newName = pNameET.getText().toString();
                        if (!newName.equals(selectedName))
                            if (isThereAProject(newName)) {
                                input.setError("Проект с таким названием уже сущесвует");
                                alertRename.getButton(-1).setVisibility(INVISIBLE);
                            } else {
                                alertRename.dismiss();
                                renameProject(newName, selectedName);
                            }
                    }
                } else if (et.length() == 0) {
                    input.setError("Введите новое название помещения");
                    alertRename.getButton(-1).setVisibility(INVISIBLE);
                } else {
                    String newName = et.getText().toString();
                    if (!newName.equals(selectedName)) {
                        if (doesProjectContain(newName, theProject)) {
                            input.setError("Помещение с таким названием уже сущесвует");
                            alertRename.getButton(-1).setVisibility(INVISIBLE);
                        } else {
                            alertRename.dismiss();
                            renameRoom(newName, selectedName, theProject);
                        }
                    }
                }
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (!p())
            loadProjectList();
        else
            super.onBackPressed();
    }

//    AlertDialog firstEntryAlert;

    @SuppressLint("InflateParams")
    private void showCityDialog(boolean isItFirst) {
        final String[] cites = getResources().getStringArray(R.array.cites);
        View content = getLayoutInflater().inflate(R.layout.dialog_city, null, false);

        final Dialog d = new Dialog(this, R.style.AppTheme);
        d.setContentView(content);
        d.show();


        TextView temperBtn = (TextView) content.findViewById(R.id.enter_temper_btn);
        temperBtn.setOnClickListener(new OnClickListener() {
            private NumberPicker np;
            private String[] values;

            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.enter_temper_btn: //При клике на кнопку "Ввести температуру вручную"
                        Utils.closeKeyBrd(StartActivity.this);
                        Utils.closeKeyBrd2(StartActivity.this);
                        d.setOnKeyListener(new DialogInterface.OnKeyListener() {
                            public boolean onKey(DialogInterface d, int code, KeyEvent e) {
                                if (code == KEYCODE_BACK) {
                                    d.dismiss();
                                    showCityDialog(true);
                                    return true;
                                } else
                                    return false;
                            }
                        });
                        View content = getLayoutInflater()
                                .inflate(R.layout.dialog_temper, null, false);
                        d.setContentView(content);

                        np = (NumberPicker) d.findViewById(R.id.nPicker);
                        final int minValue = 10, maxValue = -52;
                        values = new String[minValue - maxValue + 1];
                        int qq = minValue;
                        for (int i = 0; qq >= maxValue; i++, qq--)
                            values[i] = Integer.toString(qq);
                        np.setMinValue(0);
                        np.setMaxValue(minValue - maxValue);
                        np.setDisplayedValues(values);
                        np.setValue(minValue + prefs.getInt(A_PREF_COLDEST_T, 28));

                        TextView enterBtn = (TextView) d.findViewById(R.id.enter_btn);
                        assert enterBtn != null;
                        enterBtn.setOnClickListener(this);
                        break;

                    case R.id.enter_btn: //При клике на кнопку ввода температуры на улице
                        d.dismiss();
                        int value = -Integer.parseInt(values[np.getValue()]);
                        Editor editor = prefs.edit();
                        editor.putInt(A_PREF_COLDEST_T, value);
                        editor.putString(A_PREF_CITY, null);
                        editor.putBoolean(A_PREF_EXISTS, true);
                        editor.apply();
                        cityItem.setTitle(getString(R.string.selected_temper
                                , prefs.getInt(A_PREF_COLDEST_T, 2147483647)));
                        break;
                }
            }
        });
        final AutoCompleteTextView autoET =
                (AutoCompleteTextView) content.findViewById(R.id.city_et);
        assert autoET != null;
        final ArrayAdapter<?> autoAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, cites);
        autoET.setAdapter(autoAdapter);
        autoET.setThreshold(1);
        autoET.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) {
            }

            public void onTextChanged(CharSequence s, int st, int b, int c) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                autoET.postDelayed(new Runnable() {
                    public void run() {
                        autoET.setTextColor(autoAdapter.getCount() == 0 ? RED : BLACK);
                    }
                }, 80);
            }
        });
        autoET.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                Utils.closeKeyBrd2(StartActivity.this);
                int[] arr = getResources().getIntArray(R.array.cityVals);
                final String txt = ((TextView) v).getText().toString();
                int index = 0;
                while (!txt.equals(cites[index])) index++;
                d.dismiss();
                Editor editor = prefs.edit();
                editor.putInt(A_PREF_COLDEST_T, arr[index]);
                editor.putString(A_PREF_CITY, cites[index]);
                editor.putBoolean(A_PREF_EXISTS, true);
                editor.apply();
                cityItem.setTitle(getString(R.string.selected_city, txt, prefs.getInt(A_PREF_COLDEST_T, 2147483647)));
            }
        });
    }

    protected void onRestart() {
        super.onRestart();
        loadProject(openedPrj, true, false);
    }

    private class ProjectAdapter extends RecyclerView.Adapter<ProjectAdapter.MyViewHolder> {
        private PlaceList<Place> places;

        private ProjectAdapter(@NonNull PlaceList<Place> places) {
            this.places = places;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(
                    getLayoutInflater().inflate(R.layout.recycler_item, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder h, int pos) {
            h.nameTV.setText(places.get(pos).name);

            long date = places.get(pos).date;
            if (date == 0) h.dateTV.setText(null);
            else {
                String dateStr = DateFormat.format("dd.MM.yy", new Date(date)).toString();
                h.dateTV.setText(dateStr);
            }

            int power = places.get(pos).power;
            float area = places.get(pos).area;

            CharSequence s;
            if (area == 0 && power == 0) {
                s = getString(p() ? R.string.no_saved_rooms_in_the_p : R.string.room_ve_not_saved);
            } else if (area == 0) {
                s = getString(R.string.power_unit, (float) power / 1000);
            } else if (power == 0)
                s = getString(R.string.area_value, area);
            else //noinspection deprecation
                s = fromHtml(getString(R.string.area_n_power, area,
                        getString(R.string.power_unit, (float) power / 1000)));
            h.areaNPowerTV.setText(s);
        }

        @Override
        public int getItemCount() {
            return places.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView nameTV;
            private TextView areaNPowerTV;
            private TextView dateTV;

            MyViewHolder(View itemView) { //itemView click is listen by MyRippleView
                super(itemView);
                nameTV = (TextView) itemView.findViewById(R.id.name_tv);
                areaNPowerTV = (TextView) itemView.findViewById(R.id.area_sum_tv);
                dateTV = (TextView) itemView.findViewById(R.id.date_tv);
            }
        }
    }

    private Comparator<Place> obtainSelectedComparator() {
        int comparator = prefs.getInt(A_PREF_SELECTED_COMPARATOR, 0);
        switch (comparator) {
            case Comparators.BY_NAME:
                return comparators.COMPARATOR_BY_NAME;

            case Comparators.BY_DATE:
                return comparators.COMPARATOR_BY_DATE;

            case Comparators.BY_AREA:
                return comparators.COMPARATOR_BY_AREA;

            default:
                return comparators.COMPARATOR_BY_NAME;
        }
    }

    private class Comparators {
        static final int BY_NAME = 0, BY_DATE = 1, BY_AREA = 2;
        final Comparator<Place> COMPARATOR_BY_NAME = new Comparator<Place>() {
            @Contract(pure = true)
            @Override
            public int compare(Place o1, Place o2) {
                return o1.name.compareTo(o2.name);
            }
        };
        final Comparator<Place> COMPARATOR_BY_DATE = new Comparator<Place>() {
            @Contract(pure = true)
            @Override
            public int compare(Place o1, Place o2) {
                return o1.date > o2.date ? 1 : o1.date < o2.date ? -1 : 0;
            }
        };

        final Comparator<Place> COMPARATOR_BY_AREA = new Comparator<Place>() {
            @Contract(pure = true)
            @Override
            public int compare(Place o1, Place o2) {
                return o1.area > o2.area ? 1 : o1.area < o2.area ? -1 : 0;
            }
        };

    }

    @Contract(pure = true)
    boolean p() {
        return theProject == null;
    }

    /**
     * The stub
     */
    @Contract(pure = true)
    SharedPreferences getPreferences(String name) {
        return Utils.getPreferences(name);
    }
}