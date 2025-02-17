package org.maplibre.android.plugins.testapp.activity.annotation;

import android.content.Context;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.plugins.annotation.ClusterOptions;
import org.maplibre.android.plugins.annotation.Symbol;
import org.maplibre.android.plugins.annotation.SymbolManager;
import org.maplibre.android.plugins.annotation.SymbolOptions;
import org.maplibre.android.plugins.testapp.TestStyles;
import org.maplibre.android.plugins.testapp.R;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.Point;
import timber.log.Timber;

import java.io.*;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Test activity showcasing adding a large amount of Symbols with a cluster configuration.
 */
public class ClusterSymbolActivity extends AppCompatActivity {

    private SymbolManager symbolManager;
    private List<Symbol> symbols = new ArrayList<>();

    private MapLibreMap maplibreMap;
    private MapView mapView;
    private FeatureCollection locations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this::initMap);
    }

    private void initMap(MapLibreMap maplibreMap) {
        this.maplibreMap = maplibreMap;
        maplibreMap.moveCamera(
            CameraUpdateFactory.newLatLngZoom(
                new LatLng(38.87031, -77.00897), 10
            )
        );

        ClusterOptions clusterOptions = new ClusterOptions()
            .withColorLevels(new Pair[]{
                new Pair(100, Color.RED),
                new Pair(50, Color.BLUE),
                new Pair(0, Color.GREEN)
            });

        maplibreMap.setStyle(new Style.Builder().fromUri(TestStyles.BRIGHT.getUrl()), style -> {
            symbolManager = new SymbolManager(mapView, maplibreMap, style, null, null, clusterOptions);
            symbolManager.setIconAllowOverlap(true);
            loadData();
        });
    }

    private void loadData() {
        int amount = 10000;
        if (locations == null) {
            new LoadLocationTask(this, amount).execute();
        } else {
            showMarkers(amount);
        }
    }

    private void onLatLngListLoaded(FeatureCollection featureCollection, int amount) {
        locations = featureCollection;
        showMarkers(amount);
    }

    private void showMarkers(int amount) {
        if (maplibreMap == null || locations == null || locations.features() == null || mapView.isDestroyed()) {
            return;
        }
        // delete old symbols
        symbolManager.delete(symbols);
        if (locations.features().size() < amount) {
            amount = locations.features().size();
        }

        showSymbols(amount);
    }

    private void showSymbols(int amount) {
        List<SymbolOptions> options = new ArrayList<>();
        Random random = new Random();
        int randomIndex;

        List<Feature> features = locations.features();
        if (features == null) {
            return;
        }

        for (int i = 0; i < amount; i++) {
            randomIndex = random.nextInt(features.size());
            Feature feature = features.get(randomIndex);
            options.add(new SymbolOptions()
                .withGeometry((Point) feature.geometry())
                .withIconImage("fire-station-11")
            );
        }
        symbols = symbolManager.create(options);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (symbolManager != null) {
            symbolManager.onDestroy();
        }

        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private static class LoadLocationTask extends AsyncTask<Void, Integer, FeatureCollection> {

        private final WeakReference<ClusterSymbolActivity> activity;
        private final int amount;

        private LoadLocationTask(ClusterSymbolActivity activity, int amount) {
            this.amount = amount;
            this.activity = new WeakReference<>(activity);
        }

        @Override
        protected FeatureCollection doInBackground(Void... params) {
            ClusterSymbolActivity activity = this.activity.get();
            if (activity != null) {
                String json = null;
                try {
                    json = GeoParseUtil.loadStringFromAssets(activity.getApplicationContext());
                } catch (IOException exception) {
                    Timber.e(exception, "Could not add markers");
                }

                if (json != null) {
                    return FeatureCollection.fromJson(json);
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(FeatureCollection locations) {
            super.onPostExecute(locations);
            ClusterSymbolActivity activity = this.activity.get();
            if (activity != null) {
                activity.onLatLngListLoaded(locations, amount);
            }
        }
    }

    public static class GeoParseUtil {

        static String loadStringFromAssets(final Context context) throws IOException {
            InputStream is = context.getAssets().open("points.geojson");
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            return readAll(rd);
        }

        private static String readAll(Reader rd) throws IOException {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        }
    }
}
