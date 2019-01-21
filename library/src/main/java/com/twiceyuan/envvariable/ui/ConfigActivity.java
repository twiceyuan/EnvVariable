package com.twiceyuan.envvariable.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.twiceyuan.envvariable.EnvVariable;
import com.twiceyuan.envvariable.R;
import com.twiceyuan.envvariable.model.Variable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ConfigActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.env_var_activity_variable_config);
        ListView listView = findViewById(R.id.listView);

        final List<Variable> variables = EnvVariable.listAllVariables();
        final ConfigAdapter adapter = new ConfigAdapter(variables);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            Variable variable = variables.get(position);
            showChoiceDialog(this, variable, adapter::notifyDataSetChanged);
        });

        TextView applyButton = findViewById(R.id.tv_apply);
        applyButton.setOnClickListener(v -> {
            EnvVariable.saveWithRefresh();
            Toast.makeText(this, "改动已应用", Toast.LENGTH_SHORT).show();
        });
    }

    private void showChoiceDialog(final Context context, final Variable variable, final Runnable afterChoiceAction) {
        AtomicInteger checkedPosition = new AtomicInteger(0);
        for (int i = 0; i < variable.selections.size(); i++) {
            Variable.Item item = variable.selections.get(i);
            if (item.value.equals(variable.getValue())) {
                checkedPosition.set(i);
            }
        }

        View dialogLayout = View.inflate(context, R.layout.env_var_layout_choice_dialog, null);

        View cancelView = dialogLayout.findViewById(R.id.tv_cancel);
        View confirmView = dialogLayout.findViewById(R.id.tv_confirm);
        ListView choiceList = dialogLayout.findViewById(R.id.list_choice);
        choiceList.setChoiceMode(ListView.CHOICE_MODE_NONE);

        ChoiceItemAdapter choiceItemAdapter = new ChoiceItemAdapter(variable.selections, checkedPosition.get());
        choiceList.setAdapter(choiceItemAdapter);
        choiceItemAdapter.setItemChoiceListener((position, item) -> checkedPosition.set(position));

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogLayout)
                .create();

        dialog.setCancelable(false);
        cancelView.setOnClickListener(v -> dialog.dismiss());
        confirmView.setOnClickListener(v -> {
            dialog.dismiss();
            variable.currentValue = variable.selections.get(checkedPosition.get());
            if (afterChoiceAction != null) {
                afterChoiceAction.run();
            }
        });

        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            // https://stackoverflow.com/questions/9102074/android-edittext-in-dialog-doesnt-pull-up-soft-keyboard
            // 解决 Dialog 里面的 ListView 里面的 EditText 获取焦点后没有弹出输入法的问题
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        }
    }
}
