package app.bare.launcher;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextClock;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends Activity {
    private static final int COLUMNS = 5;

    private static class Item {
        String label;
        Drawable image;
        boolean banner;
        ComponentName cn;
        int launches;
    }

    private final List<Item> items = new ArrayList<>();
    private GridView grid;
    private SharedPreferences prefs;
    private float dp;
    private boolean receiverOn;

    private final BroadcastReceiver pkgReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context c, Intent i) {
            load();
        }
    };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        dp = getResources().getDisplayMetrics().density;
        prefs = getSharedPreferences("launches", MODE_PRIVATE);

        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.rgb(16, 20, 24));

        LinearLayout col = new LinearLayout(this);
        col.setOrientation(LinearLayout.VERTICAL);
        col.setPadding(px(48), px(24), px(48), px(24));

        TextClock clock = new TextClock(this);
        clock.setFormat12Hour("h:mm a");
        clock.setFormat24Hour("HH:mm");
        clock.setTextColor(Color.WHITE);
        clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
        clock.setGravity(Gravity.END);
        col.addView(clock, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        grid = new GridView(this);
        grid.setNumColumns(COLUMNS);
        grid.setVerticalSpacing(px(24));
        grid.setHorizontalSpacing(px(24));
        GradientDrawable outer = new GradientDrawable();
        outer.setCornerRadius(px(10));
        outer.setColor(Color.argb(30, 255, 255, 255));
        outer.setStroke(px(3), Color.WHITE);
        GradientDrawable inner = new GradientDrawable();
        inner.setCornerRadius(px(7));
        inner.setStroke(px(3), Color.rgb(16, 20, 24));
        LayerDrawable sel = new LayerDrawable(new Drawable[] {outer, inner});
        sel.setLayerInset(1, px(3), px(3), px(3), px(3));
        grid.setSelector(sel);
        grid.setDrawSelectorOnTop(true);
        grid.setClipToPadding(false);
        grid.setPadding(px(8), px(24), px(8), px(24));
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> p, View v, int pos, long id) {
                launch(items.get(pos));
            }
        });
        col.addView(grid, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f));

        root.addView(col, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setContentView(root);
        grid.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        load();
        if (!receiverOn) {
            IntentFilter f = new IntentFilter();
            f.addAction(Intent.ACTION_PACKAGE_ADDED);
            f.addAction(Intent.ACTION_PACKAGE_REMOVED);
            f.addAction(Intent.ACTION_PACKAGE_REPLACED);
            f.addDataScheme("package");
            registerReceiver(pkgReceiver, f);
            receiverOn = true;
        }
    }

    @Override
    protected void onStop() {
        if (receiverOn) {
            unregisterReceiver(pkgReceiver);
            receiverOn = false;
        }
        super.onStop();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (grid != null && grid.getCount() > 0) {
            grid.setSelection(0);
            grid.requestFocus();
        }
    }

    // Home screen: Back does nothing.
    @SuppressWarnings("deprecation")
    @Override
    public void onBackPressed() {
    }

    private void launch(Item it) {
        Intent i = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
                .setComponent(it.cn).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        try {
            startActivity(i);
            prefs.edit().putInt(it.cn.flattenToShortString(), it.launches + 1).apply();
        } catch (Exception ignored) {
        }
    }

    private void load() {
        PackageManager pm = getPackageManager();
        List<ResolveInfo> all = new ArrayList<>();
        all.addAll(pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER), 0));
        all.addAll(pm.queryIntentActivities(
                new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER), 0));
        Set<String> seen = new HashSet<>();
        List<Item> out = new ArrayList<>();
        for (ResolveInfo ri : all) {
            String pkg = ri.activityInfo.packageName;
            if (pkg.equals(getPackageName())) continue;
            // One tile per package.
            if (!seen.add(pkg)) continue;
            Item it = new Item();
            it.cn = new ComponentName(pkg, ri.activityInfo.name);
            it.label = String.valueOf(ri.loadLabel(pm));
            Drawable d = ri.activityInfo.loadBanner(pm);
            if (d == null) d = ri.activityInfo.applicationInfo.loadBanner(pm);
            if (d != null) {
                it.banner = true;
            } else {
                d = ri.loadIcon(pm);
            }
            it.image = d;
            it.launches = prefs.getInt(it.cn.flattenToShortString(), 0);
            out.add(it);
        }
        Collections.sort(out, new Comparator<Item>() {
            @Override
            public int compare(Item a, Item b) {
                if (a.launches != b.launches) return b.launches - a.launches;
                return a.label.compareToIgnoreCase(b.label);
            }
        });
        items.clear();
        items.addAll(out);
        adapter.notifyDataSetChanged();
        grid.requestFocus();
    }

    private int px(int v) {
        return Math.round(v * dp);
    }

    private final BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int p) {
            return items.get(p);
        }

        @Override
        public long getItemId(int p) {
            return p;
        }

        @Override
        public View getView(int p, View v, ViewGroup parent) {
            Holder h;
            if (v == null) {
                h = new Holder();
                LinearLayout cell = new LinearLayout(MainActivity.this);
                cell.setOrientation(LinearLayout.VERTICAL);
                cell.setGravity(Gravity.CENTER_HORIZONTAL);
                final int radius = px(10);
                h.img = new ImageView(MainActivity.this);
                h.img.setClipToOutline(true);
                h.img.setOutlineProvider(new ViewOutlineProvider() {
                    @Override
                    public void getOutline(View view, Outline o) {
                        o.setRoundRect(0, 0, view.getWidth(), view.getHeight(), radius);
                    }
                });
                cell.addView(h.img, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, px(84)));
                h.txt = new TextView(MainActivity.this);
                h.txt.setTextColor(Color.WHITE);
                h.txt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                h.txt.setSingleLine(true);
                h.txt.setGravity(Gravity.CENTER);
                h.txt.setPadding(0, px(6), 0, 0);
                cell.addView(h.txt, new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                cell.setTag(h);
                v = cell;
            }
            h = (Holder) v.getTag();
            Item it = items.get(p);
            h.img.setImageDrawable(it.image);
            if (it.banner) {
                h.img.setScaleType(ImageView.ScaleType.FIT_XY);
                h.img.setPadding(0, 0, 0, 0);
                h.img.setBackground(null);
            } else {
                h.img.setScaleType(ImageView.ScaleType.FIT_CENTER);
                h.img.setPadding(px(20), px(8), px(20), px(8));
                h.img.setBackground(frame(false));
            }
            h.txt.setText(it.label);
            h.txt.setVisibility(it.banner ? View.GONE : View.VISIBLE);
            return v;
        }
    };

    private static class Holder {
        ImageView img;
        TextView txt;
    }

    private Drawable frame(boolean focused) {
        GradientDrawable g = new GradientDrawable();
        g.setCornerRadius(px(10));
        g.setColor(Color.rgb(28, 34, 42));
        return g;
    }
}
