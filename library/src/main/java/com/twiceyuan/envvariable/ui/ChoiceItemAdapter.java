package com.twiceyuan.envvariable.ui;

import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.twiceyuan.envvariable.R;
import com.twiceyuan.envvariable.model.Variable;

import java.util.List;

public class ChoiceItemAdapter extends BaseAdapter {

    private static final String TAG = "ChoiceItemAdapter";

    private final List<Variable.Item> selections;
    private int selectionPosition;

    private SparseBooleanArray editModeMap = new SparseBooleanArray();

    private OnItemClickListener itemChoiceListener;

    ChoiceItemAdapter(List<Variable.Item> selections, int selectionPosition) {
        this.selections = selections;
        this.selectionPosition = selectionPosition;
        disableAllEditor();
    }

    private void disableAllEditor() {
        for (int position = 0; position < selections.size(); position++) {
            editModeMap.put(position, false);
        }
    }

    @Override
    public int getCount() {
        return selections.size();
    }

    @Override
    public Variable.Item getItem(int position) {
        return selections.get(position);
    }

    @Override
    public long getItemId(int position) {
        Variable.Item item = getItem(position);
        return (item.value + editModeMap.get(position)).hashCode();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.env_var_item_choice_item, null);
            holder = new Holder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        Variable.Item item = selections.get(position);
        holder.name.setText(item.name);
        holder.editText.setText(item.value);

        if (selectionPosition == position) {
            holder.checkbox.setChecked(true);
        } else {
            holder.checkbox.setChecked(false);
        }

        holder.editText.setOnFocusChangeListener((v, hasFocus) -> {
            String log = v.toString() + " / " + hasFocus;
            Log.i(TAG, log);
        });

        holder.layoutCheckBox.setOnClickListener(v -> {

            // 改变选择的位置
            selectionPosition = position;
            disableAllEditor();
            notifyDataSetChanged();

            if (itemChoiceListener != null) {
                itemChoiceListener.onClick(position, item);
            }
        });

        setupEditor(holder, position, item);

        return convertView;
    }

    private void setupEditor(Holder holder, int position, Variable.Item item) {
        if (!item.isEditable) {
            holder.iconEdit.setVisibility(View.INVISIBLE);
            toggleEditor(holder.editText, false);
            return;
        } else {
            holder.iconEdit.setVisibility(View.VISIBLE);
        }

        boolean isEditMode = editModeMap.get(position);
        if (isEditMode) {
            // 编辑模式下
            toggleEditor(holder.editText, true);
            holder.iconEdit.setImageResource(R.drawable.env_var_ic_done_black_24dp);
            holder.iconEdit.setOnClickListener(v -> {
                // 保存值到内存，然后通知更新
                item.updateValue(holder.editText.getText().toString());
                editModeMap.put(position, false);
                notifyDataSetChanged();
            });
        } else {
            toggleEditor(holder.editText, false);
            holder.iconEdit.setImageResource(R.drawable.env_var_ic_mode_edit_black_24dp);
            holder.iconEdit.setOnClickListener(v -> {
                for (int i = 0; i < editModeMap.size(); i++) {
                    if (i == position) {
                        editModeMap.put(i, true);
                    } else {
                        editModeMap.put(i, false);
                    }
                }
                notifyDataSetChanged();
            });
        }
    }

    private void toggleEditor(EditText editText, boolean isActive) {
        editText.setTextIsSelectable(isActive);
        editText.setFocusable(isActive);
        editText.setFocusableInTouchMode(isActive);
        editText.setEnabled(isActive);
        if (isActive) {
            editText.setSelection(editText.length());
        }
    }

    public interface OnItemClickListener {
        void onClick(int position, Variable.Item item);
    }

    public void setItemChoiceListener(OnItemClickListener itemChoiceListener) {
        this.itemChoiceListener = itemChoiceListener;
    }

    static class Holder {
        final View rootView;
        final TextView name;
        final EditText editText;
        final AppCompatImageView iconEdit;
        final FrameLayout layoutCheckBox;
        final RadioButton checkbox;

        Holder(View rootView) {
            this.rootView = rootView;
            name = rootView.findViewById(R.id.tv_name);
            editText = rootView.findViewById(R.id.et_value);
            iconEdit = rootView.findViewById(R.id.img_mode);
            layoutCheckBox = rootView.findViewById(R.id.layout_checkbox);
            checkbox = rootView.findViewById(R.id.checkbox);
        }
    }
}
