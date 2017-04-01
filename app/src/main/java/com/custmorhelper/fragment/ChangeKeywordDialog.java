package com.custmorhelper.fragment;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.custmorhelper.R;
import com.custmorhelper.manager.GlobleManager;
import com.custmorhelper.util.Constants;

/**
 * Created by Administrator on 2017/4/1.
 */
public class ChangeKeywordDialog extends DialogFragment {

    public interface OnKeywordChangeListener {
        void onKeywordChange (String keyword);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_change_keyword_dialog, container, false);

        final EditText changeKeywordEditText = (EditText) view.findViewById(R.id.changeKeywordEditText);
        changeKeywordEditText.setText(Constants.MONITOR_KEYWORD_DEFAULT);
        changeKeywordEditText.setSelection(Constants.MONITOR_KEYWORD_DEFAULT.length());

        Button okButton = (Button) view.findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String keyword = changeKeywordEditText.getText().toString();

                SharedPreferences sp = GlobleManager.getSharePreferenceMonitor();
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Constants.SP_MONITOR_KEYWORD, keyword);
                editor.commit();

                OnKeywordChangeListener onKeywordChangeListener = (OnKeywordChangeListener) getActivity();
                onKeywordChangeListener.onKeywordChange(keyword);

                getDialog().dismiss();
            }
        });

        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });

        return view;
    }
}
