package com.xgj.app.xgj;

import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.philliphsu.bottomsheetpickers.BottomSheetPickerDialog;
import com.philliphsu.bottomsheetpickers.date.DatePickerDialog;
import com.xgj.app.mylibrary.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddHomeService extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    private static final String TAG = "AddHomeService";

    List<String> tags = new ArrayList<>();
    FlowTagLayout mFlowTagLayout_tag;
    TagAdapter<String> mTagAdapter_tag;
    List<String> frequency = new ArrayList<>();
    FlowTagLayout mFlowTagLayout_frequency;
    TagAdapter<String> mTagAdapter_frequency;
    TextView set_date;
    FlowTagLayout mFlowTagLayout_tag_container;
    LinearLayout.LayoutParams params;
    List<TextView> tagView;
    List<Boolean> tagViewState;
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_home_service);
        Button btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //set next service date
        set_date = (TextView) findViewById(R.id.textView_next_service_date);
        set_date.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                DialogFragment dialog = createDialog();
                dialog.show(getSupportFragmentManager(), TAG);
            }
        });

        //add selected tags flowtaglayout
        mFlowTagLayout_tag_container = (FlowTagLayout) findViewById(R.id.tag_container);
        params=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(15,6,0,0);
        tagView = new ArrayList<>();
        tagViewState = new ArrayList<>();
        //创建编辑中的标签
        initEditText();
        //添加到layout中
        mFlowTagLayout_tag_container.addView(editText);
        //对软键盘的Enter和Del键监听
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (KeyEvent.ACTION_DOWN == event.getAction()) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_ENTER:
                            String editTextContent = editText.getText().toString();
                            if (editTextContent.equals(""))
                                return true;
                            //判断标签是否重复添加
                            for(TextView tag:tagView){
                                String tempStr=tag.getText().toString();
                                if(tempStr.equals(editTextContent)) {
                                    Log.e("tag","重复添加");
                                    editText.setText("");
                                    editText.requestFocus();
                                    return true;
                                }
                            }
                            //添加标签
                            addTextView(editText.getText().toString());
                            return true;
                        case KeyEvent.KEYCODE_DEL:
                            //update tags check status
                            int lastIndex = tagView.size() - 1;
                            //没有添加标签则不继续执行
                            if (lastIndex < 0)
                                return false;
                            //获取前一个标签
                            TextView prevTag = tagView.get(lastIndex);
                            //第一次按下Del键则变成选中状态，选中状态下按Del键则删除
                            if (tagViewState.get(lastIndex)) {
                                int idx = -1;
                                for (int i = 0; i < tags.size(); i++) {
                                    if ((tags.get(i) + " ×").equals(tagView.get(lastIndex).getText().toString())) idx = i;
                                }
                                if (idx != -1) {
                                    mFlowTagLayout_tag.mCheckedTagArray.put(idx, false);
                                    mFlowTagLayout_tag.clearOption(idx);
                                }
                                tagView.remove(prevTag);
                                tagViewState.remove(lastIndex);
                                mFlowTagLayout_tag_container.removeView(prevTag);
                            } else {
                                String te = editText.getText().toString();
                                if (te.equals("")) {
                                    prevTag.setText(prevTag.getText() + " ×");
                                    tagViewState.set(lastIndex, true);
                                }
                            }
                            break;
                    }
                }
                return false;
            }
        });

        //监听编辑标签的输入事件
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //输入文字时取消已经选中的标签
                for (int i = 0; i < tagViewState.size(); i++) {
                    if (tagViewState.get(i)) {
                        TextView tmp = tagView.get(i);
                        tmp.setText(tmp.getText().toString().replace(" ×", ""));
                        tagViewState.set(i, false);
                        tmp.setTextColor(Color.parseColor("#66CDAA"));
                    }
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        //tags
        mFlowTagLayout_tag = (FlowTagLayout) findViewById(R.id.tag_flow_layout);
        mTagAdapter_tag = new TagAdapter<>(this);
        mFlowTagLayout_tag.setTagCheckedMode(FlowTagLayout.FLOW_TAG_CHECKED_MULTI);
        mFlowTagLayout_tag.setAdapter(mTagAdapter_tag);
        mFlowTagLayout_tag.setOnTagClickListener(new OnTagClickListener() {
            @Override
            public void onItemClick(FlowTagLayout parent, View view, int position) {
                if (!parent.mCheckedTagArray.get(position)) {
                    //判断标签是否重复添加
                    for(TextView tag:tagView){
                        String tempStr=tag.getText().toString();
                        if(tempStr.equals(tags.get(position))) {
                            Log.e("tag","重复添加");
                            editText.setText("");
                            editText.requestFocus();
                            return ;
                        }
                    }
                    //添加标签
                    addTextView(tags.get(position));
                } else {
                    //remove tag
                    int idx = -1;
                    for (TextView tag : tagView) {
                        if (tag.getText().toString().equals(tags.get(position))) idx = tagView.indexOf(tag);
                    }
                    if (idx < 0) return ;
                    TextView prevTag = tagView.get(idx);
                    tagView.remove(prevTag);
                    tagViewState.remove(idx);
                    mFlowTagLayout_tag_container.removeView(prevTag);
                }
            }
        });
        initTags();
        //expire
        mFlowTagLayout_frequency = (FlowTagLayout) findViewById(R.id.frequency_flow_layout);
        mTagAdapter_frequency = new TagAdapter<>(this);
        mFlowTagLayout_frequency.setTagCheckedMode(FlowTagLayout.FLOW_TAG_CHECKED_SINGLE);
        mFlowTagLayout_frequency.setAdapter(mTagAdapter_frequency);
        mFlowTagLayout_frequency.setOnTagSelectListener(new OnTagSelectListener() {
            @Override
            public void onItemSelect(FlowTagLayout parent, List<Integer> selectedList) {

            }
        });
        initFrequency();

    }

    private  TextView getTag(String tag){
        TextView textView=new TextView(getApplicationContext());
        textView.setTextSize(12);
        textView.setText(tag);
        textView.setLayoutParams(params);
        textView.setBackgroundResource(R.drawable.round_rectangle_bg);
        textView.setPadding(20,10,20,10);
        textView.setTextColor(getResources().getColor(R.color.normal_text_color));
        return  textView;
    }

    @Override
    public void onDateSet(DatePickerDialog dialog, int year, int monthOfYear, int dayOfMonth) {
        Calendar cal = new java.util.GregorianCalendar();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, monthOfYear);
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        set_date.setText("Next Service Date: " + DateFormat.getDateFormat(this).format(cal.getTime()));
    }

    private DialogFragment createDialog() {
        return createDialogWithSetters();
    }

    private DialogFragment createDialogWithSetters() {
        BottomSheetPickerDialog dialog = null;

        Calendar now = Calendar.getInstance();
        dialog = DatePickerDialog.newInstance(
                AddHomeService.this,
                now.get(Calendar.YEAR),
                now.get(Calendar.MONTH),
                now.get(Calendar.DAY_OF_MONTH));
        DatePickerDialog dateDialog = (DatePickerDialog) dialog;
        dateDialog.setMinDate(now);
        Calendar max = Calendar.getInstance();
        max.add(Calendar.YEAR, 10);
        dateDialog.setMaxDate(max);
        dateDialog.setYearRange(1970, 2032);
        return dialog;
    }

    private void initTags() {
        tags.add("Love");
        tags.add("Fish");
        tags.add("Food");
        tags.add("Snack");
        tags.add("Fruit");
        tags.add("Meat");
        tags.add("Medicine");
        tags.add("Makeup");
        tags.add("Easy");
        tags.add("Great");
        mTagAdapter_tag.onlyAddAll(tags);
    }

    private void initFrequency() {
        frequency.add("Day");
        frequency.add("Week");
        frequency.add("Month");
        frequency.add("Year");
        mTagAdapter_frequency.onlyAddAll(frequency);
    }

    private void initEditText() {
        editText=new EditText(getApplicationContext());
        editText.setHint("Add Tag");
        editText.setMinEms(4);
        editText.setTextSize(12);
        editText.setHintTextColor(Color.parseColor("#b4b4b4"));
        editText.setTextColor(Color.parseColor("#000000"));
        editText.setLayoutParams(params);
    }

    private void addTextView(String tag) {
        final TextView temp = getTag(tag);
        tagView.add(temp);
        tagViewState.add(false);
        //添加点击事件，点击变成选中状态，选中状态下被点击则删除
        temp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int curIndex = tagView.indexOf(temp);
                if (!tagViewState.get(curIndex)) {
                    //显示 ×号删除
                    temp.setText(temp.getText() + " ×");
                    //修改选中状态
                    tagViewState.set(curIndex, true);
                } else {
                    int idx = -1;
                    for (int i = 0; i < tags.size(); i++) {
                        if ((tags.get(i) + " ×").equals(temp.getText().toString())) idx = i;
                    }
                    if (idx != -1) {
                        mFlowTagLayout_tag.mCheckedTagArray.put(idx, false);
                        mFlowTagLayout_tag.clearOption(idx);
                    }
                    mFlowTagLayout_tag_container.removeView(temp);
                    tagView.remove(curIndex);
                    tagViewState.remove(curIndex);
                }
            }
        });
        mFlowTagLayout_tag_container.addView(temp);
        //让编辑框在最后一个位置上
        editText.bringToFront();
        //清空编辑框
        editText.setText("");
        editText.requestFocus();
    }

}
