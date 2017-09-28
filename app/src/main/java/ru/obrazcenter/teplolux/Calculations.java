//Если этот код работает, его написал Соколов Филипп, а если нет, то не знаю, кто его писал.
package ru.obrazcenter.teplolux;

import android.widget.Toast;

import org.jetbrains.annotations.Contract;

import ru.obrazcenter.teplolux.ProjectLogics.Room;


import static android.widget.Toast.LENGTH_SHORT;
import static java.lang.Math.min;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.GRUNT;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.INTERFLOOR;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.LOGS;
import static ru.obrazcenter.teplolux.Fragment1.FloorValues.WITH_UNDERGROUND;
import static ru.obrazcenter.teplolux.Main.frag1;
import static ru.obrazcenter.teplolux.Main.frag2;
import static ru.obrazcenter.teplolux.Main.frag3;
import static ru.obrazcenter.teplolux.Main.frag4;
import static ru.obrazcenter.teplolux.Main.frag5;
import static ru.obrazcenter.teplolux.Main.mainActivity;
import static ru.obrazcenter.teplolux.StartActivity.activity1;
import static ru.obrazcenter.teplolux.StartActivity.gson;

enum Calculations {
    ;

    static Answer calculate(int temper, String roomName, String projectName) {
        Answer ans = new Answer();
        ans.wallErrors = new WallValues.Errors[4];

        int[] lAmounts = {frag2.layerNum, frag3.layerNum, frag4.layerNum, frag5.layerNum};

        int T_DIFFERENCE;
        if (frag1.getInsideTempTxt().length() == 0) {
            ans.insTempErr = true;
            T_DIFFERENCE = 0;
        } else {
            T_DIFFERENCE = Integer.parseInt(frag1.getInsideTempTxt()) + temper;
            ans.insTempErr = false;
        }

        float HEIGHT;
        if (frag1.getCHeight().length() == 0) {
            ans.heightErr = true;
            HEIGHT = Float.POSITIVE_INFINITY;
        } else {
            HEIGHT = Float.parseFloat(frag1.getCHeight());
            ans.heightErr = false;
        }

        Fragment1.Values cValues = frag1.getCeilingValues();
        Fragment1.FloorValues fValues = frag1.getFloorValues(/*HEIGHT*/);
        //if; (fValues.fType == HEATED_BASEMENT) HEIGHT -= fValues.depth;

        WallValues[] v = new WallValues[4];
        v[0] = frag2.getValues(1, HEIGHT, -1);
        v[1] = frag3.getValues(2, HEIGHT, -1);
        v[2] = frag4.getValues(3, HEIGHT,
                v[0].errors.length ? -1 : v[0].length);
        v[3] = frag5.getValues(4, HEIGHT,
                v[1].errors.length ? -1 : v[1].length);
        ans.wErrs = new boolean[4];

        for (int i = 0; i < 4; i++)
            if (v[i].err) {
                ans.wErrs[i] = true;
                ans.wallErrors[i] = v[i].errors;
            }
        if (cValues.err) {
            ans.cErrors = cValues.errors;
            ans.cErr = true;
        }
        if (fValues.err) {
            ans.fErrors = fValues.errors;
            ans.fErr = true;
        }
        if (ans.insTempErr || ans.heightErr || v[0].err || v[1].err || v[2].err || v[3].err) {
            ans.err = true;
            return ans;
        } else ans.err = false;
        float[] s = new float[4]; //Площади стен, m^2
        float[][] k = {new float[4], new float[4], new float[4]}; //Коэффиценты тепло-ти, Вт/м*°С
        float glassR, wQ = 0, //Теплопотери 4-х стен
                cQ, fQ, winQ = 0;
        int winType = frag1.getWindowType();
        boolean isWinAlum = frag1.alCB.isChecked();
        switch (winType) {
            case 0: //Обычное (двойное) остекление
                glassR = 0.44f;
                break;
            case 1: //1к-й ст-т
                glassR = isWinAlum ? 0.34f : 0.38f;
                break;
            case 2: //1к-й ст-т с i
                glassR = isWinAlum ? 0.47f : 0.56f;
                break;
            case 3: //2-й ст-т
                glassR = isWinAlum ? 0.45f : 0.54f;
                break;
            case 4: //2-й ст-т с i
                glassR = isWinAlum ? 0.52f : 0.68f;
                break;
            default:
                throw new RuntimeException("Неизвестный тип окон!");
        }
        for (int i = 0; i < 4; i++)
            if (v[i].isOutside) {
                winQ += v[i].glassArea * T_DIFFERENCE / glassR;

                s[i] = v[i].length * HEIGHT - v[i].glassArea;
                float denominator = 0;
                for (int n = 0; n < lAmounts[i]; n++) {
                    switch (v[i].materials[n]) {
                        case "Силикатный кирпич":
                            k[n][i] = 0.87f;
                            break;
                        case "Теплая керамика":
                        case "Пустотная керамика":
                            k[n][i] = 0.186f;
                            break;
                        case "МинВата":
                            k[n][i] = 0.045f;
                            break;
                        case "Пенополистирол":
                            k[n][i] = 0.041f;
                            break;
                        case "Дерево (сосна)":
                        case "Дерево":
                            k[n][i] = 0.12f;
                            break;
                        case "Газосиликат":
                            k[n][i] = 0.47f;
                            break;
                        case "Пустотелый кирпич":
                            k[n][i] = 0.39f;
                            break;
                        case "Пеноблок":
                        case "Пеноблок Д 600":
                            k[n][i] = 0.14f;
                            break;
                        case "Пеноблок Д 800":
                            k[n][i] = 0.22f;
                            break;
                        default:
                            throw new RuntimeException("Неизвестный материал " + n + "-го слоя" + i
                                    + "-й стены");
                    }
                    denominator += v[i].thicknesses[n] / k[n][i];
                    wQ += s[i] * T_DIFFERENCE / denominator;
                }
            }
        cQ = calculateCeil(cValues, T_DIFFERENCE, v[0].length * v[1].length) * 100;
        fQ = calculateFloor(fValues, v, T_DIFFERENCE) * 100;

        wQ *= 100;
        winQ *= 100;
        ans.power = wQ + fQ + cQ + winQ;
        ans.wQ = wQ;
        ans.cQ = cQ;
        ans.fQ = fQ;
        ans.winQ = winQ;

        int totalS = Math.round(v[0].length * v[1].length);
        int power = (int) Math.ceil(ans.power);


        long time = gson.fromJson(activity1.getPreferences(projectName)
                .getString(roomName, "err"), ProjectLogics.Place.class).date;

        Room room = new Room(roomName, time, totalS, power,
                HEIGHT, T_DIFFERENCE - temper, v, cValues, fValues, winType, isWinAlum);
        ProjectLogics.saveRoom(room, roomName, projectName);

        Toast.makeText(mainActivity, "Помещение сохраненно!", LENGTH_SHORT).show();
        return ans;
    }


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    private static float calculateFloor(Fragment1.FloorValues values,
                                        WallValues[] v, int T_DIFFERENCE) {
        if (values.fType == INTERFLOOR) return 0;
        float fQ = 0, denominator = 0;
        // Если у пола есть утепление
        if ((values.fType != GRUNT && values.fType != LOGS) || values.isInsul) {
            float[] fK = new float[frag1.fLNumber];
            for (int n = 0; n < frag1.fLNumber; n++) {
                switch (values.materials[n]) {
                    case "МинВата":
                        fK[n] = 0.045f;
                        break;
                    case "Пенополистирол":
                        fK[n] = 0.041f;
                        break;
                    case "Дерево":
                        fK[n] = 0.12f;
                        break;
                    default:
                        throw new RuntimeException("Неизвестный материал " +
                                (n + 1) + "-го слоя пола!");
                }
                denominator += values.thicknesses[n] / fK[n];
            }
        }
        if (WITH_UNDERGROUND == values.fType) {
            denominator += 0.25f;
            fQ = v[0].length * v[1].length * T_DIFFERENCE / denominator;

        }/* else if (HEATED_BASEMENT == values.fType) {
            if (!v[0].isOutside && !v[1].isOutside && !v[2].isOutside && !v[3].isOutside)
                return v[0].length * v[1].length * T_DIFFERENCE / (14.2f + denominator);

            float[] s = new float[4];
            //удвоенное кол-во наружных стен по одной оси
            int n0 = v[0].isOutside ^ v[2].isOutside ? 2 : v[0].isOutside ? 4 : 0;
            int n1 = v[1].isOutside ^ v[3].isOutside ? 2 : v[1].isOutside ? 4 : 0;

            float d0 = v[0].length, d1 = v[1].length;

            if (values.depth >= 2) {
                d0 = limDiff(d0, 2);
                s[0] = n0 * d0 + n1 * d1;
            }
            float min0 = d0 < n0 ? d0 : n0;
            float min1 = d1 < n1 ? d1 : n1;
            fQ = 0;
        }*/ else {
            if (LOGS == values.fType) denominator += 1.18f;

            if (!v[0].isOutside && !v[1].isOutside && !v[2].isOutside && !v[3].isOutside)
                return v[0].length * v[1].length * T_DIFFERENCE / (14.2f + denominator);

            float[] s = new float[4];
            float d0 = v[0].length, d1 = v[1].length;

            // удвоенное кол-во наружных стен по одной оси
            int n0 = (v[0].isOutside ^ v[2].isOutside) ? 2 : v[0].isOutside ? 4 : 0;
            int n1 = (v[1].isOutside ^ v[3].isOutside) ? 2 : v[1].isOutside ? 4 : 0;

            s[0] = d1 * min(d0, n0) + d0 * min(d1, n1);

            fQ += s[0] * T_DIFFERENCE / (2.1f + denominator);

            d0 = limDiff(d0, n0);
            d1 = limDiff(d1, n1);

            for (int i = 1; i < s.length; i++) {
                d0 = limDiff(d0, n0);
                d1 = limDiff(d1, n1);
                s[i] = d0 * d1;
            }

            fQ += s[0]/*     */ * T_DIFFERENCE / (2.1f + denominator);

            fQ += (s[1] - s[2]) * T_DIFFERENCE / (4.3f + denominator);

            fQ += (s[2] - s[3]) * T_DIFFERENCE / (8.6f + denominator);

            fQ += s[3]/*     */ * T_DIFFERENCE / (14.2f + denominator);
        }
        return fQ;
    }


    @Contract(pure = true)
    private static float calculateCeil(Fragment1.Values v, float T_DIFFERENCE, float s) {
        float cQ;
        if (v.isOutside) {
            float[] cK = new float[3];
            float denominator = 0;
            for (int n = 0; n < frag1.cLNumber; n++) {
                switch (v.materials[n]) {
                    case "МинВата":
                        cK[n] = 0.045f;
                        break;
                    case "Пенополистирол":
                        cK[n] = 0.041f;
                        break;
                    case "Дерево":
                        cK[n] = 0.12f;
                        break;
                    default:
                        throw new RuntimeException("Неизвестный материал " + (n + 1) + "-го слоя потолка!");
                }
                denominator += v.thicknesses[n] / cK[n];
            }
            denominator += 8.7f + 12;
            cQ = s * T_DIFFERENCE / denominator;
        } else cQ = 0;
        return cQ;
    }

    /**
     * The limited difference.
     *
     * @return the difference of {@param t1} and {@param t2}
     * or 0 if <tt>t2 &gt; t1<tt>.
     */
    @Contract(pure = true)
    private static float limDiff(float t1, int t2) {
        return t1 > t2 ? t1 - t2 : 0;
    }
}