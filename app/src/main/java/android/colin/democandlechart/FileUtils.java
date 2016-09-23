package android.colin.democandlechart;

import android.content.res.AssetManager;
import android.util.Log;

import com.github.mikephil.charting.data.BarEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lkw on 2016/9/22.
 */
public class FileUtils {
    private static final String LOG = "MPChart-FileUtils";

    public static List<BarEntry> loadBarEntriesFromAssets(AssetManager am, String path) {

        List<BarEntry> entries = new ArrayList<BarEntry>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(am.open(path), "UTF-8"));

            String line = reader.readLine();

            while (line != null) {
                // process line
                String[] split = line.split("#");

                entries.add(new BarEntry(Float.parseFloat(split[0]), Integer.parseInt(split[1])));

                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(LOG, e.toString());

        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG, e.toString());
                }
            }
        }

        return entries;

    }

    public static List<Float>getFloat(AssetManager am, String path){
        List<Float> entries = new ArrayList<Float>();

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(am.open(path), "UTF-8"));

            String line = reader.readLine();

            while (line != null) {
                // process line
                String[] split = line.split("#");
                entries.add(Float.parseFloat(split[0]));

                line = reader.readLine();
            }
        } catch (IOException e) {
            Log.e(LOG, e.toString());

        } finally {

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(LOG, e.toString());
                }
            }
        }

        return entries;

    }
}
