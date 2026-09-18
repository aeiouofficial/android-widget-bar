package com.aeiou.widgetbar;

final class TapGesturePolicy {
    private TapGesturePolicy() {}

    static boolean isDoubleTap(long firstTapAt, long secondTapAt, long timeoutMillis) {
        if (firstTapAt < 0L || secondTapAt < firstTapAt) {
            return false;
        }
        return secondTapAt - firstTapAt <= timeoutMillis;
    }
}
