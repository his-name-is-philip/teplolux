//Если этот код работает, его написал Соколов Филипп, а если нет, то не знаю, кто его писал.
package ru.obrazcenter.teplolux;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.NestedScrollView.OnScrollChangeListener;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;


import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.text.Html.fromHtml;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT;
import static ru.obrazcenter.teplolux.Main.frag1;
import static ru.obrazcenter.teplolux.Main.mainActivity;

@SuppressLint("InlinedApi")
public class WallFragment extends Fragment implements AdapterView.OnItemSelectedListener, OnClickListener {
    TextView lengthET;
    CompoundButton isItOutsideCB;
    TextView glassAreaET;
    Spinner materials, advSp2, advSp3;
    TextView wallThickness, advEt2, advEt3;
    View plusInsulant;
    View minusInsulant;
    private View advL2, advL3;

    int layerNum = 1;

    private int n;
    private View fragV;
    private LayoutInflater inf;
    NestedScrollView scrollView;
    ViewGroup layout;
    private CustomAdapter<?> adapter;
    private String[] matStr;
    private OnScrollChangeListener scrollListener;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        n = getArguments().getInt(getString(R.string.number), -1);
        inf = getLayoutInflater(state);
        if (!performed) {
            fragV = inf.inflate(R.layout.wall_fragment, null, false);
            isItOutsideCB = (CompoundButton) fragV.findViewById(R.id.isOutside_cb);
            scrollView = (NestedScrollView) fragV.findViewById(R.id.scrollView);
            scrollView.setOnScrollChangeListener(scrollListener);
            layout = (ViewGroup) fragV.findViewById(R.id.wall_layout);
        }
        if (Build.VERSION.SDK_INT < 24) {  //noinspection deprecation
            ((TextView) fragV.findViewById(R.id.unitOfArea))
                    .setText(fromHtml(getString(R.string.areaUnit)));
        } else {
            ((TextView) fragV.findViewById(R.id.unitOfArea))
                    .setText(fromHtml(getString(R.string.areaUnit), Html.FROM_HTML_MODE_COMPACT));
        }

        if (n == 3 || n == 4) {
            isItOutsideCB.setChecked(false);
            for (int i = layout.getChildCount() - 1; i > 2; i--)
                layout.getChildAt(i).setVisibility(GONE);
            layout.getChildAt(1).setVisibility(GONE);
            layout.getChildAt(2).setVisibility(GONE);
        } else
            lengthET = (TextView) fragV.findViewById(R.id.length_et);
        isItOutsideCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton bv, boolean b) {
                if (b) {
                    for (int i = 3; i < layerNum * 2 + 4; i++)
                        layout.getChildAt(i).setVisibility(VISIBLE);
                    for (int i = 10; i < layout.getChildCount(); i++)
                        layout.getChildAt(i).setVisibility(VISIBLE);
                } else
                    for (int i = 3; i < layout.getChildCount(); i++)
                        layout.getChildAt(i).setVisibility(GONE);
            }
        });
        if (performed) return;
        performed = true;

        matStr = Main.res.getStringArray(R.array.wallMaterials);
        adapter = new CustomAdapter<>(mainActivity, android.R.layout.simple_spinner_dropdown_item, matStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        materials = (Spinner) fragV.findViewById(R.id.matSp);
        materials.setAdapter(adapter);
        materials.setSelection(matStr.length - 1, true);
        materials.setOnItemSelectedListener(this);

        plusInsulant = fragV.findViewById(R.id.plusInsulant);
        minusInsulant = fragV.findViewById(R.id.minusInsulant);
        minusInsulant.setEnabled(false);

        glassAreaET = (TextView) fragV.findViewById(R.id.glassArea_et);
        wallThickness = (TextView) fragV.findViewById(R.id.insulThick_et);

        if (n < 4) wallThickness.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    switch (n) {
                        case 1:
                            if (!Main.frag3.performed) Main.frag3.kostyl(2);
                            Main.frag3.wallThickness.setText(s);
                            break;
                        case 2:
                            if (!Main.frag4.performed) Main.frag4.kostyl(3);
                            Main.frag4.wallThickness.setText(s);
                            break;
                        case 3:
                            if (!Main.frag5.performed) Main.frag5.kostyl(4);
                            Main.frag5.wallThickness.setText(s);
                            break;
                    }
                }
            }

            public void beforeTextChanged(CharSequence t, int s, int c, int a) {
            }

            public void onTextChanged(CharSequence t, int s, int b, int c) {
            }
        });
        wallThickness = (TextView) fragV.findViewById(R.id.insulThick_et);

        plusInsulant.setOnClickListener(this);
        minusInsulant.setOnClickListener(this);

        advSp2 = (Spinner) fragV.findViewById(R.id.adv_sp2);
        advEt2 = (TextView) fragV.findViewById(R.id.adv_et2);
        if (n < 4) advEt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                switch (n) {
                    case 1:
                        if (!Main.frag3.performed) Main.frag3.kostyl(2);
                        Main.frag3.advEt2.setText(s);
                        break;
                    case 2:
                        if (!Main.frag4.performed) Main.frag4.kostyl(3);
                        Main.frag4.advEt2.setText(s);
                        break;
                    case 3:
                        if (!Main.frag5.performed) Main.frag5.kostyl(4);
                        Main.frag5.advEt2.setText(s);
                        break;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence t, int s, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence t, int s, int b, int c) {
            }
        });

        advSp3 = (Spinner) fragV.findViewById(R.id.adv_sp3);
        advEt3 = (TextView) fragV.findViewById(R.id.adv_et3);
        if (n < 4) advEt3.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                switch (n) {
                    case 1:
                        if (!Main.frag3.performed) Main.frag3.kostyl(2);
                        Main.frag3.advEt3.setText(s);
                        break;
                    case 2:
                        if (!Main.frag4.performed) Main.frag4.kostyl(3);
                        Main.frag4.advEt3.setText(s);
                        break;
                    case 3:
                        if (!Main.frag5.performed) Main.frag5.kostyl(4);
                        Main.frag5.advEt3.setText(s);
                        break;
                }
            }

            public void beforeTextChanged(CharSequence t, int s, int c, int a) {
            }

            public void onTextChanged(CharSequence t, int s, int b, int c) {
            }
        });

        advSp2.setAdapter(adapter);
        advSp2.setSelection(matStr.length - 1, true);
        advSp3.setAdapter(adapter);
        advSp3.setSelection(matStr.length - 1, true);

        advSp2.setOnItemSelectedListener(this);
        advSp3.setOnItemSelectedListener(this);

        advL2 = fragV.findViewById(R.id.advL2);
        advL3 = fragV.findViewById(R.id.advL3);
    }

    @SuppressLint("InflateParams")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plusInsulant:
                if (layerNum == 1) {
                    if (isItOutsideCB.isChecked()) {
                        advSp2.setVisibility(VISIBLE);
                        advL2.setVisibility(VISIBLE);
                    }
                    minusInsulant.setEnabled(true);
                } else {
                    if (isItOutsideCB.isChecked()) {
                        advSp3.setVisibility(VISIBLE);
                        advL3.setVisibility(VISIBLE);
                    }
                    plusInsulant.setEnabled(false);
                }
                layerNum++;
                switch (n) {
                    case 1:
                        if (Main.frag3.layerNum < layerNum) {
                            if (!Main.frag3.performed) Main.frag3.kostyl(2);
                            do Main.frag3.plusInsulant.performClick();
                            while (Main.frag3.layerNum < layerNum);
                        }
                        break;
                    case 2:
                        if (Main.frag4.layerNum < layerNum) {
                            if (!Main.frag4.performed) Main.frag4.kostyl(3);
                            do Main.frag4.plusInsulant.performClick();
                            while (Main.frag4.layerNum < layerNum);
                        }
                        break;
                    case 3:
                        if (Main.frag5.layerNum < layerNum) {
                            if (!Main.frag5.performed) Main.frag5.kostyl(4);
                            do Main.frag5.plusInsulant.performClick();
                            while (Main.frag5.layerNum < layerNum);
                        }
                        break;
                }
                break;
            case R.id.minusInsulant:
                if (layerNum == 2) {
                    advSp2.setVisibility(GONE);
                    advL2.setVisibility(GONE);
                    minusInsulant.setEnabled(false);
                } else {
                    advSp3.setVisibility(GONE);
                    advL3.setVisibility(GONE);
                    plusInsulant.setEnabled(true);
                }
                layerNum--;
                switch (n) {
                    case 1:
                        if (Main.frag3.layerNum > layerNum) {
                            if (!Main.frag3.performed) Main.frag3.kostyl(2);
                            do Main.frag3.minusInsulant.performClick();
                            while (Main.frag3.layerNum > layerNum);
                        }
                        break;
                    case 2:
                        if (Main.frag4.layerNum > layerNum) {
                            if (!Main.frag4.performed) Main.frag4.kostyl(3);
                            do Main.frag4.minusInsulant.performClick();
                            while (Main.frag4.layerNum > layerNum);
                        }
                        break;
                    case 3:
                        if (Main.frag5.layerNum > layerNum) {
                            if (!Main.frag5.performed) Main.frag5.kostyl(4);
                            do Main.frag5.minusInsulant.performClick();
                            while (Main.frag5.layerNum > layerNum);
                        }
                        break;
                }
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> p, View v, int pos, long l) {
        final int id = p.getId();
        if (id == R.id.matSp) switch (n) {
            case 1:
                if (!Main.frag3.performed) Main.frag3.kostyl(2);
                Main.frag3.materials.setSelection(pos, true);
                break;
            case 2:
                if (!Main.frag4.performed) Main.frag4.kostyl(3);
                Main.frag4.materials.setSelection(pos, true);
                break;
            case 3:
                if (!Main.frag5.performed) Main.frag5.kostyl(4);
                Main.frag5.materials.setSelection(pos, true);
                break;
        }
        else if (advSp2 != null && id == advSp2.getId()) switch (n) {
            case 1:
                if (!Main.frag3.performed) Main.frag3.kostyl(2);
                Main.frag3.advSp2.setSelection(pos, true);
                break;
            case 2:
                if (!Main.frag4.performed) Main.frag4.kostyl(3);
                Main.frag4.advSp2.setSelection(pos, true);
                break;
            case 3:
                if (!Main.frag5.performed) Main.frag5.kostyl(4);
                Main.frag5.advSp2.setSelection(pos, true);
                break;
        }
        else if (advSp3 != null && id == advSp3.getId()) switch (n) {
            case 1:
                if (!Main.frag3.performed) Main.frag3.kostyl(2);
                Main.frag3.advSp3.setSelection(pos, true);
                break;
            case 2:
                if (!Main.frag4.performed) Main.frag4.kostyl(3);
                Main.frag4.advSp3.setSelection(pos, true);
                break;
            case 3:
                if (!Main.frag5.performed) Main.frag5.kostyl(4);
                Main.frag5.advSp3.setSelection(pos, true);
                break;
        }
    }


    boolean performed = false;

    @SuppressLint("InflateParams")
    void kostyl(final int n) {
        performed = true;
        this.n = n;
        matStr = Main.res.getStringArray(R.array.wallMaterials);
        adapter = new CustomAdapter<>(mainActivity, android.R.layout.simple_spinner_dropdown_item, matStr);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        inf = LayoutInflater.from(mainActivity);
        fragV = inf.inflate(R.layout.wall_fragment, null, false);

        plusInsulant = fragV.findViewById(R.id.plusInsulant);
        minusInsulant = fragV.findViewById(R.id.minusInsulant);
        minusInsulant.setEnabled(false);

        scrollView = (NestedScrollView) fragV.findViewById(R.id.scrollView);
        scrollListener = new OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int x, int y, int oldX, int oldY) {
                CollapsingToolbarLayout collapsing = ((Main) mainActivity).collapsingLayout;
                int height = collapsing.getTop() - collapsing.getBottom();
                if (y > Utils.toDp(height) && height > 0)
                    ((Main) mainActivity).appBarLayout.setExpanded(false);
            }
        };
        scrollView.setOnScrollChangeListener(scrollListener);
        layout = (ViewGroup) fragV.findViewById(R.id.wall_layout);

        if (n == 1 || n == 2)
            lengthET = (TextView) fragV.findViewById(R.id.length_et);
        isItOutsideCB = (CheckBox) fragV.findViewById(R.id.isOutside_cb);
        glassAreaET = (TextView) fragV.findViewById(R.id.glassArea_et);
        wallThickness = (TextView) fragV.findViewById(R.id.insulThick_et);
        materials = (Spinner) fragV.findViewById(R.id.matSp);
        materials.setAdapter(adapter);
        materials.setSelection(matStr.length - 1, true);
        materials.setOnItemSelectedListener(this);

        if (n == 3 || n == 4) isItOutsideCB.setChecked(false);

        if (n < 4) wallThickness.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals("")) {
                    switch (n) {
                        case 1:
                            if (!Main.frag3.performed) Main.frag3.kostyl(2);
                            Main.frag3.wallThickness.setText(s);
                            break;
                        case 2:
                            if (!Main.frag4.performed) Main.frag4.kostyl(3);
                            Main.frag4.wallThickness.setText(s);
                            break;
                        case 3:
                            if (!Main.frag5.performed) Main.frag5.kostyl(4);
                            Main.frag5.wallThickness.setText(s);
                            break;
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence t, int s, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence t, int s, int b, int c) {
            }
        });

        plusInsulant.setOnClickListener(this);
        minusInsulant.setOnClickListener(this);

        advSp2 = (Spinner) fragV.findViewById(R.id.adv_sp2);
        advEt2 = (TextView) fragV.findViewById(R.id.adv_et2);
        if (n < 4) advEt2.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                switch (n) {
                    case 1:
                        if (!Main.frag3.performed) Main.frag3.kostyl(2);
                        Main.frag3.advEt2.setText(s);
                        break;
                    case 2:
                        if (!Main.frag4.performed) Main.frag4.kostyl(3);
                        Main.frag4.advEt2.setText(s);
                        break;
                    case 3:
                        if (!Main.frag5.performed) Main.frag5.kostyl(4);
                        Main.frag5.advEt2.setText(s);
                        break;
                }
            }

            @Override
            public void beforeTextChanged(CharSequence t, int s, int c, int a) {
            }

            @Override
            public void onTextChanged(CharSequence t, int s, int b, int c) {
            }
        });

        advSp3 = (Spinner) fragV.findViewById(R.id.adv_sp3);
        advEt3 = (TextView) fragV.findViewById(R.id.adv_et3);
        if (n < 4) advEt3.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                switch (n) {
                    case 1:
                        if (!Main.frag3.performed) Main.frag3.kostyl(2);
                        Main.frag3.advEt3.setText(s);
                        break;
                    case 2:
                        if (!Main.frag4.performed) Main.frag4.kostyl(3);
                        Main.frag4.advEt3.setText(s);
                        break;
                    case 3:
                        if (!Main.frag5.performed) Main.frag5.kostyl(4);
                        Main.frag5.advEt3.setText(s);
                        break;
                }
            }

            public void beforeTextChanged(CharSequence t, int s, int c, int a) {
            }

            public void onTextChanged(CharSequence t, int s, int b, int c) {
            }
        });

        advSp2.setAdapter(adapter);
        advSp3.setAdapter(adapter);

        advSp2.setAdapter(adapter);
        advSp2.setSelection(matStr.length - 1, false);
        advSp3.setAdapter(adapter);
        advSp3.setSelection(matStr.length - 1, true);

        advL2 = fragV.findViewById(R.id.advL2);
        advL3 = fragV.findViewById(R.id.advL3);
    }

    private boolean b = false;

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup vg, Bundle st) {
        if (!b && n == 3 || n == 4) {
            isItOutsideCB.setChecked(false);
            b = true;
        }
        materials.setSelection(materials.getSelectedItemPosition());
        return fragV;
    }


    WallValues getValues(int n, final float height, final float length) {
        final WallValues v = new WallValues();
        v.errors = v.new Errors();

        v.errors.zeros = new boolean[3];
        v.materials = new String[3];
        v.thicknesses = new int[3];
        v.errors.materials = new boolean[3];
        v.errors.thicknesses = new boolean[3];
        if (!performed) {
            if (n == 1 || n == 2) {
                v.err = v.isOutside = v.errors.length =
                        v.errors.thicknesses[0] = v.errors.materials[0] = true;
            } else
                v.isOutside = v.err = false;
            return v;
        }
        v.thicknesses = new int[3];
        v.materials = new String[3];
        v.err = false;

        if (!isItOutsideCB.isChecked()) {
            if (n <= 2)
                if (lengthET.length() == 0)
                    v.errors.length = v.err = true;
                else {
                    float l = Float.parseFloat(lengthET.getText().toString());
                    if (l == 0.0f)
                        v.errors.zeroLength = v.err = true;
                    else v.length = l;
                }
            v.isOutside = false; //если стена внутренняя
            return v;
        }
        v.isOutside = true; //если стена наружняя
        v.glassArea = glassAreaET.length() == 0 ? 0
                : Integer.parseInt(glassAreaET.getText().toString()); //площадь остекления

        if (n <= 2)
            if (lengthET.length() == 0)
                v.errors.length = v.err = true;
            else {
                float l = Float.parseFloat(lengthET.getText().toString());
                if (l == 0)
                    v.errors.zeroLength = v.err = true;
                else {
                    if ((v.length = l) * height < v.glassArea)
                        v.errors.tooBigGlassArea = v.err = true;
                }
            }
        else if (v.glassArea > length * height)
            v.errors.tooBigGlassArea = v.err = true;

        for (int i = 0; i < layerNum; i++) {
            if (materials.getSelectedItemPosition() == matStr.length - 1) //толщина утеплителя
                v.errors.materials[i] = v.err = true;
            else
                v.materials[i] = materials.getSelectedItem().toString(); //тип утеплителя

            if (wallThickness.length() == 0)
                v.errors.thicknesses[i] = v.err = true;
            else {
                int l = Integer.parseInt(wallThickness.getText().toString());
                if (l == 0)
                    v.errors.zeros[i] = v.err = true;
                else
                    v.thicknesses[i] = l;
            }
        }
        return v;
    }

    void setFocusByLink(final int i) {
        switch (i) {
            case 1:
                frag1.setFocus(lengthET);
                break;
            case 2:
                frag1.setFocus(materials);
                break;
            case 3:
                frag1.setFocus(wallThickness);
                break;
            case 4:
                frag1.setFocus(advSp2);
                break;
            case 5:
                frag1.setFocus(advEt2);
                break;
            case 6:
                frag1.setFocus(advSp3);
                break;
            case 7:
                frag1.setFocus(advEt3);
                break;
        }
    }

    private static void setFocus(final View v) {
        mainActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (v == null) Thread.sleep(15);
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                assert v != null;
                if (v instanceof EditText/* && !isHardwareKeyBoardAvailable()*/) {
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) mainActivity.getSystemService(INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInputFromWindow(
                            v.getApplicationWindowToken(), SHOW_IMPLICIT, 0);
                }
                v.requestFocus();
//                ((InputMethodManager) mainActivity.getSystemService(INPUT_METHOD_SERVICE))
//                        .toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
    }

    public void onNothingSelected(AdapterView<?> p) {
    }
}