package com.brioal.gobang;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import com.brioal.gobang.view.Panel;

public class MainActivity extends AppCompatActivity {
    private Panel panel; // 棋盘VIew
    private AlertDialog.Builder builder; //Dialog构建

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); // 设置全屏
        builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("游戏结束"); // 设置Dialog的标题
        builder.setNegativeButton("退出", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // 设置退出按钮和点击事件
                MainActivity.this.finish();
            }
        });
        builder.setPositiveButton("再来一局", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { // 设置再来一局的按钮和点击事件
                panel.reStartGame();
            }
        });
        panel = (Panel) findViewById(R.id.main_panel);
        panel.setOnGameListener(new Panel.onGameListener() {
            @Override
            public void onGameOVer(int i) { // 设置监听器
                String text = "";
                if (i == Panel.WHITE_WIN) {
                    //白子胜利
                    text = "白子胜利";
                } else if (i == Panel.BLACK_WIN) {
                    //黑子胜利
                    text = "黑子胜利";
                }
                builder.setMessage(text); // 设置Dialog内容
                builder.setCancelable(false); // 设置不可返回键取消
                AlertDialog dialog = builder.create(); // 构建Dialog
                Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams params = new WindowManager.LayoutParams();
                params.x = 0;//设置x坐标
                params.y = panel.getUnder();//设置y坐标
                dialogWindow.setAttributes(params); // 设置新的LayoutParams
                dialog.setCanceledOnTouchOutside(false); // 设置点击外部不取消
                dialog.show(); // 显示Dialog
            }
        });
    }


}
