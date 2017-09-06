//Если этот код работает, его написал Соколов Филипп, а если нет, то не знаю, кто его писал.
package ru.obrazcenter.teplolux;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.NestedScrollView.OnScrollChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;


import static android.content.res.Configuration.KEYBOARD_NOKEYS;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.GRUNT;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.INTERFLOOR;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.LOGS;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.WITH_UNDERGROUND;
import static ru.obrazcenter.teplolux.Main.mainActivity;

public class Fragment1 extends Fragment
        implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {
    Spinner winTypesSp;
    CompoundButton alCB;

    Spinner ceilSp,
            advCSp2, advCSp3;
    TextView cThicknessET,
            advCEt2, advCEt3;
    CompoundButton isCeilOuter;

    Spinner floorSp,
            advFSp2, advFSp3;
    TextView fThicknessET,
            advFEt2, advFEt3;
    CompoundButton[] rb = new CompoundButton[4];

    int cLNumber = 1;
    int fLNumber = 1;

    View fragV;
    private NestedScrollView scrollV;
    LinearLayout layout;
    View plusC, minusC,
            plusF, minusF;
    View advCL2, advCL3, advFL2, advFL3;
    CheckBox isInsulCB;
    //TextView depthET;

    private CharSequence[] cList, fList;
    private boolean performed = false;

    @SuppressLint("InflateParams")
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        cList = Main.res.getStringArray(R.array.ceilMaterials);
        fList = Main.res.getStringArray(R.array.floorMaterials);
        CustomAdapter<CharSequence> cAdapter = new CustomAdapter<>(mainActivity, android.R.layout.simple_spinner_dropdown_item, cList);
        CustomAdapter<CharSequence> fAdapter = new CustomAdapter<>(mainActivity, android.R.layout.simple_spinner_dropdown_item, fList);

        ArrayAdapter<CharSequence> winAdapter = ArrayAdapter
                .createFromResource(mainActivity, R.array.windowTypes, android.R.layout.simple_spinner_dropdown_item);
        winAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        LayoutInflater inf = getLayoutInflater(savedInstanceState);
        fragV = inf.inflate(R.layout.fragment_1, null, false);
        layout = (LinearLayout) fragV.findViewById(R.id.layout_1);
        scrollV = (NestedScrollView) fragV.findViewById(R.id.scrollView);
        scrollV.setOnScrollChangeListener(new OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int x, int y, int oldX, int oldY) {
                CollapsingToolbarLayout collapsingLayout = ((Main) mainActivity).collapsingLayout;
                if (y > Utils.toDp(28) && collapsingLayout.getTop() < collapsingLayout.getBottom()) {
                    ((Main) mainActivity).appBarLayout.setExpanded(false);
                }
            }
        });

        cThicknessET = (TextView) fragV.findViewById(R.id.ceilThickness_et);
        fThicknessET = (TextView) fragV.findViewById(R.id.floorThickness_et);
        winTypesSp = (Spinner) fragV.findViewById(R.id.windowTypesSp);
        alCB = (CompoundButton) fragV.findViewById(R.id.al_cb);

        RadioGroup rGroup = (RadioGroup) fragV.findViewById(R.id.fType_rg);
        rb[0] = (CompoundButton) rGroup.getChildAt(0); // Пол над холодным подпольем
        rb[1] = (CompoundButton) rGroup.getChildAt(1); // Пол по грунту
        rb[2] = (CompoundButton) rGroup.getChildAt(2); // Пол по лагам
        rb[3] = (CompoundButton) rGroup.getChildAt(3); // Междуэтажное перекрытие
        rGroup.setOnCheckedChangeListener(this);

        isCeilOuter = (CompoundButton) fragV.findViewById(R.id.outerC_cb);

        plusC = fragV.findViewById(R.id.plusCeiling);
        minusC = fragV.findViewById(R.id.minusCeiling);
        plusF = fragV.findViewById(R.id.plusFloor);
        minusF = fragV.findViewById(R.id.minusFloor);
        minusC.setEnabled(false);
        minusF.setEnabled(false);

        isInsulCB = (CheckBox) fragV.findViewById(R.id.isInsul_cb);
//        depthET = (TextView) fragV.findViewById(R.id.flDepth_et);
        ceilSp = (Spinner) fragV.findViewById(R.id.ceilingSp);
        floorSp = (Spinner) fragV.findViewById(R.id.floorSp);
        ceilSp.setAdapter(cAdapter);
        floorSp.setAdapter(fAdapter);
        ceilSp.setSelection(cList.length - 1, true);
        floorSp.setSelection(fList.length - 1, true);
        winTypesSp.setAdapter(winAdapter);
        winTypesSp.setSelection(2, true);
        isCeilOuter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton btnView, final boolean b) {
//                View focused = ((Activity) mainActivity).getCurrentFocus();
//                if (focused != null && cThicknessET.getId() == focused.getId()) {
//                    ((Main) mainActivity).closeKeyBrd().closeKeyBrd2();
//                }
                if (b) {
                    for (int i = 6; i < 6 + 2 * cLNumber; i++)
                        layout.getChildAt(i).setVisibility(VISIBLE);
                    layout.getChildAt(12).setVisibility(VISIBLE);
                } else {
                    for (int i = 6; i < 6 + 2 * cLNumber; i++)
                        layout.getChildAt(i).setVisibility(GONE);
                    layout.getChildAt(12).setVisibility(GONE);
                }
            }
        });
        isInsulCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton btnView, boolean b) {
                if (b) {
                    for (int i = 17; i < 17 + 2 * fLNumber; i++)
                        layout.getChildAt(i).setVisibility(VISIBLE);
                    layout.getChildAt(23).setVisibility(VISIBLE);
                } else {
                    for (int i = 17; i < 17 + 2 * fLNumber; i++)
                        layout.getChildAt(i).setVisibility(GONE);
                    layout.getChildAt(23).setVisibility(GONE);
                }
            }
        });
        plusC.setOnClickListener(this);
        minusC.setOnClickListener(this);
        plusF.setOnClickListener(this);
        minusF.setOnClickListener(this);

        winTypesSp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
                if (pos == 0) alCB.setVisibility(GONE);
                else {
                    alCB.setVisibility(VISIBLE);
                    scrollV.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            scrollV.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    }, 256);
                    scrollV.setSmoothScrollingEnabled(true);
                }
            }

            public void onNothingSelected(AdapterView<?> p) {
            }
        });

        advCSp2 = (Spinner) fragV.findViewById(R.id.advC_sp2);
        advFSp2 = (Spinner) fragV.findViewById(R.id.advF_sp2);
        advCSp3 = (Spinner) fragV.findViewById(R.id.advC_sp3);
        advFSp3 = (Spinner) fragV.findViewById(R.id.advF_sp3);

        advCL2 = fragV.findViewById(R.id.advC_L2);
        advFL2 = fragV.findViewById(R.id.advF_L2);
        advCL3 = fragV.findViewById(R.id.advC_L3);
        advFL3 = fragV.findViewById(R.id.advF_L3);

        advCEt2 = (TextView) advCL2.findViewById(R.id.advC_et2);
        advFEt2 = (TextView) advFL2.findViewById(R.id.advF_et2);
        advCEt3 = (TextView) advCL3.findViewById(R.id.advC_et3);
        advFEt3 = (TextView) advFL3.findViewById(R.id.advF_et3);

        advCSp2.setAdapter(cAdapter);
        advFSp2.setAdapter(fAdapter);
        advCSp3.setAdapter(cAdapter);
        advFSp3.setAdapter(fAdapter);

        advCSp2.setSelection(cList.length - 1, true);
        advFSp2.setSelection(fList.length - 1, true);
        advCSp3.setSelection(cList.length - 1, true);
        advFSp3.setSelection(fList.length - 1, true);
    }

    @Override
    public void onAttach(Context c) {
        super.onAttach(c);
        performed = true;
    }

    @Override
    public View onCreateView(LayoutInflater inf, ViewGroup g, Bundle savedS) {
        return fragV;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            // Пол над холожным подпольем
            case R.id.rb0:
                isInsulCB.setVisibility(GONE);
                for (int i = 17; i < 17 + 2 * fLNumber; i++)
                    layout.getChildAt(i).setVisibility(VISIBLE);
                layout.getChildAt(23).setVisibility(VISIBLE);
                break;
//            case R.id.rb3:
//                Toast.makeText(mainActivity, "ВНИМАНИЕ: Пока моё приложение\n" +
//                        "не умеет считать теплотери подвала", LENGTH_SHORT).show();
//                if (isInsulCB.isChecked()) {
//                    isInsulCB.setVisibility(VISIBLE);
//                    for (int i = 17; i < 17 + 2 * fLNumber; i++)
//                        layout.getChildAt(i).setVisibility(VISIBLE);
//                    layout.getChildAt(23).setVisibility(VISIBLE);
//                } else {
//                    for (int i = 17; i < 17 + 2 * fLNumber; i++)
//                        layout.getChildAt(i).setVisibility(GONE);
//                    layout.getChildAt(23).setVisibility(GONE);
//                    isInsulCB.setVisibility(VISIBLE);
//                }
//                layout.getChildAt(24).setVisibility(VISIBLE);
//                break;
            // Пол по грунту
            case R.id.rb1:
                // Пол по лагам
            case R.id.rb2:
                isInsulCB.setVisibility(VISIBLE);
                if (isInsulCB.isChecked()) {
                    for (int i = 17; i < 17 + 2 * fLNumber; i++)
                        layout.getChildAt(i).setVisibility(VISIBLE);
                    layout.getChildAt(23).setVisibility(VISIBLE);
                } else {
                    // Скрытие слоев утепления стены
                    for (int i = 17; i < 17 + 2 * fLNumber; i++)
                        layout.getChildAt(i).setVisibility(GONE);
                    // Скрытие кнопок +/-
                    layout.getChildAt(23).setVisibility(GONE);
                }
//                layout.getChildAt(24).setVisibility(GONE);
                break;
            // Междуэтажное перекрытие
            case R.id.rb4:
                // Скрытие слоев утепления стены
                for (int i = 16; i < 17 + 2 * fLNumber; i++)
                    layout.getChildAt(i).setVisibility(GONE);
                // Скрытие кнопок +/-
                layout.getChildAt(23).setVisibility(GONE);
//                layout.getChildAt(24).setVisibility(GONE);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.plusCeiling:
                if (cLNumber == 1) {
                    advCL2.setVisibility(VISIBLE);
                    advCSp2.setVisibility(VISIBLE);
                    minusC.setEnabled(true);
                } else {
                    advCL3.setVisibility(VISIBLE);
                    advCSp3.setVisibility(VISIBLE);
                    plusC.setEnabled(false);
                }
                cLNumber++;
                break;
            case R.id.minusCeiling:
                if (cLNumber == 2) {
                    advCL2.setVisibility(GONE);
                    advCSp2.setVisibility(GONE);
                    minusC.setEnabled(false);
                } else {
                    advCL3.setVisibility(GONE);
                    advCSp3.setVisibility(GONE);
                    plusC.setEnabled(true);
                }
                cLNumber--;
                break;

            case R.id.plusFloor:
                if (fLNumber == 1) {
                    advFL2.setVisibility(VISIBLE);
                    advFSp2.setVisibility(VISIBLE);
                    minusF.setEnabled(true);
                } else {
                    advFL3.setVisibility(VISIBLE);
                    advFSp3.setVisibility(VISIBLE);
                    plusF.setEnabled(false);
                }
                fLNumber++;
                break;
            case R.id.minusFloor:
                if (fLNumber == 2) {
                    advFL2.setVisibility(GONE);
                    advFSp2.setVisibility(GONE);
                    minusF.setEnabled(false);
                } else {
                    advFL3.setVisibility(GONE);
                    advFSp3.setVisibility(GONE);
                    plusF.setEnabled(true);
                }
                fLNumber--;
                break;
        }
    }


    //Высота помещения
    String getCHeight() {
        return ((TextView) fragV.findViewById(R.id.cHeight)).getText().toString();
    }

    //внутренняя температура
    String getInsideTempTxt() {
        if (fragV == null)
            throw new NullPointerException("NPE при попытке получить данные из 1й вкладки");
        return ((TextView) fragV.findViewById(R.id.insideTemp_et)).getText().toString();
    }

    //тип окон
    int getWindowType() {
        return winTypesSp.getSelectedItemPosition();
    }


    Values getCeilingValues() {
        final Values v = new Values();

        if (!isCeilOuter.isChecked()) {
            v.isOutside = v.err = false;
            return v;
        } else v.isOutside = true;
        v.errors = v.new Errors();
        v.errors.zeros = new boolean[3];

        v.errors.materials = new boolean[3];
        v.errors.thicknesses = new boolean[3];
        v.materials = new String[3];
        v.thicknesses = new int[3];

        if (ceilSp.getSelectedItemPosition() == cList.length - 1)
            v.errors.materials[0] = v.err = true;
        else {
            v.materials[0] = ceilSp.getSelectedItem().toString();
            v.errors.materials[0] = false;
        }
        if (cThicknessET.length() == 0)
            v.errors.thicknesses[0] = v.err = true;
        else {
            int l = Integer.parseInt(cThicknessET.getText().toString());
            if (l == 0)
                v.errors.zeros[0] = v.err = true;
            else {
                v.thicknesses[0] = l;
                v.errors.thicknesses[0] = false;
            }
        }

        if (cLNumber > 1) {
            if (advCSp2.getSelectedItemPosition() == cList.length - 1)
                v.errors.materials[1] = v.err = true;
            else {
                v.materials[1] = advCSp2.getSelectedItem().toString();
                v.errors.materials[1] = false;
            }
            if (advCEt2.length() == 0)
                v.errors.thicknesses[1] = v.err = true;
            else {
                int l = Integer.parseInt(advCEt2.getText().toString());
                if (l == 0)
                    v.errors.zeros[1] = v.err = true;
                else {
                    v.thicknesses[1] = l;
                    v.errors.thicknesses[1] = false;
                }
            }

            if (cLNumber == 3) {
                if (advCSp3.getSelectedItemPosition() == cList.length - 1)
                    v.errors.materials[2] = v.err = true;
                else {
                    v.materials[2] = advCSp3.getSelectedItem().toString();
                    v.errors.materials[2] = false;
                }
                if (advCEt3.length() == 0)
                    v.errors.thicknesses[2] = v.err = true;
                else {
                    int l = Integer.parseInt(advCEt3.getText().toString());
                    if (l == 0)
                        v.errors.zeros[2] = v.err = true;
                    else {
                        v.thicknesses[2] = l;
                        v.errors.thicknesses[2] = false;
                    }
                }
            }
        }
        return v;
    }

    FloorValues getFloorValues(/*float height*/) {
        final FloorValues v = new FloorValues();
        if (rb[3].isChecked()) {
            v.fType = INTERFLOOR;
            v.err = false;
            return v;
        } else if (rb[0].isChecked())
            v.fType = WITH_UNDERGROUND;
        else {
            if (rb[1].isChecked())
                v.fType = GRUNT;
            else if (rb[2].isChecked())
                v.fType = LOGS;
            if (isInsulCB.isChecked())
                v.isInsul = true;
            else {
                v.isInsul = v.err = false;
                return v;
            }
//             else if (rb[3].isChecked()) {
//                 v.fType = HEATED_BASEMENT;
//                 if (depthET.length() == 0) v.depth = 0;
//                 else {
//                     float qq = Float.parseFloat(depthET.getText().toString());
//                     v.depth = qq > height ? height : qq;
//                 }
//             }
        }
        v.errors = v.new Errors();
        v.errors.zeros = new boolean[3];

        v.errors.materials = new boolean[3];
        v.errors.thicknesses = new boolean[3];
        v.materials = new String[3];
        v.thicknesses = new int[3];

        if (floorSp.getSelectedItemPosition() == fList.length - 1)
            v.errors.materials[0] = v.err = true;
        else {
            v.materials[0] = floorSp.getSelectedItem().toString();
            v.errors.materials[0] = false;
        }
        if (fThicknessET.length() == 0)
            v.errors.thicknesses[0] = v.err = true;
        else {
            int l = Integer.parseInt(fThicknessET.getText().toString());
            if (l == 0)
                v.errors.zeros[0] = v.err = true;
            else {
                v.thicknesses[0] = l;
                v.errors.thicknesses[0] = false;
            }
        }

        if (fLNumber > 1) {
            if (advFSp2.getSelectedItemPosition() == fList.length - 1)
                v.errors.materials[1] = v.err = true;
            else {
                v.materials[1] = advFSp2.getSelectedItem().toString();
                v.errors.materials[1] = false;
            }
            if (advFEt2.length() == 0)
                v.errors.thicknesses[1] = v.err = true;
            else {
                if (advFEt2.length() == 0)
                    v.errors.thicknesses[1] = v.err = true;
                else {
                    int l = Integer.parseInt(advFEt2.getText().toString());
                    if (l == 0)
                        v.errors.zeros[1] = v.err = true;
                    else {
                        v.thicknesses[1] = l;
                        v.errors.thicknesses[1] = false;
                    }
                }
            }

            if (fLNumber == 3) {
                if (advFSp3.getSelectedItemPosition() == fList.length - 1)
                    v.errors.materials[2] = v.err = true;
                else {
                    v.materials[2] = advFSp3.getSelectedItem().toString();
                    v.errors.materials[2] = false;
                }
                if (advFEt3.length() == 0)
                    v.errors.thicknesses[2] = v.err = true;
                else {
                    int l = Integer.parseInt(advFEt3.getText().toString());
                    if (l == 0)
                        v.errors.zeros[2] = v.err = true;
                    else {
                        v.thicknesses[2] = l;
                        v.errors.thicknesses[2] = false;
                    }
                }
            }
        }
        return v;
    }

    void setFocusByLink(int i) {
        switch (i) {
            case 1:
                setFocus(fragV.findViewById(R.id.insideTemp_et));
                break;
            case 2:
                setFocus(fragV.findViewById(R.id.cHeight));
                break;
            case 3:
                setFocus(ceilSp);
                break;
            case 4:
                setFocus(cThicknessET);
                break;
            case 5:
                setFocus(advCSp2);
                break;
            case 6:
                setFocus(advCEt2);
                break;
            case 7:
                setFocus(advCSp3);
                break;
            case 8:
                setFocus(advCEt3);
                break;
            case 9:
                setFocus(floorSp);
                break;
            case 10:
                setFocus(fThicknessET);
                break;
            case 11:
                setFocus(advFSp2);
                break;
            case 12:
                setFocus(advFEt2);
                break;
            case 13:
                setFocus(advFSp3);
                break;
            case 14:
                setFocus(advFEt3);
                break;
        }
    }

    void setFocus(final View v) {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (v instanceof Spinner) {
                    v.performClick();
                } else {
                    v.setFocusableInTouchMode(true);
                    v.requestFocus();
                }
//                    InputMethodManager inputMethodManager =
//                            (InputMethodManager) mainActivity.getSystemService(INPUT_METHOD_SERVICE);
//                    inputMethodManager.toggleSoftInputFromWindow(
//                            v.getApplicationWindowToken(),InputMethodManager.SHOW_IMPLICIT, 0);
//                    ((InputMethodManager) mainActivity.getSystemService(INPUT_METHOD_SERVICE))
//                            .showSoftInput(v, SHOW_IMPLICIT);
//                ((InputMethodManager) mainActivity.getSystemService(INPUT_METHOD_SERVICE))
//                        .toggleSoftInput(SHOW_FORCED, 0);
//                if (v instanceof EditText && !isHardwareKeyBoardAvailable())
//                    new Handler().postDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            InputMethodManager imm = (InputMethodManager)
//                                    mainActivity.getSystemService(Context.INPUT_METHOD_SERVICE);
//                            imm.showSoftInput(v, InputMethodManager.SHOW_IMPLICIT);
//                        }
//                    }, 200);

//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        //noinspection ConstantConditions
//                        ((Activity) mainActivity).getCurrentFocus().clearFocus();
//                        v.requestFocus();
//                    }
//                }, 200);
            }
        }, 400);
    }

    class Values {
        String[] materials;
        int[] thicknesses;
        int lNumb; // only for saving

        class Errors {
            boolean[] zeros, materials, thicknesses;
        }

        Errors errors;
        boolean err;
        boolean isOutside = false;
    }

    class FloorValues extends Values {
        static final int WITH_UNDERGROUND = 0;
        static final int GRUNT = 1;
        static final int LOGS = 2;
        //static final int HEATED_BASEMENT = 3;
        static final int INTERFLOOR = -1;
        int fType;
        boolean isInsul;
//        float depth;
    }
}