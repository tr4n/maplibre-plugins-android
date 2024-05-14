package com.mapbox.mapboxsdk.plugins.annotation;

import android.view.View;

import org.maplibre.android.maps.MapLibreMap;

import org.hamcrest.Matcher;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import org.maplibre.android.maps.MapLibreMap;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class MapboxMapAction implements ViewAction {

    private OnInvokeActionListener invokeViewAction;
    private MapLibreMap maplibreMap;

    public MapboxMapAction(OnInvokeActionListener invokeViewAction, MapLibreMap maplibreMap) {
        this.invokeViewAction = invokeViewAction;
        this.maplibreMap = maplibreMap;
    }

    @Override
    public Matcher<View> getConstraints() {
        return isDisplayed();
    }

    @Override
    public String getDescription() {
        return getClass().getSimpleName();
    }

    @Override
    public void perform(UiController uiController, View view) {
        invokeViewAction.onInvokeAction(uiController, maplibreMap);
    }

    public static void invoke(MapLibreMap maplibreMap, OnInvokeActionListener invokeViewAction) {
        onView(withId(android.R.id.content)).perform(new MapboxMapAction(invokeViewAction, maplibreMap));
    }

    public interface OnInvokeActionListener {
        void onInvokeAction(UiController uiController, MapLibreMap maplibreMap);
    }
}


