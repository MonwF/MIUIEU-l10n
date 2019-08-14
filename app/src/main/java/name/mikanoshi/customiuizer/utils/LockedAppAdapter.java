package name.mikanoshi.customiuizer.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import name.mikanoshi.customiuizer.R;

public class LockedAppAdapter extends BaseAdapter implements Filterable {
	private Context ctx;
	private LayoutInflater mInflater;
	private ThreadPoolExecutor pool;
	private ItemFilter mFilter = new ItemFilter();
	private ArrayList<AppData> originalAppList;
	private ArrayList<AppData> filteredAppList;
	private Object mSecurityManager;
	private Method getApplicationAccessControlEnabled;

	@SuppressLint("WrongConstant")
	public LockedAppAdapter(Context context, ArrayList<AppData> arr) {
		ctx = context;
		mInflater = LayoutInflater.from(context);
		originalAppList = arr;
		filteredAppList = arr;
		int cpuCount = Runtime.getRuntime().availableProcessors();
		pool = new ThreadPoolExecutor(cpuCount + 1, cpuCount * 2 + 1, 2, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

		try {
			mSecurityManager = context.getSystemService("security");
			getApplicationAccessControlEnabled = mSecurityManager.getClass().getDeclaredMethod("getApplicationAccessControlEnabled", String.class);
			getApplicationAccessControlEnabled.setAccessible(true);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		sortList();
	}

	private void sortList() {
		Collections.sort(filteredAppList, new Comparator<AppData>() {
			public int compare(AppData app1, AppData app2) {
				try {
					boolean app1checked = (boolean)getApplicationAccessControlEnabled.invoke(mSecurityManager, app1.pkgName);
					boolean app2checked = (boolean)getApplicationAccessControlEnabled.invoke(mSecurityManager, app2.pkgName);
					if (app1checked && app2checked)
						return 0;
					else if (app1checked)
						return -1;
					else if (app2checked)
						return 1;
					return 0;
				} catch (Throwable t) {
					return 0;
				}
			}
		});
	}

	public boolean isEnabled(int position) {
		AppData ad = getItem(position);
		return ad == null || !"com.miui.securitycenter".equals(ad.pkgName);
	}

	public int getCount() {
		return filteredAppList.size();
	}

	public AppData getItem(int position) {
		return filteredAppList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		View row;
		if (convertView != null)
			row = convertView;
		else
			row = mInflater.inflate(R.layout.applist_item, parent, false);

		ImageView itemIsDis = row.findViewById(R.id.am_isDisable_icon);
		CheckBox itemChecked = row.findViewById(R.id.am_checked_icon);
		TextView itemTitle = row.findViewById(R.id.am_label);
		ImageView itemIcon = row.findViewById(R.id.am_icon);

		AppData ad = getItem(position);
		itemIcon.setTag(position);
		itemTitle.setText(ad.label);
		itemIsDis.setVisibility(ad.enabled ? View.INVISIBLE : View.VISIBLE);
		Bitmap icon = Helpers.memoryCache.get(ad.pkgName + "|" + ad.actName);
		//int iconSize = getResources().getDimensionPixelSize(android.R.dimen.app_icon_size);

		if (icon == null) {
			Drawable[] dualIcon = new Drawable[1];
			dualIcon[0] = ctx.getResources().getDrawable(R.drawable.card_icon_default, ctx.getTheme());
			TransitionDrawable crossfader = new TransitionDrawable(dualIcon);
			crossfader.setCrossFadeEnabled(true);
			itemIcon.setImageDrawable(crossfader);
			(new BitmapCachedLoader(itemIcon, ad, ctx)).executeOnExecutor(pool);
		} else {
			itemIcon.setImageBitmap(icon);
		}

		try {
			itemChecked.setVisibility(View.VISIBLE);
			itemChecked.setChecked((boolean)getApplicationAccessControlEnabled.invoke(mSecurityManager, ad.pkgName));
		} catch (Throwable t) {
			itemChecked.setVisibility(View.GONE);
		}

		boolean enabled = !"com.miui.securitycenter".equals(ad.pkgName);
		itemIcon.setAlpha(enabled ? 1.0f : 0.5f);
		itemTitle.setAlpha(enabled ? 1.0f : 0.5f);
		itemChecked.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
		row.setEnabled(enabled);

		return row;
	}

	private class ItemFilter extends Filter {
		@Override
		protected FilterResults performFiltering(CharSequence constraint) {
			String filterString = constraint.toString().toLowerCase();
			FilterResults results = new FilterResults();

			int count = originalAppList.size();
			final ArrayList<AppData> nlist = new ArrayList<AppData>();
			AppData filterableData;

			for (int i = 0; i < count; i++) {
				filterableData = originalAppList.get(i);
				if (filterableData.label.toLowerCase().contains(filterString)) nlist.add(filterableData);
			}

			results.values = nlist;
			results.count = nlist.size();
			return results;
		}

		@Override
		@SuppressWarnings("unchecked")
		protected void publishResults(CharSequence constraint, FilterResults results) {
			filteredAppList = (ArrayList<AppData>)results.values;
			sortList();
			notifyDataSetChanged();
		}
	}

	@Override
	public Filter getFilter() {
		return mFilter;
	}
}