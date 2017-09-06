//Если этот код работает, его написал Соколов Филипп, а если нет, то не знаю, кто его писал.

//Todo: В будущем лучше использовать для проектов с комнатами Recycler View with Expandable Items
// площадь и мощность проекта
//Todo: использование ActionMode
//Todo: Override pending transition
//Todo: Use files instead of SharedPreferences
//http://stackoverflow.com/questions/7944601/saving-a-hash-map-into-shared-preferences
package ru.obrazcenter.teplolux;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.EditText;
import android.widget.ImageView;
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


import static android.graphics.Bitmap.createBitmap;
import static android.graphics.Color.TRANSPARENT;
import static android.support.v7.widget.DividerItemDecoration.VERTICAL;
import static android.text.Html.fromHtml;
import static android.view.KeyEvent.ACTION_DOWN;
import static android.view.KeyEvent.KEYCODE_ENTER;
import static android.view.View.GONE;
import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_SHORT;
import static ru.obrazcenter.teplolux.Main.APP_PREFERENCES;
import static ru.obrazcenter.teplolux.Main.A_PREF_SELECTED_COMPARATOR;
import static ru.obrazcenter.teplolux.Main.SAVED_ROOM;
import static ru.obrazcenter.teplolux.Main.SUBTITLE;
import static ru.obrazcenter.teplolux.Main.TITLE;
import static ru.obrazcenter.teplolux.ProjectLogics.isThereAProject;
import static ru.obrazcenter.teplolux.ProjectLogics.isThereARoomInProject;
import static ru.obrazcenter.teplolux.ProjectLogics.saveNewRoom;
import static ru.obrazcenter.teplolux.Utils.toPx;

public class StartActivity extends AppCompatActivity
        implements OnClickListener, DialogInterface.OnClickListener {

    static SharedPreferences prefs;
    private TextInputLayout pInput;
    private TextInputLayout rInput;
    private EditText pNameET;
    private EditText rNameET;
    private Builder builder;
    private AlertDialog alert;
    private RecyclerView rv;
    private View fab;
    private TextView noElementsTV;
    static String theProject = null;
    private ImageView bmOfRv;
    private Menu menu;
    private MenuItem sortMenuItem;
    private MenuItem byName;
    private MenuItem byDate;
    private MenuItem byArea;

    @SuppressLint("StaticFieldLeak")
    static StartActivity activity1;
    private final Comparators comparators = new Comparators();
    static final Gson gson = new Gson();
    static boolean isInSelectedItemMode;
    private String selectedName;
    View selectedRL;
    private boolean qq = false;
    private AlertDialog renameAlert;

    @SuppressLint("InflateParams")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity1 = this;
        setContentView(R.layout.app_bar_start);
        prefs = getPreferences(APP_PREFERENCES);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        fab = findViewById(R.id.fab);
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
                }
                return false;
            }
        };
        pNameET.setOnKeyListener(keyListener);
        rNameET.setOnKeyListener(keyListener);

        pNameET.addTextChangedListener(watcher);
        rNameET.addTextChangedListener(watcher);

        noElementsTV = (TextView) findViewById(R.id.noElements_tv);
        rv = (RecyclerView) findViewById(R.id.projects_recycler_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setItemAnimator(new DefaultItemAnimator());
        rv.addItemDecoration(new DividerItemDecoration(this, VERTICAL));
        builder = new Builder(this).setNegativeButton("Отмена", this);
        loadProjectList();
        bmOfRv = (ImageView) findViewById(R.id.bmOfRv);
    }

    @SuppressWarnings("ConstantConditions")
    void loadProjectList() {
        theProject = null;
        ActionBar bar = getSupportActionBar();
        bar.setTitle(R.string.project_list);
        bar.setDisplayHomeAsUpEnabled(false);
        PlaceList<Place> projects = ProjectLogics.obtainProjectList(obtainSelectedComparator());
        if (projects != null) {
            noElementsTV.setVisibility(GONE);
            rv.setAdapter(new ProjectAdapter(projects));
            rv.setVisibility(VISIBLE);
            if (sortMenuItem != null) // else in onCreateOptionsMenu()
                sortMenuItem.setVisible(true);
        } else {
            rv.setAdapter(new ProjectAdapter(new PlaceList<Place>(0)));
            rv.setVisibility(GONE);
            noElementsTV.setVisibility(VISIBLE);
            noElementsTV.setText(R.string.no_saved_projects);
            if (sortMenuItem != null) // else in onCreateOptionsMenu()
                sortMenuItem.setVisible(false);
        }
    }

    @SuppressWarnings("ConstantConditions")
    void loadProject(String pName, boolean isFull) {
        theProject = pName;
        ActionBar bar = getSupportActionBar();
        bar.setTitle(theProject);
        bar.setDisplayHomeAsUpEnabled(true);
        if (isInSelectedItemMode) stopSelectedItemMode();
        PlaceList<Place> rooms = !isFull ? null : ProjectLogics.obtainRoomList(
                theProject, obtainSelectedComparator());
        if (rooms != null) {
            noElementsTV.setVisibility(GONE);
            rv.setVisibility(VISIBLE);

            ProjectAdapter adapter = new ProjectAdapter(rooms);
            pOpeningAnimation(adapter);
            if (sortMenuItem != null)
                sortMenuItem.setVisible(true);
        } else {
            rv.setAdapter(new ProjectAdapter(new PlaceList<Place>(0)));
            rv.setVisibility(GONE);
            noElementsTV.setText(R.string.no_saved_rooms);
            noElementsTV.setVisibility(VISIBLE);
            if (sortMenuItem != null)
                sortMenuItem.setVisible(false);
        }
    }

    private void pOpeningAnimation(ProjectAdapter adapter) {
        bmOfRv.setVisibility(VISIBLE);
        initRvCopy();
        AnimationSet anim = new AnimationSet(true);
        anim.addAnimation(new TranslateAnimation(0, -0.5f, 0, 0));
        anim.addAnimation(new ScaleAnimation(1, 0.5f, 0, 0, 0, 0));
        anim.addAnimation(new AlphaAnimation(1, 0));
        anim.setDuration(3000);
        anim.setAnimationListener(new AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bmOfRv.setVisibility(GONE);
            }
        }); // Switch off copy of recyclerView
        bmOfRv.startAnimation(anim);
        rv.setAdapter(adapter);

        AnimationSet anim2 = new AnimationSet(true);
        anim2.addAnimation(new TranslateAnimation(0.5f, 0, 0, 0));
        anim2.addAnimation(new AlphaAnimation(0, 1));
        anim2.setDuration(3000);
        rv.startAnimation(anim2);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab:
                if (isInSelectedItemMode)
                    stopSelectedItemMode();
                initDialog();
                alert.show();
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
            alert = builder.create();
            alert.getWindow().getAttributes().horizontalWeight = 0.8f;
            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    pInput.setError(null);
                    alert.getButton(-1).setVisibility(VISIBLE);
                }
            });
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    alert.getButton(-1).setOnClickListener(StartActivity.this);
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
            if (parent != null)
                if (parent instanceof ViewGroup)
                    ((ViewGroup) parent).removeView(rInput);
            //noinspection deprecation
            builder.setView(rInput, toPx(36), toPx(12), toPx(12), toPx(12))
                    .setPositiveButton("Создать помещение", this);
            alert = builder.create();
            alert.getWindow().getAttributes().horizontalWeight = 0.8f;
            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    pInput.setError(null);
                    alert.getButton(-1).setVisibility(VISIBLE);
                }
            });
            alert.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface d) {
                    alert.getButton(-1).setOnClickListener(StartActivity.this);
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
                alert.getButton(-1).setVisibility(INVISIBLE);
            } else if (isThereAProject(s)) {
                pInput.setError("Проект с таким названием уже сущесвует");
                alert.getButton(-1).setVisibility(INVISIBLE);
            } else {
                pInput.setError(null);
                alert.getButton(-1).setVisibility(VISIBLE);
            }
            else if (s.length() == 0) {
                rInput.setError("Введите название помещения");
                alert.getButton(-1).setVisibility(INVISIBLE);
            } else if (Utils.getPreferences(theProject).contains(s)) {
                rInput.setError("Помещение с таким названием уже сущесвует");
                alert.getButton(-1).setVisibility(INVISIBLE);
            } else {
                rInput.setError(null);
                alert.getButton(-1).setVisibility(VISIBLE);
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
                    alert.getButton(-1).setVisibility(INVISIBLE);
                } else {
                    String pName = pNameET.getText().toString();
                    if (isThereAProject(pName)) {
                        pInput.setError("Проект с таким названием уже сущесвует");
                        alert.getButton(-1).setVisibility(INVISIBLE);
                    } else {
                        alert.dismiss();
                        ProjectLogics.saveNewProject(pName);
                        loadProject(pName, false);
                        fab.performClick();
                    }
                }
            } else if (rNameET.length() == 0) {
                rInput.setError("Введите название помещения");
                alert.getButton(-1).setVisibility(INVISIBLE);
            } else {
                String roomName = rNameET.getText().toString();
                if (isThereARoomInProject(roomName, theProject)) {
                    rInput.setError("Помещение с таким названием уже сущесвует");
                    alert.getButton(-1).setVisibility(INVISIBLE);
                } else {
                    alert.dismiss();
                    saveNewRoom(roomName, System.currentTimeMillis(), theProject);
                    loadRoom(roomName);
                }
            }
        }
    }

    @SuppressWarnings("deprecation")
    private void initRvCopy() {
        int width = rv.getRight() - rv.getLeft();
        int height = rv.getBottom() - rv.getTop();
        Bitmap bm = createBitmap(width, height, Config.RGB_565);
        rv.layout(0, 0, width, height);
        rv.draw(new Canvas(bm));
        bmOfRv.setImageBitmap(bm);
    }


    @SuppressLint("InflateParams")
    void loadRoom(String roomName) {
        rv.setVisibility(VISIBLE);
        String roomStr = ProjectLogics.obtainRoomStr(roomName, theProject);

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
        if (sortMenuItem != null)
            sortMenuItem.setVisible(false);
    }

    void stopSelectedItemMode() {
        isInSelectedItemMode = false;
        selectedRL.setBackgroundColor(TRANSPARENT);
        menu.setGroupVisible(R.id.selection_group, false);
        if (sortMenuItem != null)
            sortMenuItem.setVisible(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds names to the action bar if it is present.
        getMenuInflater().inflate(R.menu.start, menu);
        this.menu = menu;
        sortMenuItem = menu.findItem(R.id.sort_by);
        SubMenu subMenu = sortMenuItem.getSubMenu();
        byName = subMenu.findItem(R.id.by_name);
        byDate = subMenu.findItem(R.id.by_date);
        byArea = subMenu.findItem(R.id.by_area);
        byName.setChecked(true);
        sortMenuItem.setVisible(rv.getAdapter().getItemCount() != 0);
        return true;
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
                if (p()) {
                    ProjectLogics.deleteProject(selectedName);
                } else {
                    ProjectLogics.deleteRoom(selectedName, theProject);
                }
                ProjectAdapter a = (ProjectAdapter) rv.getAdapter();
                int pos = a.places.indexOfPlaceWithName(selectedName);
                a.places.remove(pos);
                rv.removeViewAt(pos);
                a.notifyItemRemoved(pos);
                a.notifyItemRangeChanged(pos, a.places.size());
                if (a.places.isEmpty()) {
                    rv.setVisibility(GONE);
                    noElementsTV.setVisibility(VISIBLE);
                    if (sortMenuItem != null)
                        sortMenuItem.setVisible(false);
                }
                return true;
            case R.id.rename_project: // запустить диалог выбора нового имени
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
                renameAlert = builder.create();
                //noinspection ConstantConditions
                content.getEditText().addTextChangedListener(new TextWatcher() {
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    public void afterTextChanged(Editable s) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int st, int b, int c) {
                        if (s.length() == 0)
                            if (!qq) {
                                content.setError("Введите название "
                                        + (p() ? "проекта" : "помещения"));
                                alert.getButton(-1).setVisibility(INVISIBLE);
                            } else qq = false;
                        else if (p() ? isThereAProject(s.toString())
                                : isThereARoomInProject(s.toString(), theProject)
                                && !s.equals(selectedName)) {

                            content.setError((p() ? "Проект" : "Помещение")
                                    + " с таким названием уже существует");
                            alert.getButton(-1).setVisibility(INVISIBLE);
                        } else {
                            content.setError(null);
                            alert.getButton(-1).setVisibility(VISIBLE);
                        }
                    }
                });
                alert.show();
                return true;
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
                ProjectAdapter adapter = (ProjectAdapter) rv.getAdapter();
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
                        renameAlert.getButton(-1).setVisibility(INVISIBLE);
                    } else {
                        String newName = pNameET.getText().toString();
                        if (!newName.equals(selectedName))
                            if (isThereAProject(newName)) {
                                input.setError("Проект с таким названием уже сущесвует");
                                renameAlert.getButton(-1).setVisibility(INVISIBLE);
                            } else {
                                renameAlert.dismiss();
                                ProjectLogics.renameProject(newName, selectedName);
                            }
                    }
                } else if (et.length() == 0) {
                    input.setError("Введите новое название помещения");
                    renameAlert.getButton(-1).setVisibility(INVISIBLE);
                } else {
                    String newName = et.getText().toString();
                    if (!newName.equals(selectedName)) {
                        if (ProjectLogics.isThereARoomInProject(newName, theProject)) {
                            input.setError("Помещение с таким названием уже сущесвует");
                            renameAlert.getButton(-1).setVisibility(INVISIBLE);
                        } else {
                            renameAlert.dismiss();
                            ProjectLogics.renameRoom(newName, selectedName, theProject);
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

//    @Override
//    protected void onRestart() {
//        super.onRestart();
//        loadProjectList();
//    }

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

    @Contract(pure = true)
    private boolean p() {
        return theProject == null;
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

//    class Place implements Comparable<Place>, Cloneable {
//        String name;
//        float area;
//        int power;
//        long date;
//
//        private Place() {
//        }
//
//        Place(@NonNull String name, long millis) {
//            this.name = name;
//            date = millis;
//        }
//
//        @SuppressWarnings("unused")
//        Place(@NonNull String name, long millis, int area, int power) {
//            this.name = name;
//            this.area = area;
//            this.power = power;
//        }
//
//        public int compareTo(@NonNull Place p) {
//            return name.compareTo(p.name);
//        }
//
//        public String toString() {
//            return name;
//        }
//    }
//
//    class Room extends Place {
//        Values cv;
//        FloorValues fv;
//        WallValues[] wv;
//        float height;
//        int insideTemper;
//        boolean saved;
//
//
//        Room(@NonNull String name, long millis) {
//            this.name = name;
//            this.date = millis;
//            saved = false;
//        }
//
//        Room(@NonNull String name, long millis, int area, int power,
//             float height, int insideTemper, @NonNull WallValues[] wv,
//             @NonNull Values cv, @NonNull FloorValues fv) {
//            this.name = name;
//            this.date = millis;
//            this.area = area;
//            this.power = power;
//            saved = true;
//            this.wv = wv;
//            this.cv = cv;
//            this.fv = fv;
//            this.height = height;
//            this.insideTemper = insideTemper;
//            //it works only in case when Main activity is ready
//            this.wv[0].lNumb = frag2.performed ? frag2.layerNum : 0;
//            this.wv[1].lNumb = frag3.performed ? frag3.layerNum : 0;
//            this.wv[2].lNumb = frag4.performed ? frag4.layerNum : 0;
//            this.wv[3].lNumb = frag5.performed ? frag5.layerNum : 0;
//        }
//    }
//
//    private class PlaceList<T> extends ArrayList<T> {
//        PlaceList(int initialCapacity) {
//            super(initialCapacity);
//        }
//
//        int indexOfPlaceWithName(@NonNull String name) {
//            try {
//                Field field = getClass().getDeclaredField("elementData");
//                field.setAccessible(true);
//                Place[] elementData = (Place[]) field.get(this);
//                for (int i = 0; i < size(); i++)
//                    if (name.equals(elementData[i].name))
//                        return i;
//            } catch (NoSuchFieldException e) {
//                e.printStackTrace();
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            }
//            return -1;
//        }
//    }

    /**
     * The stub
     */
    @Contract(pure = true)
    SharedPreferences getPreferences(String name) {
        return Utils.getPreferences(name);
    }
}