package com.vuitv.soundrecorder.fragment;


import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;


import com.vuitv.soundrecorder.BuildConfig;
import com.vuitv.soundrecorder.MySharedPreference;
import com.vuitv.soundrecorder.R;

/**
 * Created by vuitv on 10/29/2018.
 */

public class SettingFragment extends PreferenceFragmentCompat {

    public static SettingFragment newInstance(int position) {
        SettingFragment fragment = new SettingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("position", position);
        fragment.setArguments(bundle);
        return fragment;
    }


    public SettingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        final CheckBoxPreference checkBoxPreference = (CheckBoxPreference) findPreference(getResources().getString(R.string.pref_high_quality_key));
        checkBoxPreference.setChecked(MySharedPreference.getPrefHightQuality(getActivity()));
        checkBoxPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                MySharedPreference.setPrefHightQuality(getActivity(), (Boolean) newValue);
                return true;
            }
        });

        Preference preference = findPreference(getResources().getString(R.string.pref_about_key));
        preference.setSummary(getString(R.string.pref_about_desc, BuildConfig.VERSION_NAME));

    }

}
