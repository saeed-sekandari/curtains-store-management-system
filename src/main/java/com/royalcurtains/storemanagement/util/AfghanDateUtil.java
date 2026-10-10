package com.royalcurtains.storemanagement.util;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AfghanDateUtil {

    private static final String[] MONTH_NAMES = {
            "Hamal",
            "Sawr",
            "Jawza",
            "Saratan",
            "Asad",
            "Sunbula",
            "Mizan",
            "Aqrab",
            "Qaws",
            "Jadi",
            "Dalwa",
            "Hoot"
    };

    private AfghanDateUtil() {
        // Utility class does not need to be created.
    }

    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "—";
        }

        int[] afghanDate = toAfghanDate(dateTime.toLocalDate());

        return String.format(
                "%02d %s %04d %02d:%02d",
                afghanDate[2],
                MONTH_NAMES[afghanDate[1] - 1],
                afghanDate[0],
                dateTime.getHour(),
                dateTime.getMinute()
        );
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return "—";
        }

        int[] afghanDate = toAfghanDate(date);

        return String.format(
                "%02d %s %04d",
                afghanDate[2],
                MONTH_NAMES[afghanDate[1] - 1],
                afghanDate[0]
        );
    }

    public static int getYear(LocalDate date) {
        return toAfghanDate(date)[0];
    }

    public static int getMonth(LocalDate date) {
        return toAfghanDate(date)[1];
    }

    public static int getDay(LocalDate date) {
        return toAfghanDate(date)[2];
    }

    public static String getMonthName(int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException(
                    "Afghan month must be between 1 and 12"
            );
        }

        return MONTH_NAMES[month - 1];
    }

    public static String[] getMonthNames() {
        return MONTH_NAMES.clone();
    }

    public static LocalDate toGregorianDate(
            int afghanYear,
            int afghanMonth,
            int afghanDay) {

        if (afghanMonth < 1 || afghanMonth > 12) {
            throw new IllegalArgumentException(
                    "Afghan month must be between 1 and 12"
            );
        }

        int maximumDay = afghanMonth <= 6 ? 31 : 30;

        if (afghanMonth == 12) {
            maximumDay = 30;
        }

        if (afghanDay < 1 || afghanDay > maximumDay) {
            throw new IllegalArgumentException(
                    "Invalid Afghan day"
            );
        }

        int jy = afghanYear;
        int jm = afghanMonth;
        int jd = afghanDay;

        int gy;

        if (jy > 979) {
            gy = 1600;
            jy -= 979;
        } else {
            gy = 621;
        }

        int days =
                365 * jy
                        + (jy / 33) * 8
                        + ((jy % 33 + 3) / 4)
                        + 78
                        + jd
                        + (jm < 7
                        ? (jm - 1) * 31
                        : ((jm - 7) * 30) + 186);

        gy += 400 * (days / 146097);
        days %= 146097;

        if (days > 36524) {
            gy += 100 * (--days / 36524);
            days %= 36524;

            if (days >= 365) {
                days++;
            }
        }

        gy += 4 * (days / 1461);
        days %= 1461;

        if (days > 365) {
            gy += (days - 1) / 365;
            days = (days - 1) % 365;
        }

        int gd = days + 1;
        int[] daysInGregorianMonth = {
                31, 28, 31, 30, 31, 30,
                31, 31, 30, 31, 30, 31
        };

        int gm = 0;

        while (gm < 12
                && gd > daysInGregorianMonth[gm]) {

            boolean leapYear =
                    gy % 400 == 0
                            || (gy % 4 == 0 && gy % 100 != 0);

            int monthLength = daysInGregorianMonth[gm];

            if (gm == 1 && leapYear) {
                monthLength = 29;
            }

            if (gd <= monthLength) {
                break;
            }

            gd -= monthLength;
            gm++;
        }

        return LocalDate.of(gy, gm + 1, gd);
    }

    private static int[] toAfghanDate(LocalDate date) {
        int gy = date.getYear();
        int gm = date.getMonthValue();
        int gd = date.getDayOfMonth();

        int[] daysInGregorianMonth = {
                31, 28, 31, 30, 31, 30,
                31, 31, 30, 31, 30, 31
        };

        int jy;

        if (gy > 1600) {
            jy = 979;
            gy -= 1600;
        } else {
            jy = 0;
            gy -= 621;
        }

        int gy2 = gm > 2 ? gy + 1 : gy;

        int days =
                365 * gy
                        + (gy2 + 3) / 4
                        - (gy2 + 99) / 100
                        + (gy2 + 399) / 400
                        - 80
                        + gd;

        for (int i = 0; i < gm - 1; i++) {
            days += daysInGregorianMonth[i];
        }

        if (gm > 2 && isGregorianLeapYear(gy + 1600)) {
            days++;
        }

        jy += 33 * (days / 12053);
        days %= 12053;

        jy += 4 * (days / 1461);
        days %= 1461;

        if (days > 365) {
            jy += (days - 1) / 365;
            days = (days - 1) % 365;
        }

        int jm;
        int jd;

        if (days < 186) {
            jm = 1 + days / 31;
            jd = 1 + days % 31;
        } else {
            jm = 7 + (days - 186) / 30;
            jd = 1 + (days - 186) % 30;
        }

        return new int[]{jy, jm, jd};
    }

    private static boolean isGregorianLeapYear(int year) {
        return year % 400 == 0
                || (year % 4 == 0 && year % 100 != 0);
    }
}