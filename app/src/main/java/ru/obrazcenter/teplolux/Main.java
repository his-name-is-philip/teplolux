//Если этот код работает, его написал Соколов Филипп, а если нет, то не знаю, кто его писал.
package ru.obrazcenter.teplolux;

import android.animation.LayoutTransition;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AlertDialog.Builder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import ru.obrazcenter.teplolux.Fragment1.FloorValues;
import ru.obrazcenter.teplolux.Fragment1.Values;
import ru.obrazcenter.teplolux.ProjectLogics.Room;


import static android.graphics.Color.BLACK;
import static android.graphics.Color.RED;
import static android.support.v4.view.GravityCompat.START;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.String.valueOf;
import static ru.obrazcenter.teplolux.Calculations.calculate;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.GRUNT;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.INTERFLOOR;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.LOGS;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.WITH_UNDERGROUND;
import static ru.obrazcenter.teplolux.Utils.closeKeyBrd;

@SuppressLint("StaticFieldLeak")
public class Main extends AppCompatActivity {
    private SharedPreferences mSettings;
    static final String APP_PREFERENCES = "my settings",
            A_PREF_CITY = "city",
            A_PREF_COLDEST_T = "coldest temper",
            A_PREF_EXISTS = "exists",
            A_PREF_PROJECT_NAMES = "project names",
            A_PREF_SELECTED_COMPARATOR = "selected comparator",
            TITLE = "room_name",
            SUBTITLE = "project_name",
            SAVED_ROOM = "saved_room";
    static Fragment1 frag1;
    static WallFragment frag2, frag3, frag4, frag5;
    static Activity mainActivity;
    static Resources res;
    CollapsingToolbarLayout collapsingLayout;
    AppBarLayout appBarLayout;
    private DrawerLayout drawer;
    private ViewPager vp;
    private int[] pageLinks,
            pageFocusLinks;
    private final ArrayList<String> errList = new ArrayList<>(40);
    private String roomName, projectName;
    private TextView cityBtn;
    private String[] cites;
    private AlertDialog alert;

    private ViewGroup ansL;
    private ListView errLV;
    private ArrayAdapter<String> errAdapter;
    private boolean performed = false;
    private int i;
    private TextView wTV;
    private TextView cTV;
    private TextView fTV;
    private TextView winTV;
    private TextView totalTV;
    private TextView headerTV;
    private String[] values;
    private NumberPicker np;
    private Answer a;

    private View progressBar;

    private Builder builder;
    private boolean isCitySelected = false; // Only for city setup

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(final Bundle state) {
        super.onCreate(state);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);
        mainActivity = this;
        res = getResources();

        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        collapsingLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_layout);
        final Toolbar bar = (Toolbar) findViewById(R.id.collapsing_toolbar);
        Intent intent = getIntent();
        roomName = intent.getStringExtra(TITLE);
        projectName = intent.getStringExtra(SUBTITLE);
        setSupportActionBar(bar);
        final Room r = new Gson().fromJson(intent.getStringExtra(SAVED_ROOM), Room.class);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        MyToggle toggle = new MyToggle
                (this, drawer, R.string.nav_drawer_open, R.string.nav_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        ActionBar bar1 = getSupportActionBar();
        bar1.setTitle(getString(R.string.Place, roomName));
        bar1.setSubtitle(getString(R.string.Project, projectName));
        toggle.setDrawerIndicatorEnabled(false);
        bar1.setDisplayHomeAsUpEnabled(true);
        bar1.setHomeAsUpIndicator(R.drawable.ic_action_close);
        vp = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager();
        ((TabLayout) findViewById(R.id.tabLayout)).setupWithViewPager(vp);

        vp.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrolled(int pos, float posOffset, int offsetPx) {
                closeKeyBrd(Main.this);
            }

            public void onPageSelected(int p) {
            }

            public void onPageScrollStateChanged(int s) {
            }
        });
        findViewById(R.id.fab)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawer.openDrawer(START);
                    }
                });
        mSettings = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        cityBtn = (TextView) findViewById(R.id.cityBtn);
        final String cityStr = mSettings.getString(A_PREF_CITY, null);
        if (!mSettings.getBoolean(A_PREF_EXISTS, false)) {
            SharedPreferences.Editor editor = mSettings.edit();
            editor.putInt(A_PREF_COLDEST_T, 25);
            editor.putString(A_PREF_CITY, "Москва");
            editor.putBoolean(A_PREF_EXISTS, true);
            editor.apply();
            cityBtn.setText(getString(R.string.selected_city, "Москва", 25));
            toggle.showMyDialog();
            alert.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface d) {
                    onMyDialogExit();
                }
            });
            alert.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface d) {
                    onMyDialogExit();
                }
            });
        } else if (cityStr == null)
            cityBtn.setText(getString(R.string.selected_temper, mSettings.getInt(A_PREF_COLDEST_T, 2147483647)));
        else {
            cityBtn.setText(getString(R.string.selected_city, cityStr, mSettings.getInt(A_PREF_COLDEST_T, 2147483647)));
        }
        if (r.saved) new Handler().postDelayed(new Runnable() {
            public void run() {
                loadRoom(r);
            }
        }, 3000);
    }

    private class MyToggle extends ActionBarDrawerToggle
            implements OnItemClickListener, OnClickListener {

        private MyToggle(Activity a, DrawerLayout dl, int o, int c) {
            super(a, dl, o, c);
        }

        @Override
        public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
            // При нажатии на элемент списка errLV
            if (p.getId() == R.id.err_lv) {
                drawer.closeDrawers();
                vp.setCurrentItem(pageLinks[pos] + 1);
                switch (pageLinks[pos]) {
                    case -1:
                        frag1.setFocusByLink(pageFocusLinks[pos]);
                        break;
                    case 0:
                        frag2.setFocusByLink(pageFocusLinks[pos]);
                        break;
                    case 1:
                        frag3.setFocusByLink(pageFocusLinks[pos]);
                        break;
                    case 2:
                        frag4.setFocusByLink(pageFocusLinks[pos]);
                        break;
                    case 3:
                        frag5.setFocusByLink(pageFocusLinks[pos]);
                        break;
                }
            } else { // При нажатии на элемент из autoET
                Utils.closeKeyBrd2(Main.this);
                int[] arr = res.getIntArray(R.array.cityVals);
                final String TXT = ((TextView) v).getText().toString();
                int index = 0;
                while (!TXT.equals(cites[index])) index++;
                alert.dismiss();
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(A_PREF_COLDEST_T, arr[index]);
                editor.putString(A_PREF_CITY, cites[index]);
                editor.putBoolean(A_PREF_EXISTS, true);
                editor.apply();

                Toast t = Toast.makeText(Main.this, cites[index] + ": -" + arr[index], LENGTH_LONG);
                t.setGravity(Gravity.TOP, 0, 0);
                t.show();
                cityBtn.setText(getString(R.string.selected_city, cites[index], arr[index]));
                alert.setOnCancelListener(null);
                alert.setOnDismissListener(null);
                isCitySelected = true;
                if (drawer.isDrawerOpen(START)) onDrawerOpened(null); //Пересчитать
            }
        }

        @SuppressLint("InflateParams")
        @Override
        public void onClick(View v) {
            // При клике на кнопку ввода температуры на улице
            if (v.getId() == R.id.enter_btn) {
                alert.dismiss();
                int value = -Integer.parseInt(values[np.getValue()]);
                SharedPreferences.Editor editor = mSettings.edit();
                editor.putInt(A_PREF_COLDEST_T, value);
                editor.putString(A_PREF_CITY, null);
                editor.putBoolean(A_PREF_EXISTS, true);
                editor.apply();

                Toast t = Toast.makeText(Main.this, "Температура: -" + value, LENGTH_SHORT);
                t.setGravity(Gravity.TOP, 0, 0);
                t.show();
                cityBtn.setText(value >= 0 ? getString(R.string.selected_temper, value) : getString(R.string.selected_temper2, -value));
                alert.setOnCancelListener(null);
                alert.setOnDismissListener(null);
                if (drawer.isDrawerOpen(drawer)) onDrawerOpened(null);
            } else { // При клике на кнопку "Ввести температуру вручную"
                Utils.closeKeyBrd(Main.this);
                Utils.closeKeyBrd2(Main.this);
                alert.setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface d, int keyCode, KeyEvent e) {
                        if (keyCode == KeyEvent.KEYCODE_BACK) {
                            alert.dismiss();
                            showMyDialog();
                            return true;
                        }
                        return false;
                    }
                });
                View content = getLayoutInflater().inflate(R.layout.dialog_temper, null, false);
                builder.setView(content).setTitle("Выбрать температуру");

                np = (NumberPicker) alert.findViewById(R.id.nPicker);
                final int minValue = 10, maxValue = -52;
                values = new String[minValue - maxValue + 1];
                int qq = minValue;
                for (int i = 0; qq >= maxValue; i++, qq--)
                    values[i] = Integer.toString(qq);
                np.setMinValue(0);
                np.setMaxValue(minValue - maxValue);
                np.setDisplayedValues(values);
                np.setValue(minValue + mSettings.getInt(A_PREF_COLDEST_T, 28));

                TextView enterBtn = (TextView) alert.findViewById(R.id.enter_btn);
                assert enterBtn != null;
                enterBtn.setOnClickListener(this);
                if (drawer.isDrawerOpen(drawer)) onDrawerOpened(null);
            }
        }


        @SuppressLint("InflateParams")
        private void showMyDialog() {
            cites = res.getStringArray(R.array.cites);
            View content = getLayoutInflater().inflate(R.layout.dialog_city, null, false);
            builder = new Builder(Main.this)
                    .setTitle("Выбрать город")
                    .setView(content);
            alert = builder.create();

            Window w = alert.getWindow();
            assert w != null;
            WindowManager.LayoutParams wlp = w.getAttributes();
            wlp.gravity = Gravity.TOP;
            w.setAttributes(wlp);
            alert.show();
            alert.setOnCancelListener(null);
            alert.setOnDismissListener(null);
            TextView temperBtn = (TextView) content.findViewById(R.id.enter_temper_btn);
            temperBtn.setOnClickListener(this);
            final AutoCompleteTextView autoET =
                    (AutoCompleteTextView) content.findViewById(R.id.city_et);
            assert autoET != null;
            final ArrayAdapter<?> autoAdapter =
                    new ArrayAdapter<>(Main.this, android.R.layout.simple_list_item_1, cites);
            autoET.setAdapter(autoAdapter);
            autoET.setThreshold(1);
            autoET.setOnItemClickListener(this);
            autoET.addTextChangedListener(new TextWatcher() {
                public void beforeTextChanged(CharSequence s, int st, int c, int a) {
                }

                public void onTextChanged(CharSequence s, int st, int b, int c) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    autoET.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            autoET.setTextColor(autoAdapter.getCount() == 0 ? RED : BLACK);
                        }
                    }, 80);
                }
            });
        }

        @Override
        public void onDrawerOpened(final View drawerV) {
            closeKeyBrd(Main.this);
            runOnUiThread(new Runnable() {
                public void run() {
                    cityBtn.setVisibility(VISIBLE);
                    if (!performed) {
                        performed = true;
                        progressBar = findViewById(R.id.progress_bar_nav);
                        ansL = (ViewGroup) findViewById(R.id.ans_layout);
                        errLV = (ListView) findViewById(R.id.err_lv);
                        errLV.setOnItemClickListener(MyToggle.this);
                        errAdapter = new ArrayAdapter<>(Main.this, R.layout.my_list_item, errList);
                        errLV.setAdapter(errAdapter);
                        cityBtn = (TextView) findViewById(R.id.cityBtn);
                        cityBtn.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                showMyDialog();
                            }
                        });
                        wTV = (TextView) findViewById(R.id.wQ);
                        cTV = (TextView) findViewById(R.id.cQ);
                        fTV = (TextView) findViewById(R.id.fQ);
                        winTV = (TextView) findViewById(R.id.winQ);
                        totalTV = (TextView) findViewById(R.id.totalQ);
                        headerTV = (TextView) findViewById(R.id.nav_header);
                    }
                    a = calculate(mSettings.getInt(A_PREF_COLDEST_T, 2147483647),
                            roomName, projectName);

                    if (a.err) {
                        pageLinks = new int[40];
                        pageFocusLinks = new int[40];
                        i = 0;
                        errList.clear();
                        if (a.insTempErr) {
                            errList.add("Вы не ввели внутреннюю температуру помещения");
                            pageFocusLinks[i] = 1;
                            pageLinks[i] = -1;
                            i++;
                        }
                        if (a.heightErr) {
                            errList.add("Вы не ввели высоту помещения");
                            pageFocusLinks[i] = 2;
                            pageLinks[i] = -1;
                            i++;
                        }
                        if (a.cErr) handleCErrs(a.cErrors);
                        if (a.fErr) handleFErrs(a.fErrors);

                        if (a.wErrs[0] && a.wallErrors[0].length) {
                            errList.add("Вы не ввели длину 1-й стенки");
                            pageFocusLinks[i] = 1;
                            pageLinks[i] = 0;
                            i++;
                        }
                        if (a.wErrs[1] && a.wallErrors[1].length) {
                            errList.add("Вы не ввели длину 2-й стенки");
                            pageFocusLinks[i] = 1;
                            pageLinks[i] = 1;
                            i++;
                        }
                        handleWErrs(a.wallErrors, a.wErrs);
                        errAdapter.notifyDataSetChanged();

                        errLV.setVisibility(VISIBLE);
                    } else {
                        headerTV.setText(getString(R.string.nav_header2));
                        if (a.wQ == 0) wTV.setVisibility(GONE);
                        else {
                            wTV.setText(getString(R.string.wHeatLoss, a.wQ));
                            wTV.setVisibility(VISIBLE);
                        }

                        if (a.cQ == 0) cTV.setVisibility(GONE);
                        else {
                            cTV.setText(getString(R.string.cHeatLoss, a.cQ));
                            cTV.setVisibility(VISIBLE);
                        }

                        if (a.fQ == 0) fTV.setVisibility(GONE);
                        else {
                            fTV.setText(getString(R.string.fHeatLoss, a.fQ));
                            fTV.setVisibility(VISIBLE);
                        }

                        if (a.winQ == 0) winTV.setVisibility(GONE);
                        else {
                            winTV.setText(getString(R.string.winHeatLoss, a.winQ));
                            winTV.setVisibility(VISIBLE);
                        }
                        totalTV.setText(getString(R.string.heatLoss, a.power));

                        ansL.setVisibility(VISIBLE);
                    }
                    progressBar.setVisibility(GONE);
                }
            });
        }

        public void onDrawerClosed(final View drawerV) {
            errLV.setVisibility(GONE);
            ansL.setVisibility(GONE);
            headerTV.setText(getString(R.string.nav_header));
            progressBar.setVisibility(VISIBLE);
            super.onDrawerClosed(drawerV);
        }

        private void handleWErrs(WallValues.Errors[] e, boolean[] wErrs) {
            for (int pageNum = 0; pageNum < 4; pageNum++)
                if (wErrs[pageNum]) {
                    int ind = i;
                    if (e[pageNum].tooBigGlassArea) {
                        errList.add("Площадь остекления " + (pageNum + 1) + "-й ст. не может быть больше площади стены!");
                        pageFocusLinks[i] = 8;
                        i++;
                    }
                    if (e[pageNum].thicknesses[0]) {
                        errList.add("Вы не ввели толщину утеплителя " + (pageNum + 1) + "-й ст.");
                        pageFocusLinks[i] = 3;
                        i++;
                    } else if (e[pageNum].zeros[0]) {
                        errList.add("Толщина утеплителя " + (pageNum + 1) + "-й ст. не может быть равна 0");
                        pageFocusLinks[i] = 3;
                        i++;
                    }
                    if (e[pageNum].materials[0]) {
                        errList.add("Вы не выбрали утеплитель " + (pageNum + 1) + "-й стены");
                        pageFocusLinks[i] = 2;
                        i++;
                    }

                    for (int n = 1; n <= 2; n++) {
                        if (e[pageNum].zeros[n]) {
                            errList.add("Толщина " + (n + 1) + "-го слоя утеплителя " + (pageNum + 1) + "-й ст. не может быть равна 0");
                            pageFocusLinks[i] = 2 * n + 3;
                            i++;
                        } else if (e[pageNum].thicknesses[n]) {
                            errList.add("Вы не ввели толщину " + (n + 1) + "-го слоя утеплителя " + (pageNum + 1) + "-й ст.");
                            pageFocusLinks[i] = 2 * n + 3;
                            i++;
                        }
                        if (e[pageNum].materials[n]) {
                            errList.add("Вы не выбрали утеплитель " + (n + 1) + "-го слоя " + (pageNum + 1) + "-й ст.");
                            pageFocusLinks[i] = (n + 1) * 2;
                            i++;
                        }
                    }
                    // "ind" is a previous value of "i"
                    while (ind < i) {
                        pageLinks[ind] = pageNum;
                        ind++;
                    }
                }
        }

        private void handleCErrs(Fragment1.Values.Errors e) {
            int ind = i;
            if (e.thicknesses[0]) {
                errList.add("Вы не ввели толщину утеплителя потолка");
                pageFocusLinks[i] = 4;
                i++;
            } else if (e.zeros[0]) {
                errList.add("Толщина утеплителя потолка не может быть равна 0");
                pageFocusLinks[i] = 4;
                i++;
            }
            if (e.materials[0]) {
                errList.add("Вы не выбрали утеплитель потолка");
                pageFocusLinks[i] = 3;
                i++;
            }

            for (int n = 1; n <= 2; n++) {
                if (e.thicknesses[n]) {
                    errList.add("Вы не ввели толщину " + (n + 1) + "-го слоя утеплителя потолка");
                    pageFocusLinks[i] = 2 * n + 4;
                    i++;
                } else if (e.zeros[n]) {
                    errList.add("Толщина " + (n + 1) + "-го слоя утеплителя потолка не может быть равна 0");
                    pageFocusLinks[i] = 2 * n + 4;
                    i++;
                }
                if (e.materials[n]) {
                    errList.add("Вы не выбрали утеплитель " + (n + 1) + "-го слоя потолка");
                    pageFocusLinks[i] = 2 * n + 3;
                    i++;
                }
            }
            // "ind" is a previous value of "i"
            while (ind < i) {
                pageLinks[ind] = -1;
                ind++;
            }
        }

        private void handleFErrs(Fragment1.Values.Errors e) {
            int ind = i;
            if (e.thicknesses[0]) {
                errList.add("Вы не ввели толщину утеплителя пола");
                pageFocusLinks[i] = 10;
                i++;
            } else if (e.zeros[0]) {
                errList.add("Толщина утеплителя пола не может быть равна 0");
                pageFocusLinks[i] = 10;
                i++;
            }
            if (e.materials[0]) {
                errList.add("Вы не выбрали утеплитель пола");
                pageFocusLinks[i] = 9;
                i++;
            }

            for (int n = 1; n <= 2; n++) {
                if (e.thicknesses[n]) {
                    errList.add("Вы не ввели толщину " + (n + 1) + "-го слоя утеплителя пола");
                    pageFocusLinks[i] = 2 * n + 10;
                    i++;
                } else if (e.zeros[n]) {
                    errList.add("Толщина " + (n + 1) + "-го слоя утеплителя пола не может быть равна 0");
                    pageFocusLinks[i] = 2 * n + 10;
                    i++;
                }
                if (e.materials[n]) {
                    errList.add("Вы не выбрали утеплитель " + (n + 1) + "-го слоя пола");
                    pageFocusLinks[i] = 2 * n + 9;
                    i++;
                }
            }
            // "ind" is a previous value of "i"
            while (ind < i) {
                pageLinks[ind] = -1;
                ind++;
            }
        }

    }

    private void loadRoom(Room r) {
        //height & inside temperature
        ((TextView) frag1.fragV.findViewById(R.id.cHeight)).setText(valueOf(r.height));
        ((TextView) frag1.fragV.findViewById(R.id.insideTemp_et)).setText(valueOf(r.insideTemper));

        @SuppressWarnings("unchecked")
        class RoomLoading {
            private CustomAdapter<CharSequence> a;

            private void initWalls(@NonNull WallValues[] v) {
                wall1:
                {
                    if (!frag2.performed) frag2.kostyl(1);
                    if (!v[0].isOutside) {
                        frag2.isItOutsideCB.setChecked(false);
                        break wall1;
                    }
                    frag2.isItOutsideCB.setChecked(true);
                    frag2.lengthET.setText(valueOf(v[0].length));
                    frag2.glassAreaET.setText(valueOf(v[0].glassArea));
                }
                wall2:
                {
                    if (!frag3.performed) frag3.kostyl(2);
                    if (!v[1].isOutside) {
                        frag3.isItOutsideCB.setChecked(false);
                        break wall2;
                    }
                    frag3.isItOutsideCB.setChecked(true);
                    frag3.lengthET.setText(valueOf(v[1].length));
                    frag3.glassAreaET.setText(valueOf(v[1].glassArea));

                }
                wall3:
                {
                    if (!frag4.performed) frag4.kostyl(3);
                    if (!v[2].isOutside) {
                        frag4.isItOutsideCB.setChecked(false);
                        break wall3;
                    }
                    frag4.isItOutsideCB.setChecked(true);
                    frag4.lengthET.setText(valueOf(v[2].length));
                    frag4.glassAreaET.setText(valueOf(v[2].glassArea));
                }
                wall4:
                {
                    if (!frag5.performed) frag5.kostyl(4);
                    if (!v[3].isOutside) {
                        frag5.isItOutsideCB.setChecked(false);
                        break wall4;
                    }
                    frag5.isItOutsideCB.setChecked(true);
                    frag5.lengthET.setText(valueOf(v[3].length));
                    frag5.glassAreaET.setText(valueOf(v[3].glassArea));
                }
                initWMaterialsNThicknesses(v);
            }

            private void initWMaterialsNThicknesses(WallValues[] v) {
                a = (CustomAdapter<CharSequence>) frag2.materials.getAdapter();

                if (v[0].isOutside) {
                    frag2.materials.setSelection(a.getPosition(v[0].materials[0]));
                    frag2.wallThickness.setText(valueOf(v[0].thicknesses[0]));
                    if (v[0].lNumb > 1) {
                        frag2.plusInsulant.performClick();
                        frag2.advSp2.setSelection(a.getPosition(v[0].materials[1]));
                        frag2.advEt2.setText(valueOf(v[0].thicknesses[1]));
                        if (v[0].lNumb == 3) {
                            frag2.plusInsulant.performClick();
                            frag2.advSp3.setSelection(a.getPosition(v[0].materials[2]));
                            frag2.advEt3.setText(valueOf(v[0].thicknesses[2]));
                        }
                    }
                }

                if (v[1].isOutside) {
                    frag3.materials.setSelection(a.getPosition(v[1].materials[0]));
                    frag3.wallThickness.setText(valueOf(v[1].thicknesses[0]));
                    if (v[1].lNumb > 1) {
                        frag3.plusInsulant.performClick();
                        frag3.advSp2.setSelection(a.getPosition(v[1].materials[1]));
                        frag3.advEt2.setText(valueOf(v[1].thicknesses[1]));
                        if (v[1].lNumb == 3) {
                            frag3.plusInsulant.performClick();
                            frag3.advSp3.setSelection(a.getPosition(v[1].materials[2]));
                            frag3.advEt3.setText(valueOf(v[1].thicknesses[2]));
                        }
                    }
                }

                if (v[2].isOutside) {
                    frag4.materials.setSelection(a.getPosition(v[2].materials[0]));
                    frag4.wallThickness.setText(valueOf(v[2].thicknesses[0]));
                    if (v[2].lNumb > 1) {
                        frag4.plusInsulant.performClick();
                        frag4.advSp2.setSelection(a.getPosition(v[2].materials[1]));
                        frag4.advEt2.setText(valueOf(v[2].thicknesses[1]));
                        if (v[2].lNumb == 3) {
                            frag4.plusInsulant.performClick();
                            frag4.advSp3.setSelection(a.getPosition(v[2].materials[2]));
                            frag4.advEt3.setText(valueOf(v[2].thicknesses[2]));
                        }
                    }
                }

                if (v[3].isOutside) {
                    frag5.materials.setSelection(a.getPosition(v[3].materials[0]));
                    frag5.wallThickness.setText(valueOf(v[3].thicknesses[0]));
                    if (v[3].lNumb > 1) {
                        frag5.plusInsulant.performClick();
                        frag5.advSp2.setSelection(a.getPosition(v[3].materials[1]));
                        frag5.advEt2.setText(valueOf(v[3].thicknesses[1]));
                        if (v[3].lNumb == 3) {
                            frag5.plusInsulant.performClick();
                            frag5.advSp3.setSelection(a.getPosition(v[3].materials[2]));
                            frag5.advEt3.setText(valueOf(v[3].thicknesses[2]));
                        }
                    }
                }
            }

            private void initCMaterialsNThicknesses(Values cv) {
                a = (CustomAdapter<CharSequence>) frag1.ceilSp.getAdapter();
                frag1.ceilSp.setSelection(a.getPosition(cv.materials[0]));
                frag1.cThicknessET.setText(valueOf(cv.thicknesses[0]));
                if (cv.lNumb > 1) {
                    frag1.plusC.performClick();
                    frag1.advCSp2.setSelection(a.getPosition(cv.materials[1]));
                    frag1.advCEt2.setText(valueOf(cv.thicknesses[1]));
                    if (cv.lNumb == 3) {
                        frag1.plusC.performClick();
                        frag1.advCSp3.setSelection(a.getPosition(cv.materials[2]));
                        frag1.advCEt3.setText(valueOf(cv.thicknesses[2]));
                    }
                }
            }

            private void initFMaterialsNThicknesses(FloorValues fv) {
                a = (CustomAdapter<CharSequence>) frag1.floorSp.getAdapter();
                frag1.floorSp.setSelection(a.getPosition(fv.materials[0]));
                frag1.fThicknessET.setText(valueOf(fv.thicknesses[0]));
                if (fv.lNumb > 1) {
                    frag1.plusF.performClick();
                    frag1.advFSp2.setSelection(a.getPosition(fv.materials[1]));
                    frag1.advFEt2.setText(valueOf(fv.thicknesses[1]));
                    if (fv.lNumb == 3) {
                        frag1.plusF.performClick();
                        frag1.advFSp3.setSelection(a.getPosition(fv.materials[2]));
                        frag1.advFEt3.setText(valueOf(fv.thicknesses[2]));
                    }
                }
            }
        }
        RoomLoading loading = new RoomLoading();
        //ceil
        if (r.cv.isOutside)
            loading.initCMaterialsNThicknesses(r.cv);
        else frag1.isCeilOuter.setChecked(false);

        //floor
        switch (r.fv.fType) {
            case INTERFLOOR:
                frag1.rb[3].setChecked(true);
                break;
            case WITH_UNDERGROUND:
                frag1.rb[0].setChecked(true);
                loading.initFMaterialsNThicknesses(r.fv);
                break;
            case LOGS:
                frag1.rb[2].setChecked(true);
                if (r.fv.isInsul) {
                    frag1.isInsulCB.setChecked(true);
                    loading.initFMaterialsNThicknesses(r.fv);
                } else frag1.isInsulCB.setChecked(false);
                break;
            case GRUNT:
                frag1.rb[1].setChecked(true);
                if (r.fv.isInsul) {
                    frag1.isInsulCB.setChecked(true);
                    loading.initFMaterialsNThicknesses(r.fv);
                } else frag1.isInsulCB.setChecked(false);
                break;
        }

        //walls
        loading.initWalls(r.wv);
        enableAutoAnimation();
    }

    private void enableAutoAnimation() {
        LayoutTransition transition = new LayoutTransition();
        frag1.layout.setLayoutTransition(transition);
        frag2.layout.setLayoutTransition(transition);
        frag3.layout.setLayoutTransition(transition);
        frag4.layout.setLayoutTransition(transition);
        frag5.layout.setLayoutTransition(transition);
    }


    private void onMyDialogExit() {
        if (!isCitySelected) {
            Toast.makeText(this, "ВНИМАНИЕ: Выбранный город: Москва", LENGTH_LONG).show();
            alert.setOnCancelListener(null);
            alert.setOnDismissListener(null);
        }
    }

    private void setupViewPager() {
        frag1 = new Fragment1();
        frag2 = new WallFragment();
        frag3 = new WallFragment();
        frag4 = new WallFragment();
        frag5 = new WallFragment();

        Bundle[] b = new Bundle[4];
        int i = 0;
        while (i < 4) {
            b[i] = new Bundle();
            b[i].putInt(getString(R.string.number), i = i + 1);
        }
        frag2.setArguments(b[0]);
        frag3.setArguments(b[1]);
        frag4.setArguments(b[2]);
        frag5.setArguments(b[3]);


        final ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(frag1, "ОБЩИЕ");
        adapter.addFragment(frag2, "1 СТ");
        adapter.addFragment(frag3, "2 СТ");
        adapter.addFragment(frag4, "3 СТ");
        adapter.addFragment(frag5, "4 СТ");
        vp.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(START)) drawer.closeDrawers();
        else super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case android.R.id.home:
                Toast.makeText(this, "Закрытие проекта", Toast.LENGTH_SHORT).show();
                finish();
                overridePendingTransition(0, 0);
//            new Handler().post(new Runnable() {
//                public void run() {
//                    activity1.loadProjectList();
//                }
//            });
        }

        return super.onOptionsItemSelected(item);
    }
}