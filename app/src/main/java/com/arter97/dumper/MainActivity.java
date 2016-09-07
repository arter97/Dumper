package com.arter97.dumper;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btn = (Button)findViewById(R.id.button);
        btn.setOnClickListener(this);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button:
                startDump();
                break;
        }
    }

    private void startDump() {
        TextView tv1 = (TextView)findViewById(R.id.TextView1);
        Button btn = (Button)findViewById(R.id.button);
        startDump(this, this, tv1, btn);
    }


    public static void startDump(Activity activity, Context context, TextView p1, Button p2) {
        Runnable sd = new startDumpClass(activity, context, p1, p2);
        Thread sdThread = new Thread(sd);
        sdThread.start();
    }

    private static class startDumpClass implements Runnable {
        private final Activity activity;
        private final Context context;
        private final TextView tv1;
        private final Button btn;
        private static String s;

        public startDumpClass(Activity act, Context cont, TextView p1, Button p2) {
            activity = act;
            context = cont;
            tv1 = p1;
            btn = p2;
        }

        public void run() {
            // Copy from assets
            copyAssets(context, context.getApplicationInfo().dataDir + "/files");

            // Run dump.sh
            Runtime rt = Runtime.getRuntime();
            String[] commands = {context.getFilesDir().getAbsolutePath() + "/dump.sh",
                    context.getFilesDir().getAbsolutePath(), // arg1 : self-path
            };

            Process proc;

            try {
                proc = rt.exec(commands);
            } catch (Exception e) {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tv1.setText("ERROR! Please try again");
                        sleep(10);
                    }
                });
                return;
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    btn.setEnabled(false);
                    sleep(10);
                }
            });

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(proc.getInputStream()));

            // read the output from the command
            try {
                while ((s = stdInput.readLine()) != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String tmp = s;
                            if (isInteger(tmp)) {
                                btn.setText(tmp + " %");
                            } else {
                                tv1.setText(tmp);
                            }
                        }
                    });
                }
            } catch (Exception e) {
                // Do nothing
            }

            // Wait until process ends
            while (isRunning(proc)) {
                sleep(100);
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tv1.setText("Saved to /sdcard/Dumper");
                    btn.setText("Done!");
                }
            });
        }
    }


    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    public static boolean isRunning(Process process) {
        try {
            process.exitValue();
            return false;
        } catch (Exception e) {
            return true;
        }
    }
    public static void sleep(long mili) {
        try {
            Thread.sleep(mili);
        } catch (Exception e) {
            // Do nothing
        }
    }

    private static void copyAssets(Context context, String path) {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            // Do nothing
        }
        path += '/';
        File destDir = new File(path);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        if (files != null) for (String filename : files) {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = assetManager.open(filename);
                File outFile = new File(path, filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
                if (outFile.exists())
                    outFile.setExecutable(true, true);
            } catch (IOException e) {
                // Do nothing
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }
        }
    }
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }
}
