package org.maplibre.android.plugins.testapp.activity.annotation;

import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.plugins.annotation.Fill;
import org.maplibre.android.plugins.annotation.FillManager;
import org.maplibre.android.plugins.annotation.FillOptions;
import org.maplibre.android.plugins.testapp.TestStyles;
import org.maplibre.android.plugins.testapp.R;
import org.maplibre.android.plugins.testapp.Utils;
import org.maplibre.android.utils.ColorUtils;
import org.maplibre.geojson.FeatureCollection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Activity showcasing adding fills using the annotation plugin
 */
public class FillActivity extends AppCompatActivity {

    private final Random random = new Random();

    private MapView mapView;
    private FillManager fillManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_annotation);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(maplibreMap -> maplibreMap.setStyle(TestStyles.BRIGHT.getUrl(), style -> {
            findViewById(R.id.fabStyles).setOnClickListener(v -> maplibreMap.setStyle(Utils.INSTANCE.getNextStyle()));

            maplibreMap.moveCamera(CameraUpdateFactory.zoomTo(2));

            fillManager = new FillManager(mapView, maplibreMap, style);
            fillManager.addClickListener(fill -> {
                Toast.makeText(FillActivity.this,
                    String.format("Fill clicked %s with title: %s", fill.getId(), getTitleFromFill(fill)),
                    Toast.LENGTH_SHORT
                ).show();
                return false;
            });

            fillManager.addLongClickListener(fill -> {
                Toast.makeText(FillActivity.this,
                    String.format("Fill long clicked %s with title: %s", fill.getId(), getTitleFromFill(fill)),
                    Toast.LENGTH_SHORT
                ).show();
                return false;
            });

            // create a fixed fill
            List<LatLng> innerLatLngs = new ArrayList<>();
            innerLatLngs.add(new LatLng(-10.733102, -3.363937));
            innerLatLngs.add(new LatLng(-19.716317, 1.754703));
            innerLatLngs.add(new LatLng(-21.085074, -15.747196));
            innerLatLngs.add(new LatLng(-10.733102, -3.363937));
            List<List<LatLng>> latLngs = new ArrayList<>();
            latLngs.add(innerLatLngs);

            FillOptions fillOptions = new FillOptions()
                .withLatLngs(latLngs)
                .withData(new JsonPrimitive("Foobar"))
                .withFillColor(ColorUtils.colorToRgbaString(Color.RED));
            fillManager.create(fillOptions);

            // random add fills across the globe
            List<FillOptions> fillOptionsList = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                int color = Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256));
                fillOptionsList.add(new FillOptions()
                    .withLatLngs(createRandomLatLngs())
                    .withFillColor(ColorUtils.colorToRgbaString(color))
                );
            }
            fillManager.create(fillOptionsList);

            try {
                fillManager.create(FeatureCollection.fromJson(Utils.INSTANCE.loadStringFromAssets(this, "annotations.json")));
            } catch (IOException e) {
                throw new RuntimeException("Unable to parse annotations.json");
            }
        }));
    }

    private String getTitleFromFill(Fill fill) {
        String title = "unknown";
        JsonElement customData = fill.getData();
        if (!(customData.isJsonNull())) {
            title = customData.getAsString();
        }
        return title;
    }

    private List<List<LatLng>> createRandomLatLngs() {
        List<LatLng> latLngs = new ArrayList<>();
        LatLng firstLast = new LatLng((random.nextDouble() * -180.0) + 90.0,
            (random.nextDouble() * -360.0) + 180.0);
        latLngs.add(firstLast);
        for (int i = 0; i < random.nextInt(10); i++) {
            latLngs.add(new LatLng((random.nextDouble() * -180.0) + 90.0,
                (random.nextDouble() * -360.0) + 180.0));
        }
        latLngs.add(firstLast);

        List<List<LatLng>> resulting = new ArrayList<>();
        resulting.add(latLngs);
        return resulting;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_fill, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_action_draggable) {
            for (int i = 0; i < fillManager.getAnnotations().size(); i++) {
                Fill fill = fillManager.getAnnotations().get(i);
                fill.setDraggable(!fill.isDraggable());
            }
            return true;
        }
        return false;
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
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (fillManager != null) {
            fillManager.onDestroy();
        }

        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}