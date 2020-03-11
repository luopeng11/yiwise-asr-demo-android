package com.yiwise.asr.demo.handler;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.yiwise.asr.demo.R;
import com.yiwise.asr.demo.utils.ThreadUtils;

public class ResultViewHandler implements ResultViewHandlerInterface {
    private ScrollView dialogFlowScrollView;
    private ViewGroup dialogFlowViewGroup;

    private Activity activity;

    private int dialogFlowViewGroupWith;

    public ResultViewHandler(Activity activity) {
        this.activity = activity;
        dialogFlowViewGroup = activity.findViewById(R.id.dialog_flow);
        dialogFlowScrollView = activity.findViewById(R.id.dialog_flow_scroll);

        // 获取宽度
        ViewGroup.LayoutParams layoutParams = dialogFlowViewGroup.getLayoutParams();
        dialogFlowViewGroupWith = layoutParams.width;
    }

    public void handleResult(String text, boolean isEnd) {
        // 刷新UI
        ThreadUtils.getInstance().runOnUiThread(() -> {
            TextView textView = new TextView(activity);
            textView.setWidth(dialogFlowViewGroupWith);
            textView.setTextSize(12);

            textView.setText(text);

            if (isEnd) {
                textView.setTextColor(0xFF2A1885);
            } else {
                textView.setTextColor(0xFFFFAB17);
            }

            textView.setHeight((int) (textView.getLineHeight() * 2.5));
            dialogFlowViewGroup.addView(textView);
            dialogFlowScrollView.fullScroll(View.FOCUS_DOWN);
        });
    }

    public void clearContext(){
        dialogFlowViewGroup.removeAllViewsInLayout();
    }
}
